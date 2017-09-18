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

import org.biojavax.bio.seq.RichLocation;
import org.biojavax.bio.seq.RichLocation.Strand;
import org.ensembl.genomeloader.util.biojava.LocationUtils;
import org.biojavax.bio.seq.SimplePosition;
import org.biojavax.bio.seq.SimpleRichLocation;

import junit.framework.TestCase;

/**
 * @author dstaines
 *
 */
public class GlobalLocationTest extends TestCase {

	public void testGlobal1() throws Exception {
		RichLocation floc = LocationUtils.getGlobalLocation(LocationUtils
				.parseEmblLocation("1..10"), LocationUtils
				.parseEmblLocation("2..8"));
		// System.out.println(floc);
		assertEquals("Expected start", 2, floc.getMin());
		assertEquals("Expected end", 8, floc.getMax());
		assertEquals("Expected strand", Strand.POSITIVE_STRAND, floc
				.getStrand());
	}

	public void testGlobal2() throws Exception {
		RichLocation floc = LocationUtils.getGlobalLocation(LocationUtils
				.parseEmblLocation("1..10"), LocationUtils
				.parseEmblLocation("complement(2..8)"));
		assertEquals("Expected start", 2, floc.getMin());
		assertEquals("Expected end", 8, floc.getMax());
		assertEquals("Expected strand", Strand.NEGATIVE_STRAND, floc
				.getStrand());
	}

	public void testGlobal3() throws Exception {
		RichLocation floc = LocationUtils.getGlobalLocation(LocationUtils
				.parseEmblLocation("complement(1..10)"), LocationUtils
				.parseEmblLocation("2..8"));
		// System.out.println(floc);
		assertEquals("Expected start", 3, floc.getMin());
		assertEquals("Expected end", 9, floc.getMax());
		assertEquals("Expected strand", Strand.NEGATIVE_STRAND, floc
				.getStrand());
	}

	public void testGlobal4() throws Exception {
		RichLocation floc = LocationUtils.getGlobalLocation(LocationUtils
				.parseEmblLocation("complement(1..10)"), LocationUtils
				.parseEmblLocation("complement(2..8)"));
		assertEquals("Expected start", 3, floc.getMin());
		assertEquals("Expected end", 9, floc.getMax());
		assertEquals("Expected strand", Strand.POSITIVE_STRAND, floc
				.getStrand());
	}

	public void testGlobal5() throws Exception {
		RichLocation floc = LocationUtils.getGlobalLocation(LocationUtils
				.parseEmblLocation("join(1..4,7..10)"), LocationUtils
				.parseEmblLocation("2..6"));
		// System.out.println(floc);
		assertEquals("Expected start", 2, floc.getMin());
		assertEquals("Expected end", 8, floc.getMax());
		assertEquals("Expected strand", Strand.POSITIVE_STRAND, floc
				.getStrand());
	}

	public void testGlobal6() throws Exception {
		RichLocation floc = LocationUtils.getGlobalLocation(LocationUtils
				.parseEmblLocation("join(1..4,7..10)"), LocationUtils
				.parseEmblLocation("complement(2..6)"));
		assertEquals("Expected start", 2, floc.getMin());
		assertEquals("Expected end", 8, floc.getMax());
		assertEquals("Expected strand", Strand.NEGATIVE_STRAND, floc
				.getStrand());
	}

	public void testGlobal7() throws Exception {
		RichLocation floc = LocationUtils.getGlobalLocation(LocationUtils
				.parseEmblLocation("complement(join(1..4,7..10))"),
				LocationUtils.parseEmblLocation("2..6"));
		// System.out.println(floc);
		assertEquals("Expected start", 3, floc.getMin());
		assertEquals("Expected end", 9, floc.getMax());
		assertEquals("Expected strand", Strand.NEGATIVE_STRAND, floc
				.getStrand());
	}

	public void testGlobal8() throws Exception {
		RichLocation floc = LocationUtils.getGlobalLocation(LocationUtils
				.parseEmblLocation("complement(join(1..4,7..10))"),
				LocationUtils.parseEmblLocation("complement(2..6)"));
		// System.out.println(floc);
		assertEquals("Expected start", 3, floc.getMin());
		assertEquals("Expected end", 9, floc.getMax());
		assertEquals("Expected strand", Strand.POSITIVE_STRAND, floc
				.getStrand());
	}

