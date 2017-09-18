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
package uk.ac.ebi.proteome.materializer.ena.genome_collections;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.proteome.genomebuilder.metadata.GenomeMetaData;
import uk.ac.ebi.proteome.genomebuilder.metadata.GenomicComponentMetaData;
import uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo.OrganismNameType;
import uk.ac.ebi.proteome.genomebuilder.model.impl.GenomeInfoImpl;
import uk.ac.ebi.proteome.materializer.ena.EnaGenomeConfig;
import uk.ac.ebi.proteome.services.sql.ROResultSet;
import uk.ac.ebi.proteome.services.sql.SqlService;
import uk.ac.ebi.proteome.util.collections.CollectionUtils;
import uk.ac.ebi.proteome.util.sql.RowMapper;
import uk.ac.ebi.proteome.util.sql.SqlLib;
import uk.ac.ebi.proteome.util.sql.SqlServiceTemplate;
import uk.ac.ebi.proteome.util.sql.SqlServiceTemplateImpl;

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
            final GenomeMetaData g = new GenomeMetaData(
                    new GenomeInfoImpl(rs.getString(1), rs.getInt(5), scientificName, null));
            g.setVersion(rs.getString(2));
            g.setOrganismName(OrganismNameType.FULL, scientificName);
            final String commonName = rs.getString(4);
            if (!StringUtils.isEmpty(commonName)) {
                g.setOrganismName(OrganismNameType.COMMON, commonName);
            }
            g.setAssemblyName(rs.getString(6));
            g.setDescription(rs.getString(7));
            return g;
        }
    };

    private final RowMapper<GenomicComponentMetaData> genomeComponentMapper = new RowMapper<GenomicComponentMetaData>() {
        public GenomicComponentMetaData mapRow(ROResultSet rs, int arg1) throws SQLException {
            // acc.version
            final String vAcc = rs.getString(1);
            final String[] acc = vAcc.split("\\.");
            final GenomicComponentMetaData md = new GenomicComponentMetaData(ENA_SRC, acc[0], acc[0], null);
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
        this(new SqlServiceTemplateImpl(config.getEtaUri(), srv), GcWgsPolicy.valueOf(config.getWgsPolicy().toUpperCase()));
    };

    public OracleGenomeCollections(SqlServiceTemplate gcServer, GcWgsPolicy policy) {
        this.gcServer = gcServer;
        this.sqlLib = new SqlLib("/" + this.getClass().getName().replaceAll("\\.", "/") + ".xml");
        this.policy = policy;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.ebi.proteome.materializer.ena.genome_collections.GenomeCollections
     * #getGenomeForSetChain(java.lang.String)
     */
    public GenomeMetaData getGenomeForSetChain(String setChain) {
        getLog().info("Fetching metadata for " + setChain);
        final List<GenomeMetaData> mds = gcServer.queryForList(sqlLib.getQuery("getGenomeForId"), genomeMapper,
                setChain);
        final GenomeMetaData md = CollectionUtils.getFirstElement(mds, null);
        if (md != null) {
            md.getComponentMetaData().addAll(getComponentsForGenome(md));
        }
        return md;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.ebi.proteome.materializer.ena.genome_collections.GenomeCollections
     * #getGenomeForOrganism(java.lang.String)
     */
    public GenomeMetaData getGenomeForOrganism(String name) {
        return getGenomeForSetChain(getSetChainForOrganism(name));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.ebi.proteome.materializer.ena.genome_collections.GenomeCollections
     * #getGenomeForTaxId(int)
     */
    public GenomeMetaData getGenomeForTaxId(int taxId) {
        return getGenomeForSetChain(getSetChainForTaxId(taxId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.ebi.proteome.materializer.ena.genome_collections.GenomeCollections
     * #getGenomeForEnaAccession(java.lang.String)
     */
    public GenomeMetaData getGenomeForEnaAccession(String accession) {
        return getGenomeForSetChain(getSetChainForEnaAccession(accession));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.ebi.proteome.materializer.ena.genome_collections.GenomeCollections
     * #getComponentsForGenome
     * (uk.ac.ebi.proteome.genomebuilder.metadata.GenomeMetaData)
     */
    public List<GenomicComponentMetaData> getComponentsForGenome(GenomeMetaData md) {
        return getComponentsForGenome(md.getId(), md.getVersion());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.ebi.proteome.materializer.ena.genome_collections.GenomeCollections
     * #getComponentsForGenome(java.lang.String, int)
     */
    public List<GenomicComponentMetaData> getComponentsForGenome(String setChain, String version) {
        List<GenomicComponentMetaData> components = null;
        switch (policy) {
        case WGS:
            getLog().info("Using WGS components only");
            components = getWgs(setChain, version);
            break;
        case COMPONENTS:
            getLog().info("Using non-WGS components");
            components = getComponents(setChain, version);
            break;
        case AUTOMATIC:
        default:
            getLog().info("Resolving non-WGS vs. WGS components automatically");
            components = resolveComponents(getComponents(setChain, version), getWgs(setChain, version));
            break;
        }
        getLog().info("Found " + components.size() + " components for " + setChain + "." + version);
        return components;
    }

    protected List<GenomicComponentMetaData> getComponents(String setChain, String version) {
        final List<GenomicComponentMetaData> components = new ArrayList<GenomicComponentMetaData>();
        components.addAll(gcServer.queryForList(sqlLib.getQuery("getRepliconComponentsForGenome"),
                genomeComponentMapper, setChain, version));
        components.addAll(gcServer.queryForList(sqlLib.getQuery("getUnplacedComponentsForGenome"),
                genomeComponentMapper, setChain, version));
        components.addAll(gcServer.queryForList(sqlLib.getQuery("getUnlocalisedComponentsForGenome"),
                genomeComponentMapper, setChain, version));
        return components;
    }

    protected List<GenomicComponentMetaData> getWgs(String setChain, String version) {
        final List<String> wgsPrefixes = gcServer.queryForDefaultObjectList(sqlLib.getQuery("getWgsPrefixForGenome"),
                String.class, setChain, version);
        final String wgsPrefix = CollectionUtils.getFirstElement(wgsPrefixes, null);
        if (StringUtils.isEmpty(wgsPrefix)) {
            return new ArrayList<GenomicComponentMetaData>(0);
        } else {
            return gcServer.queryForList(sqlLib.getQuery("getWgsComponentsForPrefix"), genomeComponentMapper,
                    wgsPrefix);
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
     * @see
     * uk.ac.ebi.proteome.materializer.ena.genome_collections.GenomeCollections
     * #getSetChainForOrganism(java.lang.String)
     */
    public String getSetChainForOrganism(String name) {
        return gcServer.queryForDefaultObject("getSetChainForName", String.class, name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.ebi.proteome.materializer.ena.genome_collections.GenomeCollections
     * #getSetChainForTaxId(int)
     */
    public String getSetChainForTaxId(int taxId) {
        return gcServer.queryForDefaultObject("getSetChainForTaxId", String.class, taxId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.ebi.proteome.materializer.ena.genome_collections.GenomeCollections
     * #getSetChainForEnaAccession(java.lang.String)
     */
    public String getSetChainForEnaAccession(String accession) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
