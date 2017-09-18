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
 * File: ProteinToGenomicLocationTest.java
 * Created by: dstaines
 * Created on: Jan 3, 2008
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.util.biojava;

import java.util.Collections;
import java.util.List;

import org.biojavax.bio.seq.RichLocation;
import org.biojavax.bio.seq.RichLocation.Strand;
import org.ensembl.genomeloader.util.biojava.FeatureLocationGapOverlapException;
import org.ensembl.genomeloader.util.biojava.LocationUtils;

import junit.framework.TestCase;

/**
 * @author dstaines
 *
 */
public class RelativeLocationTest extends TestCase {

	public void testConvertCoordinates1() throws Exception {
		RichLocation ref = LocationUtils.parseEmblLocation("1..200");
		RichLocation sub = LocationUtils.parseEmblLocation("10..200");
		RichLocation floc = LocationUtils.getRelativeLocation(sub, ref);
		assertEquals("Expected start", 10, floc.getMin());
		assertEquals("Expected end", 200, floc.getMax());
	}

	public void testConvertCoordinates2() throws Exception {
		RichLocation ref = LocationUtils.parseEmblLocation("101..1200");
		RichLocation sub = LocationUtils.parseEmblLocation("110..1200");
		RichLocation floc = LocationUtils.getRelativeLocation(sub, ref);
		assertEquals("Expected start", 10, floc.getMin());
		assertEquals("Expected end", 1100, floc.getMax());
	}

	public void testConvertCoordinates4() throws Exception {
		RichLocation ref = LocationUtils.parseEmblLocation("1..5,10..200");
		RichLocation sub = LocationUtils.parseEmblLocation("10..200");
		RichLocation floc = LocationUtils.getRelativeLocationWithAllGaps(sub,
				ref);
		assertEquals("Expected start", 6, floc.getMin());
	}

	public void testConvertCoordinates4a() throws Exception {
		RichLocation ref = LocationUtils.parseEmblLocation("1..5,10..200");
		RichLocation sub = LocationUtils.parseEmblLocation("10..200");
		RichLocation floc = LocationUtils.getRelativeLocation(sub, ref);
		assertEquals("Expected start", 6, floc.getMin());
	}

	public void testConvertCoordinates5() throws Exception {
		RichLocation ref = LocationUtils.parseEmblLocation("1..2,4..5,10..200");
		RichLocation sub = LocationUtils.parseEmblLocation("10..200");
		RichLocation floc = LocationUtils.getRelativeLocationWithAllGaps(sub,
				ref);
		assertEquals("Expected start", 5, floc.getMin());
	}

	public void testConvertCoordinates5a() throws Exception {
		RichLocation ref = LocationUtils.parseEmblLocation("1..2,4..5,10..200");
		RichLocation sub = LocationUtils.parseEmblLocation("10..200");
		RichLocation floc = LocationUtils.getRelativeLocation(sub, ref);
		assertEquals("Expected start", 5, floc.getMin());
	}

	public void testConvertCoordinates6() throws Exception {
		RichLocation ref = LocationUtils.parseEmblLocation("1..5,10..200");
		RichLocation sub = LocationUtils.parseEmblLocation("10..20,30..200");
		RichLocation floc = LocationUtils.getRelativeLocationWithAllGaps(sub,
				ref);
		assertEquals("Expected start", 6, floc.getMin());
	}

	public void testConvertCoordinates6a() throws Exception {
		RichLocation ref = LocationUtils.parseEmblLocation("1..5,10..200");
		RichLocation sub = LocationUtils.parseEmblLocation("10..20,30..200");
		RichLocation floc = LocationUtils.getRelativeLocation(sub, ref);
		assertEquals("Expected start", 6, floc.getMin());
	}

	public void testConvertCoordinates7() throws Exception {
		RichLocation ref = LocationUtils.parseEmblLocation("1..2,4..5,10..200");
		RichLocation sub = LocationUtils.parseEmblLocation("10..20,30..200");
		RichLocation floc = LocationUtils.getRelativeLocationWithAllGaps(sub,
				ref);
		assertEquals("Expected start", 5, floc.getMin());
	}

