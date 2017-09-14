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

import static uk.ac.ebi.proteome.util.reflection.ReflectionUtils.newInstance;

import uk.ac.ebi.proteome.services.ServiceConfigProvider;
import uk.ac.ebi.proteome.services.ServiceContext;
import uk.ac.ebi.proteome.services.config.ConfigException;
import uk.ac.ebi.proteome.services.config.ServiceConfig;
import uk.ac.ebi.proteome.services.config.ServiceConfigFactory;
import uk.ac.ebi.proteome.services.config.impl.DefaultingXmlServiceConfigFactory;

/**
 * Used to construct a {@link ServiceConfig} object using default properties.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class DefaultServiceConfigProvider implements ServiceConfigProvider {

	private ServiceContext context = null;

	public DefaultServiceConfigProvider(ServiceContext context) {
		this.context = context;
	}

	public ServiceConfig getServiceConfig() throws ConfigException {

		String key = ServiceContext.Properties.CONFIG_FACTORY.getProperty();
		String defaultValue = DefaultingXmlServiceConfigFactory.class.getName();
		String className = context.getPropertyLocator().getProperty(key, defaultValue);
		ServiceConfig config = null;

		try {
			ServiceConfigFactory fact = (ServiceConfigFactory)newInstance(className);

			if (fact == null) {
				throw new ConfigException("Could not initialise factory " + className);
			}

			config = fact.buildServiceConfig(context);
		}
		catch (RuntimeException e) {
			throw new ConfigException("Could not generate ServiceConfig object", e);
		}


		return config;
	}

}
