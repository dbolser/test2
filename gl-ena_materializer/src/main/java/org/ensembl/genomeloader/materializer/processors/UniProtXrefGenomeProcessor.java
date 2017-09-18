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
import org.ensembl.genomeloader.genomebuilder.model.DatabaseReference;
import org.ensembl.genomeloader.genomebuilder.model.DatabaseReferenceType;
import org.ensembl.genomeloader.genomebuilder.model.Gene;
import org.ensembl.genomeloader.genomebuilder.model.Genome;
import org.ensembl.genomeloader.genomebuilder.model.GenomicComponent;
import org.ensembl.genomeloader.genomebuilder.model.Protein;
import org.ensembl.genomeloader.genomebuilder.model.Transcript;
import org.ensembl.genomeloader.genomebuilder.model.DatabaseReferenceType.TypeEnum;
import org.ensembl.genomeloader.genomebuilder.model.impl.DatabaseReferenceImpl;
import org.ensembl.genomeloader.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import org.ensembl.genomeloader.materializer.EnaGenomeConfig;
import org.ensembl.genomeloader.services.sql.ROResultSet;
import org.ensembl.genomeloader.services.sql.SqlService;
import org.ensembl.genomeloader.util.InputOutputUtils;
import org.ensembl.genomeloader.util.collections.CollectionUtils;
import org.ensembl.genomeloader.util.sql.RowMapper;
import org.ensembl.genomeloader.util.sql.SqlLib;
import org.ensembl.genomeloader.util.sql.SqlServiceTemplate;
import org.ensembl.genomeloader.util.sql.SqlServiceTemplateImpl;

/**
 * Processor to add transitive xrefs from UniProt based on UniProt mappings
 * 
 * @author dstaines
 *
 */
public class UniProtXrefGenomeProcessor implements GenomeProcessor {

    private Log log;
    private final SqlServiceTemplate upSrv;
    private final DatabaseReferenceType swType;
    private final DatabaseReferenceType trType;
    private final DatabaseReferenceTypeRegistry registry;
    private final Map<String, DatabaseReferenceType> types;
    private final SqlLib sqlLib;
    private final static int BATCH_SIZE = 500;
    private final List<String> placeholders;
    private final String dbList;

    public UniProtXrefGenomeProcessor(SqlServiceTemplate upSrv, DatabaseReferenceTypeRegistry registry) {
        this.registry = registry;
        this.upSrv = upSrv;
        this.swType = registry.getTypeForQualifiedName("UniProtKB", "Swiss-Prot");
        this.trType = registry.getTypeForQualifiedName("UniProtKB", "TrEMBL");
        this.types = CollectionUtils.createHashMap();
        this.sqlLib = new SqlLib("/uk/ac/ebi/proteome/materializer/ena/sql.xml");
        placeholders = CollectionUtils.createArrayList(BATCH_SIZE);
        for (int i = 0; i < BATCH_SIZE; i++) {
            placeholders.add("?");
        }

        final List<String> dbs = CollectionUtils.createArrayList();
        for (final String db : InputOutputUtils
                .resourceToList("/uk/ac/ebi/proteome/materializer/ena/uniprot_xref_whitelist.txt")) {
            dbs.add("'" + db + "'");
        }
        dbList = StringUtils.join(dbs.iterator(), ',');

    }

    protected DatabaseReferenceType getType(String name) {
        DatabaseReferenceType type = types.get(name);
        if (type == null) {
            type = registry.getTypeForOtherName(name);
            if (type != null) {
                types.put(name, type);
            }
        }
        return type;
    }

    public UniProtXrefGenomeProcessor(EnaGenomeConfig config, SqlService srv, DatabaseReferenceTypeRegistry registry) {
        this(new SqlServiceTemplateImpl(config.getUniProtUri(), srv), registry);
    }

    protected Log getLog() {
        if (log == null) {
            log = LogFactory.getLog(this.getClass());
        }
        return log;
    }

