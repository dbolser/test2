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

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * An attempt at giving Java duck typing but rather than applying the ducking to
 * individual objects or inline in code asking objects will they correctly
 * respond, this proxy will return the output from a list of potentially
 * responding object known as delegators. These delegators are:
 *
 * <ul>
 * <li>POJOs</li>
 * <li>Do not have to implement the interface they will work through</li>
 * </ul>
 *
 * When a proxy is created the interceptor will:
 *
 * <ol>
 * <li>Create a DuckType[]</li>
 * <li>The interceptor is run whenever any method is run on the proxy</li>
 * <li>It loops through the delegators looking for a class which has the target
 * method</li>
 * <li>If it finds one it will return the first hit (order of delegators is
 * implicit)</li>
 * <li>If it does not then it will raise an
 * {@link ReflectionUncheckedException}</li>
 * </ol>
 *
 * This implementation uses the non-strict duck types i.e. All delegators can be
 * partial implementations of interface. This is useful when you want to
 * override one particular method of an interface and then send all other calls
 * off to the acutal backing implementation e.g. overriding a method in a result
 * set interface but calling the backing result set for all other methods
 *
 * <p>
 * Performance is dependent on the number of delegates the proxy has to consult
 * before finding one which has the correct method signature. Using a two level
 * delegation i.e. the correct object was 2nd in the delegates array; over
 * 1,000,000 calls cost of delegation is x2. The cost of resolving down a
 * delegate over this number of calls is approximatly 300ns
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 * @see ReflectionUncheckedException
 */
public class DelegatingProxy implements MethodInterceptor {

	private Log log = null;

	protected Log getLog() {
		if (log == null) {
			log = LogFactory.getLog(this.getClass());
		}
		return log;
	}

	/**
	 * Creates the proxy which will be an instance of the given interface
	 *
	 * @param intrface
	 *            Not a typo; interface is a keyword. The interface to generate
	 *            the proxy object to (and will be castable to)
	 * @param delegatingObjects
	 *            The objects which may respond to methods in the interface. The
	 *            order of the objects given here is the order which they will
	 *            be called in
	 *
	 * @return The proxy object backed by this DelgatingProxy instance
	 */
	public static <T> T createProxy(Class<T> intrface,
			Object... delegatingObjects) {
		DelegatingProxy callback = new DelegatingProxy(delegatingObjects);
		T output = ProxyUtils.createCglibProxy(intrface, callback);
		return output;
	}

	private final DuckType[] delegatingObjects;

	protected DelegatingProxy(Object[] delegatingObjects) {

		assertParams(delegatingObjects);
		this.delegatingObjects = new DuckType[delegatingObjects.length];

		// Creating the duck typed objects
		for (int i = 0; i < delegatingObjects.length; i++) {
			this.delegatingObjects[i] = new DuckType(delegatingObjects[i]);
		}
	}

	/**
	 * Throws an exception if the objects are null or is length 0
	 */
	private static void assertParams(Object[] delegatingObjects) {
		if (delegatingObjects == null) {
			raiseException("Delegating objects array cannot be null");
		}
		if (delegatingObjects.length == 0) {
			raiseException("Must define delegators for this proxy to work");
		}
	}

	/**
	 * Main method for processing the intercept as defined in the class javadoc
	 */
	public Object intercept(Object obj, Method method, Object[] args,
			MethodProxy proxy) throws Throwable {

		boolean noMatchingMethod = true;
		Object output = null;

		for (DuckType currentObject : delegatingObjects) {
			int index = currentObject.getFastMethodIndex(method);
			if (index != ReflectionUtils.INVALID_METHOD_INDEX) {
				noMatchingMethod = false;
				output = currentObject.invoke(index, args);
				break;
			}
		}

		if (noMatchingMethod) {
			raiseException("Could not find a matching method ("
					+ method.getName() + ")in all " + "registered objects");
		}

		return output;
	}

	private static void raiseException(String msg) {
		throw new ReflectionUncheckedException(msg);
	}
}
