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

import static uk.ac.ebi.proteome.util.collections.CollectionUtils.getFirstElement;
import static uk.ac.ebi.proteome.util.collections.CollectionUtils.createArrayList;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocation;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.materializer.model.finders.FinderUncheckedException;

import java.util.Collection;

import org.biojavax.bio.seq.RichLocation;

/**
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class FinderUtils {

	/**
	 * Returns the first element of a collection
	 *
	 * @throws FinderUncheckedException Thrown if collection size > 1
	 * @return First element of collection or null (if collection was empty)
	 */
	public <T> T first(Collection<T> collection) throws FinderUncheckedException {
		int size = collection.size();
		if(size > 1) {
			throw new FinderUncheckedException("Collection size was greater than 1. Was "+size);
		}
		return getFirstElement(collection, null);
	}
	
	public Collection<RichLocation> unwrapLocations(Collection<Persistable<EntityLocation>>locations) {
		Collection<RichLocation> locs = createArrayList();
		for(Persistable<EntityLocation> loc: locations) {
			locs.add(loc.getPersistableObject());
		}
		return locs;
	}

}
