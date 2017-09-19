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
 * File: TestLocationParsing.java
 * Created by: dstaines
 * Created on: Oct 1, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.util.biojava;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.biojavax.bio.seq.RichLocation;

import junit.framework.TestCase;

/**
 * @author dstaines
 *
 */
public class LocationParsingTest extends TestCase {

	public void testListParseSimplePoint() throws Exception {
		String locStr = "join(1)";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 1, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));

		Iterator<RichLocation> locIter = locs.blockIterator();
		RichLocation loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 1, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertEquals("Expected EMBL format", "1", LocationUtils
				.locationToEmblFormat(locs));
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(loc));
	}

	public void testListParseSimpleSingle() throws Exception {
		String locStr = "join(1..3)";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 3, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));

		Iterator<RichLocation> locIter = locs.blockIterator();
		RichLocation loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 3, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertEquals("Expected EMBL format", "1..3", LocationUtils
				.locationToEmblFormat(locs));
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(loc));
	}

	public void testListParseSimple() throws Exception {
		String locStr = "join(123..456)";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 123, locs.getMin());
		assertEquals("Expected end", 456, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));

		Iterator<RichLocation> locIter = locs.blockIterator();
		RichLocation loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 123, locs.getMin());
		assertEquals("Expected end", 456, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertEquals("Expected EMBL format", "123..456", LocationUtils
				.locationToEmblFormat(locs));
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(loc));
	}

	public void testListParseSimpleComplement() throws Exception {
		String locStr = "complement(join(123..456))";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 123, locs.getMin());
		assertEquals("Expected end", 456, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));

		Iterator<RichLocation> locIter = locs.blockIterator();
		RichLocation loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 123, loc.getMin());
		assertEquals("Expected end", 456, loc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertEquals("Expected EMBL format", "complement(123..456)",
				LocationUtils.locationToEmblFormat(locs));
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(loc));
	}

	public void testListParseComplex() throws Exception {
		String locStr = "join(123..456,789..1000)";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 123, locs.getMin());
		assertEquals("Expected end", 1000, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));

		Iterator<RichLocation> locIter = locs.blockIterator();
		RichLocation loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 123, loc.getMin());
		assertEquals("Expected end", 456, loc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				loc.getStrand());
		loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 789, loc.getMin());
		assertEquals("Expected end", 1000, loc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				loc.getStrand());
		assertEquals("Expected EMBL format", locStr, LocationUtils
				.locationToEmblFormat(locs));
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(loc));
	}

	public void testListParseComplexComplement() throws Exception {
		String locStr = "complement(join(123..456,789..1000))";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 123, locs.getMin());
		assertEquals("Expected end", 1000, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		Iterator<RichLocation> locIter = locs.blockIterator();
		RichLocation loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 123, loc.getMin());
		assertEquals("Expected end", 456, loc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				loc.getStrand());
		loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 789, loc.getMin());
		assertEquals("Expected end", 1000, loc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				loc.getStrand());
		assertEquals("Expected EMBL format", locStr, LocationUtils
				.locationToEmblFormat(locs));
	}

	public void testListParseComplexComplementAlt() throws Exception {
		String locStr = "complement(join(123..456,complement(789..1000)))";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 123, locs.getMin());
		assertEquals("Expected end", 1000, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.UNKNOWN_STRAND,
				locs.getStrand());
		Iterator<RichLocation> locIter = locs.blockIterator();
		RichLocation loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 123, loc.getMin());
		assertEquals("Expected end", 456, loc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				loc.getStrand());
		loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 789, loc.getMin());
		assertEquals("Expected end", 1000, loc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				loc.getStrand());
	}

	public void testListParseComplexMixed() throws Exception {
		String locStr = "join(123..456,complement(789..1000))";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 123, locs.getMin());
		assertEquals("Expected end", 1000, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.UNKNOWN_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));

		Iterator<RichLocation> locIter = locs.blockIterator();
		RichLocation loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 123, loc.getMin());
		assertEquals("Expected end", 456, loc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				loc.getStrand());
		loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 789, loc.getMin());
		assertEquals("Expected end", 1000, loc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				loc.getStrand());
		assertEquals("Expected EMBL format", locStr, LocationUtils
				.locationToEmblFormat(locs));
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(loc));

	}

	public void testRealTransplicedExample() {
		String locStr = "join(11024..11409,complement(239890..240081),complement(241499..241580),complement(251354..251412),complement(315036..315294))";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 11024, locs.getMin());
		assertEquals("Expected end", 315294, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.UNKNOWN_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));

		Iterator<RichLocation> locIter = locs.blockIterator();
		RichLocation loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 11024, loc.getMin());
		assertEquals("Expected end", 11409, loc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				loc.getStrand());
		loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 239890, loc.getMin());
		assertEquals("Expected end", 240081, loc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				loc.getStrand());
		assertEquals("Expected EMBL format", locStr, LocationUtils
				.locationToEmblFormat(locs));
		assertFalse(StringUtils.isEmpty(locs.toString()));
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(loc));

	}

	public void testRealComplementExample() {
		String locStr = "complement(join(3371316..3371723))";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 3371316, locs.getMin());
		assertEquals("Expected end", 3371723, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));
	}

	public void testListParseSimplePointNoJoin() throws Exception {
		String locStr = "1";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 1, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));

		Iterator<RichLocation> locIter = locs.blockIterator();
		RichLocation loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 1, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertEquals("Expected EMBL format", locStr, LocationUtils
				.locationToEmblFormat(locs));
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(loc));
	}

	public void testListParseSimpleSingleNoJoin() throws Exception {
		String locStr = "1..3";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 3, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));

		Iterator<RichLocation> locIter = locs.blockIterator();
		RichLocation loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 3, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertEquals("Expected EMBL format", locStr, LocationUtils
				.locationToEmblFormat(locs));
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(loc));
	}

	public void testListParseSimpleNoJoin() throws Exception {
		String locStr = "123..456";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 123, locs.getMin());
		assertEquals("Expected end", 456, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));

		Iterator<RichLocation> locIter = locs.blockIterator();
		RichLocation loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 123, locs.getMin());
		assertEquals("Expected end", 456, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertEquals("Expected EMBL format", locStr, LocationUtils
				.locationToEmblFormat(locs));
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(loc));
	}

	public void testListParseSimpleComplementNoJoin() throws Exception {
		String locStr = "complement(123..456)";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 123, locs.getMin());
		assertEquals("Expected end", 456, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));

		Iterator<RichLocation> locIter = locs.blockIterator();
		RichLocation loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 123, loc.getMin());
		assertEquals("Expected end", 456, loc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertEquals("Expected EMBL format", locStr, LocationUtils
				.locationToEmblFormat(locs));
		assertFalse("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(loc));
	}

	public void testListParseSimpleNoJoinFuzzy() throws Exception {
		String locStr = "<123..>456";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 123, locs.getMin());
		assertEquals("Expected end", 456, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertTrue("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(locs));

		Iterator<RichLocation> locIter = locs.blockIterator();
		RichLocation loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 123, locs.getMin());
		assertEquals("Expected end", 456, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertEquals("Expected EMBL format", locStr, LocationUtils
				.locationToEmblFormat(locs));
		assertTrue("Expected unfuzzy location", LocationUtils
				.isLocationFuzzy(loc));
		assertTrue("Expected fuzzy start", loc.getMinPosition().getFuzzyStart());
		assertTrue("Expected fuzzy end", loc.getMaxPosition().getFuzzyEnd());
	}

	public void testListParseSimpleComplementNoJoinFuzzy() throws Exception {
		String locStr = "complement(<123..>456)";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 123, locs.getMin());
		assertEquals("Expected end", 456, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertTrue("Expected fuzzy location", LocationUtils
				.isLocationFuzzy(locs));

		Iterator<RichLocation> locIter = locs.blockIterator();
		RichLocation loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 123, loc.getMin());
		assertEquals("Expected end", 456, loc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertEquals("Expected EMBL format", locStr, LocationUtils
				.locationToEmblFormat(locs));
		assertTrue("Expected fuzzy location", LocationUtils
				.isLocationFuzzy(loc));
		assertTrue("Expected fuzzy start", loc.getMinPosition().getFuzzyStart());
		assertTrue("Expected fuzzy end", loc.getMaxPosition().getFuzzyEnd());
	}
	
	public void testSingleFuzzy() throws Exception {
		String locStr = "join(1029815..1030156,1030689..>1030793)";
		RichLocation loc = LocationUtils.parseEmblLocation(locStr);
		assertNotNull(loc);
		assertFalse(loc.getMinPosition().getFuzzyStart());
		assertFalse(loc.getMinPosition().getFuzzyEnd());
		assertFalse(loc.getMaxPosition().getFuzzyStart());
		assertTrue(loc.getMaxPosition().getFuzzyEnd());
		String cLocStr = "complement(join(1029815..1030156,1030689..>1030793))";
		RichLocation cLoc = LocationUtils.parseEmblLocation(cLocStr);
		assertNotNull(cLoc);
		assertFalse(loc.getMinPosition().getFuzzyStart());
		assertFalse(loc.getMinPosition().getFuzzyEnd());
		assertFalse(loc.getMaxPosition().getFuzzyStart());
		assertTrue(loc.getMaxPosition().getFuzzyEnd());
	}

	public void testStrand() throws Exception {
		RichLocation location1 = LocationUtils.parseEmblLocation("complement(join(1..9,11..16))");
		assertEquals(RichLocation.Strand.NEGATIVE_STRAND,location1.getStrand());
		for(RichLocation location: LocationUtils.sortLocation(location1)) {
			assertEquals(RichLocation.Strand.NEGATIVE_STRAND,location.getStrand());
		}
	}

	public void testCircularLocation() throws Exception {
		String locStr = "join(1737907..1738505,1..61)";
		RichLocation location1 = LocationUtils.parseEmblLocation(locStr);
		System.out.println(location1.getClass());
	}

}