    public void processGenome(final Genome genome) {
        getLog().info("Adding UniProt transitive xrefs to genome " + genome.getId());
        // hash proteins by UniProt
        final Map<String, Collection<Protein>> proteinsByAc = CollectionUtils.createHashMap();
        final Map<String, DatabaseReference> uniprotRefs = CollectionUtils.createHashMap();
        final Map<String, Collection<Gene>> genesByAc = CollectionUtils.createHashMap();
        for (final GenomicComponent genomicComponent : genome.getGenomicComponents()) {
            getLog().info("Hashing proteins by UniProt accession for component " + genomicComponent.getAccession());
            for (final Gene gene : genomicComponent.getGenes()) {
                for (final Protein protein : gene.getProteins()) {
                    for (final DatabaseReference ref : protein.getDatabaseReferences()) {
                        if (ref.getDatabaseReferenceType().equals(swType)
                                || ref.getDatabaseReferenceType().equals(trType)) {
                            final String acc = ref.getPrimaryIdentifier();
                            uniprotRefs.put(acc, ref);
                            Collection<Protein> proteins = proteinsByAc.get(acc);
                            if (proteins == null) {
                                proteins = CollectionUtils.createArrayList();
                                proteinsByAc.put(acc, proteins);
                            }
                            proteins.add(protein);
                            Collection<Gene> genes = genesByAc.get(acc);
                            if (genes == null) {
                                genes = CollectionUtils.createArrayList();
                                genesByAc.put(acc, genes);
                            }
                            genes.add(gene);
                            break;
                        }
                    }
                }
            }
        }
        final List<String> acs = new ArrayList<String>(proteinsByAc.keySet());
        int start = 0;
        final int size = acs.size();
        while (start < size) {
            int end = start + BATCH_SIZE;
            if (end >= size) {
                end = size;
            }
            final List<String> acSub = acs.subList(start, end);
            getLog().info("Adding xrefs for batch of " + acSub.size() + " (" + end + "/" + size + ")");

            final String pH = StringUtils.join(placeholders.subList(0, acSub.size()).iterator(), ',');
            final String sql = sqlLib.getQuery("uniProtXrefsBatch", new String[] { pH, dbList });
            upSrv.queryForList(sql, new RowMapper<String>() {

                public String mapRow(ROResultSet resultSet, int position) throws SQLException {
                    final String acc = resultSet.getString(1);
                    final String dbName = resultSet.getString(2);
                    final String pid = resultSet.getString(3);
                    final String sid = resultSet.getString(4);
                    final String note = resultSet.getString(5);
                    final String qid = resultSet.getString(6);
                    if (dbName.equalsIgnoreCase("GO")) {
                        final DatabaseReference ref = uniprotRefs.get(acc);
                        for (final Protein p : proteinsByAc.get(acc)) {
                            final DatabaseReferenceImpl goX = new DatabaseReferenceImpl(getType(dbName), pid,
                                    note.replaceAll(":.*", StringUtils.EMPTY));
                            goX.setSource(ref);
                            p.addDatabaseReference(goX);
                        }
                    } else {
                        final DatabaseReferenceType type = getType(dbName);
                        if (type == null) {
                            getLog().debug("Unknown database reference type " + dbName + " from UniProt " + acc);
                        } else {
                            final DatabaseReferenceImpl ref = new DatabaseReferenceImpl(type, pid, sid);
                            ref.setQuarternaryIdentifier(qid);
                            ref.setDescription(note);
                            if (type.getType().equals(TypeEnum.PROTEIN) || type.getType().equals(TypeEnum.TRANSCRIPT)) {
                                for (final Protein p : proteinsByAc.get(acc)) {
                                    if (type.getType().equals(TypeEnum.TRANSCRIPT)) {
                                        for (final Transcript t : p.getTranscripts()) {
                                            t.addDatabaseReference(ref);
                                        }
                                    } else {
                                        p.addDatabaseReference(ref);
                                    }
                                }
                            } else {
                                for (final Gene g : genesByAc.get(acc)) {
                                    g.addDatabaseReference(ref);
                                }
                            }
                        }
                    }
                    return acc;
                }

            }, acSub.toArray());
            start = end;
        }
        getLog().info("Finished adding UniProt transitive xrefs to genome " + genome.getId());
    }

}
