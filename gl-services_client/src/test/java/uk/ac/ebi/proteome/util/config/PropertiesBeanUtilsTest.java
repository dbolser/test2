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

import java.util.Properties;

import junit.framework.TestCase;

/**
 * Runs tests for assuring that the properties based config loader/parser
 * which correctly loads from classpath resources and can respect default
 * object values i.e. This is not a tool for marshalling.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class PropertiesBeanUtilsTest extends TestCase {

  private static final String TEST_PROPERTIES_LOCATION =
    "/uk/ac/ebi/proteome/util/config/test.properties";

  public void testBeanFilling() {
    Test bean = new Test();
    Properties properties = getDefaultProperties();
    PropertiesBeanUtils.populateBeanFromProperties(bean, properties);
    assertBasicBeanDetails(bean);
  }

  public void testBeanFillingRespectingDefaults() {
    TestExtension bean = new TestExtension();
    Properties properties = getDefaultProperties();
    PropertiesBeanUtils.populateBeanFromProperties(bean, properties);
    assertPlanetValue(bean, "earth");
  }

  public void testBeanFillingReplacingDefaults() {
    TestExtension bean = new TestExtension();
    Properties properties = getDefaultProperties();
    properties.setProperty("planet", "mars");

    PropertiesBeanUtils.populateBeanFromProperties(bean, properties);
    assertPlanetValue(bean, "mars");
  }

  public void testBeanFillingFromClasspathFile() {
    Test bean = new Test();
    PropertiesBeanUtils.populateBeanFromClasspathPropertiesFile(bean,
        TEST_PROPERTIES_LOCATION);
    assertBasicBeanDetails(bean);
  }

  private Properties getDefaultProperties() {
    Properties properties = new Properties();
    properties.setProperty("name", "Andy");
    properties.setProperty("age", "26");
    properties.setProperty("male", "true");
    return properties;
  }

  private void assertBasicBeanDetails(Test bean) {
    assertEquals("Name was not the same", "Andy", bean.getName());
    assertEquals("Age was not the same", 26, bean.getAge());
    assertEquals("Male was not the same", true, bean.isMale());
  }

  private void assertPlanetValue(TestExtension bean, String expected) {
    String actual = bean.getPlanet();
    assertEquals("The property planet was not set to expected value",
        expected, actual);
  }

  /**
   * Used in {@link PropertiesBeanUtilsTest#testBeanFilling()}
   *
   * @author ayates
   */
  public static class Test {
    private String name;
    private int age;
    private boolean male;

    public int getAge() {
      return age;
    }
    public void setAge(int age) {
      this.age = age;
    }
    public boolean isMale() {
      return male;
    }
    public void setMale(boolean male) {
      this.male = male;
    }
    public String getName() {
      return name;
    }
    public void setName(String name) {
      this.name = name;
    }
  }

  /**
   * Used in {@link PropertiesBeanUtilsTest#testBeanFillingRespectingDefaults()}
   *
   * @author ayates
   */
  public static class TestExtension extends Test {
    private String planet = "earth";

    public String getPlanet() {
      return planet;
    }
    public void setPlanet(String planet) {
      this.planet = planet;
    }
  }
}
