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
 * File: MergeLocationTest.java
 * Created by: dstaines
 * Created on: Oct 3, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.util.biojava;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.biojavax.bio.seq.RichLocation;
import org.biojavax.bio.seq.SimplePosition;
import org.biojavax.bio.seq.SimpleRichLocation;
import org.ensembl.genomeloader.util.collections.CollectionUtils;

import junit.framework.TestCase;

/**
 * @author dstaines
 *
 */
public class MergeLocationTest extends TestCase {

	public void testSimpleMerge() throws Exception {
		RichLocation location1 = LocationUtils.parseEmblLocation("join(1..10)");
		RichLocation location2 = LocationUtils
				.parseEmblLocation("join(20..30)");
		RichLocation merged = LocationUtils
				.mergeLocations(location1, location2);
		System.out.println(merged);
		assertEquals(1, merged.getMin());
		assertEquals(30, merged.getMax());
		assertEquals(RichLocation.Strand.POSITIVE_STRAND, merged.getStrand());
		assertEquals(RankedCompoundRichLocation.class, merged.getClass());
	}

	public void testSimpleMergeRev() throws Exception {
		RichLocation location1 = LocationUtils
				.parseEmblLocation("join(20..30)");
		RichLocation location2 = LocationUtils.parseEmblLocation("join(1..10)");
		RichLocation merged = LocationUtils
				.mergeLocations(location1, location2);
		System.out.println(merged);
		assertEquals(1, merged.getMin());
		assertEquals(30, merged.getMax());
		assertEquals(RichLocation.Strand.POSITIVE_STRAND, merged.getStrand());
		assertEquals(RankedCompoundRichLocation.class, merged.getClass());
	}

	public void testSimpleComplementMerge() throws Exception {
		RichLocation location1 = LocationUtils
				.parseEmblLocation("join(complement(1..10))");
		RichLocation location2 = LocationUtils
				.parseEmblLocation("join(complement(20..30))");
		RichLocation merged = LocationUtils
				.mergeLocations(location1, location2);
		assertEquals(1, merged.getMin());
		assertEquals(30, merged.getMax());
		assertEquals(RichLocation.Strand.NEGATIVE_STRAND, merged.getStrand());
		assertEquals(RankedCompoundRichLocation.class, merged.getClass());
	}

	public void testSimpleComplementMergeRev() throws Exception {
		RichLocation location2 = LocationUtils
				.parseEmblLocation("join(complement(1..10))");
		RichLocation location1 = LocationUtils
				.parseEmblLocation("join(complement(20..30))");
		RichLocation merged = LocationUtils
				.mergeLocations(location1, location2);
		assertEquals(1, merged.getMin());
		assertEquals(30, merged.getMax());
		assertEquals(RichLocation.Strand.NEGATIVE_STRAND, merged.getStrand());
		assertEquals(RankedCompoundRichLocation.class, merged.getClass());
	}

	public void testSimpleMixedMerge() throws Exception {
		RichLocation location1 = LocationUtils.parseEmblLocation("join(1..10)");
		RichLocation location2 = LocationUtils
				.parseEmblLocation("join(complement(20..30))");
		RichLocation merged = LocationUtils
				.mergeLocations(location1, location2);
		assertEquals(1, merged.getMin());
		assertEquals(30, merged.getMax());
		assertEquals(RichLocation.Strand.UNKNOWN_STRAND, merged.getStrand());
		assertEquals(TranssplicedCompoundRichLocation.class, merged.getClass());
	}

	public void testOverlapMerge() throws Exception {
		RichLocation location1 = LocationUtils.parseEmblLocation("join(1..10)");
		RichLocation location2 = LocationUtils.parseEmblLocation("join(8..20)");
		RichLocation merged = LocationUtils
				.mergeLocations(location1, location2);
		assertEquals(1, merged.getMin());
		assertEquals(20, merged.getMax());
		assertEquals(RichLocation.Strand.POSITIVE_STRAND, merged.getStrand());
		assertEquals(SimpleRichLocation.class, merged.getClass());
	}

	public void testOverlapComplementMerge() throws Exception {
		RichLocation location1 = LocationUtils
				.parseEmblLocation("join(complement(1..10))");
		RichLocation location2 = LocationUtils
				.parseEmblLocation("join(complement(8..20))");
		RichLocation merged = LocationUtils
				.mergeLocations(location1, location2);
		assertEquals(1, merged.getMin());
		assertEquals(20, merged.getMax());
		assertEquals(RichLocation.Strand.NEGATIVE_STRAND, merged.getStrand());
		assertEquals(SimpleRichLocation.class, merged.getClass());
	}

	public void testOverlappingMixedMerge() throws Exception {
		RichLocation location1 = LocationUtils.parseEmblLocation("join(1..10)");
		RichLocation location2 = LocationUtils
				.parseEmblLocation("join(complement(8..20))");
		RichLocation merged = LocationUtils
				.mergeLocations(location1, location2);
		assertEquals(1, merged.getMin());
		assertEquals(20, merged.getMax());
		assertEquals(RichLocation.Strand.UNKNOWN_STRAND, merged.getStrand());
		assertEquals(TranssplicedCompoundRichLocation.class, merged.getClass());
	}

