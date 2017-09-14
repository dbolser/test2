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

package uk.ac.ebi.proteome.materializer.model.finders.protein;

import uk.ac.ebi.proteome.genomebuilder.model.Protein;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocationModifier;
import uk.ac.ebi.proteome.materializer.model.finders.abstracts.AbstractLocationFinder;
import uk.ac.ebi.proteome.materializer.model.finders.generic.SubLocationFinder;
import uk.ac.ebi.proteome.materializer.model.finders.generic.LocationExceptionFinder;
import uk.ac.ebi.proteome.materializer.model.finders.generic.LocationInsertionFinder;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.materializer.MaterializedDataInstance;

import java.util.Collection;

/**
 * Depending on the number of parameters given this object will generate
 * a fully formed location (if you give all the data) or just a top
 * level location (which can be misleading).
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class ProteinLocationFinder extends AbstractLocationFinder<Protein> {

	/**
	 * Returns a basic top level model
	 */
	public ProteinLocationFinder(MaterializedDataInstance<Collection<Persistable<EntityLocation>>, Protein> mdi) {
		super(mdi);
	}

	/**
	 * Will generate a fully formed model.
	 */
	public ProteinLocationFinder(
		MaterializedDataInstance<Collection<Persistable<EntityLocation>>, Protein> mdi,
		MaterializedDataInstance<Collection<Persistable<EntityLocation>>, EntityLocation> subLocationMdi,
		MaterializedDataInstance<Collection<Persistable<EntityLocationModifier>>, EntityLocation> modifierMdi) {

		super(	mdi,
						new SubLocationFinder(subLocationMdi),
						new LocationExceptionFinder(modifierMdi),
						new LocationInsertionFinder(modifierMdi)
		);
	}
}
