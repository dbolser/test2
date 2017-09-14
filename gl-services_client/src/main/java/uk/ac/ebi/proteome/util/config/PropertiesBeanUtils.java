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

package uk.ac.ebi.proteome.util.config;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.apache.commons.beanutils.BeanUtils;

import uk.ac.ebi.proteome.services.config.ConfigException;
import uk.ac.ebi.proteome.util.PropertyUtils;

/**
 * Works in a similar fashion to the {@link XmlBeanUtils} to provide simple
 * properties file -> JavaBean object. This has been written to offer an 
 * alternative to the XML based config files which for much larger files is
 * a better idea (and can be backed by DTD/XSD/Relax-NG schemas). This class
 * should mean that a config object can being life as a properties file and
 * then move to an XML file easily if required.
 * 
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class PropertiesBeanUtils {

  /**
   * Used as a shortcut for initalisation which delegates properties file
   * loading from the classpath using {@link PropertyUtils#getProperties(String)}
   * and the passes the generated Object into 
   * {@link #populateBeanFromProperties(Object, Properties)}. 
   */
  public static void populateBeanFromClasspathPropertiesFile(Object bean, 
      String classpathLocation) throws ConfigException {
    Properties properties = PropertyUtils.getProperties(classpathLocation);
    populateBeanFromProperties(bean, properties);
  }
  
  /**
   * Used to populate a bean from a properties object which has already been
   * initalised.
   */
  public static void populateBeanFromProperties(Object bean, 
      Properties properties) throws ConfigException {
    try {
      BeanUtils.populate(bean, properties);
    }
    catch (IllegalAccessException e) {
      throw new ConfigException("Cannot populate bean from properties", e);
    }
    catch (InvocationTargetException e) {
      throw new ConfigException("Cannot populate bean from properties", e);
    }
  }
  
}
