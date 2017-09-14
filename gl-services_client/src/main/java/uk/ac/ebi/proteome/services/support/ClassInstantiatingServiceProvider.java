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

import static uk.ac.ebi.proteome.util.reflection.ReflectionUtils.hasConstructor;
import static uk.ac.ebi.proteome.util.reflection.ReflectionUtils.newInstance;

import java.util.EnumMap;
import java.util.Map;

import uk.ac.ebi.proteome.services.PropertyLocator;
import uk.ac.ebi.proteome.services.ServiceContext;
import uk.ac.ebi.proteome.services.ServiceProvider;
import uk.ac.ebi.proteome.services.ServiceUncheckedException;
import uk.ac.ebi.proteome.services.sql.SqlService;
import uk.ac.ebi.proteome.util.reflection.ReflectionUtils;

/**
 * This works by giving the class a property locator. It will then use the
 * result of {@link ServicesEnum#getProperty()} to locate the currently set
 * implementation type. This means that the implementation of a given service
 * can be set/reset up-until it is requested, after which a new instance of this
 * class must be created to register/create the new service
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class ClassInstantiatingServiceProvider implements ServiceProvider {

    private MapServiceProvider holdingProvider;
    private ServiceContext context;

    private Map<ServicesEnum, Class<?>> servicesImplMap = new EnumMap<ServicesEnum, Class<?>>(ServicesEnum.class);

    public ClassInstantiatingServiceProvider(ServiceContext context) {
        this(context, new MapServiceProvider());
    }

    public ClassInstantiatingServiceProvider(ServiceContext context, MapServiceProvider holdingProvider) {
        this.holdingProvider = holdingProvider;
        this.context = context;
    }

    protected static final Class<?>[] SERVICE_PROVIDER_CONSTRUCTOR = new Class[] { ServiceProvider.class };

    protected static final Class<?>[] SERVICE_CONTEXT_CONSTRUCTOR = new Class[] { ServiceContext.class };

    protected static final Class<?>[] NO_ARGS_CONSTRUCTOR = new Class[0];

    public SqlService getSqlService() {
        return getService(ServicesEnum.SQL);
    }

    /**
     * Uses initalise on demand to create the services as & when they are
     * required
     */
    @SuppressWarnings("unchecked")
    public <T> T getService(ServicesEnum service) {
        T serviceInstance = null;

        if (holdingProvider.isServiceRegistered(service)) {
            serviceInstance = (T) holdingProvider.getService(service);
        } else {
            Class<?> implementation = getServiceImplClass(service);
            serviceInstance = (T) createInstance(implementation);
            holdingProvider.setService(service, serviceInstance);
        }

        return serviceInstance;
    }

    /**
     * Creates an instance of an Object according to the classes in
     * {@link #SERVICE_PROVIDER_CONSTRUCTOR},
     * {@link #SERVICE_CONTEXT_CONSTRUCTOR} or a no-args constructor
     */
    public Object createInstance(Class<?> implementation) {
        Object serviceInstance = null;
        if (hasConstructor(implementation, SERVICE_PROVIDER_CONSTRUCTOR)) {
            Object[] args = new Object[] { this };
            serviceInstance = newInstance(implementation, SERVICE_PROVIDER_CONSTRUCTOR, args);
        } else if (hasConstructor(implementation, SERVICE_CONTEXT_CONSTRUCTOR)) {
            Object[] args = new Object[] { context };
            serviceInstance = newInstance(implementation, SERVICE_CONTEXT_CONSTRUCTOR, args);
        } else if (hasConstructor(implementation, NO_ARGS_CONSTRUCTOR)) {
            serviceInstance = newInstance(implementation);
        } else {
            throw new ServiceUncheckedException(
                    "Cannot find a constructor " + "for the service implementation " + implementation + ". Acceptable "
                            + "constructors accept a service context, a service provider or no arguments");
        }
        return serviceInstance;
    }

    /**
     * For the given service this method will query the {@link PropertyLocator}
     * which the object was constructed with & generate the required class
     */
    public Class<?> getServiceImplClass(ServicesEnum service) {
        Class<?> classImplementation = servicesImplMap.get(service);
        if (classImplementation == null) {
            String className = context.getProperty(service.getProperty());
            if (className == null) {
                throw new ServiceUncheckedException(
                        "Service " + service + " did not " + "have a corresponding service in the given "
                                + "properties object when looking for property " + service.getProperty());
            }
            classImplementation = ReflectionUtils.getClassFromName(className);
            servicesImplMap.put(service, classImplementation);
        }
        return classImplementation;
    }
}
