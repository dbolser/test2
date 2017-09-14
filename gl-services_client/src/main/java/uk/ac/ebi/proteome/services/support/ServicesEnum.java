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

package uk.ac.ebi.proteome.services.support;

import uk.ac.ebi.proteome.services.sql.SqlService;

/**
 * Holds references to the different types of services offered by the Integr8
 * core system.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public enum ServicesEnum {

	SQL("sqlServiceImpl") {
		@Override
		public Class<SqlService> getInterfaceClass() {
			return SqlService.class;
		}
	};

	private final String property;

	private ServicesEnum(String property) {
		this.property = property;
	}

	/**
	 * Returns how this service is represented
	 */
	public String getProperty() {
		return property;
	}

	/**
	 * Used to represent the interface of a service
	 */
	public abstract Class<?> getInterfaceClass();
}
