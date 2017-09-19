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
 * File: CompoundLocationOverlapTest.java
 * Created by: dstaines
 * Created on: Oct 3, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.util.biojava;

import org.biojavax.bio.seq.RichLocation;

import junit.framework.TestCase;

/**
 * @author dstaines
 *
 */
public class CompoundLocationOverlapTest extends TestCase {

	/**
	 * @param name
	 */
	public CompoundLocationOverlapTest(String name) {
		super(name);
	}

	public void testCompoundOverlap1() throws Exception {
		// expect an internal overlap as gap is a codon
		RichLocation location1 = LocationUtils
				.parseEmblLocation("join(1..6,10..12)");
		RichLocation location2 = LocationUtils
				.parseEmblLocation("join(10..12)");
		assertTrue("Expected outer overlap between " + location1 + " and "
				+ location2, LocationUtils.outerOverlapsInFrame(location1,
				location2));
		assertTrue("Expected overlap between " + location1 + " and "
				+ location2, LocationUtils
				.overlapsInFrame(location1, location2));
	}

	public void testCompoundOverlap2() throws Exception {
		// do not expect an internal overlap as gap is not a codon
		RichLocation location1 = LocationUtils
				.parseEmblLocation("join(1..5,10..12)");
		RichLocation location2 = LocationUtils
				.parseEmblLocation("join(10..12)");
		assertTrue("Expected outer overlap between " + location1 + " and "
				+ location2, LocationUtils.outerOverlapsInFrame(location1,
				location2));
		assertFalse("Expected overlap between " + location1 + " and "
				+ location2, LocationUtils
				.overlapsInFrame(location1, location2));
	}

	public void testCompoundOverlap3() throws Exception {
		// expect an internal overlap as gap is a codon
		RichLocation location1 = LocationUtils
				.parseEmblLocation("join(1..6,10..12)");
		RichLocation location2 = LocationUtils
				.parseEmblLocation("join(10..12,14..20)");
		assertTrue("Expected outer overlap between " + location1 + " and "
				+ location2, LocationUtils.outerOverlapsInFrame(location1,
				location2));
		assertTrue("Expected overlap between " + location1 + " and "
				+ location2, LocationUtils
				.overlapsInFrame(location1, location2));
	}

	public void testCompoundOverlap4() throws Exception {
		// do not expect an internal overlap as gap is not a codon
		RichLocation location1 = LocationUtils
				.parseEmblLocation("join(1..5,10..12)");
		RichLocation location2 = LocationUtils
				.parseEmblLocation("join(10..12,14..20)");
		assertTrue("Expected outer overlap between " + location1 + " and "
				+ location2, LocationUtils.outerOverlapsInFrame(location1,
				location2));
		assertFalse("Expected overlap between " + location1 + " and "
				+ location2, LocationUtils
				.overlapsInFrame(location1, location2));
	}

	public void testCompoundOverlap5() throws Exception {
		RichLocation location1 = LocationUtils
				.parseEmblLocation("join(1..6,11..13)");
		RichLocation location2 = LocationUtils
				.parseEmblLocation("join(10..13)");
		assertTrue("Expected outer overlap between " + location1 + " and "
				+ location2, LocationUtils.outerOverlapsInFrame(location1,
				location2));
		assertFalse("Expected overlap between " + location1 + " and "
				+ location2, LocationUtils
				.overlapsInFrame(location1, location2));
	}

	public void testCompoundOverlap6() throws Exception {
		RichLocation location1 = LocationUtils
				.parseEmblLocation("join(1..5,9..12)");
		RichLocation location2 = LocationUtils
				.parseEmblLocation("join(10..12)");
		assertTrue("Expected outer overlap between " + location1 + " and "
				+ location2, LocationUtils.outerOverlapsInFrame(location1,
				location2));
		assertTrue("Expected overlap between " + location1 + " and "
				+ location2, LocationUtils
				.overlapsInFrame(location1, location2));
	}