	public void testGlobal9() throws Exception {

		String locStr = "join(1..6,8..10)";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 10, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
		RichLocation protLoc = LocationUtils.getGlobalLocation(locs,
				LocationUtils.parseEmblLocation("1..6"));
		// System.out.println(protLoc);
		assertEquals("Expected start", 1, protLoc.getMin());
		assertEquals("Expected end", 6, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				protLoc.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(protLoc));
		assertEquals("Expected number of inner locations", 1, LocationUtils
				.countInnerLocations(protLoc));

	}

	public void testGlobal1a() throws Exception {
		RichLocation floc = LocationUtils.getGlobalLocation(LocationUtils
				.parseEmblLocation("101..110"), LocationUtils
				.parseEmblLocation("2..8"));
		// System.out.println(floc);
		assertEquals("Expected start", 102, floc.getMin());
		assertEquals("Expected end", 108, floc.getMax());
		assertEquals("Expected strand", Strand.POSITIVE_STRAND, floc
				.getStrand());
	}

	public void testGlobal2a() throws Exception {
		RichLocation floc = LocationUtils.getGlobalLocation(LocationUtils
				.parseEmblLocation("101..110"), LocationUtils
				.parseEmblLocation("complement(2..8)"));
		assertEquals("Expected start", 102, floc.getMin());
		assertEquals("Expected end", 108, floc.getMax());
		assertEquals("Expected strand", Strand.NEGATIVE_STRAND, floc
				.getStrand());
	}

	public void testGlobal3a() throws Exception {
		RichLocation floc = LocationUtils.getGlobalLocation(LocationUtils
				.parseEmblLocation("complement(101..110)"), LocationUtils
				.parseEmblLocation("2..8"));
		// System.out.println(floc);
		assertEquals("Expected start", 103, floc.getMin());
		assertEquals("Expected end", 109, floc.getMax());
		assertEquals("Expected strand", Strand.NEGATIVE_STRAND, floc
				.getStrand());
	}

	public void testGlobal4a() throws Exception {
		RichLocation floc = LocationUtils.getGlobalLocation(LocationUtils
				.parseEmblLocation("complement(101..110)"), LocationUtils
				.parseEmblLocation("complement(2..8)"));
		assertEquals("Expected start", 103, floc.getMin());
		assertEquals("Expected end", 109, floc.getMax());
		assertEquals("Expected strand", Strand.POSITIVE_STRAND, floc
				.getStrand());
	}

	public void testGlobal5a() throws Exception {
		RichLocation floc = LocationUtils.getGlobalLocation(LocationUtils
				.parseEmblLocation("join(101..104,107..110)"), LocationUtils
				.parseEmblLocation("2..6"));
		// System.out.println(floc);
		assertEquals("Expected start", 102, floc.getMin());
		assertEquals("Expected end", 108, floc.getMax());
		assertEquals("Expected strand", Strand.POSITIVE_STRAND, floc
				.getStrand());
	}

	public void testGlobal6a() throws Exception {
		RichLocation floc = LocationUtils.getGlobalLocation(LocationUtils
				.parseEmblLocation("join(101..104,107..110)"), LocationUtils
				.parseEmblLocation("complement(2..6)"));
		assertEquals("Expected start", 102, floc.getMin());
		assertEquals("Expected end", 108, floc.getMax());
		assertEquals("Expected strand", Strand.NEGATIVE_STRAND, floc
				.getStrand());
	}

	public void testGlobal7a() throws Exception {
		RichLocation floc = LocationUtils.getGlobalLocation(LocationUtils
				.parseEmblLocation("complement(join(101..104,107..110))"),
				LocationUtils.parseEmblLocation("2..6"));
		// System.out.println(floc);
		assertEquals("Expected start", 103, floc.getMin());
		assertEquals("Expected end", 109, floc.getMax());
		assertEquals("Expected strand", Strand.NEGATIVE_STRAND, floc
				.getStrand());
	}

