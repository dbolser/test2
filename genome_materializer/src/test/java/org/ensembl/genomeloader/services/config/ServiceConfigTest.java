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

package org.ensembl.genomeloader.services.config;

import junit.framework.TestCase;

/**
 * Test cases for the service config
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class ServiceConfigTest extends TestCase {

	public void testCopyConstructor() {
		ServiceConfig config = new ServiceConfig();
		config.setApiSrcLocation("something");

		ServiceConfig newConfig = new ServiceConfig(config);

		assertEquals("Two copied configurations are not equal for apiSrcLocation",
			config.getApiSrcLocation(), newConfig.getApiSrcLocation());

		newConfig.setApiSrcLocation("something else now");
		assertFalse("apiSrcLocation was changed but both values equal each other",
				config.getApiSrcLocation().equals(newConfig.getApiSrcLocation()));
	}

}