	public void testConvertCoordinates7a() throws Exception {
		RichLocation ref = LocationUtils.parseEmblLocation("1..2,4..5,10..200");
		RichLocation sub = LocationUtils.parseEmblLocation("10..20,30..200");
		RichLocation floc = LocationUtils.getRelativeLocation(sub, ref);
		assertEquals("Expected start", 5, floc.getMin());
	}

	public void testConvertCoordsStrandPP() throws Exception {
		RichLocation floc = LocationUtils.getRelativeLocation(LocationUtils
				.parseEmblLocation("2..5"), LocationUtils
				.parseEmblLocation("1..10"));
		assertEquals("Expected start", 2, floc.getMin());
		assertEquals("Expected end", 5, floc.getMax());
		assertEquals("Expected strand", Strand.POSITIVE_STRAND, floc
				.getStrand());
	}

	public void testConvertCoordsStrandPN() throws Exception {
		RichLocation floc = LocationUtils.getRelativeLocation(LocationUtils
				.parseEmblLocation("complement(2..5)"), LocationUtils
				.parseEmblLocation("1..10"));
		assertEquals("Expected start", 2, floc.getMin());
		assertEquals("Expected end", 5, floc.getMax());
		assertEquals("Expected strand", Strand.NEGATIVE_STRAND, floc
				.getStrand());
	}

	public void testConvertCoordsStrandNP() throws Exception {
		RichLocation floc = LocationUtils.getRelativeLocation(LocationUtils
				.parseEmblLocation("2..5"), LocationUtils
				.parseEmblLocation("complement(1..10)"));
		assertEquals("Expected start", 6, floc.getMin());
		assertEquals("Expected end", 9, floc.getMax());
		assertEquals("Expected strand", Strand.NEGATIVE_STRAND, floc
				.getStrand());
	}

	public void testConvertCoordsStrandNN() throws Exception {
		RichLocation floc = LocationUtils.getRelativeLocation(LocationUtils
				.parseEmblLocation("complement(2..5)"), LocationUtils
				.parseEmblLocation("complement(1..10)"));
		assertEquals("Expected start", 6, floc.getMin());
		assertEquals("Expected end", 9, floc.getMax());
		assertEquals("Expected strand", Strand.POSITIVE_STRAND, floc
				.getStrand());
	}

	public void testConvertCoordsStrandPP2() throws Exception {
		RichLocation floc = LocationUtils.getRelativeLocationWithAllGaps(
				LocationUtils.parseEmblLocation("2..5"), LocationUtils
						.parseEmblLocation("join(1..3,5..10)"));
		assertEquals("Expected start", 2, floc.getMin());
		assertEquals("Expected end", 4, floc.getMax());
		assertEquals("Expected strand", Strand.POSITIVE_STRAND, floc
				.getStrand());
	}

	public void testConvertCoordsStrandPN2() throws Exception {
		RichLocation floc = LocationUtils.getRelativeLocationWithAllGaps(
				LocationUtils.parseEmblLocation("complement(2..5)"),
				LocationUtils.parseEmblLocation("join(1..3,5..10)"));
		assertEquals("Expected start", 2, floc.getMin());
		assertEquals("Expected end", 4, floc.getMax());
		assertEquals("Expected strand", Strand.NEGATIVE_STRAND, floc
				.getStrand());
	}

	public void testConvertCoordsStrandNP2() throws Exception {
		RichLocation floc = LocationUtils.getRelativeLocationWithAllGaps(
				LocationUtils.parseEmblLocation("2..5"), LocationUtils
						.parseEmblLocation("complement(join(1..3,5..10))"));
		assertEquals("Expected start", 6, floc.getMin());
		assertEquals("Expected end", 8, floc.getMax());
		assertEquals("Expected strand", Strand.NEGATIVE_STRAND, floc
				.getStrand());
	}