	public void testGlobal8a() throws Exception {
		RichLocation floc = LocationUtils.getGlobalLocation(LocationUtils
				.parseEmblLocation("complement(join(101..104,107..110))"),
				LocationUtils.parseEmblLocation("complement(2..6)"));
		assertEquals("Expected start", 103, floc.getMin());
		assertEquals("Expected end", 109, floc.getMax());
		assertEquals("Expected strand", Strand.POSITIVE_STRAND, floc
				.getStrand());
	}

	public void testGlobal9a() throws Exception {

		String locStr = "join(101..106,108..110)";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 101, locs.getMin());
		assertEquals("Expected end", 110, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
		RichLocation protLoc = LocationUtils.getGlobalLocation(locs,
				LocationUtils.parseEmblLocation("1..6"));
		assertEquals("Expected start", 101, protLoc.getMin());
		assertEquals("Expected end", 106, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				protLoc.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(protLoc));
		assertEquals("Expected number of inner locations", 1, LocationUtils
				.countInnerLocations(protLoc));

	}

	public void testGlobalCircular() throws Exception {
		RichLocation loc = LocationUtils.parseEmblLocation("1..10,91..100");
		loc.setCircularLength(100);
		RichLocation floc = LocationUtils.getGlobalLocation(loc, LocationUtils
				.parseEmblLocation("1..20"));
		assertEquals("Expected start", 91, floc.getMin());
		assertEquals("Expected end", 10, floc.getMax());
		assertEquals("Expected strand", Strand.POSITIVE_STRAND, floc
				.getStrand());
	}

	public void testGlobalCircularC() throws Exception {
		RichLocation loc = LocationUtils
				.parseEmblLocation("complement(join(1..10,91..100))");
		loc.setCircularLength(100);
		RichLocation floc = LocationUtils.getGlobalLocation(loc, LocationUtils
				.parseEmblLocation("1..20"));
		assertEquals("Expected start", 91, floc.getMin());
		assertEquals("Expected end", 10, floc.getMax());
		assertEquals("Expected strand", Strand.NEGATIVE_STRAND, floc
				.getStrand());
	}

	public void testGlobalCircular2() throws Exception {
		RichLocation loc = LocationUtils.parseEmblLocation("1..10,91..100");
		loc.setCircularLength(100);
		RichLocation floc = LocationUtils.getGlobalLocation(loc, LocationUtils
				.parseEmblLocation("2..12"));
		assertEquals("Expected start", 92, floc.getMin());
		assertEquals("Expected end", 2, floc.getMax());
		assertEquals("Expected strand", Strand.POSITIVE_STRAND, floc
				.getStrand());
	}

	public void testGlobalCircular2C() throws Exception {
		RichLocation loc = LocationUtils
				.parseEmblLocation("complement(join(1..10,91..100))");
		loc.setCircularLength(100);
		RichLocation floc = LocationUtils.getGlobalLocation(loc, LocationUtils
				.parseEmblLocation("2..12"));
		assertEquals("Expected start", 99, floc.getMin());
		assertEquals("Expected end", 9, floc.getMax());
		assertEquals("Expected strand", Strand.NEGATIVE_STRAND, floc
				.getStrand());
	}

	public void testGlobalCircularProt() throws Exception {
		RichLocation loc = LocationUtils.parseEmblLocation("1..11,91..100");
		loc.setCircularLength(100);
		RichLocation floc = LocationUtils.getLocationForProteinFeature(loc, 1,
				6);
		assertEquals("Expected start", 91, floc.getMin());
		assertEquals("Expected end", 8, floc.getMax());
	}

	public void testGlobalCircularProt2() throws Exception {
		RichLocation loc = LocationUtils.parseEmblLocation("1..11,91..100");
		loc.setCircularLength(100);
		RichLocation floc = LocationUtils.getLocationForProteinFeature(loc, 2,
				4);
		assertEquals("Expected start", 94, floc.getMin());
		assertEquals("Expected end", 2, floc.getMax());
	}

	public void testGlobalCircularProtAK() throws Exception {
		RichLocation loc = LocationUtils
				.parseEmblLocation("join(1..2502,92527..92721)");
		loc.setCircularLength(92721);
		RichLocation floc = LocationUtils.getLocationForProteinFeature(loc, 1,
				35);
		assertEquals("Expected start", 92527, floc.getMin());
		assertEquals("Expected end", 92631, floc.getMax());
	}

