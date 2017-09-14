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

import org.apache.commons.lang.StringUtils;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.Before;
import uk.ac.ebi.proteome.materializer.MapWrappingDataMaterializer;
import uk.ac.ebi.proteome.materializer.Util;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.materializer.DataMaterializer;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * 
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class RedundancyRemovalTest {

	private Util<String> util = new Util<String>();
	private DataMaterializer<Collection<Persistable<String>>, Object> materializer;
	private Map<Object, Collection<Persistable<String>>> finalData;

	@Before
	public void populate() {
		Map<Object, Collection<Persistable<String>>> map = getDataMap();
		materializer = new MapWrappingDataMaterializer<String, Object>(map);
	}

	@Test
	public void redundancyRemoval() {
		CollectionRedundancyFilterMaterializer<String,Object> filter =
			new CollectionRedundancyFilterMaterializer<String,Object>(materializer);

		finalData = filter.getMaterializedDataInstance(1).getMap();

		assertExpected("B", 1L, "A");
		assertExpected("A", 2L, "B");
		assertExpected("C", 3L, "D");
		assertExpected("C", 4L, "F");
	}

	private void assertExpected(String targetMap, Long id, String expected) {
		String msg = "Value for "+id+" in map "+targetMap+" is expected to be "+expected;
		assertEquals(msg, find(finalData.get(targetMap), id), expected);
	}

	private String find(Collection<Persistable<String>> pers, Long id) {
		for(Persistable<String> per: pers) {
			if(id.equals(per.getId())) {
				return per.getPersistableObject();
			}
		}
		return StringUtils.EMPTY;
	}

	@SuppressWarnings("serial")
	private Map<Object, Collection<Persistable<String>>> getDataMap() {
		return new TreeMap<Object, Collection<Persistable<String>>>(){{
			put("A", util.createCollection(1,"A",2,"B"));
			put("B", util.createCollection(1,"C",3,"D"));
			put("C", util.createCollection(3,"E",4,"F"));
		}};
	}
}
