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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import uk.ac.ebi.proteome.services.ServiceProvider;
import uk.ac.ebi.proteome.services.sql.SqlService;

/**
 * Unlike the {@link SynchronizedServiceProvider} this class creates a lock for
 * each member of {@link ServicesEnum}. This allows the access to the different
 * services to have locks exclusive of each other. The lock map is generated at
 * construction time and is then made unmodifiable. This then removes any
 * requirement to synchronize access to the lock map (since it is only a read
 * operation).
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class PerServiceSynchronizedServiceProvider implements ServiceProvider {

    private final ServiceProvider provider;
    private final Map<ServicesEnum, Object> lockMap;

    public PerServiceSynchronizedServiceProvider(ServiceProvider provider) {
        this.provider = provider;
        Map<ServicesEnum, Object> localLockMap = new HashMap<ServicesEnum, Object>();
        for (ServicesEnum service : ServicesEnum.values()) {
            localLockMap.put(service, new Object());
        }
        this.lockMap = Collections.unmodifiableMap(localLockMap);
    }

    /**
     * The synchronized block which works on a per service basis
     */
    @SuppressWarnings("unchecked")
    public <T> T getService(ServicesEnum service) {
        T serviceImpl = null;
        Object lock = lockMap.get(service);
        synchronized (lock) {
            serviceImpl = (T) provider.getService(service);
        }
        return serviceImpl;
    }

    public SqlService getSqlService() {
        return getService(ServicesEnum.SQL);
    }
}
