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

package uk.ac.ebi.proteome.util.sql;

import java.util.Arrays;

import junit.framework.TestCase;

/**
 * Tests the correct functioning of the {@link TransactionalDmlHolder} class
 * 
 * @author ayates
 * @author $Author$
 * @version $Version$
 */
public class TransactionalDmlHolderTest extends TestCase {

  public TransactionalDmlHolderTest(String name) {
    super(name);
  }
  
  public void testNullHandling() {
    TransactionalDmlHolder holder = new TransactionalDmlHolder();
    String sql = "one";
    String id = holder.addStatement(sql, null);
    int expected = 0;
    int actual = holder.getParamHolder().get(id).length;
    assertEquals("Submitted array parameters was null but length was " +
        "not as expected - should convert to an empty object array", 
        expected, actual);
  }
  
  public void testDmlExtractionSameAsInsert() {
    TransactionalDmlHolder holder = getDefaultHolder();
    
    String[] expected = new String[]{"one", "two", "three"};
    String[] actual = holder.getStatementsArray();
    assertTrue("The extracted dml array was not the same as expected", 
        Arrays.deepEquals(expected, actual));
    
    String expectedOne = expected[1];
    String actualOne = actual[1];
    assertEquals("The values held in position one were not the same between the arrays", 
        expectedOne, actualOne);
  }
  
  public void testParameterExtractionSameAsInsert() {
    TransactionalDmlHolder holder = getDefaultHolder();
    
    Object[] expected = new Object[]{15};
    Object[] actual = holder.getParametersArray()[1];
    assertTrue("The extracted parameter array was not the same as expected", 
        Arrays.deepEquals(expected, actual));
  }

  private TransactionalDmlHolder getDefaultHolder() {
    TransactionalDmlHolder holder = new TransactionalDmlHolder();
    
    String sqlOne = "one";
    Object[] paramsOne = null;
    holder.addStatement(sqlOne, paramsOne);
    
    String sqlTwo = "two";
    Object[] paramsTwo = new Object[]{15};
    holder.addStatement(sqlTwo, paramsTwo);
    
    String sqlThree = "three";
    Object[] paramsThree = new Object[]{"param"};
    holder.addStatement(sqlThree, paramsThree);
    
    return holder;
  }
}
