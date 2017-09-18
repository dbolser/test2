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

package uk.ac.ebi.proteome.util.biojava;

import java.util.Arrays;
import java.util.Iterator;

import org.biojavax.bio.seq.RichLocation;
import org.biojavax.bio.seq.SimplePosition;
import org.biojavax.bio.seq.SimpleRichLocation;

import junit.framework.TestCase;

public class CircularGenomeOverlapTest extends TestCase {

	public void testSimpleOverlap() throws Exception {
		// 12 bp genome, 11..4 should not overlap 1..3 but should overlap 2..4
		SimpleRichLocation loc1 = new SimpleRichLocation(
				new SimplePosition(11), new SimplePosition(4), 1);
		SimpleRichLocation loc2 = new SimpleRichLocation(new SimplePosition(1),
				new SimplePosition(3), 1);
		SimpleRichLocation loc3 = new SimpleRichLocation(new SimplePosition(2),
				new SimplePosition(4), 1);
		loc1.setCircularLength(12);
		loc2.setCircularLength(12);
		loc3.setCircularLength(12);
		assertTrue(loc1.overlaps(loc2));
		assertTrue(loc1.overlaps(loc3));
		assertFalse(LocationUtils.overlapsInFrame(loc1, loc2));
		assertTrue(LocationUtils.overlapsInFrame(loc1, loc3));
	}

	public void testSimpleOverlap2() throws Exception {
		// 12 bp genome, 10..3 should overlap 1..3 but should not overlap 2..4
		SimpleRichLocation loc1 = new SimpleRichLocation(
				new SimplePosition(10), new SimplePosition(3), 1);
		SimpleRichLocation loc2 = new SimpleRichLocation(new SimplePosition(1),
				new SimplePosition(3), 1);
		SimpleRichLocation loc3 = new SimpleRichLocation(new SimplePosition(2),
				new SimplePosition(4), 1);
		loc1.setCircularLength(12);
		loc2.setCircularLength(12);
		loc3.setCircularLength(12);
		assertTrue(loc1.overlaps(loc2));
		assertTrue(loc1.overlaps(loc3));
		assertTrue(LocationUtils.overlapsInFrame(loc1, loc2));
		assertFalse(LocationUtils.overlapsInFrame(loc1, loc3));
	}

	public void testCompositeOverlap() throws Exception {
		// 12 bp genome, 11..12,1..4 should not overlap 1..3 but should overlap
		// 2..4
		SimpleRichLocation loc1a = new SimpleRichLocation(
				new SimplePosition(11), new SimplePosition(12), 1);
		SimpleRichLocation loc1b = new SimpleRichLocation(
				new SimplePosition(1), new SimplePosition(4), 2);
		RichLocation loc1 = LocationUtils.construct(Arrays
				.asList(new RichLocation[] { loc1a, loc1b }));
		SimpleRichLocation loc2 = new SimpleRichLocation(new SimplePosition(1),
				new SimplePosition(3), 1);
		SimpleRichLocation loc3 = new SimpleRichLocation(new SimplePosition(2),
				new SimplePosition(4), 1);
		loc1.setCircularLength(12);
		loc2.setCircularLength(12);
		loc3.setCircularLength(12);
		assertTrue(loc1.overlaps(loc2));
		assertTrue(loc1.overlaps(loc3));
		assertFalse(LocationUtils.overlapsInFrame(loc1, loc2));
		assertTrue(LocationUtils.overlapsInFrame(loc1, loc3));
	}

	public void testCompositeOverlap2() throws Exception {
		// 12 bp genome, 10..12,1..3 should overlap 1..3 but should not overlap
		// 2..4
		SimpleRichLocation loc1a = new SimpleRichLocation(
				new SimplePosition(10), new SimplePosition(12), 1);
		SimpleRichLocation loc1b = new SimpleRichLocation(
				new SimplePosition(1), new SimplePosition(3), 2);
		RichLocation loc1 = LocationUtils.construct(Arrays
				.asList(new RichLocation[] { loc1a, loc1b }));
		SimpleRichLocation loc2 = new SimpleRichLocation(new SimplePosition(1),
				new SimplePosition(3), 1);
		SimpleRichLocation loc3 = new SimpleRichLocation(new SimplePosition(2),
				new SimplePosition(4), 1);
		loc1.setCircularLength(12);
		loc2.setCircularLength(12);
		loc3.setCircularLength(12);
		assertTrue(loc1.overlaps(loc2));
		assertTrue(loc1.overlaps(loc3));
		assertTrue(LocationUtils.overlapsInFrame(loc1, loc2));
		assertFalse(LocationUtils.overlapsInFrame(loc1, loc3));
	}

	public void testCompositeCircularNotation() {
		RichLocation loc = LocationUtils.parseEmblLocation("join(1..10,20..50)");
		loc.setCircularLength(45);
		assertEquals("Expected circular length ",45,loc.getCircularLength());
		Iterator<RichLocation> locI = loc.blockIterator();
		while(locI.hasNext()) {
			RichLocation l = locI.next();
			assertEquals("Expected circular length ",45,l.getCircularLength());
		}
	}

}
