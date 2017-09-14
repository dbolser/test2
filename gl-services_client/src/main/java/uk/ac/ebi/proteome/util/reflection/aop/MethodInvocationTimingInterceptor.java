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
import java.text.MessageFormat;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import uk.ac.ebi.proteome.util.reflection.ProxyUtils;

/**
 * Very basic implementation of around advice to provide timing metrics. If this
 * becomes a more custom requirement (rather than intercepting all methods and
 * timing them) then it will be migrated to one of the AOP frameworks. At the
 * moment it does not require it.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class MethodInvocationTimingInterceptor implements MethodInterceptor {

	/**
	 * Creates a proxy interface over the given object for the given interface
	 * class.
	 */
	public static <I> I generateProxy(Class<I> intrface, Object obj) {
		Callback callback = new MethodInvocationTimingInterceptor(obj);
		return ProxyUtils.createCglibProxy(intrface, callback);
	}

	private final Object target;

	private MethodInvocationTimingInterceptor(Object target) {
		this.target = target;
	}

	public Object intercept(Object obj, Method method, Object[] args,
			MethodProxy proxy) throws Throwable {

		Log log = LogFactory.getLog(target.getClass());

		long initalTime = 0L;
		if (log.isDebugEnabled()) {
			initalTime = getCurrentTime();
		}

		Object output = proxy.invoke(target, args);

		if (log.isDebugEnabled()) {
			String message = message(method, initalTime);
			log.debug(message);
		}

		return output;
	}

	private String message(Method method, long initalTime) {
		long time = getCurrentTime() - initalTime;
		String message = "{0} took {1}";
		String duration = DurationFormatUtils.formatDurationHMS(time);
		return MessageFormat.format(message, new Object[] { method.getName(),
				duration });
	}

	private long getCurrentTime() {
		return System.currentTimeMillis();
	}
}
