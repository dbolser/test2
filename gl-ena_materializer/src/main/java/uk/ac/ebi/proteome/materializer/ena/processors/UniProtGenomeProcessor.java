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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReferenceType;
import uk.ac.ebi.proteome.genomebuilder.model.Gene;
import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.model.ModelUtils;
import uk.ac.ebi.proteome.genomebuilder.model.Protein;
import uk.ac.ebi.proteome.genomebuilder.model.impl.DatabaseReferenceImpl;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.materializer.ena.EnaGenomeConfig;
import uk.ac.ebi.proteome.services.ServiceContext;
import uk.ac.ebi.proteome.services.sql.ROResultSet;
import uk.ac.ebi.proteome.util.collections.CollectionUtils;
import uk.ac.ebi.proteome.util.sql.RowMapper;
import uk.ac.ebi.proteome.util.sql.SqlLib;
import uk.ac.ebi.proteome.util.sql.SqlServiceTemplate;
import uk.ac.ebi.proteome.util.sql.SqlServiceTemplateImpl;

/**
 * Processor to add UniProt accessions for genomes where there are no UniProt
 * accessions ie. where the genome is recently loaded and ENA has not yet
 * received the dbxrefs. Uses protein_id to look up in SWPREAD database
 * 
 * @author dstaines
 *
 */
public class UniProtGenomeProcessor implements GenomeProcessor {

    private Log log;
    private final SqlServiceTemplate upSrv;
    private final DatabaseReferenceType swType;
    private final DatabaseReferenceType trType;
    private final SqlLib sqlLib;
    private final static int BATCH_SIZE = 500;
    private final List<String> placeholders;

    public UniProtGenomeProcessor(SqlServiceTemplate upSrv, DatabaseReferenceType swType,
            DatabaseReferenceType trType) {
        this.upSrv = upSrv;
        this.swType = swType;
        this.trType = trType;
        this.sqlLib = new SqlLib("/uk/ac/ebi/proteome/materializer/ena/sql.xml");
        placeholders = CollectionUtils.createArrayList(BATCH_SIZE);
        for (int i = 0; i < BATCH_SIZE; i++) {
            placeholders.add("?");
        }
    }

    public UniProtGenomeProcessor(EnaGenomeConfig config, ServiceContext context,
            DatabaseReferenceTypeRegistry registry) {
        this(new SqlServiceTemplateImpl(config.getUniProtUri()),
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
        // only do this for genomes with NO uniprot xrefs
        getLog().info("Looking for components with missing UniProt xrefs for genome " + genome.getId());
        for (final GenomicComponent genomicComponent : genome.getGenomicComponents()) {
            // hash proteins by UPI
            final Map<String, Protein> proteinsById = CollectionUtils.createHashMap();
            int nProteinsUniProt = 0;
            getLog().debug(
                    "Hashing UniProt-less proteins by protein_id for component " + genomicComponent.getAccession());
            for (final Gene gene : genomicComponent.getGenes()) {
                if (!gene.isPseudogene()) {
                    for (final Protein protein : gene.getProteins()) {
                        // does it already have uniprot xrefs?
                        if (!ModelUtils.hasReferenceForType(protein, swType)
                                && !ModelUtils.hasReferenceForType(protein, trType)) {
                            proteinsById.put(protein.getIdentifyingId(), protein);
                        } else {
                            nProteinsUniProt++;
                        }
                    }
                }
            }
            final List<String> pids = new ArrayList<String>(proteinsById.keySet());
            final int size = pids.size();
            if (nProteinsUniProt == 0 && size > 0) {
                getLog().info("Adding missing UniProt xrefs to genome " + genome.getId() + " component "
                        + genomicComponent.getAccession());

                int start = 0;
                while (start < size) {
                    int end = start + BATCH_SIZE;
                    if (end >= size) {
                        end = size;
                    }
                    final List<String> pidSub = pids.subList(start, end);
                    getLog().info("Adding features for batch of " + pidSub.size() + " (" + end + "/" + size + ")");

                    final String pH = StringUtils.join(placeholders.subList(0, pidSub.size()).iterator(), ',');
                    final String sql = sqlLib.getQuery("pidToUniProtBatch", new String[] { pH });
                    upSrv.queryForList(sql, new RowMapper<String>() {

                        public String mapRow(ROResultSet resultSet, int position) throws SQLException {
                            final String pid = resultSet.getString(1);
                            final String acc = resultSet.getString(2);
                            final int type = resultSet.getInt(3);
                            final Protein p = proteinsById.get(pid);
                            if (p != null) {
                                getLog().debug("Adding missing UniProt " + acc + " to protein " + pid + " from genome "
                                        + genome.getId());
                                p.addDatabaseReference(new DatabaseReferenceImpl(type == 0 ? swType : trType, acc));
                            }
                            return acc;
                        }

                    }, pidSub.toArray());
                    start = end;
                }
            }
        }
        getLog().info("Finished adding missing UniProt xrefs to genome " + genome.getId());
    }
}
