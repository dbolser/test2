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
 * File: ServiceConfigFactory.java
 * Created by: dstaines
 * Created on: Nov 10, 2006
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.services.config;

import uk.ac.ebi.proteome.services.ServiceContext;

/**
 * Interface for a factory to construct and populate a config object
 *
 * @author dstaines
 *
 */
public interface ServiceConfigFactory {

	/**
	 * Allows for the building of a configuration from an instance of the
	 * {@link ServiceContext}
	 */
	ServiceConfig buildServiceConfig(ServiceContext context) throws ConfigException;
}
