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

package config;

import uk.ac.ebi.proteome.util.config.PropertiesBeanUtils;

/**
 * Provides configuration for the integration tests
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class IntegrationConfig {

	public static final String LOCATION = "/integration.properties";

	public static IntegrationConfig create() {
		IntegrationConfig config = new IntegrationConfig();
		PropertiesBeanUtils.populateBeanFromClasspathPropertiesFile(config, LOCATION);
		return config;
	}

	private String modelUri;
	private String iwebUri;
	private String serviceUri;
	private String specUri;

	public String getModelUri() {
		return modelUri;
	}

	public void setModelUri(String modelUri) {
		this.modelUri = modelUri;
	}

	public String getIwebUri() {
		return iwebUri;
	}

	public void setIwebUri(String iwebUri) {
		this.iwebUri = iwebUri;
	}

	public String getServiceUri() {
		return serviceUri;
	}

	public void setServiceUri(String serviceUri) {
		this.serviceUri = serviceUri;
	}

	public String getSpecUri() {
		return this.specUri;
	}

	public void setSpecUri(String specUri) {
		this.specUri = specUri;
	}
	
}
