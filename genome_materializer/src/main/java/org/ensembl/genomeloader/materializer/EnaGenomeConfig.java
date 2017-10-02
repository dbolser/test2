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

package org.ensembl.genomeloader.materializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.materializer.genome_collections.OracleGenomeCollections.GcWgsPolicy;
import org.ensembl.genomeloader.services.ServiceUncheckedException;
import org.ensembl.genomeloader.util.InputOutputUtils;
import org.ensembl.genomeloader.util.config.XmlBeanUtils;

public class EnaGenomeConfig {

	public static final String CFG_DIR = "./etc";

	public static final String CFG_FILE = "enagenome_config.xml";

	private static Object classLock = EnaGenomeConfig.class;

	private static EnaGenomeConfig config;

	/**
	 * @return current configuration
	 */
	public static EnaGenomeConfig getConfig() {
		synchronized (classLock) {
			if (config == null) {
				final String xmlLocation = CFG_DIR + File.separator + CFG_FILE;
				config = readConfig(xmlLocation);
			}
		}
		return config;
	}

	/**
	 * Read the config file specified and create a new config object from it
	 * 
	 * @param xmlLocation
	 *            path to XML file
	 * @return config object
	 */
	public static EnaGenomeConfig readConfig(String xmlLocation) {
		final EnaGenomeConfig cfg = new EnaGenomeConfig();
		readConfig(xmlLocation, cfg);
		return cfg;
	}

	/**
	 * Read the config file specified and apply to the specified object
	 * 
	 * @param xmlLocation
	 *            path to XML file
	 * @param cfg
	 *            config object
	 */
	protected static void readConfig(String xmlLocation, EnaGenomeConfig cfg) {
		InputStream is = null;
		try {
			final File cfgFile = new File(xmlLocation);
			if (cfgFile.exists()) {
				is = new FileInputStream(cfgFile);
				XmlBeanUtils.xmlToBean(cfg, "config", is);
			} else {
				LogFactory.getLog(EnaGenomeConfig.class).warn(
						"EnaGenomeConfig file " + xmlLocation
								+ " not found - using defaults");
			}
		} catch (final IOException e) {
			throw new ServiceUncheckedException(
					"Could not read genomeloader configuration from"
							+ xmlLocation, e);
		} finally {
			InputOutputUtils.closeQuietly(is);
		}
	}

	private String enaUri;
	private String etaUri;
	private String uniparcUri;
	private String interproUri;
	private String uniProtUri;
	private String enaXmlUrl;
	private String idUri;
	private String rfamUri;
	private Date strictDate = EnaGenomeMaterializer.parseEnaDate("2004-01-01");
	private double nullCdsTagThreshold = 0.02;
	private String wgsPolicy = GcWgsPolicy.AUTOMATIC.name();
	private boolean interProBatch = true;
	private String componentSorter = "automatic";
	private boolean loadAssembly = true;
	private boolean allowMissingUpis = false;
	private boolean useAccessionsForNames = false;
	private int maxEnaConnections = 10;
	private String lockFileDir = System.getProperty("user.dir");
	private boolean loadTrackingReferences = true;
    private boolean allowEmptyGenomes = false;
    private int minGeneCount = 50;
    private boolean allowMixedCoordSystems = false;

	public String getEnaXmlUrl() {
		return enaXmlUrl;
	}

	public void setEnaXmlUrl(String enaXmlUrl) {
		this.enaXmlUrl = enaXmlUrl;
	}

	public String getEnaUri() {
		return enaUri;
	}

	public void setEnaUri(String enaUri) {
		this.enaUri = enaUri;
	}

	public String getUniparcUri() {
		return uniparcUri;
	}

	public void setUniparcUri(String uniparcUri) {
		this.uniparcUri = uniparcUri;
	}

	public String getInterproUri() {
		return interproUri;
	}

	public void setInterproUri(String interproUri) {
		this.interproUri = interproUri;
	}

	public String getIdUri() {
		return idUri;
	}

	public void setIdUri(String idUri) {
		this.idUri = idUri;
	}

	public Date getStrictDate() {
		return strictDate;
	}

	public void setStrictDate(Date strictDate) {
		this.strictDate = strictDate;
	}

	public double getNullCdsTagThreshold() {
		return nullCdsTagThreshold;
	}

	public void setNullCdsTagThreshold(double nullCdsTagThreshold) {
		this.nullCdsTagThreshold = nullCdsTagThreshold;
	}

	public String getEtaUri() {
		return etaUri;
	}

	public void setEtaUri(String etaUri) {
		this.etaUri = etaUri;
	}

	public String getWgsPolicy() {
		return wgsPolicy;
	}

	public void setWgsPolicy(String wgsPolicy) {
		this.wgsPolicy =wgsPolicy;
	}

	/**
	 * @return the interProBatch
	 */
	public boolean isInterProBatch() {
		return interProBatch;
	}

	/**
	 * @param interProBatch the interProBatch to set
	 */
	public void setInterProBatch(boolean interProBatch) {
		this.interProBatch = interProBatch;
	}

	public String getComponentSorter() {
		return componentSorter;
	}

	public void setComponentSorter(String componentSorter) {
		this.componentSorter = componentSorter;
	}

	public String getUniProtUri() {
		return uniProtUri;
	}

	public void setUniProtUri(String uniProtUri) {
		this.uniProtUri = uniProtUri;
	}

	public boolean isLoadAssembly() {
		return loadAssembly;
	}

	public void setLoadAssembly(boolean loadAssembly) {
		this.loadAssembly = loadAssembly;
	}

	public boolean isAllowMissingUpis() {
		return allowMissingUpis;
	}

	public void setAllowMissingUpis(boolean allowMissingUpis) {
		this.allowMissingUpis = allowMissingUpis;
	}

	public String getRfamUri() {
		return rfamUri;
	}

	public void setRfamUri(String rfamUri) {
		this.rfamUri = rfamUri;
	}

	public boolean isUseAccessionsForNames() {
		return useAccessionsForNames;
	}

	public void setUseAccessionsForNames(boolean useAccessionsForNames) {
		this.useAccessionsForNames = useAccessionsForNames;
	}

    public int getMaxEnaConnections() {
        return maxEnaConnections;
    }

    public void setMaxEnaConnections(int maxEnaConnections) {
        this.maxEnaConnections = maxEnaConnections;
    }

    public String getLockFileDir() {
        return lockFileDir;
    }

    public void setLockFileDir(String lockFileDir) {
        this.lockFileDir = lockFileDir;
    }

    public boolean isLoadTrackingReferences() {
        return loadTrackingReferences;
    }

    public void setLoadTrackingReferences(boolean loadTrackingReferences) {
        this.loadTrackingReferences = loadTrackingReferences;
    }

    public boolean isAllowEmptyGenomes() {
        return allowEmptyGenomes;
    }

    public void setAllowEmptyGenomes(boolean allowEmptyGenomes) {
        this.allowEmptyGenomes = allowEmptyGenomes;
    }

    public int getMinGeneCount() {
        return minGeneCount;
    }

    public boolean isAllowMixedCoordSystems() {
        return allowMixedCoordSystems;
    }

    public void setMinGeneCount(int minGeneCount) {
        this.minGeneCount = minGeneCount;
    }

    public void setAllowMixedCoordSystems(boolean allowMixedCoordSystems) {
        this.allowMixedCoordSystems = allowMixedCoordSystems;
    }

}
