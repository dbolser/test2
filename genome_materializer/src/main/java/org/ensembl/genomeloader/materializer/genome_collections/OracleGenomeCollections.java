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
 * GenomeCollections
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package org.ensembl.genomeloader.materializer.genome_collections;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.materializer.EnaGenomeConfig;
import org.ensembl.genomeloader.metadata.GenomeMetaData;
import org.ensembl.genomeloader.metadata.GenomeMetaData.OrganismNameType;
import org.ensembl.genomeloader.metadata.GenomicComponentMetaData;
import org.ensembl.genomeloader.services.sql.ROResultSet;
import org.ensembl.genomeloader.services.sql.SqlService;
import org.ensembl.genomeloader.util.collections.CollectionUtils;
import org.ensembl.genomeloader.util.sql.RowMapper;
import org.ensembl.genomeloader.util.sql.SqlLib;
import org.ensembl.genomeloader.util.sql.SqlServiceTemplate;
import org.ensembl.genomeloader.util.sql.SqlServiceTemplateImpl;

/**
 * DAO for Genome Collections database
 * 
 * @author dstaines
 * 
 */
public class OracleGenomeCollections implements GenomeCollections {

    private final RowMapper<GenomeMetaData> genomeMapper = new RowMapper<GenomeMetaData>() {
        public GenomeMetaData mapRow(ROResultSet rs, int arg1) throws SQLException {
            // set_chain, set_version, scientific_name, common_name, taxid,
            // ass_name, project_name, strain
            String scientificName = rs.getString(3);
            final String strain = rs.getString(8);
            if (!StringUtils.isEmpty(strain) && !scientificName.contains(strain)) {
                scientificName = scientificName + " str. " + strain;
            }
            final GenomeMetaData g = new GenomeMetaData(rs.getString(1), scientificName, rs.getInt(5));
            g.setVersion(rs.getString(2));
            g.setOrganismName(OrganismNameType.FULL, scientificName);
            final String commonName = rs.getString(4);
            if (!StringUtils.isEmpty(commonName)) {
                g.setOrganismName(OrganismNameType.COMMON, commonName);
            }
            g.setAssemblyName(rs.getString(6));
            g.setDescription(rs.getString(7));
            g.setAssemblyDefault(g.getAssemblyName().replaceAll("[^A-z0-9_.-]+", "_"));
            return g;
        }
    };

    private class GenomicComponentMapper implements RowMapper<GenomicComponentMetaData> {

        private final GenomeMetaData genomeMetaData;

        public GenomicComponentMapper(GenomeMetaData genomeMetaData) {
            this.genomeMetaData = genomeMetaData;
        }

        public GenomicComponentMetaData mapRow(ROResultSet rs, int arg1) throws SQLException {
            // acc.version
            final String vAcc = rs.getString(1);
            final String[] acc = vAcc.split("\\.");
            final GenomicComponentMetaData md = new GenomicComponentMetaData(acc[0], genomeMetaData);
            md.setVersion(acc[1]);
            return md;
        }
    };

    private Log log;

    protected Log getLog() {
        if (log == null) {
            log = LogFactory.getLog(this.getClass());
        }
        return log;
    }

    private final SqlServiceTemplate gcServer;
    protected final SqlLib sqlLib;

    public static enum GcWgsPolicy {
        AUTOMATIC, WGS, COMPONENTS;
    }

    private final GcWgsPolicy policy;

    public OracleGenomeCollections(EnaGenomeConfig config, SqlService srv) {
        this(new SqlServiceTemplateImpl(config.getEtaUri(), srv),
                GcWgsPolicy.valueOf(config.getWgsPolicy().toUpperCase()));
    };