	public void testOuterLocation1() throws Exception {
		RichLocation location1 = LocationUtils.parseEmblLocation("join(1..10)");
		RichLocation location2 = LocationUtils.getOuterLocation(location1);
		assertEquals(location1.getMin(), location2.getMin());
		assertEquals(location1.getMax(), location2.getMax());
		assertEquals(location1.getStrand(), location2.getStrand());
		assertEquals(location1.getCircularLength(), location2
				.getCircularLength());
		assertEquals(1, LocationUtils.countInnerLocations(location2));
		assertTrue(location1 == location2);
	}

	public void testOuterLocation2() throws Exception {
		RichLocation location1 = LocationUtils
				.parseEmblLocation("join(1..10,20..30)");
		RichLocation location2 = LocationUtils.getOuterLocation(location1);
		assertEquals(location1.getMin(), location2.getMin());
		assertEquals(location1.getMax(), location2.getMax());
		assertEquals(location1.getStrand(), location2.getStrand());
		assertEquals(location1.getCircularLength(), location2
				.getCircularLength());
		assertEquals(1, LocationUtils.countInnerLocations(location2));
	}

	public void testOuterLocation3() throws Exception {
		RichLocation location1 = LocationUtils
				.parseEmblLocation("complement(join(1..10,20..30))");
		RichLocation location2 = LocationUtils.getOuterLocation(location1);
		assertEquals(location1.getMin(), location2.getMin());
		assertEquals(location1.getMax(), location2.getMax());
		assertEquals(location1.getStrand(), location2.getStrand());
		assertEquals(location1.getCircularLength(), location2
				.getCircularLength());
		assertEquals(1, LocationUtils.countInnerLocations(location2));
	}

	public void testOuterLocation4() throws Exception {
		RichLocation location1 = LocationUtils
				.parseEmblLocation("join(1..10,20..30))");
		location1.setCircularLength(30);
		RichLocation location2 = LocationUtils.getOuterLocation(location1);
		assertEquals(location1.getMin(), location2.getMin());
		assertEquals(location1.getMax(), location2.getMax());
		assertEquals(location1.getStrand(), location2.getStrand());
		assertEquals(location1.getCircularLength(), location2
				.getCircularLength());
		assertEquals(1, LocationUtils.countInnerLocations(location2));
	}

	public void testMergeLocation1() throws Exception {
		RichLocation location = LocationUtils.parseEmblLocation("1..10");
		RichLocation loc2 = LocationUtils.unite(location);
		assertEquals(1, LocationUtils.countInnerLocations(loc2));
	}

	public void testMergeLocation2() throws Exception {
		RichLocation location = LocationUtils.parseEmblLocation("1..5,6..10");
		RichLocation loc2 = LocationUtils.unite(location);
		assertEquals(1, LocationUtils.countInnerLocations(loc2));
	}

	public void testMergeLocation3() throws Exception {
		RichLocation location = LocationUtils.parseEmblLocation("1..5,7..10");
		RichLocation loc2 = LocationUtils.unite(location);
		assertEquals(2, LocationUtils.countInnerLocations(loc2));
	}

	public void testMergeLocation4() throws Exception {
		RichLocation location = LocationUtils.parseEmblLocation("1..5,5..10");
		RichLocation loc2 = LocationUtils.unite(location);
		assertEquals(2, LocationUtils.countInnerLocations(loc2));
	}

	public void testToolsMergeLocation() throws Exception {
		List<RichLocation> locs = Arrays.asList(new RichLocation[] {
				new SimpleRichLocation(new SimplePosition(1),
						new SimplePosition(10), 1),
				new SimpleRichLocation(new SimplePosition(8),
						new SimplePosition(20), 1),
				new SimpleRichLocation(new SimplePosition(1),
						new SimplePosition(5), 1) });
		Collection<RichLocation> mergedLocs = LocationUtils.merge(locs);
		assertEquals(1, mergedLocs.size());
		assertEquals(1, LocationUtils.countInnerLocations(CollectionUtils
				.getFirstElement(mergedLocs, null)));
		assertEquals(1, CollectionUtils.getFirstElement(mergedLocs, null)
				.getMin());
		assertEquals(20, CollectionUtils.getFirstElement(mergedLocs, null)
				.getMax());
	}

	public void testToolsMergeLocation2() throws Exception {
		List<RichLocation> locs = Arrays.asList(new RichLocation[] {
				new SimpleRichLocation(new SimplePosition(1),
						new SimplePosition(10), 1),
				new SimpleRichLocation(new SimplePosition(1),
						new SimplePosition(5), 1),
				new SimpleRichLocation(new SimplePosition(8),
						new SimplePosition(20), 1), });
		Collection<RichLocation> mergedLocs = LocationUtils.merge(locs);
		assertEquals(1, mergedLocs.size());
		assertEquals(1, LocationUtils.countInnerLocations(CollectionUtils
				.getFirstElement(mergedLocs, null)));
		assertEquals(1, CollectionUtils.getFirstElement(mergedLocs, null)
				.getMin());
		assertEquals(20, CollectionUtils.getFirstElement(mergedLocs, null)
				.getMax());
	}