	public void testConvertCoordsStrandNN2() throws Exception {
		RichLocation floc = LocationUtils
				.getRelativeLocationWithAllGaps(LocationUtils
						.parseEmblLocation("complement(2..5)"), LocationUtils
						.parseEmblLocation("complement(join(1..3,5..10))"));
		assertEquals("Expected start", 6, floc.getMin());
		assertEquals("Expected end", 8, floc.getMax());
		assertEquals("Expected strand", Strand.POSITIVE_STRAND, floc
				.getStrand());
	}

	public void testConvertCoordsGapped1() throws Exception {
		RichLocation floc = LocationUtils.getRelativeLocationGappedWithAllGaps(
				LocationUtils.parseEmblLocation("2..8"), LocationUtils
						.parseEmblLocation("join(1..4,6..10)"));
		assertEquals("Expected start", 2, floc.getMin());
		assertEquals("Expected end", 8, floc.getMax());
		List<RichLocation> locs = LocationUtils.sortLocation(floc);
		assertEquals("Expected start", 2, locs.get(0).getMin());
		assertEquals("Expected end", 4, locs.get(0).getMax());
		assertEquals("Expected start", 6, locs.get(1).getMin());
		assertEquals("Expected end", 8, locs.get(1).getMax());
	}

	public void testConvertCoordsGapped2() throws Exception {
		RichLocation floc = LocationUtils.getRelativeLocationGappedWithAllGaps(
				LocationUtils.parseEmblLocation("102..108"), LocationUtils
						.parseEmblLocation("join(101..104,106..110)"));
		assertEquals("Expected start", 2, floc.getMin());
		assertEquals("Expected end", 8, floc.getMax());
		List<RichLocation> locs = LocationUtils.sortLocation(floc);
		assertEquals("Expected start", 2, locs.get(0).getMin());
		assertEquals("Expected end", 4, locs.get(0).getMax());
		assertEquals("Expected start", 6, locs.get(1).getMin());
		assertEquals("Expected end", 8, locs.get(1).getMax());
	}

	public void testConvertCoordsGapped3() throws Exception {
		RichLocation floc = LocationUtils.getRelativeLocationGappedWithAllGaps(
				LocationUtils.parseEmblLocation("2..8"), LocationUtils
						.parseEmblLocation("complement(join(1..4,6..10))"));
		assertEquals("Expected start", 3, floc.getMin());
		assertEquals("Expected end", 9, floc.getMax());
		List<RichLocation> locs = LocationUtils.sortLocation(floc);
		Collections.reverse(locs);
		assertEquals("Expected start", 3, locs.get(0).getMin());
		assertEquals("Expected end", 5, locs.get(0).getMax());
		assertEquals("Expected start", 7, locs.get(1).getMin());
		assertEquals("Expected end", 9, locs.get(1).getMax());
	}

	public void testConvertCoordsGapped4() throws Exception {
		RichLocation floc = LocationUtils
				.getRelativeLocationGappedWithAllGaps(
						LocationUtils.parseEmblLocation("102..108"),
						LocationUtils
								.parseEmblLocation("complement(join(101..104,106..110))"));
		assertEquals("Expected start", 3, floc.getMin());
		assertEquals("Expected end", 9, floc.getMax());
		List<RichLocation> locs = LocationUtils.sortLocation(floc);
		Collections.reverse(locs);
		assertEquals("Expected start", 3, locs.get(0).getMin());
		assertEquals("Expected end", 5, locs.get(0).getMax());
		assertEquals("Expected start", 7, locs.get(1).getMin());
		assertEquals("Expected end", 9, locs.get(1).getMax());
	}

	public void testConvertCoordsGapped1a() throws Exception {
		try {
			RichLocation floc = LocationUtils
					.getRelativeLocationGapped(LocationUtils
							.parseEmblLocation("2..8"), LocationUtils
							.parseEmblLocation("join(1..4,6..10)"));
			fail("Exception should have been raised");
		} catch (FeatureLocationGapOverlapException e) {
		}
	}

