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

package uk.ac.ebi.proteome.materializer.model.finders.proteinfeature;

import uk.ac.ebi.proteome.genomebuilder.model.ProteinFeature;
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
 * Will return a simple top level model object if the one parameter version
 * is used otherwise it will return a fully formed location model object.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class ProteinFeatureLocationFinder extends AbstractLocationFinder<ProteinFeature> {
	public ProteinFeatureLocationFinder(MaterializedDataInstance<Collection<Persistable<EntityLocation>>, ProteinFeature> mdi) {
		super(mdi);
	}

	public ProteinFeatureLocationFinder(
		MaterializedDataInstance<Collection<Persistable<EntityLocation>>, ProteinFeature> mdi,
		MaterializedDataInstance<Collection<Persistable<EntityLocation>>, EntityLocation> subLocationMdi,
		MaterializedDataInstance<Collection<Persistable<EntityLocationModifier>>, EntityLocation> modifierMdi) {

		super(	mdi,
						new SubLocationFinder(subLocationMdi),
						new LocationExceptionFinder(modifierMdi),
						new LocationInsertionFinder(modifierMdi));
	}
}
