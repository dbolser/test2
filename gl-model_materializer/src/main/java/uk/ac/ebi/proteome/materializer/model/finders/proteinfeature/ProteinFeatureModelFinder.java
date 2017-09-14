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

import uk.ac.ebi.proteome.genomebuilder.model.EntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.Protein;
import uk.ac.ebi.proteome.genomebuilder.model.ProteinFeature;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocationModifier;
import uk.ac.ebi.proteome.materializer.model.finders.abstracts.AbstractModelFinder;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.materializer.MaterializedDataInstance;

import java.util.Collection;

/**
 * Generates a fully formed protein feature object from a given protein
 * query object.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class ProteinFeatureModelFinder extends AbstractModelFinder<ProteinFeature, Protein> {

	public ProteinFeatureModelFinder(
		MaterializedDataInstance<Collection<Persistable<ProteinFeature>>, Protein> mdi,
		MaterializedDataInstance<Collection<Persistable<EntityLocation>>, ProteinFeature> locationMdi,
		MaterializedDataInstance<Collection<Persistable<EntityLocation>>, EntityLocation> subLocationMdi,
		MaterializedDataInstance<Collection<Persistable<EntityLocationModifier>>, EntityLocation> modsMdi) {

		super(mdi);

		addPostProcessor(new LocationPostProcessor<ProteinFeature>(
			new ProteinFeatureLocationFinder(locationMdi, subLocationMdi, modsMdi)));
	}
}
