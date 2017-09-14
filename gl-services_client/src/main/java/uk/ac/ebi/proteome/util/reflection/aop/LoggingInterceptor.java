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

import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import uk.ac.ebi.proteome.util.reflection.ProxyUtils;
import uk.ac.ebi.proteome.util.templating.TemplateBuilder;

/**
 * Used to define logging of method calls and parameters to said method at a
 * debug level. The logger used is an instance of the object to proxy onto.
 * This should not be used in production for extensively called objects due
 * to the performance decrease which will be seen with all proxy objects.
 * However it can be very useful in a debugging scenario.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class LoggingInterceptor implements MethodInterceptor {

	/**
	 * Creates a proxy interface over the given object for the given interface
	 * class.
	 */
	public static <I> I generateProxy(Class<I> intrface, Object obj) {
		Callback callback = new LoggingInterceptor(obj);
		return ProxyUtils.createCglibProxy(intrface, callback);
	}

	private final Object target;
	private final Log log;

	public LoggingInterceptor(Object target) {
		this.target = target;
		log = LogFactory.getLog(target.getClass());
	}

	public Object intercept(Object obj, Method method, Object[] args,
			MethodProxy proxy) throws Throwable {

		if(log.isDebugEnabled()) {
			log.debug(createLogMessage(method, args));
		}

		Object output = proxy.invoke(target, args);

		return output;
	}

	public String createLogMessage(Method method, Object[] args) {
		String template = "Method: $method$ $ls$Arguments: $args$";
		TemplateBuilder builder = new TemplateBuilder(template);
		builder.addPlaceHolder("method", method.getName());
		builder.addPlaceHolder("ls", SystemUtils.LINE_SEPARATOR);
		builder.addPlaceHolder("args", arrayToString(args));
		return builder.generate();
	}

	public String arrayToString(Object[] objects) {
		StringBuilder builder = new StringBuilder();
		builder.append('[');
		if(objects == null) {
			builder.append("null");
		}
		else {
			int length = objects.length;
			int limit = length -1;
			for(int i=0; i<objects.length; i++) {
				Object current = objects[i];
				String objectToString = StringUtils.EMPTY;
				if(current == null) {
					objectToString = "null";
				}
				else if(current.getClass().isArray()) {
					objectToString = arrayToString((Object[])current);
				}
				else {
					objectToString = String.valueOf(current);
				}

				builder.append(objectToString);

				if(i != limit) {
					builder.append(',');
				}
			}
		}
		builder.append(']');
		return builder.toString();
	}
}