	public void testCompoundOverlap7() throws Exception {
		RichLocation location1 = LocationUtils
				.parseEmblLocation("complement(join(1..6,10..12))");
		RichLocation location2 = LocationUtils
				.parseEmblLocation("complement(join(4..6))");
		assertTrue("Expected outer overlap between " + location1 + " and "
				+ location2, LocationUtils.outerOverlapsInFrame(location1,
				location2));
		assertTrue("Expected overlap between " + location1 + " and "
				+ location2, LocationUtils
				.overlapsInFrame(location1, location2));
	}

	public void testCompoundOverlap8() throws Exception {
		// expect an internal overlap as gap is a codon
		RichLocation location1 = LocationUtils
				.parseEmblLocation("join(101..106,110..112)");
		RichLocation location2 = LocationUtils
				.parseEmblLocation("join(110..112)");
		assertTrue("Expected outer overlap between " + location1 + " and "
				+ location2, LocationUtils.outerOverlapsInFrame(location1,
				location2));
		assertTrue("Expected overlap between " + location1 + " and "
				+ location2, LocationUtils
				.overlapsInFrame(location1, location2));
	}

	public void testCompoundOverlap9() throws Exception {
		// do not expect an internal overlap as gap is not a codon
		RichLocation location1 = LocationUtils
				.parseEmblLocation("join(101..105,110..112)");
		RichLocation location2 = LocationUtils
				.parseEmblLocation("join(110..112)");
		assertTrue("Expected outer overlap between " + location1 + " and "
				+ location2, LocationUtils.outerOverlapsInFrame(location1,
				location2));
		assertFalse("Expected overlap between " + location1 + " and "
				+ location2, LocationUtils
				.overlapsInFrame(location1, location2));
	}

	public void testCompoundOverlap10() throws Exception {
		// expect an internal overlap as gap is a codon
		RichLocation location1 = LocationUtils
				.parseEmblLocation("join(101..106,110..112)");
		RichLocation location2 = LocationUtils
				.parseEmblLocation("join(110..112,114..120)");
		assertTrue("Expected outer overlap between " + location1 + " and "
				+ location2, LocationUtils.outerOverlapsInFrame(location1,
				location2));
		assertTrue("Expected overlap between " + location1 + " and "
				+ location2, LocationUtils
				.overlapsInFrame(location1, location2));
	}

	public void testCompoundOverlap11() throws Exception {
		// do not expect an internal overlap as gap is not a codon
		RichLocation location1 = LocationUtils
				.parseEmblLocation("join(101..105,110..112)");
		RichLocation location2 = LocationUtils
				.parseEmblLocation("join(110..112,114..120)");
		assertTrue("Expected outer overlap between " + location1 + " and "
				+ location2, LocationUtils.outerOverlapsInFrame(location1,
				location2));
		assertFalse("Expected overlap between " + location1 + " and "
				+ location2, LocationUtils
				.overlapsInFrame(location1, location2));
	}

	public void testCompoundOverlap12() throws Exception {
		RichLocation location1 = LocationUtils
				.parseEmblLocation("join(101..106,111..113)");
		RichLocation location2 = LocationUtils
				.parseEmblLocation("join(110..113)");
		assertTrue("Expected outer overlap between " + location1 + " and "
				+ location2, LocationUtils.outerOverlapsInFrame(location1,
				location2));
		assertFalse("Expected overlap between " + location1 + " and "
				+ location2, LocationUtils
				.overlapsInFrame(location1, location2));
	}

	public void testCompoundOverlap13() throws Exception {
		RichLocation location1 = LocationUtils
				.parseEmblLocation("join(101..105,109..112)");
		RichLocation location2 = LocationUtils
				.parseEmblLocation("join(110..112)");
		assertTrue("Expected outer overlap between " + location1 + " and "
				+ location2, LocationUtils.outerOverlapsInFrame(location1,
				location2));
		assertTrue("Expected overlap between " + location1 + " and "
				+ location2, LocationUtils
				.overlapsInFrame(location1, location2));
	}

	public void testCompoundOverlap14() throws Exception {
		RichLocation location1 = LocationUtils
				.parseEmblLocation("complement(join(101..106,110..112))");
		RichLocation location2 = LocationUtils
				.parseEmblLocation("complement(join(104..106))");
		assertTrue("Expected outer overlap between " + location1 + " and "
				+ location2, LocationUtils.outerOverlapsInFrame(location1,
				location2));
		assertTrue("Expected overlap between " + location1 + " and "
				+ location2, LocationUtils
				.overlapsInFrame(location1, location2));
	}

