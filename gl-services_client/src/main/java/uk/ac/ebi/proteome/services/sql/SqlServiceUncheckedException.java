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

package uk.ac.ebi.proteome.services.sql;

import uk.ac.ebi.proteome.services.ServiceUncheckedException;

/**
 * Used to indicate a potentially thrown exception from the {@link SqlService}
 * hierarchy of classes but cannot be delt with by a calling user.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class SqlServiceUncheckedException extends ServiceUncheckedException {

	private static final long serialVersionUID = 242237559538252306L;

	public SqlServiceUncheckedException() {
		super();
	}

	public SqlServiceUncheckedException(String message) {
		super(message);
	}

	public SqlServiceUncheckedException(Throwable cause) {
		super(cause);
	}

	public SqlServiceUncheckedException(String message, Throwable cause) {
		super(message, cause);
	}

}
