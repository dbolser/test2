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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import uk.ac.ebi.proteome.services.ServiceProvider;
import uk.ac.ebi.proteome.services.ServiceUncheckedException;
import uk.ac.ebi.proteome.services.sql.SqlService;

/**
 * Uses a backing Map instance which can be given (or this Object will construct
 * one which is concurrency friendly).
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class MapServiceProvider implements ServiceProvider {

	private Map<ServicesEnum, Object> serviceMap = null;

	/**
	 * Creates an instance of this class backed by the given map. You should
	 * be aware of the possible impacts of concurrency caused by access
	 * on this class
	 */
	public MapServiceProvider(Map<ServicesEnum, Object> serviceMap) {
		this.serviceMap = serviceMap;
	}

	/**
	 * Creates a default provider using the default map instance which is
	 * an instance of {@link ConcurrentHashMap} created with the default settings
	 */
	public MapServiceProvider() {
		this(new ConcurrentHashMap<ServicesEnum, Object>());
	}

	/**
	 * Allows for construction of a provider on the basis of another. Each
	 * service will be populated into this Object afterwards you are free
	 * to modify the instance as required. This method is very dependent on
	 * the hardcoded number of available services. If more  are introduced
	 * (maybe even taken out) this constructor must be updated accordingly
	 */
	public MapServiceProvider(ServiceProvider provider) {
		this();
		for(ServicesEnum servicesEnum: ServicesEnum.values()) {
			setService(servicesEnum, provider.getService(servicesEnum));
		}
	}

	/**
	 * Allows the setting of services into this object instance. It is
	 * possible to set services as many times as you like.
	 */
	public void setService(ServicesEnum service, Object serviceInstance) {
		serviceMap.put(service, serviceInstance);
	}

	/***
	 * Used to get a service from this provider as given to it by
	 * {@link #setService(Class, Object)}
	 *
	 * @param <T> The type of service required
	 * @param service The service to look for
	 * @return The instance of the service
	 * @throws ServiceUncheckedException Thrown if the given interface cannot be
	 * found
	 */
	@SuppressWarnings("unchecked")
	public <T> T getService(ServicesEnum service) throws ServiceUncheckedException {
		T serviceInstance = (T)serviceMap.get(service);
		if(serviceInstance == null) {
			throw new ServiceUncheckedException("Cannot find an " +
					"registered implementation for "+service);
		}
		return serviceInstance;
	}

	/**
	 * Returns a boolean indicating if this service is present in the underlying
	 * map. If true then the map contains the interface however makes no
	 * guarantee that the returning object will be valid
	 */
	public <T> boolean isServiceRegistered(ServicesEnum service) {
		return serviceMap.containsKey(service);
	}

	public SqlService getSqlService() {
		return getService(ServicesEnum.SQL);
	}
}
