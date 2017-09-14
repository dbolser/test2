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

import uk.ac.ebi.proteome.services.ServiceContext;
import uk.ac.ebi.proteome.services.ServiceUncheckedException;

/**
 * Defaults the id to the name of the update process
 *
 * @author ayates
 * @author $Author$
 * @version $Version$
 */
public class UpdateProcessOwnerAccessLockIdProvider
	extends AbstractAccessLockIdProvider {

	private ServiceContext context;

	public UpdateProcessOwnerAccessLockIdProvider(ServiceContext context) {
		this.context = context;
	}

	public String getNewIdDelegate() {
		String id = context.getUpdateProcessName();
		if(id == null) {
			throw new ServiceUncheckedException("Cannot work with a " +
				"null ID. Please ensure one is set in update process " +
				"name in Service Context");
		}
		return id;
	}
}
