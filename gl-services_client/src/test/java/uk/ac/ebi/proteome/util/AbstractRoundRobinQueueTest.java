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
 * AbstractRoundRobinQueueTest
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package uk.ac.ebi.proteome.util;

import junit.framework.TestCase;
import uk.ac.ebi.proteome.util.concurrency.AbstractRoundRobinQueue;

/**
 * @author dstaines
 * 
 */
public class AbstractRoundRobinQueueTest extends TestCase {

	public void test() {

		AbstractRoundRobinQueue<String> q = new AbstractRoundRobinQueue<String>() {
			@Override
			protected String itemToKey(String e) {
				return e;
			}
		};

		q.add("a");
		q.add("a");
		q.add("a");
		q.add("a");
		q.add("a");
		q.add("a");
		q.add("b");
		q.add("b");
		q.add("c");
		q.add("c");
		q.add("c");
		q.add("a");

		assertEquals("Expected queue size", 12, q.size());
		assertEquals("Expected peek", "a", q.peek());
		assertEquals("Expected queue size", 12, q.size());
		assertEquals("Expected poll", "a", q.poll());
		assertEquals("Expected queue size", 11, q.size());
		assertEquals("Expected poll", "b", q.poll());
		assertEquals("Expected queue size", 10, q.size());
		assertEquals("Expected poll", "c", q.poll());
		assertEquals("Expected queue size", 9, q.size());
		assertEquals("Expected poll", "a", q.poll());
		assertEquals("Expected queue size", 8, q.size());
		assertEquals("Expected poll", "b", q.poll());
		assertEquals("Expected queue size", 7, q.size());
		assertEquals("Expected poll", "c", q.poll());
		assertEquals("Expected queue size", 6, q.size());
		assertEquals("Expected poll", "a", q.poll());
		assertEquals("Expected queue size", 5, q.size());
		assertEquals("Expected poll", "c", q.poll());
		assertEquals("Expected queue size", 4, q.size());
		assertEquals("Expected poll", "a", q.poll());
		assertEquals("Expected queue size", 3, q.size());
		assertEquals("Expected poll", "a", q.poll());
		assertEquals("Expected queue size", 2, q.size());
		assertEquals("Expected poll", "a", q.poll());
		assertEquals("Expected queue size", 1, q.size());
		assertEquals("Expected poll", "a", q.poll());
		assertEquals("Expected queue size", 0, q.size());
		assertNull("Expected null", q.poll());

	}

}
