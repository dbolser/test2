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

package uk.ac.ebi.proteome.services;

import junit.framework.TestCase;
import uk.ac.ebi.proteome.services.ServiceContext.ContextMemento;
import uk.ac.ebi.proteome.services.support.propertylocators.MapPropertyLocator;

/**
 * Attempts to test the memento ability of the service context
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class ServiceContextMementoTest extends TestCase {

	public void testCreateMemento() {
		String key = "test";
		ServiceContext context = new ServiceContext();
		PropertyLocator<String, String>  originalLocator = createLocator(key, "hello");
		context.setPropertyLocator(originalLocator);

		ContextMemento memento = new ContextMemento(context);

		PropertyLocator<String, String>  newLocator = createLocator(key, "boing");
		context.setPropertyLocator(newLocator);

		assertNotSame("Locators should be different", originalLocator,
				context.getPropertyLocator());

		memento.restoreContext(context);

		assertSame("The two property locators should be the same since we have " +
				"restored it via a memento",
				originalLocator, context.getPropertyLocator());

		assertLocatorPropertyEqual(originalLocator, context.getPropertyLocator(),
				key);
	}

	public PropertyLocator<String, String> createLocator(String key, String value) {
		PropertyLocator<String, String>  locator = new MapPropertyLocator<String, String>();
		locator.setProperty(key, value);
		return locator;
	}

	private void assertLocatorPropertyEqual(PropertyLocator<String, String>  expectedLocator,
			PropertyLocator<String, String>  actualLocator, String key) {
		String expected = expectedLocator.getProperty(key);
		String actual = actualLocator.getProperty(key);
		assertEquals("Property "+key+" was not the same in locators",
				expected, actual);
	}
}
