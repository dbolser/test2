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

import junit.framework.TestCase;

/**
 * Used to test the around advice to ensure that it does not mess around with
 * the output from the class
 * 
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class MethodInvocationTimingInterceptorTest extends TestCase {

	private static final String EXPECTED = "val";
	
	public void testLoggingInterceptor() {
		Temp obj = (Temp)MethodInvocationTimingInterceptor.generateProxy(Temp.class, new MyLocalTest());
		String actual = obj.getValue();
		assertEquals("Proxied object did not pass out the correct value", 
				EXPECTED, actual);
	}
	
	private static interface Temp {
		String getValue();
	}
	
	private static class MyLocalTest implements Temp {
		public String getValue() {
			return EXPECTED;
		}
	}
}
