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

package uk.ac.ebi.proteome.materializer.misc;

import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.materializer.DataMaterializer;
import uk.ac.ebi.proteome.persistence.materializer.MaterializedDataInstance;
import static uk.ac.ebi.proteome.util.collections.CollectionUtils.createHashMap;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Performs an interning of collection based {@link Persistable} data in order to
 * remove duplicates caused by many - many relationships in the database. This
 * is of course the problem when trying to solve the n+1 problem that
 * redundancy will always creep in somewhere.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class CollectionRedundancyFilterMaterializer<T,V> implements
		DataMaterializer<Collection<Persistable<T>>, V> {

	private final DataMaterializer<Collection<Persistable<T>>, V> baseMaterializer;
	private Map<Object,Persistable<T>> internMap;

	public CollectionRedundancyFilterMaterializer(DataMaterializer<Collection<Persistable<T>>, V> baseMaterializer) {
		this.baseMaterializer = baseMaterializer;
	}

	public MaterializedDataInstance<Collection<Persistable<T>>, V> getMaterializedDataInstance(Object... args) {
		MaterializedDataInstance<Collection<Persistable<T>>, V> materializedDataInstance =
			baseMaterializer.getMaterializedDataInstance(args);
		filter(materializedDataInstance);
		return materializedDataInstance;
	}

	protected void filter(MaterializedDataInstance<Collection<Persistable<T>>,V> data) {

		Map<Object,Persistable<T>> intern = getInternMap(data);

		for(Map.Entry<Object, Collection<Persistable<T>>> entry: data.getMap().entrySet()) {

			Map<Object, Persistable<T>> toBeAdded = createHashMap();

			for (Iterator<Persistable<T>> iter = entry.getValue().iterator(); iter.hasNext(); ) {
				Persistable<T> persistable = iter.next();
				Object id = persistable.getId();
				//Intern only if the intern map has it & we haven't attempted to get it before
				if(intern.containsKey(id) && !toBeAdded.containsKey(id)) {
					iter.remove();
					toBeAdded.put(id, intern.get(id));
				}
				else {
					intern.put(id, persistable);
				}
			}

			//If we had things in the toBeAdded Map then add them & reset the map
			//Otherwise just reuse the last map
			if(!toBeAdded.isEmpty()) {
				entry.getValue().addAll(toBeAdded.values());
				toBeAdded = createHashMap();
			}
		}
	}
	
	private Map<Object, Persistable<T>> getInternMap(MaterializedDataInstance<Collection<Persistable<T>>,V> data) {
		if(internMap == null) {
			this.internMap = createHashMap(data.getMap().size()*2);
		}
		return internMap;
	}
}
