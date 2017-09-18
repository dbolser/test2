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

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;

/**
 * A collection of methods which is useful to run when creating proxies
 * 
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class ProxyUtils {

	/**
	 * This is meant as a shortcut method to help generating CGLIB proxy classes.
	 * It assumes a basic setup as seen in other AOP style proxies in
	 * services_client
	 * 
	 * @param intrface
	 *          The interface to generate the proxy from
	 * @param callback
	 *          The call back to use in this proxy
	 * @return An instance of a proxy object which will implement the given
	 *         interface.
	 */
	@SuppressWarnings("unchecked")
	public static <I> I createCglibProxy(Class<I> intrface, Callback callback) {
		Enhancer enhancer = new Enhancer();
		ReflectionUtils.assertClassIsInterface(intrface);
		enhancer.setInterfaces(new Class[] { intrface });
		enhancer.setCallback(callback);
		Object enhancedObject = enhancer.create();
		ReflectionUtils.assertObjectImplementsClass(intrface, enhancedObject);
		return (I)enhancedObject;
	}

}
