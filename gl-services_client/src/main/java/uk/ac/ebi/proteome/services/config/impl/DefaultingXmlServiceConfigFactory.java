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
 * File: XmlServiceConfigFactory.java
 * Created by: dstaines
 * Created on: Nov 10, 2006
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.services.config.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.proteome.services.ServiceContext;
import uk.ac.ebi.proteome.services.config.ConfigException;
import uk.ac.ebi.proteome.services.config.ServiceConfig;
import uk.ac.ebi.proteome.services.config.ServiceConfigFactory;
import uk.ac.ebi.proteome.util.InputOutputUtils;
import uk.ac.ebi.proteome.util.UtilUncheckedException;
import uk.ac.ebi.proteome.util.config.XmlBeanUtils;

/**
 * Factory that does not overwrite default values in the bean
 *
 * @author dstaines
 *
 */
public class DefaultingXmlServiceConfigFactory implements ServiceConfigFactory {

	private Log log = LogFactory.getLog(this.getClass());

	public ServiceConfig buildServiceConfig(ServiceContext context)
			throws ConfigException {
		return getServiceConfig(context);
	}

	private InputStream getDefaultFileStream() throws ConfigException {
		String cfgFile = "/uk/ac/ebi/proteome/services/default_services_config.xml";
		try {
			return InputOutputUtils.openClasspathResource(cfgFile);
		} catch (UtilUncheckedException e) {
			throw new ConfigException(
					"Could not create reader from default_services_config.xml",
					e);
		}
	}

	/**
	 * If configLocation cannot be found this will use the default file stream
	 * as provided by {@link #getDefaultFileStream()}
	 */
	public ServiceConfig getServiceConfig(ServiceContext context) throws ConfigException {
		String configLocation = context.getConfigFile();
		ServiceConfig config = new ServiceConfig();
		InputStream is = null;

		String configFile = configLocation;

		try {
			if (StringUtils.isEmpty(configFile)) {
				is = getDefaultFileStream();
			} else {
				is = new FileInputStream(context.transformFilePath(configFile));
			}
		} catch (FileNotFoundException e) {
			if(log.isWarnEnabled()) {
				String location = context.transformFilePath(configFile);
				log.warn("Exception raised. Config file "+location+" was not found. Using default");
				log.debug("Debug exception", e);
			}
			is = getDefaultFileStream();
		}

		try {
			XmlBeanUtils.xmlToBean(config, "servicesConfig", is);
		}
		catch (IOException e) {
			throw new ConfigException(
					"Could not parse configuration file from input stream", e);
		}

		return config;
	}
}