	public void testGlobalCircularProtAK2() throws Exception {
		RichLocation loc = LocationUtils
				.parseEmblLocation("join(1..2502,92527..92721)");
		loc.setCircularLength(92721);
		RichLocation floc = LocationUtils.getLocationForProteinFeature(loc, 5,
				200);
		assertEquals("Expected start", 92539, floc.getMin());
		assertEquals("Expected end", 405, floc.getMax());
	}

	public void testGlobalCircularProtAK3() throws Exception {
		RichLocation loc = LocationUtils
				.parseEmblLocation("join(1..2502,92527..92721)");
		loc.setCircularLength(92721);
		RichLocation floc = LocationUtils.getLocationForProteinFeature(loc,
				195, 200);
		assertEquals("Expected start", 388, floc.getMin());
		assertEquals("Expected end", 405, floc.getMax());
	}

	public void testStartConversion() throws Exception {
		assertEquals("Expected triplet start", 1, LocationUtils
				.getTripletStart(1));
		assertEquals("Expected triplet start", 4, LocationUtils
				.getTripletStart(2));
		assertEquals("Expected triplet start", 7, LocationUtils
				.getTripletStart(3));
	}

	public void testEndConversion() throws Exception {
		assertEquals("Expected triplet end", 3, LocationUtils.getTripletEnd(1));
		assertEquals("Expected triplet end", 6, LocationUtils.getTripletEnd(2));
		assertEquals("Expected triplet end", 9, LocationUtils.getTripletEnd(3));
	}

	public void testSimpleLocation1() throws Exception {
		String locStr = "1..9";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 9, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
		RichLocation protLoc = LocationUtils.getLocationForProteinFeature(locs,
				1, 3);
		assertEquals("Expected start", 1, protLoc.getMin());
		assertEquals("Expected end", 9, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				protLoc.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(protLoc));
		assertEquals("Expected number of inner locations", 1, LocationUtils
				.countInnerLocations(protLoc));

	}

	public void testSimpleLocation2() throws Exception {
		String locStr = "complement(1..9)";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 9, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
		RichLocation protLoc = LocationUtils.getLocationForProteinFeature(locs,
				1, 3);
		assertEquals("Expected start", 1, protLoc.getMin());
		assertEquals("Expected end", 9, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				protLoc.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(protLoc));
		assertEquals("Expected number of inner locations", 1, LocationUtils
				.countInnerLocations(protLoc));

	}

	public void testSimpleLocation3() throws Exception {
		String locStr = "1..9";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 9, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
		RichLocation protLoc = LocationUtils.getLocationForProteinFeature(locs,
				1, 2);
		assertEquals("Expected start", 1, protLoc.getMin());
		assertEquals("Expected end", 6, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				protLoc.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(protLoc));
		assertEquals("Expected number of inner locations", 1, LocationUtils
				.countInnerLocations(protLoc));

	}

	public void testSimpleLocation4() throws Exception {
		String locStr = "1..9";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 9, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
		RichLocation protLoc = LocationUtils.getLocationForProteinFeature(locs,
				2, 3);
		assertEquals("Expected start", 4, protLoc.getMin());
		assertEquals("Expected end", 9, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				protLoc.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(protLoc));
		assertEquals("Expected number of inner locations", 1, LocationUtils
				.countInnerLocations(protLoc));

	}

	public void testSimpleLocation5() throws Exception {
		String locStr = "complement(1..9)";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 9, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
		RichLocation protLoc = LocationUtils.getLocationForProteinFeature(locs,
				1, 2);
		assertEquals("Expected start", 4, protLoc.getMin());
		assertEquals("Expected end", 9, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				protLoc.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(protLoc));
		assertEquals("Expected number of inner locations", 1, LocationUtils
				.countInnerLocations(protLoc));

	}

	public void testSimpleLocation6() throws Exception {
		String locStr = "complement(1..9)";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 9, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
		RichLocation protLoc = LocationUtils.getLocationForProteinFeature(locs,
				2, 3);
		assertEquals("Expected start", 1, protLoc.getMin());
		assertEquals("Expected end", 6, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				protLoc.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(protLoc));
		assertEquals("Expected number of inner locations", 1, LocationUtils
				.countInnerLocations(protLoc));

	}

