/*
 * Copyright [2009-2014] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.proteome.materializer.ena.processors;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReference;
import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReferenceType;
import uk.ac.ebi.proteome.genomebuilder.model.Gene;
import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.model.ModelUtils;
import uk.ac.ebi.proteome.genomebuilder.model.Protein;
import uk.ac.ebi.proteome.genomebuilder.model.impl.DatabaseReferenceImpl;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.materializer.ena.EnaGenomeConfig;
import uk.ac.ebi.proteome.materializer.ena.impl.MaterializationUncheckedException;
import uk.ac.ebi.proteome.services.ServiceContext;
import uk.ac.ebi.proteome.util.sql.SqlLib;
import uk.ac.ebi.proteome.util.sql.SqlServiceTemplate;
import uk.ac.ebi.proteome.util.sql.SqlServiceTemplateImpl;

/**
 * Processor to add UPIs based on protein_ids
 * 
 * @author dstaines
 *
 */
public class UpiGenomeProcessor implements GenomeProcessor {

    private static final int CDS_THRESHOLD = 9;
    private Log log;
    private final SqlServiceTemplate upiSrv;
    private final DatabaseReferenceType upiType;
    private final DatabaseReferenceType pidType;
    private final SqlLib sqlLib;
    private final EnaGenomeConfig config;

    public UpiGenomeProcessor(EnaGenomeConfig config, SqlServiceTemplate upiSrv, DatabaseReferenceType upiType,
            DatabaseReferenceType pidType) {
        this.config = config;
        this.upiSrv = upiSrv;
        this.upiType = upiType;
        this.pidType = pidType;
        this.sqlLib = new SqlLib("/uk/ac/ebi/proteome/materializer/ena/sql.xml");
    }

    public UpiGenomeProcessor(EnaGenomeConfig config, ServiceContext context, DatabaseReferenceTypeRegistry registry) {
        this(config, new SqlServiceTemplateImpl(config.getUniparcUri(), context), registry.getTypeForName("UniParc"),
                registry.getTypeForQualifiedName("EMBL", "PROTEIN"));
    }

    protected Log getLog() {
        if (log == null) {
            log = LogFactory.getLog(this.getClass());
        }
        return log;
    }

    public void processGenome(Genome genome) {
        getLog().info("Adding UPIs to genome " + genome.getId());
        for (final GenomicComponent genomicComponent : genome.getGenomicComponents()) {
            for (final Gene gene : genomicComponent.getGenes()) {
                for (final Protein protein : gene.getProteins()) {
                    // PID is identifying identifier
                    final DatabaseReference pid = ModelUtils.getReferenceForType(protein, pidType);
                    if (pid != null) {
                        final int len = ModelUtils.getEntityLocationLength(protein.getLocation());
                        if (len >= CDS_THRESHOLD) {
                            final String upi = getUpiForProteinId(pid);
                            if (!StringUtils.isEmpty(upi)) {
                                protein.getDatabaseReferences().add(getUpiRef(upi));
                            }
                        } else {
                            getLog().warn("Skipping UPI lookup for protein_id " + pid.getPrimaryIdentifier()
                                    + " as CDS length is only " + len);
                        }
                    }
                }
            }
        }
        getLog().info("Finished adding UPIs to genome " + genome.getId());
    }

    protected DatabaseReference getUpiRef(String upi) {
        return new DatabaseReferenceImpl(upiType, upi);
    }

    protected String getUpiForProteinId(DatabaseReference pid) {
        String upi = null;
        final String[] pids = pid.getSecondaryIdentifier().split("\\.");
        final List<String> upis = upiSrv.queryForDefaultObjectList(sqlLib.getQuery("pidToUpi"), String.class, pids[0],
                pids[1]);
        if (upis.size() == 1) {
            upi = upis.get(0);
        } else if (upis.size() > 1) {
            throw new MaterializationUncheckedException("More than one UPI found for protein ID " + pid);
        } else {
            final String msg = "No UPI found for protein ID " + pid;
            if (!config.isAllowMissingUpis()) {
                throw new MaterializationUncheckedException(msg);
            } else {
                log.warn(msg);
            }
        }
        return upi;
    }

}
