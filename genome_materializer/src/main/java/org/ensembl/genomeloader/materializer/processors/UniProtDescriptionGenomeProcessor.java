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

package org.ensembl.genomeloader.materializer.processors;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.materializer.EnaGenomeConfig;
import org.ensembl.genomeloader.model.DatabaseReference;
import org.ensembl.genomeloader.model.DatabaseReferenceType;
import org.ensembl.genomeloader.model.Gene;
import org.ensembl.genomeloader.model.Genome;
import org.ensembl.genomeloader.model.GenomicComponent;
import org.ensembl.genomeloader.model.Protein;
import org.ensembl.genomeloader.model.impl.DatabaseReferenceImpl;
import org.ensembl.genomeloader.services.sql.ROResultSet;
import org.ensembl.genomeloader.services.sql.SqlService;
import org.ensembl.genomeloader.util.collections.CollectionUtils;
import org.ensembl.genomeloader.util.sql.RowMapper;
import org.ensembl.genomeloader.util.sql.SqlLib;
import org.ensembl.genomeloader.util.sql.SqlServiceTemplate;
import org.ensembl.genomeloader.util.sql.SqlServiceTemplateImpl;
import org.ensembl.genomeloader.xrefregistry.DatabaseReferenceTypeRegistry;

/**
 * Processor that adds missing descriptions to UniProt xrefs
 * 
 * @author dstaines
 *
 */
public class UniProtDescriptionGenomeProcessor implements GenomeProcessor {

    private Log log;
    private final SqlServiceTemplate upSrv;
    private final DatabaseReferenceType swType;
    private final DatabaseReferenceType trType;
    private final SqlLib sqlLib;
    private final static int BATCH_SIZE = 500;
    private final List<String> placeholders;

    public UniProtDescriptionGenomeProcessor(SqlServiceTemplate upSrv, DatabaseReferenceType swType,
            DatabaseReferenceType trType) {
        this.upSrv = upSrv;
        this.swType = swType;
        this.trType = trType;
        this.sqlLib = new SqlLib("/org/ensembl/genomeloader/materializer/sql.xml");
        placeholders = CollectionUtils.createArrayList(BATCH_SIZE);
        for (int i = 0; i < BATCH_SIZE; i++) {
            placeholders.add("?");
        }
    }

    public UniProtDescriptionGenomeProcessor(EnaGenomeConfig config, SqlService srv,
            DatabaseReferenceTypeRegistry registry) {
        this(new SqlServiceTemplateImpl(config.getUniProtUri(), srv),
                registry.getTypeForQualifiedName("UniProtKB", "Swiss-Prot"),
                registry.getTypeForQualifiedName("UniProtKB", "TrEMBL"));
    }

    protected Log getLog() {
        if (log == null) {
            log = LogFactory.getLog(this.getClass());
        }
        return log;
    }

    public void processGenome(final Genome genome) {
        getLog().info("Updating UniProt xrefs for genome " + genome.getId());
        // hash proteins by UPI
        final Map<String, Collection<DatabaseReference>> xrefsByAcc = CollectionUtils.createHashMap();
        for (final GenomicComponent genomicComponent : genome.getGenomicComponents()) {
            getLog().debug("Hashing xrefs by UniProt accession for component " + genomicComponent.getAccession());
            for (final Gene gene : genomicComponent.getGenes()) {
                for (final Protein protein : gene.getProteins()) {
                    for (final DatabaseReference ref : protein.getDatabaseReferences()) {
                        if (ref.getDatabaseReferenceType().equals(swType)
                                || ref.getDatabaseReferenceType().equals(trType)) {
                            Collection<DatabaseReference> xrefs = xrefsByAcc.get(ref.getPrimaryIdentifier());
                            if (xrefs == null) {
                                xrefs = CollectionUtils.createArrayList();
                                xrefsByAcc.put(ref.getPrimaryIdentifier(), xrefs);
                            }
                            xrefs.add(ref);
                            break;
                        }
                    }
                }
            }
        }
        final List<String> accs = new ArrayList<String>(xrefsByAcc.keySet());
        int start = 0;
        final int size = accs.size();
        while (start < size) {
            int end = start + BATCH_SIZE;
            if (end >= size) {
                end = size;
            }
            final List<String> pidSub = accs.subList(start, end);
            getLog().debug("Adding UniProt accessions for batch of " + pidSub.size() + " (" + end + "/" + size + ")");

            final String pH = StringUtils.join(placeholders.subList(0, pidSub.size()).iterator(), ',');
            final String sql = sqlLib.getQuery("uniProtDescriptionBatch", new String[] { pH });
            upSrv.queryForList(sql, new RowMapper<String>() {

                public String mapRow(ROResultSet resultSet, int position) throws SQLException {
                    final String acc = resultSet.getString(1);
                    final String name = resultSet.getString(2);
                    final String des = resultSet.getString(3);
                    final String version = resultSet.getString(4);
                    final Collection<DatabaseReference> refs = xrefsByAcc.get(acc);
                    for (final DatabaseReference ref : refs) {
                        ((DatabaseReferenceImpl) ref).setDescription(des);
                        ((DatabaseReferenceImpl) ref).setSecondaryIdentifier(name);
                        ref.setVersion(version);
                    }
                    return acc;
                }

            }, pidSub.toArray());
            start = end;
        }
        getLog().info("Finished updating UniProt xrefs for genome " + genome.getId());
    }
}