    public OracleGenomeCollections(SqlServiceTemplate gcServer, GcWgsPolicy policy) {
        this.gcServer = gcServer;
        this.sqlLib = new SqlLib("/" + this.getClass().getName().replaceAll("\\.", "/") + ".xml");
        this.policy = policy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ensembl.genomeloader.materializer.ena.genome_collections.
     * GenomeCollections #getGenomeForSetChain(java.lang.String)
     */
    public GenomeMetaData getGenomeForSetChain(String setChain) {
        getLog().info("Fetching metadata for " + setChain);
        final List<GenomeMetaData> mds = gcServer.queryForList(sqlLib.getQuery("getGenomeForId"), genomeMapper,
                setChain);
        final GenomeMetaData md = CollectionUtils.getFirstElement(mds, null);
        if (md != null) {
            addComponentsForGenome(md);
        }
        return md;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ensembl.genomeloader.materializer.ena.genome_collections.
     * GenomeCollections #getGenomeForOrganism(java.lang.String)
     */
    public GenomeMetaData getGenomeForOrganism(String name) {
        return getGenomeForSetChain(getSetChainForOrganism(name));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ensembl.genomeloader.materializer.ena.genome_collections.
     * GenomeCollections #getGenomeForTaxId(int)
     */
    public GenomeMetaData getGenomeForTaxId(int taxId) {
        return getGenomeForSetChain(getSetChainForTaxId(taxId));
    }

    public GenomeMetaData getGenomeForEnaAccession(String accession) {
        return getGenomeForSetChain(getSetChainForEnaAccession(accession));
    }

    public void addComponentsForGenome(GenomeMetaData md) {
        Collection<GenomicComponentMetaData> components = null;
        switch (policy) {
        case WGS:
            getLog().info("Using WGS components only");
            components = getWgs(md);
            break;
        case COMPONENTS:
            getLog().info("Using non-WGS components");
            components = getComponents(md);
            break;
        case AUTOMATIC:
        default:
            getLog().info("Resolving non-WGS vs. WGS components automatically");
            components = resolveComponents(getComponents(md), getWgs(md));
            break;
        }
        getLog().info("Found " + components.size() + " components for " + md.getId());
        md.getComponentMetaData().clear();
        md.getComponentMetaData().addAll(components);
    }

    protected List<GenomicComponentMetaData> getComponents(GenomeMetaData md) {
        final List<GenomicComponentMetaData> components = new ArrayList<GenomicComponentMetaData>();
        GenomicComponentMapper mapper = new GenomicComponentMapper(md);
        components.addAll(gcServer.queryForList(sqlLib.getQuery("getRepliconComponentsForGenome"), mapper, md.getId(),
                md.getVersion()));
        components.addAll(gcServer.queryForList(sqlLib.getQuery("getUnplacedComponentsForGenome"), mapper, md.getId(),
                md.getVersion()));
        components.addAll(gcServer.queryForList(sqlLib.getQuery("getUnlocalisedComponentsForGenome"), mapper,
                md.getId(), md.getVersion()));
        return components;
    }

    protected List<GenomicComponentMetaData> getWgs(GenomeMetaData md) {
        GenomicComponentMapper mapper = new GenomicComponentMapper(md);
        final List<String> wgsPrefixes = gcServer.queryForDefaultObjectList(sqlLib.getQuery("getWgsPrefixForGenome"),
                String.class, md.getId(), md.getVersion());
        final String wgsPrefix = CollectionUtils.getFirstElement(wgsPrefixes, null);
        if (StringUtils.isEmpty(wgsPrefix)) {
            return new ArrayList<GenomicComponentMetaData>(0);
        } else {
            return gcServer.queryForList(sqlLib.getQuery("getWgsComponentsForPrefix"), mapper, wgsPrefix);
        }
    }

    /**
     * @param components
     * @param wgs
     * @return
     */
    protected List<GenomicComponentMetaData> resolveComponents(List<GenomicComponentMetaData> components,
            List<GenomicComponentMetaData> wgs) {
        // decide which to use
        if (wgs.size() > 0) {
            if (components.size() == 0) {
                getLog().info("Using " + wgs.size() + "WGS components");
                return wgs;
            } else {
                final int componentFeatureN = countFeaturesGC(components);
                final int wgsFeatureN = countFeatures(wgs);
                getLog().info("Found " + componentFeatureN + " features on " + components.size() + " GC components vs. "
                        + wgsFeatureN + " on " + wgs.size() + " WGS components");
                if (componentFeatureN >= wgsFeatureN) {
                    getLog().info("Using " + components.size() + " GC components");
                    return components;
                } else {
                    getLog().info("Using " + wgs.size() + "WGS components");
                    return wgs;
                }
            }
        } else {
            getLog().info("Using " + components.size() + " GC components");
            return components;
        }
    }

    protected int countFeatures(List<GenomicComponentMetaData> components) {
        int n = 0;
        for (final GenomicComponentMetaData component : components) {
            n += gcServer.queryForDefaultObject(sqlLib.getQuery("getFeatureCountForComponent"), Integer.class,
                    component.getAccession());
        }
        return n;
    }

    protected int countFeaturesGC(List<GenomicComponentMetaData> components) {
        int n = 0;
        for (final GenomicComponentMetaData component : components) {
            n += gcServer.queryForDefaultObject(sqlLib.getQuery("getFeatureCountForComponentGC"), Integer.class,
                    component.getAccession() + "." + component.getVersion());
        }
        return n;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ensembl.genomeloader.materializer.ena.genome_collections.
     * GenomeCollections #getSetChainForOrganism(java.lang.String)
     */
    public String getSetChainForOrganism(String name) {
        return gcServer.queryForDefaultObject("getSetChainForName", String.class, name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ensembl.genomeloader.materializer.ena.genome_collections.
     * GenomeCollections #getSetChainForTaxId(int)
     */
    public String getSetChainForTaxId(int taxId) {
        return gcServer.queryForDefaultObject("getSetChainForTaxId", String.class, taxId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ensembl.genomeloader.materializer.ena.genome_collections.
     * GenomeCollections #getSetChainForEnaAccession(java.lang.String)
     */
    public String getSetChainForEnaAccession(String accession) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
