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

package uk.ac.ebi.proteome.services.support;

import uk.ac.ebi.proteome.services.ServiceConfigProvider;
import uk.ac.ebi.proteome.services.config.ConfigException;
import uk.ac.ebi.proteome.services.config.ServiceConfig;

/**
 * Provides a very thin layer between an instance of {@link ServiceConfig} and
 * the provider/ {@link ServiceContext} instance. It is intended more for
 * test cases rather than production.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class ThinWrapperServiceConfigProvider implements ServiceConfigProvider {

	private ServiceConfig config = null;

	public ThinWrapperServiceConfigProvider(ServiceConfig config) {
		this.config = config;
	}

	public ServiceConfig getServiceConfig() throws ConfigException {
		return config;
	}

}
