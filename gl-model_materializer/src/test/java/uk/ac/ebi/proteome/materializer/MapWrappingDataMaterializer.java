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

package uk.ac.ebi.proteome.materializer;

import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.materializer.DataMaterializer;
import uk.ac.ebi.proteome.persistence.materializer.MaterializedDataInstance;

import java.util.Collection;
import java.util.Map;

/**
 * Just wraps a map which will be an instance of {@link
 * MaterializedDataInstance}
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class MapWrappingDataMaterializer<T,V> implements DataMaterializer<Collection<Persistable<T>>, V> {

	private final Map<Object,Collection<Persistable<T>>> map;

	public MapWrappingDataMaterializer(Map<Object, Collection<Persistable<T>>> map) {
		this.map = map;
	}

	public MaterializedDataInstance<Collection<Persistable<T>>, V>
			getMaterializedDataInstance(Object... args) {
		return new MaterializedDataInstance<Collection<Persistable<T>>, V>(map);
	}
}
