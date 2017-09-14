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

package uk.ac.ebi.proteome.util.reflection.aop;

import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import uk.ac.ebi.proteome.util.reflection.ProxyUtils;

/**
 * Taken from <a href="http://www.onjava.com/pub/a/onjava/2003/08/20/memoization.html">
 * http://www.onjava.com/pub/a/onjava/2003/08/20/memoization.html</a> as a way
 * of implementing Memoize.pm Perl library in Java to introduce a transparent
 * proxy mechanism. Currently the implementation has the following problems:
 * 
 * <ul>
 * <li>The memoized object must implement an interface since this is using proxies</li>
 * <li>The memoized cache is object specific i.e. not static. If you lose this object you'll lose the cache</li>
 * <li>The cache takes all method arguments and converts it into a List 
 * of Objects. All method parameters should implement hashCode and equals to
 * ensure the advertised functionality</li>
 * <li>This class will not memoize any method with a return type of void
 * unless explictly told to do so. Memoizing void methods can be dangerous
 * since it assumes that output does not change between invocations.</li>
 * <li>The cache is implemented as a HashMap. This may cause problems with 
 * unintentional object rentention i.e. memory leaks. If so this class will
 * be changed to use {@link SoftReference} objects</li>
 * </ul>
 * 
 * For more information on this process please see 
 * <a href="http://en.wikipedia.org/wiki/Memoization">
 * http://en.wikipedia.org/wiki/Memoization</a>.
 * 
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class Memoizer implements MethodInterceptor {
	
	private static final boolean DEFAULT_VOID_MEMOIZE_POLICY = false;
	
	/**
	 * Default memoize create method
	 */
	public static <I> I memoize(Class<I> intrface, Object obj) {
		return memoize(intrface, obj, DEFAULT_VOID_MEMOIZE_POLICY);
	}
	
	/**
	 * Creates the memoize proxy object with the option to turn on the ability
	 * to memoize void methods (very dangerous)
	 */
	public static <I> I memoize(Class<I> intrface, Object obj, boolean memoizeVoidMethods) {		
		Callback callback = new Memoizer(obj, memoizeVoidMethods);
		return ProxyUtils.createCglibProxy(intrface, callback);
	}

	private final Object object;
	private final boolean memoizeVoidMethods;

	private Map<Method,Map<Object,Object>> caches = new HashMap<Method,Map<Object,Object>>();

	private Memoizer(Object object, boolean memoizeVoidMethods) {
		this.object = object;
		this.memoizeVoidMethods = memoizeVoidMethods;
	}
	
	/**
	 * If {@link Method#getReturnType()} equals {@link Void#TYPE} then nothing is
	 * memoized. If so it will then query against the internal map using method
	 * as the first key & then the hash lookup of args as a List 
	 */
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		if (method.getReturnType().equals(Void.TYPE) && memoizeVoidMethods == false) {
			// Don't cache void methods
			return invoke(proxy, args);
		}
		
		Map<Object,Object> cache = getCache(method);
		List<Object> key = Arrays.asList(args);
		Object value = cache.get(key);
		if (value == null && !cache.containsKey(key)) {
			value = invoke(proxy, args);
			cache.put(key, value);
		}
		
		return value;
	}
	
	/**
	 * Invokes the method and returns the output object
	 */
	private Object invoke(MethodProxy proxy, Object[] args) throws Throwable {
		Object value = proxy.invoke(object, args);
		return value;
	}

	/**
	 * Returns the cache associated with the given {@link Method} param
	 */
	private synchronized Map<Object,Object> getCache(Method m) {
		Map<Object,Object> cache = caches.get(m);
		if (cache == null) {
			cache = Collections.synchronizedMap(new HashMap<Object,Object>());
			caches.put(m, cache);
		}
		return cache;
	}
}
