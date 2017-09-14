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
 * File: HibernateModuleSpecificationResolver.java
 * Created by: dstaines
 * Created on: Feb 9, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.resolver.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import uk.ac.ebi.proteome.resolver.ModuleResolutionException;
import uk.ac.ebi.proteome.resolver.ModuleSpecification;
import uk.ac.ebi.proteome.resolver.ModuleSpecificationResolver;
import uk.ac.ebi.proteome.resolver.Resolver;
import uk.ac.ebi.proteome.resolver.TaskConcernModuleSpecification;
import uk.ac.ebi.proteome.services.ServiceContext;
import uk.ac.ebi.proteome.services.ServiceException;
import uk.ac.ebi.proteome.services.datasource.DataSourceServiceException;
import uk.ac.ebi.proteome.util.hibernate.HibernateObjectCleaner;
import uk.ac.ebi.proteome.util.hibernate.HibernateSessionFactoryFactory;
import uk.ac.ebi.proteome.util.reflection.ReflectionUtils;



/**
 * @author dstaines
 *
 */
public class HibernateModuleSpecificationResolver implements
		ModuleSpecificationResolver {

	private SessionFactory sessionFactory = null;

	/**
	 * Factory to programmatically create a suitable factory for DataSource and
	 * associated objects based on an underlying class
	 *
	 * @return
	 * @throws DataSourceServiceException
	 */
	private SessionFactory getSessionFactory() throws ModuleResolutionException {
		if (sessionFactory == null) {
			//Using a proteome.resolver based class to generate the hibernate config
			String configLocation =
				ReflectionUtils.getResourceAsStreamCompatibleName(
						Resolver.class, "hibernate.cfg.xml");

			HibernateSessionFactoryFactory factory =
				new HibernateSessionFactoryFactory(configLocation);

			factory.setUri(serviceUri);

			factory.getHbmClasses().add(TaskConcernModuleSpecification.class);

			try {
				sessionFactory = factory.generate();
			}
			catch(Throwable e) {
				String msg = "Could not initialise Hibernate session factory";
				Log log = LogFactory.getLog(HibernateModuleSpecificationResolver.class);
				log.error(msg, e);
				throw new ModuleResolutionException(msg, e,
						ServiceException.PROCESS_FATAL);
			}
		}
		return sessionFactory;
	}

	private Log log;

	private String serviceUri = null;

	public HibernateModuleSpecificationResolver() {
		serviceUri = ServiceContext.getInstance().getConfig().getServiceDbUri();
	}

	public HibernateModuleSpecificationResolver(String serviceUri) {
		this.serviceUri = serviceUri;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ebi.proteome.component.ModuleSpecificationResolver#addModuleSpecification(java.lang.String,
	 *      java.lang.String, uk.ac.ebi.proteome.component.ModuleSpecification)
	 */
	public void addModuleSpecification(String task, String concern,
			ModuleSpecification moduleSpecification)
			throws ModuleResolutionException {
		TaskConcernModuleSpecification tcspec = new TaskConcernModuleSpecification();
		tcspec.setConcern(concern);
		tcspec.setTask(task);
		tcspec.setModuleSpecification(moduleSpecification);
	}

	/**
	 * @param moduleSpecification
	 * @throws ModuleResolutionException
	 */
	public void addModuleSpecification(
			TaskConcernModuleSpecification moduleSpecification)
			throws ModuleResolutionException {
		Session session = getSessionFactory().getCurrentSession();
		session.beginTransaction();
		session.save(moduleSpecification);
		session.getTransaction().commit();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ebi.proteome.component.ModuleSpecificationResolver#deleteModuleSpecification(java.lang.String,
	 *      java.lang.String)
	 */
	public void deleteModuleSpecification(String task, String concern)
			throws ModuleResolutionException {
		TaskConcernModuleSpecification spec = getTaskConcernModuleSpecification(
				task, concern);
		if (spec == null) {
			throw new ModuleResolutionException(
					"Cannot delete specification for " + task + " and "
							+ concern + " as it does not exist",
					ServiceException.PROCESS_FATAL);
		}
		Session session = getSessionFactory().getCurrentSession();
		session.beginTransaction();
		session.delete(spec);
		session.getTransaction().commit();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ebi.proteome.component.ModuleSpecificationResolver#getModuleSpecification(java.lang.String,
	 *      java.lang.String)
	 */
	public ModuleSpecification getModuleSpecification(String task,
			String concern) throws ModuleResolutionException {
		return getTaskConcernModuleSpecification(task, concern)
				.getModuleSpecification();
	}

	/**
	 * @return the serviceUri
	 */
	public String getServiceUri() {
		return this.serviceUri;
	}

	/**
	 * @param task
	 * @param concern
	 * @return
	 * @throws ModuleResolutionException
	 */
	public TaskConcernModuleSpecification getTaskConcernModuleSpecification(
			String task, String concern) throws ModuleResolutionException {
		TaskConcernModuleSpecification spec = null;
		try {
			Session session = getSessionFactory().getCurrentSession();
			session.beginTransaction();
			spec = (TaskConcernModuleSpecification) session.load(
					TaskConcernModuleSpecification.class,
					new TaskConcernModuleSpecification(task, concern));
			session.getTransaction().commit();
			HibernateObjectCleaner.clean(spec);
		} catch (ObjectNotFoundException e) {
			String msg = "Could not resolve module specification for " + task
					+ " and " + concern;
			getLog().debug(msg, e);
		}
		return spec;
	}

	/**
	 * @param serviceUri
	 *            the serviceUri to set
	 */
	public void setServiceUri(String serviceUri) {
		this.serviceUri = serviceUri;
	}

	private Log getLog() {
		if (log == null) {
			log = LogFactory.getLog(this.getClass());
		}
		return log;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ebi.proteome.component.ModuleSpecificationResolver#getTaskConcernModuleSpecifications()
	 */
	public Collection<TaskConcernModuleSpecification> getTaskConcernModuleSpecifications()
			throws ModuleResolutionException {

		Session session = getSessionFactory().getCurrentSession();

		session.beginTransaction();

		List s = session.createQuery("from TaskConcernModuleSpecification")
				.list();

		session.getTransaction().commit();

		List<TaskConcernModuleSpecification> specs = new ArrayList<TaskConcernModuleSpecification>();
		for (Object spec : s) {
			HibernateObjectCleaner.clean(spec);
			specs.add((TaskConcernModuleSpecification) spec);
		}

		return specs;
	}

}
