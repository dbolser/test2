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

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;

/**
 * Set of tests for working with the delegating proxy tests
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class DelegatingProxyTest extends TestCase {

	private Log log = LogFactory.getLog(this.getClass());

	public Log getLog(){
		return log;
	}

  public Hello createHelloProxy(Object... delegators) {
    return DelegatingProxy.createProxy(Hello.class, delegators);
  }

  public World createWorldProxy(Object... delegators) {
    return DelegatingProxy.createProxy(World.class, delegators);
  }

  public HelloWorld createHelloWorldProxy(Object... delegators) {
    return DelegatingProxy.createProxy(HelloWorld.class, delegators);
  }

  /**
   * Makes sure that given a set of invalid constructs the proxy will behave
   */
  public void testInvalidCreation() {
    assertInvalidContentsCorrectlyDealtWith(null);
    assertInvalidContentsCorrectlyDealtWith(new Object[0]);
  }

  private void assertInvalidContentsCorrectlyDealtWith(Object[] delegators) {
    try {
      DelegatingProxy.createProxy(Hello.class, delegators);
      String toStringResult = Arrays.toString(delegators);
      fail("Should have thrown an exception for delegators: "+toStringResult);
    }
    catch(ReflectionUncheckedException e) {
      //Normal flow
    }
  }

  /**
   * Tests that if I give a proxy a delegator with no valid methods it will
   * throw an exception
   */
  public void testNoValidDelegators() {

    Hello helloObject = createHelloProxy(new DelegatorZero());

    try {
      helloObject.hello();
      fail("Should have thrown an exception");
    }
    catch(ReflectionUncheckedException e) {
      //Normal flow
    }
  }

  private static final String DELEGATE_WRONG_MSG =
    "Expected delegated output did not match actual";

  /**
   * Tests that given valid delegators, the proxy will find the right one
   */
  public void testValidDelgators() {

    Object[] delegators = new Object[]{
        new DelegatorZero(),
        new DelegatorOne(),
        new DelegatorTwo(),
        new DelegatorThree()
    };

    Hello hello = createHelloProxy(delegators);
    World world = createWorldProxy(delegators);
    HelloWorld helloWorld = createHelloWorldProxy(delegators);

    assertEquals(DELEGATE_WRONG_MSG, "hello", hello.hello());
    assertEquals(DELEGATE_WRONG_MSG, "world", world.world());
    assertEquals(DELEGATE_WRONG_MSG, "hello", helloWorld.hello());
    assertEquals(DELEGATE_WRONG_MSG, "world", helloWorld.world());

    //Change the order & re-generate (welt now comes before world)
    delegators = new Object[] {
        new DelegatorZero(),
        new DelegatorOne(),
        new DelegatorThree(),
        new DelegatorTwo()
    };

    helloWorld = createHelloWorldProxy(delegators);
    assertEquals(DELEGATE_WRONG_MSG, "hello", helloWorld.hello());
    assertEquals(DELEGATE_WRONG_MSG, "welt", helloWorld.world());
  }

  /**
   * Prints the basic cost of delegation. Rename to testCostOfDelegation
   * to activate
   */
  public void ignoreTestCostOfDelegation() {

    Object[] delegators = new Object[] {
        new DelegatorZero(),
        new DelegatorOne(),
        new DelegatorThree(),
        new DelegatorTwo()
    };

    HelloWorld helloWorld = createHelloWorldProxy(delegators);
    HelloWorld localInstance = new DelegatorThree();

    int runs = 1000000;
    long delegatedTime = 0L;
    long actualTime = 0L;

    for(int i=0; i<runs; i++) {
      long timer = getTime();
      helloWorld.hello();
      delegatedTime = delegatedTime + reportTime(timer);

      timer = getTime();
      localInstance.hello();
      actualTime = actualTime + reportTime(timer);
    }

    getLog().info("Actual time: "+ (actualTime / runs));
    getLog().info("Delegated time: "+(delegatedTime / runs));
  }

  private long getTime() {
//    return System.currentTimeMillis();
    return System.nanoTime();
  }

  private long reportTime(long time) {
    return (getTime() - time);
  }

  static class DelegatorZero {
    public String helloWorld() {
      return "a hello world";
    }
  }

  static class DelegatorOne {
    public String hello() {
      return "hello";
    }
    public String hello(String you) {
      return "hello "+you;
    }
  }

  static class DelegatorTwo {
    public String world() {
      return "world";
    }
  }

  static class DelegatorThree implements HelloWorld {
    public String hello() {
      return "hallo";
    }
    public String world() {
      return "welt";
    }
  }

  static interface Hello {
    String hello();
  }

  static interface World {
    String world();
  }

  static interface HelloWorld extends Hello {
    String world();
  }

  static interface HelloResponse extends Hello {
    String hello(String you);
  }
}
