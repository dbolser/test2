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

package uk.ac.ebi.proteome.materializer.model.abstracts;

import static uk.ac.ebi.proteome.util.collections.CollectionUtils.createArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.ebi.proteome.materializer.misc.CollectionRedundancyFilterMaterializer;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.materializer.DataMaterializer;
import uk.ac.ebi.proteome.persistence.materializer.MaterializedDataInstance;
import uk.ac.ebi.proteome.util.collections.FactoryMap;
import uk.ac.ebi.proteome.util.collections.ObjectFactory;

/**
 * Takes multiple materializers, loops through their output & attempts to merge
 * the entries based on their parent DB ID. It views each entry in the map from
 * the materializes as a distinct slice of the data therefore if we are working
 * with something like an Xref & said Xref is shared between two genes
 * this materializer will allow the separate instances to exist. To remove
 * this kind of replication use a 
 * {@link CollectionRedundancyFilterMaterializer}.
 * 
 * <p>
 * For this code to work it assumes that the objects it uses will have an
 * equals & hashcode defined other than the default one given by Java. Otherwise
 * the algorithm's backing datastructures will not work.
 * 
 * @author $Author$
 * @author ayates
 * @version $Revision$
 * @param <T> The type of Persistable object which the incoming 
 * {@link DataMaterializer} return
 * @param <Q> The querying object type
 */
public class AbstractMergingMaterializer<T,Q> implements DataMaterializer<Collection<Persistable<T>>,Q>
{

    private final DataMaterializer<Collection<Persistable<T>>,Q>[] materializers;

    public AbstractMergingMaterializer(DataMaterializer<Collection<Persistable<T>>,Q>... materializers) {
        this.materializers = materializers;
    }

    public MaterializedDataInstance<Collection<Persistable<T>>,Q> getMaterializedDataInstance(Object... objects)
    {
        List<MaterializedDataInstance<Collection<Persistable<T>>, Q>> mdis = createArrayList();
        for(DataMaterializer<Collection<Persistable<T>>,Q> mat: materializers) {
            mdis.add(mat.getMaterializedDataInstance(objects));
        }

        return merge(mdis);
    }

    protected MaterializedDataInstance<Collection<Persistable<T>>,Q> merge(List<MaterializedDataInstance<Collection<Persistable<T>>, Q>> mdis) {

    	//Create a factory and merged map. One will record if we have seen an object
    	//the second will become the new MaterializedDataInstance's backing map
      Map<Object, Set<T>> seen = new FactoryMap<Object,Set<T>>(new ObjectFactory.SetFactory<T>());
      Map<Object,Collection<Persistable<T>>> merged = new FactoryMap<Object, Collection<Persistable<T>>>(
      		new ObjectFactory<Collection<Persistable<T>>>() {
      			public Collection<Persistable<T>> get() {
      				return new ArrayList<Persistable<T>>();
      			}});

      //For each materalizer loop; get map; get parent key; get data.
      //If we haven't seen it then add it into the new data; otherwise ignore
      for(MaterializedDataInstance<Collection<Persistable<T>>,Q> mdi: mdis) {
      	Map<Object,Collection<Persistable<T>>> data = mdi.getMap();
      	for(Map.Entry<Object,Collection<Persistable<T>>> entry: data.entrySet()) {
      		Set<T> seenSet = seen.get(entry.getKey());
      		Collection<Persistable<T>> coll = merged.get(entry.getKey());

      		for(Persistable<T> persistable: entry.getValue()) {
      			T obj = persistable.getPersistableObject();
      			if(!seenSet.contains(obj)) {
      				coll.add(persistable);
      				seenSet.add(obj);
      			}
      		}
      	}
      }
      
      return new MaterializedDataInstance<Collection<Persistable<T>>,Q>(merged);
    }
}
