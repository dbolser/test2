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

/**
 * CollectionUtilsTest
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package org.ensembl.genomeloader.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.ensembl.genomeloader.util.collections.CollectionUtils;
import org.ensembl.genomeloader.util.collections.CollectionUtils.TopologicalComparator;

import junit.framework.TestCase;

/**
 * @author dstaines
 * 
 */
public class CollectionUtilsTest extends TestCase {

	public class TopoTestNode {
		public final String name;
		public final Collection<String> deps;

		public TopoTestNode(String name, String[] deps) {
			this.name = name;
			this.deps = Arrays.asList(deps);
		}

		public String toString() {
			return this.name + ":" + this.deps;
		}
	}

	private static <T> boolean firstBeforeSecond(List<T> list, T one, T two) {
		boolean success = true;
		boolean oneFound = false;
		for (T elem : list) {
			if (elem.equals(one)) {
				oneFound = true;
			} else if (elem.equals(two) && !oneFound) {
				success = false;
				break;
			}
		}
		return success;
	}

	public void testTopo() {
		TopologicalComparator<TopoTestNode> cmp = new TopologicalComparator<CollectionUtilsTest.TopoTestNode>() {
			public int countEdges(TopoTestNode t) {
				return t.deps.size();
			}

			public boolean hasEdge(TopoTestNode from, TopoTestNode to) {
				return from.deps.contains(to.name);
			}
		};
		TopoTestNode a = new TopoTestNode("a", new String[] {});
		TopoTestNode b = new TopoTestNode("b", new String[] { "d" });
		TopoTestNode c = new TopoTestNode("c", new String[] { "a" });
		TopoTestNode d = new TopoTestNode("d", new String[] { "a" });
		TopoTestNode e = new TopoTestNode("e", new String[] {});
		List<TopoTestNode> list = Arrays.asList(new TopoTestNode[] { a, b, c,
				d, e });
		list = CollectionUtils.topoSort(list, cmp);
		Collections.reverse(list);
		assertTrue(firstBeforeSecond(list, a, b));
		assertTrue(firstBeforeSecond(list, a, c));
		assertTrue(firstBeforeSecond(list, a, d));
		assertTrue(firstBeforeSecond(list, d, b));
		assertEquals(5, list.size());
		list = Arrays.asList(new TopoTestNode[] { e, d, c, b, a });
		list = CollectionUtils.topoSort(list, cmp);
		Collections.reverse(list);
		assertTrue(firstBeforeSecond(list, a, b));
		assertTrue(firstBeforeSecond(list, a, c));
		assertTrue(firstBeforeSecond(list, a, d));
		assertTrue(firstBeforeSecond(list, d, b));
		assertEquals(5, list.size());
		list = Arrays.asList(new TopoTestNode[] { b, a, c, d, e });
		list = CollectionUtils.topoSort(list, cmp);
		Collections.reverse(list);
		assertTrue(firstBeforeSecond(list, a, b));
		assertTrue(firstBeforeSecond(list, a, c));
		assertTrue(firstBeforeSecond(list, a, d));
		assertTrue(firstBeforeSecond(list, d, b));
		assertEquals(5, list.size());
	}

}
