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

import uk.ac.ebi.proteome.services.PropertyLocator;
import uk.ac.ebi.proteome.services.ServiceContext;
import uk.ac.ebi.proteome.services.ServiceProvider;
import uk.ac.ebi.proteome.services.sql.SqlService;

/**
 * A version of the {@link ServiceProvider} which uses an instance of
 * {@link ClassInstantiatingServiceProvider} to create the required services.
 * The only exception is the instance of {@link PropertyLocator} which must
 * be given to this class (it uses this to instantiate the required services)
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class DefaultServiceProvider implements ServiceProvider {

	private ServiceProvider provider = null;

	public DefaultServiceProvider(ServiceContext context) {
		provider = new PerServiceSynchronizedServiceProvider(
				createProvider(context));
	}

	/**
	 * Attempts to populate the underlying map provider with the given properties
	 * in conjunction with {@link ServicesEnum}
	 */
	public ClassInstantiatingServiceProvider createProvider(ServiceContext context) {
		ClassInstantiatingServiceProvider provider =
			new ClassInstantiatingServiceProvider(context);
		return provider;
	}

	@SuppressWarnings("unchecked")
	public <T> T getService(ServicesEnum service) {
		return (T)provider.getService(service);
	}

	public SqlService getSqlService() {
		return provider.getSqlService();
	}
}