	public void testSimpleLocationDL() throws Exception {
		int start = 30;
		int offset = 320;
		RichLocation ref = new SimpleRichLocation(new SimplePosition(start),
				new SimplePosition((start + offset - 1)), 1,
				RichLocation.Strand.NEGATIVE_STRAND);
		assertEquals("Expected start", 30, ref.getMin());
		assertEquals("Expected end", 349, ref.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				ref.getStrand());
		RichLocation protLoc = LocationUtils.getLocationForProteinFeature(ref,
				1, 100);
		System.out.println(protLoc);
		assertEquals("Expected start", 50, protLoc.getMin());
		assertEquals("Expected end", 349, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				ref.getStrand());
	}

	public void testCompoundLocation1() throws Exception {
		String locStr = "join(1..6,8..10)";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 10, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
		RichLocation protLoc = LocationUtils.getLocationForProteinFeature(locs,
				1, 3);
		assertEquals("Expected start", 1, protLoc.getMin());
		assertEquals("Expected end", 10, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				protLoc.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(protLoc));
		assertEquals("Expected number of inner locations", 2, LocationUtils
				.countInnerLocations(protLoc));
	}

	public void testCompoundLocation2() throws Exception {
		String locStr = "complement(join(1..6,8..10))";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 10, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
		RichLocation protLoc = LocationUtils.getLocationForProteinFeature(locs,
				1, 3);
		assertEquals("Expected start", 1, protLoc.getMin());
		assertEquals("Expected end", 10, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				protLoc.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(protLoc));
		assertEquals("Expected number of inner locations", 2, LocationUtils
				.countInnerLocations(protLoc));

	}

	public void testCompoundLocation3() throws Exception {

		String locStr = "join(1..6,8..10)";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 10, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
		RichLocation protLoc = LocationUtils.getLocationForProteinFeature(locs,
				1, 2);
		assertEquals("Expected start", 1, protLoc.getMin());
		assertEquals("Expected end", 6, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				protLoc.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(protLoc));
		assertEquals("Expected number of inner locations", 1, LocationUtils
				.countInnerLocations(protLoc));

	}

	public void testCompoundLocation4() throws Exception {
		String locStr = "complement(join(1..6,8..10))";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 10, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
		RichLocation protLoc = LocationUtils.getLocationForProteinFeature(locs,
				2, 3);
		assertEquals("Expected start", 1, protLoc.getMin());
		assertEquals("Expected end", 6, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				protLoc.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(protLoc));
		assertEquals("Expected number of inner locations", 1, LocationUtils
				.countInnerLocations(protLoc));

	}

	public void testCompoundLocation5() throws Exception {
		String locStr = "complement(join(1..6,8..10))";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 10, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
		RichLocation protLoc = LocationUtils.getLocationForProteinFeature(locs,
				1, 2);
		assertEquals("Expected start", 4, protLoc.getMin());
		assertEquals("Expected end", 10, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				protLoc.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(protLoc));
		assertEquals("Expected number of inner locations", 2, LocationUtils
				.countInnerLocations(protLoc));
	}

	public void testCompoundLocation6() throws Exception {
		String locStr = "complement(join(1..6,8..10))";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 10, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
		RichLocation protLoc = LocationUtils.getLocationForProteinFeature(locs,
				2, 3);
		assertEquals("Expected start", 1, protLoc.getMin());
		assertEquals("Expected end", 6, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				protLoc.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(protLoc));
		assertEquals("Expected number of inner locations", 1, LocationUtils
				.countInnerLocations(protLoc));
	}

	public void testCompoundLocation1a() throws Exception {
		String locStr = "join(1..5,7..10)";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 10, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
		RichLocation protLoc = LocationUtils.getLocationForProteinFeature(locs,
				1, 3);
		assertEquals("Expected start", 1, protLoc.getMin());
		assertEquals("Expected end", 10, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				protLoc.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(protLoc));
		assertEquals("Expected number of inner locations", 2, LocationUtils
				.countInnerLocations(protLoc));
	}

