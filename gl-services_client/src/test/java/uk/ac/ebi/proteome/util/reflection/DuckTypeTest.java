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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;


/**
 * Set of tests for the duck type object
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class DuckTypeTest extends TestCase {

	private Log log = LogFactory.getLog(this.getClass());

	public Log getLog() {
		return log;
	}

  public void testDuckTyping() {

    HelloResponse helloResponse = DuckType.
        createProxy(HelloResponse.class, new Duck());

    assertEquals("Actual ducked output was not same as expected",
        "hello", helloResponse.hello());
    assertEquals("Actual ducked output was not same as expected",
        "hello you", helloResponse.hello("you"));

    helloResponse = DuckType.createProxy(HelloResponse.class, new Swan());

    try {
      helloResponse.hello();
      fail("Did not through an exception when underlying class did not " +
          "have any valid methods");
    }
    catch(ReflectionUncheckedException e) {
      //Normal flow
    }
  }

  public void testStrictDuckTyping() {

    //Should work
    DuckType.createStrictProxy(HelloResponse.class, new Duck());
    assertBadStrictDuck(new BabyDuck());
    assertBadStrictDuck(new Swan());
  }

  private void assertBadStrictDuck(Object obj) {
    try {
      DuckType.createStrictProxy(HelloResponse.class, obj);
      fail("Given object should not have been instanciated since it was " +
          "not a full interface implementation");
    }
    catch (ReflectionUncheckedException e) {
      //NORMAL FLOW
    }
  }

  public void testBadInterface() {
    try {
      DuckType.createStrictProxy(Duck.class, new Duck());
      fail("Given class was not an interface and should have thrown an exception");
    }
    catch (ReflectionUncheckedException e) {
      //NORMAL FLOW
    }
  }

  public void ignoreTestCostOfDuck() {
    HelloResponse normal = new Duck();
    HelloResponse ducked = DuckType.createStrictProxy(HelloResponse.class, normal);

    int times = 100000;

    getLog().info("Normal execution time: "+averageTimeExceution(normal, times));
    getLog().info("Ducked execution time: "+averageTimeExceution(ducked, times));
  }

  private long averageTimeExceution(HelloResponse obj, int numberOfTimes) {
    long totalTime = 0L;
    obj.hello();
    for(int i=0; i<numberOfTimes; i++) {
      long currentTime = System.nanoTime();
      obj.hello();
      long timeTaken = System.nanoTime() - currentTime;
      totalTime = totalTime + timeTaken;
    }

    return (totalTime/numberOfTimes);
  }

  static class Duck implements HelloResponse {
    public String hello() {
      return "hello";
    }
    public String hello(String you) {
      return "hello "+you;
    }
  }

  static class BabyDuck {
    public String hello() {
      return "hello";
    }
  }

  static class Swan {
    public String hi() {
      return "hi";
    }
  }

  static interface HelloResponse {
    String hello();
    String hello(String you);
  }
}
