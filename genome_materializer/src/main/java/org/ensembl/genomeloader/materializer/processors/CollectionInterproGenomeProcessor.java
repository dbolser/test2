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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.materializer.impl.MaterializationUncheckedException;
import org.ensembl.genomeloader.model.DatabaseReference;
import org.ensembl.genomeloader.model.DatabaseReferenceType;
import org.ensembl.genomeloader.model.Gene;
import org.ensembl.genomeloader.model.Genome;
import org.ensembl.genomeloader.model.GenomicComponent;
import org.ensembl.genomeloader.model.ModelUtils;
import org.ensembl.genomeloader.model.Protein;
import org.ensembl.genomeloader.model.ProteinFeature;
import org.ensembl.genomeloader.model.ProteinFeatureSource;
import org.ensembl.genomeloader.model.ProteinFeatureType;
import org.ensembl.genomeloader.model.Transcript;
import org.ensembl.genomeloader.model.impl.DatabaseReferenceImpl;
import org.ensembl.genomeloader.model.impl.ProteinFeatureImpl;
import org.ensembl.genomeloader.services.sql.ROResultSet;
import org.ensembl.genomeloader.services.sql.SqlServiceException;
import org.ensembl.genomeloader.services.sql.impl.LocalSqlService;
import org.ensembl.genomeloader.util.collections.CollectionUtils;
import org.ensembl.genomeloader.util.sql.DbUtils;
import org.ensembl.genomeloader.util.sql.SqlLib;
import org.ensembl.genomeloader.util.sql.SqlServiceTemplate;
import org.ensembl.genomeloader.util.sql.defaultmappers.AbstractStringMapRowMapper;

import oracle.sql.ARRAY;
import oracle.sql.ArrayDescriptor;

/**
 * InterPro mapping implementation that retrieves InterPro protein features in
 * batches (query is passed in constructor to allow different implementations)
 * 
 * @author dstaines
 *
 */
public abstract class CollectionInterproGenomeProcessor implements GenomeProcessor {

    protected final static SqlLib SQLLIB = new SqlLib("/org/ensembl/genomeloader/materializer/sql.xml");

    private static final String IEA_TYPE = "IEA";
    private Log log;
    private final SqlServiceTemplate ipSrv;
    private final DatabaseReferenceType ipType;
    private final DatabaseReferenceType[] keyTypes;
    private final DatabaseReferenceType goType;
    private final String featureQuery;
    private final int BATCH_SIZE = 500;

    public CollectionInterproGenomeProcessor(SqlServiceTemplate ipSrv, DatabaseReferenceType ipType,
            DatabaseReferenceType goType, String featureQuery, DatabaseReferenceType... keyTypes) {
        this.ipSrv = ipSrv;
        this.ipType = ipType;
        this.keyTypes = keyTypes;
        this.goType = goType;
        this.featureQuery = featureQuery;
    }

    protected Log getLog() {
        if (log == null) {
            log = LogFactory.getLog(this.getClass());
        }
        return log;
    }

    public void processGenome(Genome genome) {

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {

            getLog().info("Retrieving InterPro features for " + genome.getId());
            // 1. get the connection
            con = ((LocalSqlService) ipSrv.getSqlService()).openConnection(ipSrv.getUri());
            getLog().debug("Preparing statement");
            ps = con.prepareStatement(featureQuery);
            // 2. build the array
            final ArrayDescriptor arrayDescriptor = ArrayDescriptor.createDescriptor("INTERPRO.STRING_LIST_T",
                    con.getMetaData().getConnection());
            final Map<String, List<Protein>> proteinsByKey = hashProteins(genome);
            final Set<String> features = CollectionUtils.createHashSet();
            final Set<String> goLessFeatures = CollectionUtils.createHashSet();
            // create lookup of the proteins which don't already have GO dbxrefs
            for (final List<Protein> proteins : proteinsByKey.values()) {
                for (final Protein protein : proteins) {
                    if (!ModelUtils.hasReferenceForType(protein, goType)) {
                        goLessFeatures.add(protein.getIdentifyingId());
                    }
                }
            }
            final List<String> keys = new ArrayList<String>(proteinsByKey.keySet());
            int start = 0;
            final int size = keys.size();
            int nFeatures = 0;
            while (start < size) {
                int end = start + BATCH_SIZE;
                if (end >= size) {
                    end = size;
                }
                final List<String> keySub = keys.subList(start, end);
                getLog().info("Adding features for batch of " + keySub.size() + " (" + end + "/" + size + ")");
                getLog().info("Building array of " + keySub.size() + " keys");
                final ARRAY arr = new ARRAY(arrayDescriptor, con.getMetaData().getConnection(),
                        keySub.toArray(new String[keySub.size()]));
                ps.setArray(1, arr);
                getLog().info("Running query");
                rs = ps.executeQuery();
                getLog().info("Getting results");
                int n = 0;
                while (rs.next()) {
                    n++;
                    addFeatures(proteinsByKey, features, goLessFeatures, rs);
                }
                getLog().info("Got " + n + " results");
                nFeatures += n;
                rs.close();
                start = end;
            }
            getLog().info("Finished retrieving " + nFeatures + " InterPro features for " + size + " proteins "
                    + genome.getId());

        } catch (final SqlServiceException e) {
            throw new MaterializationUncheckedException("Could not map InterPro domains to genome " + genome.getId(),
                    e);
        } catch (final SQLException e) {
            throw new MaterializationUncheckedException("Could not map InterPro domains to genome " + genome.getId(),
                    e);
        } finally {
            DbUtils.closeDbObject(rs);
            DbUtils.closeDbObject(ps);
            DbUtils.closeDbObject(con);
        }

        getLog().info("Retrieving InterPro db versions");
        genome.getMetaData().getDbVersions().putAll(
                ipSrv.queryForMap(SQLLIB.getQuery("interProDbVersions"), new AbstractStringMapRowMapper<String>() {
                    @Override
                    public String mapRow(ROResultSet resultSet, int position) throws SQLException {
                        return resultSet.getString(2);
                    }

                    @Override
                    public Map<String, String> getMap() {
                        return new HashMap<>();
                    }

                    @Override
                    public void existingObject(String currentValue, ROResultSet resultSet, int position)
                            throws SQLException {
                    }
                }));
        genome.getMetaData().setDbVersion("interpro",
                ipSrv.queryForDefaultObject(SQLLIB.getQuery("interProVersion"), String.class));

    }

