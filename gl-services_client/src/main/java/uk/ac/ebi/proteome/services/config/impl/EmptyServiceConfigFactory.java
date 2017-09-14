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
 * File: EmptyServiceConfigFactory.java
 * Created by: dstaines
 * Created on: Nov 13, 2006
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.services.config.impl;

import uk.ac.ebi.proteome.services.ServiceContext;
import uk.ac.ebi.proteome.services.config.ConfigException;
import uk.ac.ebi.proteome.services.config.ServiceConfig;
import uk.ac.ebi.proteome.services.config.ServiceConfigFactory;

/**
 * @author dstaines
 *
 */
public class EmptyServiceConfigFactory implements ServiceConfigFactory {

	public ServiceConfig buildServiceConfig(ServiceContext context)
			throws ConfigException {
		return new ServiceConfig();
	}
}