	public void testConvertCoordsGapped2a() throws Exception {
		try {
			RichLocation floc = LocationUtils
					.getRelativeLocationGapped(LocationUtils
							.parseEmblLocation("102..108"), LocationUtils
							.parseEmblLocation("join(101..104,106..110)"));
			fail("Exception should have been raised");
		} catch (FeatureLocationGapOverlapException e) {
		}
	}

	public void testConvertCoordsGapped3a() throws Exception {
		try {
			RichLocation floc = LocationUtils
					.getRelativeLocationGapped(LocationUtils
							.parseEmblLocation("2..8"), LocationUtils
							.parseEmblLocation("complement(join(1..4,6..10))"));
			fail("Exception should have been raised");
		} catch (FeatureLocationGapOverlapException e) {
		}
	}

	public void testConvertCoordsGapped4a() throws Exception {
		try {
			RichLocation floc = LocationUtils
					.getRelativeLocationGapped(
							LocationUtils.parseEmblLocation("102..108"),
							LocationUtils
									.parseEmblLocation("complement(join(101..104,106..110))"));
			fail("Exception should have been raised");
		} catch (FeatureLocationGapOverlapException e) {
		}
	}

	/*
	 * Case (2): Multiple CDSs for a given gene (AP001918)
	 *
	 * Input
	 *
	 * gene: (2869..3075)
	 *
	 * CDSs to project onto the gene: 2869..3075 2926..3075
	 *
	 * Output
	 *
	 * projected CDSs: 1..207 58..207
	 */

	public void testRealCds1() throws Exception {
		RichLocation floc = LocationUtils.getRelativeLocationGapped(
				LocationUtils.parseEmblLocation("2869..3075"), LocationUtils
						.parseEmblLocation("2869..3075"));
		assertEquals("Expected start", 1, floc.getMin());
		assertEquals("Expected end", 207, floc.getMax());
	}

	public void testRealCds2() throws Exception {
		RichLocation floc = LocationUtils.getRelativeLocationGapped(
				LocationUtils.parseEmblLocation("2926..3075"), LocationUtils
						.parseEmblLocation("2869..3075"));
		assertEquals("Expected start", 58, floc.getMin());
		assertEquals("Expected end", 207, floc.getMax());
	}

	/*
	 * Case (1): CDS on the complement strand (AP001918 & U00094)
	 *
	 * (a) Input: gene: (outerLoc) complement(4303..6399)
	 *
	 * CDS to project onto the gene: complement(4303..6399)
	 *
	 * Output
	 *
	 * projected CDS: 1..2097
	 */

	public void testRealCds3() throws Exception {
		RichLocation floc = LocationUtils.getRelativeLocationGapped(
				LocationUtils.parseEmblLocation("complement(4303..6399)"),
				LocationUtils.parseEmblLocation("complement(4303..6399)"));
		assertEquals("Expected start", 1, floc.getMin());
		assertEquals("Expected end", 2097, floc.getMax());
	}

	/*
	 * (b) Input: gene: complement(280..6007)
	 *
	 * CDS: join(complement(280..5840),complement(5989..6007))
	 *
	 * Output
	 *
	 * projected CDS: join(1..19,168..5728)
	 */

	public void testRealCds4() throws Exception {
		RichLocation floc = LocationUtils
				.getRelativeLocationGapped(
						LocationUtils
								.parseEmblLocation("join(complement(280..5840),complement(5989..6007))"),
						LocationUtils
								.parseEmblLocation("complement(280..6007)"));
		assertEquals("Expected start", 1, floc.getMin());
		assertEquals("Expected end", 5728, floc.getMax());
		assertEquals("Expected strand", Strand.POSITIVE_STRAND, floc
				.getStrand());
		List<RichLocation> locs = LocationUtils.sortLocation(floc);
		assertEquals("Expected start", 1, locs.get(0).getMin());
		assertEquals("Expected end", 19, locs.get(0).getMax());
		assertEquals("Expected strand", Strand.POSITIVE_STRAND, locs.get(0)
				.getStrand());
		assertEquals("Expected start", 168, locs.get(1).getMin());
		assertEquals("Expected end", 5728, locs.get(1).getMax());
		assertEquals("Expected strand", Strand.POSITIVE_STRAND, locs.get(0)
				.getStrand());

	}

