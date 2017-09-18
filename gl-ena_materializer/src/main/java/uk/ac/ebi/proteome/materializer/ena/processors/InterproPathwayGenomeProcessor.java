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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;
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
import uk.ac.ebi.proteome.services.sql.SqlService;
import uk.ac.ebi.proteome.services.sql.SqlServiceException;
import uk.ac.ebi.proteome.services.sql.impl.LocalSqlService;
import uk.ac.ebi.proteome.util.collections.CollectionUtils;
import uk.ac.ebi.proteome.util.sql.DbUtils;
import uk.ac.ebi.proteome.util.sql.SqlLib;
import uk.ac.ebi.proteome.util.sql.SqlServiceTemplate;
import uk.ac.ebi.proteome.util.sql.SqlServiceTemplateImpl;

/**
 * {@link GenomeProcessor} to add pathway {@link DatabaseReference} instances to
 * {@link Protein}s from a {@link Genome} based on existing InterPro
 * {@link DatabaseReference}s and InterPro2Pathways
 * 
 * @author dstaines
 * 
 */
public class InterproPathwayGenomeProcessor implements GenomeProcessor {

    protected final static SqlLib SQLLIB = new SqlLib("/uk/ac/ebi/proteome/materializer/ena/sql.xml");

    private Log log;
    private final SqlServiceTemplate ipSrv;
    private final DatabaseReferenceType ipType;
    private final DatabaseReferenceTypeRegistry registry;
    private final String pathwayQuery;

    public InterproPathwayGenomeProcessor(SqlServiceTemplate ipSrv, DatabaseReferenceTypeRegistry registry) {
        this.ipSrv = ipSrv;
        this.registry = registry;
        this.ipType = registry.getTypeForName("InterPro");
        this.pathwayQuery = SQLLIB.getQuery("interPro2Pathway");
    }

    public InterproPathwayGenomeProcessor(EnaGenomeConfig config, SqlService srv,
            DatabaseReferenceTypeRegistry registry) {
        this(new SqlServiceTemplateImpl(config.getInterproUri(), srv), registry);
    }

    protected Log getLog() {
        if (log == null) {
            log = LogFactory.getLog(this.getClass());
        }
        return log;
    }

    public void processGenome(Genome genome) {
        try {
            getLog().info("Loading InterPro pathway references for " + genome.getName());
            // 1. hash Proteins by InterPro accession
            Map<String, Collection<Protein>> proteins = hashProteinsByInterPro(genome);
            // 2. for each batch of InterPro accessions, get pathways
            Map<String, Collection<DatabaseReference>> pathways = hashPathwaysByInterPro(proteins.keySet());
            // 3. apply Pathways to Proteins
            addPathways(proteins, pathways);
            getLog().info("Finished loading InterPro pathway references for " + genome.getName());
        } finally {
        }
    }

    protected void addPathways(Map<String, Collection<Protein>> proteins,
            Map<String, Collection<DatabaseReference>> pathways) {
        getLog().info("Adding pathways to proteins");
        int pCount = 0;
        int rCount = 0;
        for (Entry<String, Collection<Protein>> entry : proteins.entrySet()) {
            Collection<DatabaseReference> refs = pathways.get(entry.getKey());
            if (refs != null && refs.size() > 0) {
                for (Protein p : entry.getValue()) {
                    pCount++;
                    for (DatabaseReference databaseReference : refs) {
                        rCount++;
                        getLog().debug("Adding " + databaseReference + " to " + p);
                        p.addDatabaseReference(databaseReference);
                    }
                }
            }
        }
        getLog().info("Added " + rCount + " pathways to " + pCount + " proteins");
    }

    private Map<String, Collection<DatabaseReference>> hashPathwaysByInterPro(Set<String> interProAcs) {
        getLog().info("Getting pathways for " + interProAcs.size() + " InterPro accessions");
        Map<String, Collection<DatabaseReference>> pathways = CollectionUtils.createHashMap();
        int nPathways = 0;
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = ((LocalSqlService) ipSrv.getSqlService()).openConnection(ipSrv.getUri());
            getLog().info("Preparing statement");
            ps = con.prepareStatement(pathwayQuery);
            ArrayDescriptor arrayDescriptor = ArrayDescriptor.createDescriptor("INTERPRO.STRING_LIST_T",
                    con.getMetaData().getConnection());
            ARRAY arr = new ARRAY(arrayDescriptor, con.getMetaData().getConnection(),
                    interProAcs.toArray(new String[interProAcs.size()]));

            ps.setArray(1, arr);
            getLog().debug("Running query");
            rs = ps.executeQuery();
            getLog().debug("Getting results");
            while (rs.next()) {
                String interProAc = rs.getString(1);
                String pathwayAc = rs.getString(2);
                String pathwayDb = rs.getString(3);
                DatabaseReferenceType type = registry.getTypeForName(pathwayDb);
                if (type == null) {
                    getLog().warn("Could not find database reference type " + pathwayDb);
                } else {
                    Collection<DatabaseReference> refs = pathways.get(interProAc);
                    if (refs == null) {
                        refs = CollectionUtils.createArrayList();
                        pathways.put(interProAc, refs);
                    }
                    getLog().debug("Adding " + type.getDbName() + ":" + pathwayAc + " for InterPro " + interProAc);
                    refs.add(new DatabaseReferenceImpl(type, pathwayAc));
                    nPathways++;
                }
            }
            getLog().info("Retrieved " + nPathways + " pathways for " + interProAcs.size() + " InterPro accessions");
            return pathways;
        } catch (SqlServiceException e) {
            throw new MaterializationUncheckedException("Could not map InterPro domains to genome ", e);
        } catch (SQLException e) {
            throw new MaterializationUncheckedException("Could not map InterPro domains to genome ", e);
        } finally {
            DbUtils.closeDbObject(rs);
            DbUtils.closeDbObject(ps);
            DbUtils.closeDbObject(con);
        }
    }

    private Map<String, Collection<Protein>> hashProteinsByInterPro(Genome genome) {
        Map<String, Collection<Protein>> proteins = CollectionUtils.createHashMap();
        getLog().info("Hashing proteins by InterPro accession for genome " + genome.getName());
        for (GenomicComponent component : genome.getGenomicComponents()) {
            for (Gene gene : component.getGenes()) {
                for (Protein protein : gene.getProteins()) {
                    for (DatabaseReference databaseReference : ModelUtils.getReferencesForType(protein, ipType)) {
                        Collection<Protein> ps = proteins.get(databaseReference.getPrimaryIdentifier());
                        if (ps == null) {
                            ps = CollectionUtils.createArrayList();
                            proteins.put(databaseReference.getPrimaryIdentifier(), ps);
                        }
                        ps.add(protein);
                    }
                }
            }
        }
        getLog().info("Hashed proteins by " + proteins.keySet().size() + " InterPro accessions for genome "
                + genome.getName());
        return proteins;
    }

}
