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

package uk.ac.ebi.proteome.materializer.model.integrity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import uk.ac.ebi.proteome.genomebuilder.model.Operon;
import uk.ac.ebi.proteome.genomebuilder.model.Transcript;
import uk.ac.ebi.proteome.materializer.model.operon.InternedOperonModelMaterializer;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.materializer.MaterializedDataInstance;

import java.util.Collection;
import java.util.Map;

/**
 * Attempts to test the interning & removal of multiple operon instances. Do
 * not run as part of the normal test framework but as integration testing
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class OperonRedundancyFiltering extends Base {

	@Test public void operonRedundancyRemoval() {
		String component = "U00096";
		Long operonId = 512061416L;

		MaterializedDataInstance<Collection<Persistable<Operon>>, Transcript> mdi =
			new InternedOperonModelMaterializer(getRegistry()).getMaterializedDataInstance(component);
		Map<Object,Collection<Persistable<Operon>>> data = mdi.getMap();

		Operon base = null;

		for(Map.Entry<Object, Collection<Persistable<Operon>>> entry: data.entrySet()) {
			for(Persistable<Operon> persistable: entry.getValue()) {
				Operon operon = persistable.getPersistableObject();
				if(persistable.getId().equals(operonId)) {
					if(base == null) {
						base = operon;
					}
					else {
						assertEquals("Interned operon not the same according to natural equals", base, operon);
					}
				}
			}
		}

		assertNotNull("Base operon was null after searching for id "+operonId+". Check it still exists on component "+component, base);
	}
}
