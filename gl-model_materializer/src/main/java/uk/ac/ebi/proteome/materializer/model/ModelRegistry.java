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

package uk.ac.ebi.proteome.materializer.model;

import uk.ac.ebi.proteome.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.materializer.misc.AbstractDbRegistry;
import uk.ac.ebi.proteome.services.ServiceContext;
import uk.ac.ebi.proteome.services.sql.SqlService;

/**
 * Constructs with the required service templates & library but still requires
 * you to give it the xref registry
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class ModelRegistry extends AbstractDbRegistry {

	public ModelRegistry(String uri, ServiceContext context) {
		super(uri, context);
	}

	public ModelRegistry(String uri, SqlService service) {
		super(uri, service);
	}

	public ModelRegistry(String uri, ServiceContext context, DatabaseReferenceTypeRegistry registry) {
		super(uri, context);
		put(DatabaseReferenceTypeRegistry.class, registry);
	}
}