	public void testCompoundOverlap15() throws Exception {
		RichLocation location1 = LocationUtils
				.parseEmblLocation("join(2..10,20..30))");
		RichLocation location2 = LocationUtils
				.parseEmblLocation("join(20..30,50..60)");
		assertTrue("Expected outer overlap between " + location1 + " and "
				+ location2, LocationUtils.outerOverlapsInFrame(location1,
				location2));
		assertTrue("Expected overlap between " + location1 + " and "
				+ location2, LocationUtils
				.overlapsInFrame(location1, location2));
	}

	public void testCompoundOverlap16() throws Exception {
		RichLocation location1 = LocationUtils
				.parseEmblLocation("join(1..3,complement(7..9))");
		RichLocation location2 = LocationUtils
				.parseEmblLocation("join(1..3)");
//		assertTrue("Expected outer overlap between " + location1 + " and "
//				+ location2, LocationUtils.outerOverlapsInFrame(location1,
//				location2));
		assertTrue("Expected overlap between " + location1 + " and "
				+ location2, LocationUtils
				.overlapsInFrame(location1, location2));
	}

	public void testCompoundOverlap17() throws Exception {
		RichLocation location1 = LocationUtils
				.parseEmblLocation("complement(join(1..3,7..9))");
		RichLocation location2a = LocationUtils
			.parseEmblLocation("complement(join(1..3,7..9))");
		RichLocation location2b = LocationUtils
		.parseEmblLocation("complement(join(1..4,7..10))");
		RichLocation location2c = LocationUtils
		.parseEmblLocation("complement(join(1..4,8..10))");
		RichLocation location2d = LocationUtils
		.parseEmblLocation("complement(join(1..4,9..10))");
		assertTrue("Expected overlap between " + location1 + " and "
				+ location2a, LocationUtils
				.overlapsInFrame(location1, location2a));
		assertFalse("No expected overlap between " + location1 + " and "
				+ location2b, LocationUtils
				.overlapsInFrame(location1, location2b));
		assertFalse("No expected overlap between " + location1 + " and "
				+ location2c, LocationUtils
				.overlapsInFrame(location1, location2c));
		assertTrue("Expected overlap between " + location1 + " and "
				+ location2d, LocationUtils
				.overlapsInFrame(location1, location2d));

	}
	public void testRealExample() throws Exception {
		RichLocation location1 = LocationUtils.parseEmblLocation("complement(join(275197..275262,275426..275434))");
		RichLocation location2 = LocationUtils.parseEmblLocation("complement(join(274577..274645,274693..274794,274844..274949,274998..275063,275112..275285,275344..275381,275437..275471,275519..275717,275765..275890))");
		assertFalse("No expected overlap",LocationUtils.overlapsInFrame(location1, location2));
	}
	public void testRealExample2() throws Exception {
		RichLocation location1 = LocationUtils.parseEmblLocation("complement(join(275197..275262,275426..275434))");
		RichLocation location2 = LocationUtils.parseEmblLocation("complement(join(274577..274645,274693..274794,274844..274949,274998..275063,275112..275262,275263..275285,275344..275381,275437..275471,275519..275717,275765..275890))");
		assertFalse("No expected overlap",LocationUtils.overlapsInFrame(location1, location2));
	}
	public void testRealExample3() throws Exception {
		RichLocation location1 = LocationUtils.parseEmblLocation("complement(join(97..162,326..334))");
		RichLocation location2 = LocationUtils.parseEmblLocation("complement(join(12..162,163..185,244..281,337..371,419..617,665..790))");
		assertFalse("No expected overlap",LocationUtils.overlapsInFrame(location1, location2));
	}
	public void testRealExample4() throws Exception {
		RichLocation location1 = LocationUtils.parseEmblLocation("complement(join(97..162,326..334))");
		RichLocation location2 = LocationUtils.parseEmblLocation("complement(join(12..185,244..281,337..371,419..617,665..790))");
		assertFalse("No expected overlap",LocationUtils.overlapsInFrame(location1, location2));
	}

}