	public void testCompoundLocation2a() throws Exception {
		String locStr = "complement(join(1..5,7..10))";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 10, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
		RichLocation protLoc = LocationUtils.getLocationForProteinFeature(locs,
				1, 3);
		assertEquals("Expected start", 1, protLoc.getMin());
		assertEquals("Expected end", 10, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				protLoc.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(protLoc));
		assertEquals("Expected number of inner locations", 2, LocationUtils
				.countInnerLocations(protLoc));
	}

	public void testCompoundLocation3a() throws Exception {
		String locStr = "join(1..5,7..10)";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 10, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
		RichLocation protLoc = LocationUtils.getLocationForProteinFeature(locs,
				1, 2);
		assertEquals("Expected start", 1, protLoc.getMin());
		assertEquals("Expected end", 7, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				protLoc.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(protLoc));
		assertEquals("Expected number of inner locations", 2, LocationUtils
				.countInnerLocations(protLoc));
	}

	public void testCompoundLocation4a() throws Exception {
		String locStr = "complement(join(1..5,7..10))";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 10, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
		RichLocation protLoc = LocationUtils.getLocationForProteinFeature(locs,
				2, 3);
		assertEquals("Expected start", 1, protLoc.getMin());
		assertEquals("Expected end", 7, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				protLoc.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(protLoc));
		assertEquals("Expected number of inner locations", 2, LocationUtils
				.countInnerLocations(protLoc));

	}

	public void testCompoundLocation5a() throws Exception {
		String locStr = "complement(join(1..5,7..10))";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 10, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
		RichLocation protLoc = LocationUtils.getLocationForProteinFeature(locs,
				1, 2);
		assertEquals("Expected start", 4, protLoc.getMin());
		assertEquals("Expected end", 10, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				protLoc.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(protLoc));
		assertEquals("Expected number of inner locations", 2, LocationUtils
				.countInnerLocations(protLoc));

	}

	public void testCompoundLocation6a() throws Exception {
		String locStr = "complement(join(1..5,7..10))";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 10, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
		RichLocation protLoc = LocationUtils.getLocationForProteinFeature(locs,
				2, 3);
		assertEquals("Expected start", 1, protLoc.getMin());
		assertEquals("Expected end", 7, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				protLoc.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(protLoc));
		assertEquals("Expected number of inner locations", 2, LocationUtils
				.countInnerLocations(protLoc));
	}

	public void testCompoundLocation1b() throws Exception {
		String locStr = "join(1..5,7..10,12..14)";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 14, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
		RichLocation protLoc = LocationUtils.getLocationForProteinFeature(locs,
				1, 4);
		assertEquals("Expected start", 1, protLoc.getMin());
		assertEquals("Expected end", 14, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				protLoc.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(protLoc));
		assertEquals("Expected number of inner locations", 3, LocationUtils
				.countInnerLocations(protLoc));

	}

	public void testCompoundLocation2b() throws Exception {
		String locStr = "complement(join(1..5,7..10,12..14))";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 14, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
		RichLocation protLoc = LocationUtils.getLocationForProteinFeature(locs,
				1, 4);
		assertEquals("Expected start", 1, protLoc.getMin());
		assertEquals("Expected end", 14, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				protLoc.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(protLoc));
		assertEquals("Expected number of inner locations", 3, LocationUtils
				.countInnerLocations(protLoc));

	}

	public void testCompoundLocation3b() throws Exception {
		String locStr = "join(1..5,7..10,12..14)";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 14, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
		RichLocation protLoc = LocationUtils.getLocationForProteinFeature(locs,
				1, 2);
		assertEquals("Expected start", 1, protLoc.getMin());
		assertEquals("Expected end", 7, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				protLoc.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(protLoc));
		assertEquals("Expected number of inner locations", 2, LocationUtils
				.countInnerLocations(protLoc));
	}

	public void testCompoundLocation4b() throws Exception {
		String locStr = "complement(join(1..5,7..10,12..14))";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 14, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
		RichLocation protLoc = LocationUtils.getLocationForProteinFeature(locs,
				2, 3);
		assertEquals("Expected start", 4, protLoc.getMin());
		assertEquals("Expected end", 10, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				protLoc.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(protLoc));
		assertEquals("Expected number of inner locations", 2, LocationUtils
				.countInnerLocations(protLoc));

	}

