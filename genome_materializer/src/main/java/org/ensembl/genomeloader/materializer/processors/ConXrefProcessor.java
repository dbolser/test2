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

/**
 * ConXrefProcessor
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package org.ensembl.genomeloader.materializer.processors;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.materializer.EnaGenomeConfig;
import org.ensembl.genomeloader.model.DatabaseReference;
import org.ensembl.genomeloader.model.DatabaseReferenceType;
import org.ensembl.genomeloader.model.DatabaseReferenceType.TypeEnum;
import org.ensembl.genomeloader.model.Gene;
import org.ensembl.genomeloader.model.Genome;
import org.ensembl.genomeloader.model.GenomicComponent;
import org.ensembl.genomeloader.model.Protein;
import org.ensembl.genomeloader.model.Transcript;
import org.ensembl.genomeloader.model.impl.DatabaseReferenceImpl;
import org.ensembl.genomeloader.services.sql.ROResultSet;
import org.ensembl.genomeloader.services.sql.SqlService;
import org.ensembl.genomeloader.util.collections.CollectionUtils;
import org.ensembl.genomeloader.util.sql.MapRowMapper;
import org.ensembl.genomeloader.util.sql.SqlLib;
import org.ensembl.genomeloader.util.sql.SqlServiceTemplate;
import org.ensembl.genomeloader.util.sql.SqlServiceTemplateImpl;
import org.ensembl.genomeloader.xrefregistry.DatabaseReferenceTypeRegistry;

/**
 * Processor that deals with inadequacies of the CON expansion code where CDS
 * feature xrefs are not included in the expanded entry
 * 
 * @author dstaines
 * 
 */
public class ConXrefProcessor implements GenomeProcessor {

    private Log log;

    protected Log getLog() {
        if (log == null) {
            log = LogFactory.getLog(this.getClass());
        }
        return log;
    }

    private final SqlServiceTemplate enaTemplate;
    private final DatabaseReferenceTypeRegistry registry;
    private final String conXrefQuery;
    private final String conConXrefQuery;

    public ConXrefProcessor(EnaGenomeConfig config, SqlService srv, DatabaseReferenceTypeRegistry registry) {
        this(new SqlServiceTemplateImpl(config.getEnaUri(), srv), registry);
    }

    public ConXrefProcessor(SqlServiceTemplate enaTemplate, DatabaseReferenceTypeRegistry registry) {
        this.enaTemplate = enaTemplate;
        this.registry = registry;
        final SqlLib sqlLib = new SqlLib("/org/ensembl/genomeloader/materializer/sql.xml");
        conXrefQuery = sqlLib.getQuery("conXrefQuery");
        conConXrefQuery = sqlLib.getQuery("conConXrefQuery");
    }

    private final MapRowMapper<String, Set<DatabaseReference>> mapper = new MapRowMapper<String, Set<DatabaseReference>>() {

        private Map<String, DatabaseReferenceType> knownXrefTypes = CollectionUtils.createHashMap();

        public Set<DatabaseReference> mapRow(ROResultSet resultSet, int position) throws SQLException {
            final Set<DatabaseReference> refs = CollectionUtils.createHashSet();
            existingObject(refs, resultSet, position);
            return refs;
        }

        public Map<String, Set<DatabaseReference>> getMap() {
            return CollectionUtils.createHashMap();
        }

        public String getKey(ROResultSet resultSet) throws SQLException {
            return resultSet.getString(1);
        }

        public void existingObject(Set<DatabaseReference> refs, ROResultSet resultSet, int position)
                throws SQLException {
            final String db = resultSet.getString(2);
            final String id = resultSet.getString(3);
            DatabaseReferenceType type = knownXrefTypes.get(db);
            if (type == null) {
                type = registry.getTypeForOtherName(db);
                if (type == null) {
                    getLog().debug("Unknown database type " + db);
                    type = new DatabaseReferenceType(-1, db, "", db, db, db, TypeEnum.GENE);
                }
                knownXrefTypes.put(db, type);
            }
            refs.add(new DatabaseReferenceImpl(type, id));
        }

    };

    /*
     * (non-Javadoc)
     * 
     * @see org.ensembl.genomeloader.materializer.ena.processors.GenomeProcessor#
     * processGenome (org.ensembl.genomeloader.genomebuilder.model.Genome)
     */
    public void processGenome(Genome genome) {
        getLog().info("Finding CDS db_xref qualifiers for CONs");
        for (final GenomicComponent component : genome.getGenomicComponents()) {

            if (component.getMetaData().isCon()) {

                getLog().info("Finding CDS db_xref qualifiers for component " + component.getVersionedAccession());
                final Map<String, Set<DatabaseReference>> refs = CollectionUtils.createHashMap();
                refs.putAll(enaTemplate.queryForMap(conXrefQuery, mapper, component.getVersionedAccession()));
                getLog().info("Found " + refs.size() + " CDS db_xref qualifiers from CONs for component "
                        + component.getVersionedAccession());
                refs.putAll(enaTemplate.queryForMap(conConXrefQuery, mapper, component.getVersionedAccession()));
                getLog().info("Found " + refs.size() + " CDS db_xref qualifiers from CONs/CONCONs for component "
                        + component.getVersionedAccession());
                if (refs.size() > 0) {
                    getLog().info("Adding CDS db_xref qualifiers for component " + component.getVersionedAccession());
                    int n = 0;
                    // work through proteins looking for protein_id
                    for (final Gene gene : component.getGenes()) {
                        for (final Protein protein : gene.getProteins()) {
                            final String pid = protein.getIdentifyingId();
                            final Set<DatabaseReference> xrefs = refs.get(pid);
                            if (xrefs != null) {
                                // attach xrefs
                                for (final DatabaseReference xref : xrefs) {
                                    n++;
                                    switch (xref.getDatabaseReferenceType().getType()) {
                                    case GENE:
                                        gene.addDatabaseReference(xref);
                                        break;
                                    case PROTEIN:
                                        protein.addDatabaseReference(xref);
                                        break;
                                    case TRANSCRIPT:
                                        for (final Transcript transcript : protein.getTranscripts()) {
                                            transcript.addDatabaseReference(xref);
                                        }
                                        break;
                                    }
                                }
                            } else {
                                getLog().warn("Could not find xrefs for protein_id " + pid);
                            }
                        }
                    }
                    getLog().info("Added " + n + " CDS db_xref qualifiers for component "
                            + component.getVersionedAccession());

                }
            }
        }
    }

}
