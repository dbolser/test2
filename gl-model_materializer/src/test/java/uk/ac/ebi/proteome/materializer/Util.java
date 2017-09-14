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

import uk.ac.ebi.proteome.persistence.IdentifierGenerator;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.persistables.SimpleWrapperPersistable;
import uk.ac.ebi.proteome.persistence.identifiers.JavaIdentifierGenerator;
import static uk.ac.ebi.proteome.util.collections.CollectionUtils.createArrayList;

import java.util.Collection;

/**
 * Helps to generate new collection of persistables. For convenience there
 * is an inbuilt identifier generator starting at 1 & incrementing by 1. We
 * will extend the constructor to allow user submitted generators if required.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class Util<T> {

	private final IdentifierGenerator<Long> idGen;

	public Util() {
		this.idGen = new JavaIdentifierGenerator(1,1);
	}

	public Collection<Persistable<T>> createCollectionAutoId(T... values) {
		Collection<Persistable<T>> persistables = createArrayList();
		for(T value: values) {
			persistables.add(createPersistable(value));
		}
		return persistables;
	}

	public Persistable<T> createPersistable(T value) {
		return new SimpleWrapperPersistable<T>(value, nextId());
	}

	public  Persistable<T> createPersistable(T value, Long id) {
		return new SimpleWrapperPersistable<T>(value, id);
	}

	private Long nextId() {
		return idGen.getNextIdentifier();
	}

	@SuppressWarnings("unchecked")
	public Collection<Persistable<T>> createCollection(Object... values) {
		Collection<Persistable<T>> persistables = createArrayList();
		int size = values.length;
		if(size % 2 != 0) {
			throw new IllegalArgumentException("Input array not a multiple of two. Was "+size);
		}

		for(int i=0; i<size; i = i+2) {
			int offset = i+1;
			persistables.add(createPersistable((T)values[offset],
				((Number)values[i]).longValue()));
		}

		return persistables;
	}

}
