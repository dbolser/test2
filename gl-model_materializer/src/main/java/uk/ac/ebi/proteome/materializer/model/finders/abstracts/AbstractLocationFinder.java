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

package uk.ac.ebi.proteome.materializer.model.finders.abstracts;

import uk.ac.ebi.proteome.genomebuilder.model.EntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocationException;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocationInsertion;
import uk.ac.ebi.proteome.materializer.model.finders.generic.FinderUtils;
import uk.ac.ebi.proteome.materializer.model.finders.generic.MockFinders;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.finder.Finder;
import uk.ac.ebi.proteome.persistence.materializer.MaterializedDataInstance;
import uk.ac.ebi.proteome.persistence.persistables.SimpleWrapperPersistable;

import java.util.Collection;

/**
 * Returns as fully formed a EntityLocation as possible with sublocations,
 * insertions and exceptions populated into the object. However this data
 * will only be populated if this class has the internal finder methods
 * overridden accordingly to return Finders which do contain data or give
 * it to the class during construction (a better way)
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractLocationFinder<Q> implements Finder<Persistable<Q>, Persistable<EntityLocation>> {

	private final MaterializedDataInstance<Collection<Persistable<EntityLocation>>, Q> mdi;
	private FinderUtils utils = new FinderUtils();

	private final Finder<Persistable<EntityLocation>, EntityLocation> subLocationFinder;
	private final Finder<Persistable<EntityLocation>, Collection<EntityLocationException>> exceptionFinder;
	private final Finder<Persistable<EntityLocation>, Collection<EntityLocationInsertion>> insertionFinder;

	/**
	 * Create a finder which just finds top level locations & no associated 
	 * information (since it has not been given any)
	 */
	protected AbstractLocationFinder(
		MaterializedDataInstance<Collection<Persistable<EntityLocation>>, Q> mdi) {
		this.mdi = mdi;
		this.subLocationFinder = MockFinders.create();
		this.exceptionFinder = MockFinders.createCollection();
		this.insertionFinder = MockFinders.createCollection();
	}

	/**
	 * Version of the finder which uses these given collections to populate
	 * a much fuller version of the EntityLocation objects.
	 */
	protected AbstractLocationFinder(
		MaterializedDataInstance<Collection<Persistable<EntityLocation>>, Q> mdi,
		Finder<Persistable<EntityLocation>, EntityLocation> subLocationFinder,
		Finder<Persistable<EntityLocation>, Collection<EntityLocationException>> exceptionFinder,
		Finder<Persistable<EntityLocation>, Collection<EntityLocationInsertion>> insertionFinder) {
		this.mdi = mdi;
		this.subLocationFinder = subLocationFinder;
		this.exceptionFinder = exceptionFinder;
		this.insertionFinder = insertionFinder;
	}

	public Persistable<EntityLocation> find(Persistable<Q> query) {
		Collection<Persistable<EntityLocation>> locs = mdi.getData(query);
		Persistable<EntityLocation> pLoc = utils.first(locs);

		if(pLoc != null) {
			EntityLocation workingLocation = pLoc.getPersistableObject();

			EntityLocation subLocation = getSubLocationFinder().find(pLoc);
			if(subLocation != null) {
				workingLocation = subLocation;
				pLoc = new SimpleWrapperPersistable<EntityLocation>(subLocation, pLoc.getId());
			}

			for(EntityLocationException exception: getLocationExceptionFinder().find(pLoc)) {
				workingLocation.addException(exception);
			}

			for(EntityLocationInsertion insertion: getLocationInsertionFinder().find(pLoc)) {
				workingLocation.addInsertion(insertion);
			}
		}

		return pLoc;
	}
	
	protected Finder<Persistable<EntityLocation>, EntityLocation> getSubLocationFinder() {
		return subLocationFinder;
	}

	protected Finder<Persistable<EntityLocation>,
		Collection<EntityLocationException>> getLocationExceptionFinder() {
		return exceptionFinder;
	}

	protected Finder<Persistable<EntityLocation>,
		Collection<EntityLocationInsertion>> getLocationInsertionFinder() {
		return insertionFinder;
	}
}