	/*
	 * Case (3): CDS overlapping origin of replication (AY057845)
	 *
	 * Input:
	 *
	 * gene: join(complement(1..316),complement(40645..40991)) (is that right ?)
	 *
	 * CDS: join(complement(1..316),complement(40645..40991))
	 *
	 * Output: 1..663
	 */

	public void testRealCds5() throws Exception {
		RichLocation loc1 = LocationUtils
				.parseEmblLocation("join(complement(1..316),complement(40645..40991))");
		loc1.setCircularLength(40991);
		RichLocation loc2 = LocationUtils
				.parseEmblLocation("join(complement(1..316),complement(40645..40991))");
		loc2.setCircularLength(40991);
		RichLocation floc = LocationUtils.getRelativeLocationGapped(loc1, loc2);
		assertEquals("Expected start", 1, floc.getMin());
		assertEquals("Expected end", 663, floc.getMax());
		assertEquals("Expected strand", Strand.POSITIVE_STRAND, floc
				.getStrand());
	}

	public void testRealCds5C() throws Exception {
		RichLocation loc1 = LocationUtils
				.parseEmblLocation("join(1..316,40645..40991)");
		loc1.setCircularLength(40991);
		RichLocation loc2 = LocationUtils
				.parseEmblLocation("join(1..316,40645..40991)");
		loc2.setCircularLength(40991);
		RichLocation floc = LocationUtils.getRelativeLocationGapped(loc1, loc2);
		assertEquals("Expected start", 1, floc.getMin());
		assertEquals("Expected end", 663, floc.getMax());
		assertEquals("Expected strand", Strand.POSITIVE_STRAND, floc
				.getStrand());
	}

	public void testRealCds5a() throws Exception {
		RichLocation loc1 = LocationUtils
				.parseEmblLocation("join(1..316,40645..40991)");
		loc1.setCircularLength(40991);
		RichLocation loc2 = LocationUtils.parseEmblLocation("join(1..316)");
		loc2.setCircularLength(40991);
		RichLocation floc = LocationUtils.getRelativeLocationGapped(loc2, loc1);
		assertEquals("Expected start", 348, floc.getMin());
		assertEquals("Expected end", 663, floc.getMax());
		assertEquals("Expected strand", Strand.POSITIVE_STRAND, floc
				.getStrand());
	}

	public void testRealCds5b() throws Exception {
		RichLocation loc1 = LocationUtils
				.parseEmblLocation("complement(814054..815645)");
		RichLocation loc2 = LocationUtils.parseEmblLocation("join(complement(815421..815645),complement(815405..815416),complement(814054..815403))");
		RichLocation floc = LocationUtils.getRelativeLocationGapped(loc2, loc1);
		assertEquals("Expected members",3,LocationUtils.countInnerLocations(floc));
		// should be join(1..225,230..241,243..1592)
	}

	/*
	 * Case (4): Fuzzy location (AM180252)
	 *
	 * Input:
	 *
	 * gene: complement(1102873..1103156) (Should not need the fuzzy location,
	 * right ?)
	 *
	 * CDS: complement(<1102873..>1103156)
	 *
	 * Output: <1..>284
	 */

	public void testRealCds6() throws Exception {
		RichLocation loc1 = LocationUtils
				.parseEmblLocation("complement(1102873..1103156)");
		loc1.setCircularLength(40991);
		RichLocation loc2 = LocationUtils
				.parseEmblLocation("complement(<1102873..>1103156)");
		loc2.setCircularLength(40991);
		RichLocation floc = LocationUtils.getRelativeLocationGapped(loc2, loc1);
		assertEquals("Expected start", 1, floc.getMin());
		assertEquals("Expected end", 284, floc.getMax());
		assertEquals("Expected strand", Strand.POSITIVE_STRAND, floc
				.getStrand());
		assertTrue("Expected fuzzy start", floc.getMinPosition()
				.getFuzzyStart());
		assertTrue("Expected fuzzy end", floc.getMaxPosition().getFuzzyEnd());
	}

