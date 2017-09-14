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
 * File: XMLActorSpecificationResolver.java
 * Created by: dstaines
 * Created on: Feb 6, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.resolver.impl;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import uk.ac.ebi.proteome.resolver.ModuleResolutionException;
import uk.ac.ebi.proteome.resolver.ModuleSpecification;
import uk.ac.ebi.proteome.resolver.ModuleSpecificationResolver;
import uk.ac.ebi.proteome.resolver.TaskConcernModuleSpecification;
import uk.ac.ebi.proteome.services.ServiceException;
import uk.ac.ebi.proteome.util.InputOutputUtils;
import uk.ac.ebi.proteome.util.XMLNodeListParser;

/**
 * Simple class for resolving module specifications, loaded from an XML file
 * 
 * @author dstaines
 * 
 */
public class XMLModuleSpecificationResolver implements
		ModuleSpecificationResolver {

	private static final String CONCERN_PATH = "concern";

	private static final String KEY = "key";

	private static final String PROPERTIES_NODE = "properties";

	private static final String PROPERTY_NODE = "property";

	private static final String SPEC_CLASSNAME = "className";

	private static final String SPEC_CONCERN = "concern";

	private static final String SPEC_NAME = "name";

	private static final String SPEC_NODE = "moduleSpecification";

	private static final String TASK_NAME = "task";

	private static final String TASKS_PATH = "//tasks/task";

	/**
	 * Factory method to make an XMLModuleSpecificationResolver from the
	 * classpath resource modules.xml in the same directory
	 * 
	 * @return
	 * @throws ModuleResolutionException
	 */
	public static XMLModuleSpecificationResolver createInstance()
			throws ModuleResolutionException {
		String resource = "/"
				+ XMLModuleSpecificationResolver.class.getPackage().getName()
						.replace('.', '/') + "/modules.xml";
		return createInstanceFromClasspathResource(resource);
	}

	/**
	 * Factory method to make an XMLModuleSpecificationResolver from a classpath
	 * resource
	 * 
	 * @param path
	 * @return
	 * @throws ModuleResolutionException
	 */
	public static XMLModuleSpecificationResolver createInstanceFromClasspathResource(
			String path) throws ModuleResolutionException {
		try {
			return new XMLModuleSpecificationResolver(InputOutputUtils
					.slurpTextClasspathResourceToStringReader(path));
		} catch (IOException e) {
			throw new ModuleResolutionException("Could not construct "
					+ XMLModuleSpecificationResolver.class.getName()
					+ " from classpath resource " + path,
					ServiceException.PROCESS_FATAL);
		}
	}

	/**
	 * Factory method to make an XMLModuleSpecificationResolver from a physical
	 * file
	 * 
	 * @param fileName
	 * @return
	 * @throws ModuleResolutionException
	 */
	public static XMLModuleSpecificationResolver createInstanceFromFile(
			String fileName) throws ModuleResolutionException {
		try {
			return new XMLModuleSpecificationResolver(fileName);
		} catch (IOException e) {
			throw new ModuleResolutionException("Could not construct "
					+ XMLModuleSpecificationResolver.class.getName()
					+ " from file " + fileName, ServiceException.PROCESS_FATAL);
		}
	}

	/**
	 * Convenience method for generating a key from a task/concern
	 * 
	 * @param task
	 * @param concern
	 * @return
	 */
	private static String getKey(String task, String concern) {
		StringBuilder s = new StringBuilder();
		s.append(task);
		s.append("_");
		s.append(concern);
		return s.toString();
	}

	/**
	 * Transform a moduleSpecification node into a ModuleSpecification bean
	 * 
	 * @param node
	 * @return
	 */
	private static ModuleSpecification getModuleSpecFromNode(Node node) {
		ModuleSpecification spec = new ModuleSpecification();
		spec.setName(node.getAttributes().getNamedItem(SPEC_NAME)
				.getNodeValue());
		spec.setClassName(node.getAttributes().getNamedItem(SPEC_CLASSNAME)
				.getNodeValue());
		NodeList kids = node.getChildNodes();
		for (int i = 0; i < kids.getLength(); i++) {
			Node kid = kids.item(i);
			if (PROPERTIES_NODE.equals(kid.getNodeName())) {
				NodeList params = kid.getChildNodes();
				for (int j = 0; j < params.getLength(); j++) {
					Node param = params.item(j);
					if (PROPERTY_NODE.equals(param.getNodeName())) {
						spec.getProperties().put(
								(String) param.getAttributes()
										.getNamedItem(KEY).getNodeValue(),
								param.getTextContent());
					}
				}
			}
		}
		return spec;
	}

	private static TaskConcernModuleSpecification getTaskConcernSpecFromNode(
			Node node) {
		TaskConcernModuleSpecification spec = new TaskConcernModuleSpecification();
		spec.setConcern(node.getAttributes().getNamedItem(SPEC_CONCERN)
				.getNodeValue());
		NodeList kids = node.getChildNodes();
		for (int i = 0; i < kids.getLength(); i++) {
			Node kid = kids.item(i);
			if (SPEC_NODE.equals(kid.getNodeName())) {				
				spec.setModuleSpecification(getModuleSpecFromNode(kid));				
				break;
			}
		}
		return spec;
	}

	private Map<String, TaskConcernModuleSpecification> specs = new HashMap<String, TaskConcernModuleSpecification>();

	public XMLModuleSpecificationResolver() {
		specs = new HashMap<String, TaskConcernModuleSpecification>();
	}

	public XMLModuleSpecificationResolver(Reader reader) throws IOException {
		loadSpecsFromReader(reader);
	}

	/**
	 * Construct from the underlying XML file
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	public XMLModuleSpecificationResolver(String fileName) throws IOException {
		loadSpecsFromFile(fileName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ebi.proteome.component.impl.ActorSpecificationResolver#addActorSpecification(java.lang.String,
	 *      java.lang.String,
	 *      uk.ac.ebi.proteome.component.impl.ActorSpecification)
	 */
	public void addModuleSpecification(String task, String concern,
			ModuleSpecification spec) throws ModuleResolutionException {
		TaskConcernModuleSpecification tspec = new TaskConcernModuleSpecification(
				task, concern);
		tspec.setModuleSpecification(spec);
		addModuleSpecification(tspec);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ebi.proteome.component.impl.ActorSpecificationResolver#deleteActorSpecification(java.lang.String,
	 *      java.lang.String)
	 */
	public void deleteModuleSpecification(String task, String concern) {
		specs.remove(getKey(task, concern));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ebi.proteome.component.impl.ActorSpecificationResolver#getActorSpecification(java.lang.String,
	 *      java.lang.String)
	 */
	public ModuleSpecification getModuleSpecification(String task,
			String concern) throws ModuleResolutionException {
		String key = getKey(task, concern);
		TaskConcernModuleSpecification taskConcernModuleSpecification = specs
				.get(key);
		if (taskConcernModuleSpecification == null)
			throw new ModuleResolutionException(
					"Could not resolve spec for task " + task + " and concern "
							+ concern, ServiceException.PROCESS_FATAL);
		return taskConcernModuleSpecification.getModuleSpecification();
	}

	/**
	 * 
	 * @return
	 */
	public Map<String, TaskConcernModuleSpecification> getTaskConcernModuleSpecification() {
		return specs;
	}

	/**
	 * Add the task/concern module specifications from the named file
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	public void loadSpecsFromFile(String fileName) throws IOException {
		loadSpecsFromReader(new FileReader(fileName));
	}

	public void loadSpecsFromReader(Reader reader) throws IOException {
		XMLNodeListParser parser = new XMLNodeListParser(reader, TASKS_PATH,
				TASK_NAME);
		for (Node task : parser.getNodesAsList()) {
			String taskName = task.getAttributes().getNamedItem(TASK_NAME)
					.getNodeValue();
			XMLNodeListParser taskParser = new XMLNodeListParser(task, CONCERN_PATH, SPEC_CONCERN);
			for (Node concern : taskParser.getNodesAsList()) {
				if (concern != null) {
					TaskConcernModuleSpecification spec = getTaskConcernSpecFromNode(concern);
					spec.setTask(taskName);
					String key = getKey(spec.getTask(), spec.getConcern());
					specs.put(key, spec);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ebi.proteome.component.ModuleSpecificationResolver#addModuleSpecification(uk.ac.ebi.proteome.component.TaskConcernModuleSpecification)
	 */
	public void addModuleSpecification(
			TaskConcernModuleSpecification moduleSpecification)
			throws ModuleResolutionException {
		specs.put(getKey(moduleSpecification.getTask(), moduleSpecification
				.getConcern()), moduleSpecification);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ebi.proteome.component.ModuleSpecificationResolver#getTaskConcernModuleSpecification(java.lang.String,
	 *      java.lang.String)
	 */
	public TaskConcernModuleSpecification getTaskConcernModuleSpecification(
			String task, String concern) throws ModuleResolutionException {
		TaskConcernModuleSpecification spec = new TaskConcernModuleSpecification(
				task, concern);
		spec.setModuleSpecification(getModuleSpecification(task, concern));
		return spec;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.ebi.proteome.component.ModuleSpecificationResolver#getTaskConcernModuleSpecifications()
	 */
	public Collection<TaskConcernModuleSpecification> getTaskConcernModuleSpecifications()
			throws ModuleResolutionException {
		return specs.values();
	}

}