	public void testToolsMergeLocation3() throws Exception {
		List<RichLocation> locs = Arrays.asList(new RichLocation[] {
				new SimpleRichLocation(new SimplePosition(1),
						new SimplePosition(5), 1),
				new SimpleRichLocation(new SimplePosition(1),
						new SimplePosition(10), 1),
				new SimpleRichLocation(new SimplePosition(8),
						new SimplePosition(20), 1), });
		Collection<RichLocation> mergedLocs = LocationUtils.merge(locs);
		assertEquals(1, mergedLocs.size());
		assertEquals(1, LocationUtils.countInnerLocations(CollectionUtils
				.getFirstElement(mergedLocs, null)));
		assertEquals(1, CollectionUtils.getFirstElement(mergedLocs, null)
				.getMin());
		assertEquals(20, CollectionUtils.getFirstElement(mergedLocs, null)
				.getMax());
	}

	public void testMergeCircularLocation1() throws Exception {
		RichLocation loc1 = LocationUtils.parseEmblLocation("30..50,1..20");
		RichLocation loc2 = LocationUtils.parseEmblLocation("10..20");
		loc1.setCircularLength(50);
		loc2.setCircularLength(50);
		RichLocation loc3 = LocationUtils.mergeLocations(loc1, loc2);
		assertEquals(2, LocationUtils.countInnerLocations(loc3));
		assertEquals(30, loc3.getMin());
		assertEquals(20, loc3.getMax());
	}

	public void testMergeCircularLocation2() throws Exception {
		RichLocation loc1 = LocationUtils.parseEmblLocation("30..50,1..20");
		RichLocation loc2 = LocationUtils.parseEmblLocation("10..20");
		loc1.setCircularLength(50);
		loc2.setCircularLength(50);
		RichLocation loc3 = LocationUtils.mergeLocations(loc2, loc1);
		assertEquals(2, LocationUtils.countInnerLocations(loc3));
		assertEquals(30, loc3.getMin());
		assertEquals(20, loc3.getMax());
	}

	public void testMergeCircularLocation3() throws Exception {
		RichLocation loc1 = LocationUtils.parseEmblLocation("30..50,1..20");
		RichLocation loc2 = LocationUtils.parseEmblLocation("33..50,1..17");
		loc1.setCircularLength(50);
		loc2.setCircularLength(50);
		RichLocation loc3 = LocationUtils.mergeLocations(loc2, loc1);
		assertEquals(2, LocationUtils.countInnerLocations(loc3));
		assertEquals(30, loc3.getMin());
		assertEquals(20, loc3.getMax());
	}

	public void testMergeCircularLocation4() throws Exception {
		RichLocation loc1 = LocationUtils.parseEmblLocation("30..50,1..20");
		RichLocation loc2 = LocationUtils.parseEmblLocation("33..50,1..17");
		RichLocation loc3 = LocationUtils.parseEmblLocation("1..20");
		loc1.setCircularLength(50);
		loc2.setCircularLength(50);
		loc3.setCircularLength(50);
		RichLocation loc4 = LocationUtils.mergeLocations(CollectionUtils
				.createArrayList(loc1, loc2, loc3));
		assertEquals(2, LocationUtils.countInnerLocations(loc4));
		assertEquals(30, loc4.getMin());
		assertEquals(20, loc4.getMax());
	}

	// public void testMergeSlippedLocation() throws Exception {
	// RichLocation location1 = LocationUtils
	// .parseEmblLocation("join(1..10,10..20))");
	// RichLocation location2 = LocationUtils.parseEmblLocation("join(1..5)");
	// // should produce complement(join(19523..20435,20435..20682))
	// RichLocation location3 = LocationUtils.mergeLocations(location1,
	// location2);
	// System.out.println(location3);
	// }

	// public void testMergeRealSlippedLocation() throws Exception {
	// RichLocation location1 = LocationUtils
	// .parseEmblLocation("complement(join(19523..20435,20435..20682))");
	// RichLocation location2 = LocationUtils
	// .parseEmblLocation("complement(19523..20332)");
	// // should produce complement(join(19523..20435,20435..20682))
	// RichLocation location3 = LocationUtils.mergeLocations(location1,
	// location2);
	// assertEquals(1,mergedLocs.size());
	// assertEquals(1,LocationUtils.countInnerLocations(CollectionUtils.getFirstElement(mergedLocs,
	// null)));
	// assertEquals(1,CollectionUtils.getFirstElement(mergedLocs,
	// null).getMin());
	// assertEquals(20,CollectionUtils.getFirstElement(mergedLocs,
	// null).getMax());
	// }

}
