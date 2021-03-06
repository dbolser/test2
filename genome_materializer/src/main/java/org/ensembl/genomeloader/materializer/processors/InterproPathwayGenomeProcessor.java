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
import org.ensembl.genomeloader.materializer.EnaGenomeConfig;
import org.ensembl.genomeloader.materializer.impl.MaterializationUncheckedException;
import org.ensembl.genomeloader.model.DatabaseReference;
import org.ensembl.genomeloader.model.DatabaseReferenceType;
import org.ensembl.genomeloader.model.Gene;
import org.ensembl.genomeloader.model.Genome;
import org.ensembl.genomeloader.model.GenomicComponent;
import org.ensembl.genomeloader.model.ModelUtils;
import org.ensembl.genomeloader.model.Protein;
import org.ensembl.genomeloader.model.impl.DatabaseReferenceImpl;
import org.ensembl.genomeloader.services.sql.SqlService;
import org.ensembl.genomeloader.services.sql.SqlServiceException;
import org.ensembl.genomeloader.services.sql.impl.LocalSqlService;
import org.ensembl.genomeloader.util.collections.CollectionUtils;
import org.ensembl.genomeloader.util.sql.DbUtils;
import org.ensembl.genomeloader.util.sql.SqlLib;
import org.ensembl.genomeloader.util.sql.SqlServiceTemplate;
import org.ensembl.genomeloader.util.sql.SqlServiceTemplateImpl;
import org.ensembl.genomeloader.xrefregistry.DatabaseReferenceTypeRegistry;

import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;

/**
 * {@link GenomeProcessor} to add pathway {@link DatabaseReference} instances to
 * {@link Protein}s from a {@link Genome} based on existing InterPro
 * {@link DatabaseReference}s and InterPro2Pathways
 * 
 * @author dstaines
 * 
 */
public class InterproPathwayGenomeProcessor implements GenomeProcessor {

    protected final static SqlLib SQLLIB = new SqlLib("/org/ensembl/genomeloader/materializer/sql.xml");

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
            getLog().debug("Preparing statement");
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
                    // use a different db type for KEGG - different things get labelled as KEGG by InterPro and UniProt
                    if(type.getDbName().equals("KEGG")) {
                        type = registry.getTypeForName("KEGG_Enzyme");
                    }
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
        getLog().debug("Hashing proteins by InterPro accession for genome " + genome.getName());
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
        getLog().debug("Hashed proteins by " + proteins.keySet().size() + " InterPro accessions for genome "
                + genome.getName());
        return proteins;
    }

}
