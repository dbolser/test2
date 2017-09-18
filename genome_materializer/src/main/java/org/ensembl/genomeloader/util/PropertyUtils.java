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

package org.ensembl.genomeloader.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.ensembl.genomeloader.services.ServiceUncheckedException;
import org.ensembl.genomeloader.util.templating.TemplateBuilder;

/**
 * A class which encapsulates common acitivties against property files, system
 * properties and resource bundles
 *
 * @author ayates
 * @author $Author$
 * @version $Version$
 */
public class PropertyUtils {

  /**
   * For a given location this method will load the file into a properties
   * object
   *
   * @param classpathLocation The loation of the properties file on the
   * classpath as defined by {@link Class#getResourceAsStream(String)}
   * @return The loaded properties bundle
   * @throws ServiceUncheckedException Thrown if an IOException is detected
   * whilst loading the properties file
   */
  public static Properties getProperties(String classpathLocation) throws ServiceUncheckedException {
    Properties output = null;
    InputStream is = null;
    try {
      Class<?> loaderClass = PropertyUtils.class;
      is = new BufferedInputStream(loaderClass.getResourceAsStream(classpathLocation));
      output = new Properties();
      output.load(is);
    }
    catch(IOException e) {
      throw new ServiceUncheckedException("Could not load library "+
          classpathLocation, e);
    }
    finally {
      InputOutputUtils.closeQuietly(is);
    }
    return output;
  }

  /**
   * Used to print out the contents of a properties object line by line
   * to the given log file.
   *
   * @param info If true this is logged to info else it is sent to debug
   */
  public static void logPropertiesContents(Properties properties, Log log, boolean info) {
  	if(info) log.info("Property dump");
  	else log.debug("Property dump");

  	TemplateBuilder builder = new TemplateBuilder("$key$ : $value$");
  	for(Entry<Object,Object> entry: properties.entrySet()) {
  		builder.addPlaceHolder("key", entry.getKey());
  		builder.addPlaceHolder("value", entry.getValue());
  		if(info) log.info(builder.generate());
  		else log.debug(builder.generate());
  	}
  }
}