    protected Map<String, List<Protein>> hashProteins(Genome genome) {
        final Map<String, List<Protein>> proteinsByKey = CollectionUtils.createHashMap();
        for (final GenomicComponent genomicComponent : genome.getGenomicComponents()) {
            getLog().info("Hashing proteins for component " + genomicComponent.getAccession());
            for (final Gene gene : genomicComponent.getGenes()) {
                for (final Protein protein : gene.getProteins()) {
                    // remove preexisting interpro xrefs as they may be stale
                    for (final DatabaseReference ref : ModelUtils.getReferencesForType(protein, ipType)) {
                        protein.getDatabaseReferences().remove(ref);
                    }
                    for (final DatabaseReferenceType type : keyTypes) {
                        final String key = getIdentifier(protein, type);
                        List<Protein> ps = proteinsByKey.get(key);
                        if (ps == null) {
                            ps = CollectionUtils.createArrayList(1);
                            proteinsByKey.put(key, ps);
                        }
                        ps.add(protein);
                    }
                }
            }
        }
        return proteinsByKey;
    }

    protected String getIdentifier(Protein protein, DatabaseReferenceType type) {
        final DatabaseReference ref = ModelUtils.getReferenceForType(protein, type);
        return ref == null ? null : ref.getPrimaryIdentifier();
    }

    private void addFeatures(final Map<String, List<Protein>> proteinsByKey, final Set<String> features,
            final Set<String> goLessFeatures, ResultSet resultSet) throws SQLException {

        final String proteinKey = resultSet.getString(1);
        final List<Protein> proteins = proteinsByKey.get(proteinKey);
        final String featureType = resultSet.getString(5);
        final String methodAc = resultSet.getString(6);
        final String methodName = resultSet.getString(7);
        final int start = resultSet.getInt(8);
        final int end = resultSet.getInt(9);

        final String key = proteinKey + ":" + featureType + ":" + methodAc + ":" + start + "-" + end;
        if (!features.contains(key)) {
            // create interpro
            final String ipro = resultSet.getString(2);
            final String iproShort = resultSet.getString(3);
            final String iproName = resultSet.getString(4);
            final DatabaseReferenceImpl iproX = new DatabaseReferenceImpl(ipType, ipro);
            iproX.setDescription(iproShort + "||" + iproName);
            for (final Protein protein : proteins) {
                protein.addDatabaseReference(iproX);
                // create protein feature
                final ProteinFeatureType type = ProteinFeatureType.forString(featureType);
                if (type == null) {
                    throw new SQLException(
                            "ProteinFeatureType " + featureType + " not found for " + resultSet.getString(2));
                }
                final ProteinFeature pf = new ProteinFeatureImpl(type, methodAc, methodName, start, end,
                        ProteinFeatureSource.INTERPRO);

                protein.addProteinFeature(pf);
            }
            features.add(key);
        }
        for (final Protein protein : proteins) {
            if (goLessFeatures.contains(protein.getIdentifyingId())) {
                final String goId = resultSet.getString(10);
                final String goSrc = resultSet.getString(11);

                // create GO
                if (!StringUtils.isEmpty(goId)) {
                    final DatabaseReference iproX = ModelUtils.getReferenceForType(protein, ipType);
                    final DatabaseReferenceImpl goX = new DatabaseReferenceImpl(goType, goId, IEA_TYPE);
                    goX.setQuarternaryIdentifier(goSrc);
                    goX.setSource(iproX);
                    for (Transcript t : protein.getTranscripts()) {
                        t.addDatabaseReference(goX);
                    }
                }
            }
        }

    }

}
