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

package uk.ac.ebi.proteome.materializer.model.finders.operon;

import uk.ac.ebi.proteome.genomebuilder.model.Operon;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocation;
import uk.ac.ebi.proteome.materializer.model.finders.abstracts.AbstractLocationFinder;
import uk.ac.ebi.proteome.persistence.materializer.MaterializedDataInstance;
import uk.ac.ebi.proteome.persistence.Persistable;

import java.util.Collection;

/**
 * Finds an Operon's location
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class OperonLocationFinder extends AbstractLocationFinder<Operon> {

	public OperonLocationFinder(
		MaterializedDataInstance<Collection<Persistable<EntityLocation>>, Operon> mdi) {
		super(mdi);
	}
}
