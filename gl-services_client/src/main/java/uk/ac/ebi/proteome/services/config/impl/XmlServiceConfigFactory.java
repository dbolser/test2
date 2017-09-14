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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import uk.ac.ebi.proteome.services.ServiceContext;
import uk.ac.ebi.proteome.services.config.ConfigException;
import uk.ac.ebi.proteome.services.config.ServiceConfig;
import uk.ac.ebi.proteome.services.config.ServiceConfigFactory;
import uk.ac.ebi.proteome.util.InputOutputUtils;
import uk.ac.ebi.proteome.util.UtilUncheckedException;

/**
 * @author dstaines
 *
 */
public class XmlServiceConfigFactory implements ServiceConfigFactory {

	public ServiceConfig buildServiceConfig(ServiceContext context)
			throws ConfigException {
		String path = context.transformFilePath(context.getConfigFile());
		return buildServiceConfig(path);
	}

	public ServiceConfig buildServiceConfig(String file) throws ConfigException {

		XStream stream = new XStream(new DomDriver());
		stream.alias("serviceConfig", ServiceConfig.class);
		Reader reader = null;

		try {
      if(StringUtils.isEmpty(file)) {
        reader = getDefaultReader();
      }
      else {
        reader = new FileReader(file);
      }
		}
		catch (FileNotFoundException e) {
			reader = getDefaultReader();
		}

		ServiceConfig config = null;

		try {
			config = (ServiceConfig) stream.fromXML(reader);
		}
		finally {
			InputOutputUtils.closeQuietly(reader);
		}

		return config;
	}

  private Reader getDefaultReader() throws ConfigException {
    String cfgFile = "/uk/ac/ebi/proteome/services/default_config.xml";
    try {
      return InputOutputUtils.slurpTextClasspathResourceToStringReader(cfgFile);
    }
    catch(UtilUncheckedException e) {
      throw new ConfigException("Could not create reader from default_config.xml", e);
    }
  }
}
