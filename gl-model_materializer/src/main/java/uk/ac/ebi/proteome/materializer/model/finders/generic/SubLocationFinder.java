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

package uk.ac.ebi.proteome.materializer.model.finders.generic;

import org.biojavax.bio.seq.RichLocation;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.impl.DelegatingEntityLocation;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.finder.Finder;
import uk.ac.ebi.proteome.persistence.materializer.MaterializedDataInstance;
import uk.ac.ebi.proteome.util.biojava.LocationUtils;

import java.util.Collection;

/**
 * Finds any possible sub locations from the given data object and
 * constructs a new EntityLocation based upon this information (since
 * the majority of the time what we need is the new representation
 * a Location).
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class SubLocationFinder implements Finder<Persistable<EntityLocation>, EntityLocation> {

	private final FinderUtils utils = new FinderUtils();

	private final
		MaterializedDataInstance<Collection<Persistable<EntityLocation>>,
			EntityLocation> mdi;

	public SubLocationFinder(
			MaterializedDataInstance<Collection<Persistable<EntityLocation>>,
				EntityLocation> mdi) {
		this.mdi = mdi;
	}

	public EntityLocation find(Persistable<EntityLocation> query) {
		if(mdi.hasData(query)) {
			Collection<Persistable<EntityLocation>> pLocs = mdi.getData(query);
			return processSubLocations(pLocs);
		}
		return null;
	}

	protected EntityLocation processSubLocations(Collection<Persistable<EntityLocation>> subLocations) {
		Collection<RichLocation> unwrappedLocations = utils.unwrapLocations(subLocations);
		RichLocation location = LocationUtils.construct(unwrappedLocations);
		return new DelegatingEntityLocation(location);
	}
}
