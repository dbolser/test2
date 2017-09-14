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

package uk.ac.ebi.proteome.util.reflection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;
import uk.ac.ebi.proteome.util.reflection.sub.InvokerTestObject;
import uk.ac.ebi.proteome.util.reflection.sub.One;

/**
 * Test cases to ensure that the reflection utils class is operating as
 * expected
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class ReflectionUtilsTest extends TestCase {

  public void testGetClassFromNameWithClass() {
    Class<?> goodExpected = CharSequence.class;
    Class<?> badExpected = ArrayList.class;
    String className = "java.lang.String";

    ReflectionUtils.getClassFromName(className, goodExpected);

    try {
      ReflectionUtils.getClassFromName(className, badExpected);
      fail("Should have thrown an exception since String is not assignable to ArrayList");
    }
    catch(ReflectionUncheckedException e) {
      //Normal flow
    }
  }

  public void testGetFieldsOfSpecifiedTypeFromObject() {
    assertGetFieldsOfTypeLength(One.class, new Class[]{String.class}, 3);
    assertGetFieldsOfTypeLength(One.class, new Class[]{Integer.class}, 1);
    assertGetFieldsOfTypeLength(One.class, new Class[]{String.class, Integer.class}, 4);
  }

  private void assertGetFieldsOfTypeLength(Class<?> targetClass, Class<?>[] objectTypes, int expected) {
    List<Field> fields = ReflectionUtils.getFieldsOfSpecifiedTypeFromObject(targetClass, objectTypes);
    int actual = fields.size();
    assertEquals("The size of the actual fields from "+
        targetClass+" for types "+Arrays.toString(objectTypes)+" was not as expected",
        expected, actual);
  }

  public void testInvokeMethod() {
  	InvokerTestObject test = new InvokerTestObject();

  	int expected = 1;
  	int actual = (Integer)ReflectionUtils.invokeMethod(test, "getVal");
  	assertEquals("Invoked value not as expected", expected, actual);

  	expected = 2;
  	actual= (Integer)ReflectionUtils.invokeMethod(test, "getVal", new Object[]{1});
  	assertEquals("Invoked value not as expected", expected, actual);

  	expected = 3;
  	actual= (Integer)ReflectionUtils.invokeMethod(test, "getVal", new Object[]{2}, new Class[]{Integer.TYPE});
  	assertEquals("Invoked value not as expected", expected, actual);

  	expected = 4;
  	actual= (Integer)ReflectionUtils.invokeMethod(test, "getVal", new Object[]{new Double(3)}, new Class[]{Number.class});
  	assertEquals("Invoked value not as expected", expected, actual);

  	ReflectionUtils.invokeMethod(test, "switchSwitcherTrue");
  	assertTrue("Switcher was not set to true", test.switcher);

  	String expectedString = "echo";
  	String actualString = ReflectionUtils.invokeMethod(InvokerTestObject.class, "echo");
  	assertEquals("Expected string did not equal reflected method value", expectedString, actualString);

  	actualString = "THIS IS WRONG!";
  	actualString = ReflectionUtils.invokeMethod("uk.ac.ebi.proteome.util.reflection.sub.InvokerTestObject", "echo");
  	assertEquals("Expected string did not equal reflected method value", expectedString, actualString);
  }

  public void testGetResourceAsStreamCompatibleName() {
  	String expected = "/uk/ac/ebi/proteome/util/reflection/tmp.txt";
  	String actual = ReflectionUtils.getResourceAsStreamCompatibleName(ReflectionUtilsTest.class, "tmp.txt");
  	assertEquals("Generated location not as expected", expected, actual);
  }
}