	/* Case from AK */
	public void testRealCds7() throws Exception {
		RichLocation cds = LocationUtils
				.parseEmblLocation("join(10..20,50..100)");
		RichLocation ft = LocationUtils
				.parseEmblLocation("join(15..20,50..97)");
		RichLocation floc = LocationUtils.getRelativeLocation(ft, cds);
		assertEquals("Expected start", 6, floc.getMin());
		assertEquals("Expected end", 59, floc.getMax());
		assertEquals("Expected strand", Strand.POSITIVE_STRAND, floc
				.getStrand());
		System.out.println(floc);
	}

	/* Case from AK */
	public void testRealCds8() throws Exception {
		RichLocation ref = LocationUtils
				.parseEmblLocation("join(1390189..1391310,1391310..1393681)");
		RichLocation sub = LocationUtils
				.parseEmblLocation("join(1390189..1391310,1391310..1393681)");
		RichLocation floc = LocationUtils.getRelativeLocation(sub, ref);
//		assertEquals("Expected start", 6, floc.getMin());
//		assertEquals("Expected end", 59, floc.getMax());
//		assertEquals("Expected strand", Strand.POSITIVE_STRAND, floc
//				.getStrand());
		System.out.println(floc);
	}
	/* Case from AK */
	public void testRealCds9() throws Exception {
		RichLocation ref = LocationUtils
				.parseEmblLocation("join(1..2502,92527..92721)");
		ref.setCircularLength(92721);
		RichLocation sub = LocationUtils
				.parseEmblLocation("join(92527..92631)");
		sub.setCircularLength(92721);
		RichLocation floc = LocationUtils.getRelativeLocation(sub, ref);
		System.out.println(floc); //1..105 expected
		assertEquals("Expected start", 1, floc.getMin());
		assertEquals("Expected end", 105, floc.getMax());
		assertEquals("Expected strand", Strand.POSITIVE_STRAND, floc
				.getStrand());
	}
	public void testProteinLocation1() throws Exception {
		RichLocation cds = LocationUtils.parseEmblLocation("1..12");
		RichLocation ft = LocationUtils.parseEmblLocation("4..9");
		RichLocation floc = LocationUtils.getProteinSubLocation(ft, cds);
		assertEquals("Expected start", 2, floc.getMin());
		assertEquals("Expected end", 3, floc.getMax());
	}

	public void testProteinLocation2() throws Exception {
		RichLocation cds = LocationUtils.parseEmblLocation("complement(1..12)");
		RichLocation ft = LocationUtils.parseEmblLocation("complement(4..9)");
		RichLocation floc = LocationUtils.getProteinSubLocation(ft, cds);
		assertEquals("Expected start", 2, floc.getMin());
		assertEquals("Expected end", 3, floc.getMax());
	}

	public void testProteinLocation3() throws Exception {
		RichLocation cds = LocationUtils.parseEmblLocation("101..112");
		RichLocation ft = LocationUtils.parseEmblLocation("104..109");
		RichLocation floc = LocationUtils.getProteinSubLocation(ft, cds);
		assertEquals("Expected start", 2, floc.getMin());
		assertEquals("Expected end", 3, floc.getMax());
	}

	public void testProteinLocation4() throws Exception {
		RichLocation loc1 = LocationUtils.parseEmblLocation("1..3,10..200");
		RichLocation loc2 = LocationUtils.parseEmblLocation("10..15,19..21");
		RichLocation floc = LocationUtils.getProteinSubLocation(loc2, loc1);
		assertEquals("Expected start", 2, floc.getMin());
		assertEquals("Expected end", 5, floc.getMax());
	}

	public void testProteinLocation5() throws Exception {
		RichLocation loc1 = LocationUtils.parseEmblLocation("1..3,10..200");
		RichLocation loc2 = LocationUtils.parseEmblLocation("10..15");
		RichLocation floc = LocationUtils.getProteinSubLocation(loc2, loc1);
		assertEquals("Expected start", 2, floc.getMin());
		assertEquals("Expected end", 3, floc.getMax());
	}
}
