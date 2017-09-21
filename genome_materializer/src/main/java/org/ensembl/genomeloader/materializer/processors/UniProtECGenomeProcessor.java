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
 * Add EC numbers based on UniProt xrefs
 * 
 * @author dstaines
 *
 */
public class UniProtECGenomeProcessor implements GenomeProcessor {

    private Log log;
    private final SqlServiceTemplate upSrv;
    private final DatabaseReferenceType swType;
    private final DatabaseReferenceType trType;
    private final DatabaseReferenceType enzType;
    private final SqlLib sqlLib;
    private final static int BATCH_SIZE = 500;
    private final List<String> placeholders;

    public UniProtECGenomeProcessor(SqlServiceTemplate upSrv, DatabaseReferenceType swType,
            DatabaseReferenceType trType, DatabaseReferenceType enzType) {
        this.upSrv = upSrv;
        this.swType = swType;
        this.trType = trType;
        this.enzType = enzType;
        this.sqlLib = new SqlLib("/org/ensembl/genomeloader/materializer/sql.xml");
        placeholders = CollectionUtils.createArrayList(BATCH_SIZE);
        for (int i = 0; i < BATCH_SIZE; i++) {
            placeholders.add("?");
        }
    }

    public UniProtECGenomeProcessor(EnaGenomeConfig config, SqlService srv, DatabaseReferenceTypeRegistry registry) {
        this(new SqlServiceTemplateImpl(config.getUniProtUri(), srv),
                registry.getTypeForQualifiedName("UniProtKB", "Swiss-Prot"),
                registry.getTypeForQualifiedName("UniProtKB", "TrEMBL"),
                registry.getTypeForOtherName("EnzymeCommission"));
    }

    protected Log getLog() {
        if (log == null) {
            log = LogFactory.getLog(this.getClass());
        }
        return log;
    }

    public void processGenome(final Genome genome) {
        getLog().info("Adding EC numbers to genome " + genome.getId());
        // hash proteins by uniprot accession
        final Map<String, Collection<Protein>> protsByAcc = CollectionUtils.createHashMap();
        for (final GenomicComponent genomicComponent : genome.getGenomicComponents()) {
            getLog().debug("Hashing proteins by UniProt accession for component " + genomicComponent.getAccession());
            for (final Gene gene : genomicComponent.getGenes()) {
                for (final Protein protein : gene.getProteins()) {
                    for (final DatabaseReference ref : protein.getDatabaseReferences()) {
                        if (ref.getDatabaseReferenceType().equals(swType)
                                || ref.getDatabaseReferenceType().equals(trType)) {
                            Collection<Protein> proteins = protsByAcc.get(ref.getPrimaryIdentifier());
                            if (proteins == null) {
                                proteins = CollectionUtils.createArrayList();
                                protsByAcc.put(ref.getPrimaryIdentifier(), proteins);
                            }
                            proteins.add(protein);
                            break;
                        }
                    }
                }
            }
        }
        final List<String> accs = new ArrayList<String>(protsByAcc.keySet());
        int start = 0;
        final int size = accs.size();
        while (start < size) {
            int end = start + BATCH_SIZE;
            if (end >= size) {
                end = size;
            }
            final List<String> pidSub = accs.subList(start, end);
            getLog().debug("Adding EC accessions for batch of " + pidSub.size() + " (" + end + "/" + size + ")");

            final String pH = StringUtils.join(placeholders.subList(0, pidSub.size()).iterator(), ',');
            final String sql = sqlLib.getQuery("uniProtECBatch", new String[] { pH });
            upSrv.queryForList(sql, new RowMapper<String>() {

                public String mapRow(ROResultSet resultSet, int position) throws SQLException {
                    final String acc = resultSet.getString(1);
                    final String ec = resultSet.getString(2);
                    for (final Protein p : protsByAcc.get(acc)) {
                        p.addDatabaseReference(new DatabaseReferenceImpl(enzType, ec));
                    }
                    return acc;
                }

            }, pidSub.toArray());
            start = end;
        }
        getLog().info("Finished adding EC numbers to genome " + genome.getId());
    }
}
