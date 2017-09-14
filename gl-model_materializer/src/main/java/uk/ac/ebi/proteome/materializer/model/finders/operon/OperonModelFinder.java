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
import uk.ac.ebi.proteome.genomebuilder.model.Transcript;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReference;
import uk.ac.ebi.proteome.materializer.model.finders.abstracts.AbstractModelFinder;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.materializer.MaterializedDataInstance;

import java.util.Collection;

/**
 * Generates a fully formed operon object from a given Transcript object
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class OperonModelFinder extends AbstractModelFinder<Operon, Transcript> {

	public OperonModelFinder(
		MaterializedDataInstance<Collection<Persistable<Operon>>, Transcript> mdi,
		MaterializedDataInstance<Collection<Persistable<EntityLocation>>, Operon> locationMdi,
		MaterializedDataInstance<Collection<Persistable<DatabaseReference>>, Operon> xrefMdi) {

		super(mdi);

		addPostProcessor(new LocationPostProcessor<Operon>(new OperonLocationFinder(locationMdi)));
		addPostProcessor(new XrefPostProcessor<Operon>(new OperonXrefFinder(xrefMdi)));
	}

}
