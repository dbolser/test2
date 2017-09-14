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

import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReference;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.finder.Finder;
import uk.ac.ebi.proteome.persistence.materializer.MaterializedDataInstance;
import static uk.ac.ebi.proteome.util.collections.CollectionUtils.createArrayList;

import java.util.Collection;
import java.util.Collections;

/**
 * Searches for the requested Xrefs against the given materialized data
 * instance and unwraps the references from the instance of Persistable
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractXrefFinder<Q> implements Finder<Persistable<Q>, Collection<DatabaseReference>> {

	private final MaterializedDataInstance<
		Collection<Persistable<DatabaseReference>>, Q> mdi;

	public AbstractXrefFinder(MaterializedDataInstance
			<Collection<Persistable<DatabaseReference>>, Q> mdi) {
		this.mdi = mdi;
	}

	public Collection<DatabaseReference> find(Persistable<Q> query) {
		final Collection<DatabaseReference> refs;

		if(mdi.hasData(query)) {
			Collection<Persistable<DatabaseReference>> persistables = mdi.getData(query);
			refs = createArrayList();
			for(Persistable<DatabaseReference> referencePersistable: persistables) {
				refs.add(referencePersistable.getPersistableObject());
			}
		}
		else {
			refs = Collections.emptyList();
		}

		return refs;
	}
}