	public void testCompoundLocation5b() throws Exception {
		String locStr = "complement(join(1..5,7..10,12..14))";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 14, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
		RichLocation protLoc = LocationUtils.getLocationForProteinFeature(locs,
				1, 2);
		assertEquals("Expected start", 8, protLoc.getMin());
		assertEquals("Expected end", 14, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				protLoc.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(protLoc));
		assertEquals("Expected number of inner locations", 2, LocationUtils
				.countInnerLocations(protLoc));

	}

	public void testCompoundLocation6b() throws Exception {
		String locStr = "complement(join(1..5,7..10,12..14))";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 14, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
		RichLocation protLoc = LocationUtils.getLocationForProteinFeature(locs,
				2, 3);
		assertEquals("Expected start", 4, protLoc.getMin());
		assertEquals("Expected end", 10, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				protLoc.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(protLoc));
		assertEquals("Expected number of inner locations", 2, LocationUtils
				.countInnerLocations(protLoc));
	}

	public void testCompoundLocation7b() throws Exception {
		String locStr = "complement(join(1..5,7..10,12..14))";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 14, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
		RichLocation protLoc = LocationUtils.getLocationForProteinFeature(locs,
				1, 3);
		assertEquals("Expected start", 4, protLoc.getMin());
		assertEquals("Expected end", 14, protLoc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				protLoc.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(protLoc));
		assertEquals("Expected number of inner locations", 3, LocationUtils
				.countInnerLocations(protLoc));
	}

	// public void testLocationOutOfBounds1() throws Exception {
	// String locStr = "join(1..6)";
	// RichLocation locs = LocationUtils.parseEmblLocation(locStr);
	// assertEquals("Expected start", 1, locs.getMin());
	// assertEquals("Expected end", 6, locs.getMax());
	// assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
	// locs.getStrand());
	// assertFalse("Expected unfuzzy location", LocationUtils
	// .isLocationFuzzy(locs));
	// try {
	// LocationUtils.getLocationForProteinFeature(locs, 1, 3);
	// fail("Exception not raised");
	// } catch (FeatureLocationNotFoundException e) {
	// }
	// }
	//
	// public void testLocationOutOfBounds2() throws Exception {
	// String locStr = "complement(join(1..6))";
	// RichLocation locs = LocationUtils.parseEmblLocation(locStr);
	// assertEquals("Expected start", 1, locs.getMin());
	// assertEquals("Expected end", 6, locs.getMax());
	// assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
	// locs.getStrand());
	// assertFalse("Expected unfuzzy location", LocationUtils
	// .isLocationFuzzy(locs));
	// try {
	// LocationUtils.getLocationForProteinFeature(locs, 1, 3);
	// fail("Exception not raised");
	// } catch (FeatureLocationNotFoundException e) {
	// }
	// }
	//
	// public void testLocationOutOfBounds3() throws Exception {
	// String locStr = "join(1..3,5..7)";
	// RichLocation locs = LocationUtils.parseEmblLocation(locStr);
	// assertEquals("Expected start", 1, locs.getMin());
	// assertEquals("Expected end", 7, locs.getMax());
	// assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
	// locs.getStrand());
	// assertFalse("Expected unfuzzy location", LocationUtils
	// .isLocationFuzzy(locs));
	// try {
	// LocationUtils.getLocationForProteinFeature(locs, 1, 3);
	// fail("Exception not raised");
	// } catch (FeatureLocationNotFoundException e) {
	// }
	// }
	//
	// public void testLocationOutOfBounds4() throws Exception {
	// String locStr = "complement(join(1..3,5..7))";
	// RichLocation locs = LocationUtils.parseEmblLocation(locStr);
	// assertEquals("Expected start", 1, locs.getMin());
	// assertEquals("Expected end", 7, locs.getMax());
	// assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
	// locs.getStrand());
	// assertFalse("Expected unfuzzy location", LocationUtils
	// .isLocationFuzzy(locs));
	// try {
	// LocationUtils.getLocationForProteinFeature(locs, 1, 3);
	// fail("Exception not raised");
	// } catch (FeatureLocationNotFoundException e) {
	// }
	// }

}
