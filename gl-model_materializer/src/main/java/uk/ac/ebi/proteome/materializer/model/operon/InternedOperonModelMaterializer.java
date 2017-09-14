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

package uk.ac.ebi.proteome.materializer.model.operon;

import uk.ac.ebi.proteome.genomebuilder.model.Operon;
import uk.ac.ebi.proteome.genomebuilder.model.Transcript;
import uk.ac.ebi.proteome.materializer.misc.CollectionRedundancyFilterMaterializer;
import uk.ac.ebi.proteome.registry.Registry;

/**
 * Creates an instance of {@link OperonModelMaterializer} and passes the data
 * via a {@link CollectionRedundancyFilterMaterializer} object in order to
 * intern any Operon objects that share the same identifier.
 * 
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class InternedOperonModelMaterializer extends
		CollectionRedundancyFilterMaterializer<Operon, Transcript> {

	public InternedOperonModelMaterializer(Registry registry) {
		super(new OperonModelMaterializer(registry));
	}
}
