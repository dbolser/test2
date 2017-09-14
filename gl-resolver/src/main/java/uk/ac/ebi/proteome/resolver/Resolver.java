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
 * File: ComponentResolver.java
 * Created by: dstaines
 * Created on: Feb 6, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.resolver;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import uk.ac.ebi.proteome.resolver.impl.XMLModuleSpecificationResolver;
import uk.ac.ebi.proteome.services.ServiceException;
import uk.ac.ebi.proteome.util.InputOutputUtils;
import uk.ac.ebi.proteome.util.reflection.ReflectionUtils;

/**
 * Base implementation of a simple module resolver, with Spring support used for
 * construction with an appropriate ModuleSpecificationResolver
 *
 * @author dstaines
 */
public class Resolver<K, V extends EntityMetaData> implements
		MaterializerResolver<K, V>, IdentifierResolver<V> {

	public static final String CONCERN_PROPERTY = "concern";

	public static final String TASK_PROPERTY = "task";

	public static final String IDENTIFICATION_CONCERN = "identification";

	private static final String BEAN_ID = "resolverBean";

	private static final String MODULES_FILE_CLASSPATH = "/uk/ac/ebi/proteome/resolver/modules.xml";

	private Map<ModuleSpecification, Object> instanceMap = null;

	private Map<ModuleSpecification, Object> getInstanceMap() {
		if (instanceMap == null) {
			instanceMap = new HashMap<ModuleSpecification, Object>();
		}
		return instanceMap;
	}

	/**
	 * Generic code for turning a module spec into a module
	 *
	 * @param spec
	 * @return
	 */
	public Object instantiateModule(ModuleSpecification spec) {

		Object instance = getInstanceMap().get(spec);

		if (instance == null) {

			if (spec.getProperties() != null) {
				instance = ReflectionUtils.newInstance(spec.getClassName(),
						new Class[] { Properties.class }, new Object[] { spec
								.getProperties() });
			}

			if (instance == null) {
				instance = ReflectionUtils.newInstance(spec.getClassName());
			}
			getInstanceMap().put(spec, instance);
		}
		return instance;

	}

	/**
	 * Backing resolver
	 */
	private ModuleSpecificationResolver resolver = null;

	public Resolver() throws ModuleResolutionException {
		this(MODULES_FILE_CLASSPATH);
	}

	public Resolver(String modulesResource) throws ModuleResolutionException {
		try {
			File f = new File(modulesResource);
			if (f.exists()) {
				resolver = new XMLModuleSpecificationResolver(f.getPath());
			} else {
				if(!InputOutputUtils.resourceExists(modulesResource)) {
					throw new ModuleResolutionException(
							"Could not read module resource " + modulesResource,
							ServiceException.PROCESS_FATAL);
				}
				resolver = new XMLModuleSpecificationResolver(
						InputOutputUtils
								.slurpTextClasspathResourceToStringReader(modulesResource));
			}
		} catch (IOException e) {
			throw new ModuleResolutionException(
					"Could not read module resource " + modulesResource, e,
					ServiceException.PROCESS_FATAL);
		} finally {

		}
	}

	/**
	 * Construct a resolver with the supplied module spec resolver
	 *
	 * @throws ModuleResolutionException
	 */
	public Resolver(ModuleSpecificationResolver resolver)
			throws ModuleResolutionException {
		this.resolver = resolver;
	}

	/**
	 * Build a component resolver with the specified file
	 *
	 * @param modulesFile
	 * @throws ModuleResolutionException
	 */
	public Resolver(File modulesFile) throws ModuleResolutionException {
		try {
			resolver = new XMLModuleSpecificationResolver(modulesFile.getPath());
		} catch (IOException e) {
			throw new ModuleResolutionException("Could not load modules file "
					+ modulesFile, e, ServiceException.PROCESS_FATAL);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ebi.proteome.component.ComponentIdentifierResolver#getComponentIdentifier(uk.ac.ebi.proteome.component.TaskDefinition)
	 */
	public IdentifierModule<V> getIdentifier(TaskDefinition task)
			throws ModuleResolutionException {
		// retrieve module
		Object idfer = resolveTask(task.getName(), IDENTIFICATION_CONCERN);
		// check return type
		if (!(idfer instanceof IdentifierModule)) {
			throw new ModuleResolutionException("The object resolved for task "
					+ task.getName()
					+ " component identification is not of the expected type",
					ServiceException.PROCESS_FATAL);
		}
		// cast and return
		return (IdentifierModule<V>) idfer;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ebi.proteome.component.ComponentMaterializerResolver#getComponentMaterializer(uk.ac.ebi.proteome.component.ComponentMetaData,
	 *      uk.ac.ebi.proteome.component.TaskDefinition)
	 */
	public MaterializerModule<K, V> getMaterializer(V entityMetaData,
			TaskDefinition task) throws ModuleResolutionException {

		// retrieve module
		Object mat = resolveTask(task.getName(), entityMetaData.getDataType());

		if (mat == null) {
			mat = resolveTask(task.getName(), entityMetaData.getDataType());
		}

		if (mat == null) {
			throw new ModuleResolutionException(
					"Could not resolve object for task " + task
							+ " and component type "
							+ entityMetaData.getDataType(),
					ServiceException.PROCESS_FATAL);
		}

		// check type
		if (!(mat instanceof MaterializerModule)) {
			throw new ModuleResolutionException("The object resolved for task "
					+ task + " and component type "
					+ entityMetaData.getDataType()
					+ " is not of the expected type",
					ServiceException.PROCESS_FATAL);
		}

		// cast and return
		return (MaterializerModule) mat;

	}

	public Object resolveTask(String task, String concern)
			throws ModuleResolutionException {

		// consult resolver to retrieve specification
		ModuleSpecification spec = resolver.getModuleSpecification(task,
				concern);

		if (spec != null) {
			// decorate with task and concern as input
			if (spec.getProperties() == null) {
				spec.setProperties(new Properties());
			}
			spec.getProperties().setProperty(TASK_PROPERTY, task);
			spec.getProperties().setProperty(CONCERN_PROPERTY, concern);
			// instantiate module from specification
			return instantiateModule(spec);
		} else {
			return null;
		}

	}

}
