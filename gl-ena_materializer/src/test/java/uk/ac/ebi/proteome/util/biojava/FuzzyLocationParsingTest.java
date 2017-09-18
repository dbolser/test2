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
package uk.ac.ebi.proteome.util.biojava;

import java.util.Iterator;

import org.biojavax.bio.seq.RichLocation;

import junit.framework.TestCase;

/**
 * @author dstaines
 *
 */
public class FuzzyLocationParsingTest extends TestCase {

	public void testListParseSimplePoint() throws Exception {
		String locStr = "<1";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertTrue("Expected fuzzy start",locs.getMinPosition().getFuzzyStart());
		assertEquals("Expected end", 1, locs.getMax());
		assertFalse("Expected fuzzy end",locs.getMaxPosition().getFuzzyEnd());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertTrue("Expected fuzzy location",LocationUtils.isLocationFuzzy(locs));
		Iterator<RichLocation> locIter = locs.blockIterator();
		RichLocation loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 1, locs.getMin());
		assertTrue("Expected fuzzy start",locs.getMinPosition().getFuzzyStart());
		assertEquals("Expected end", 1, locs.getMax());
		assertFalse("Expected fuzzy end",locs.getMaxPosition().getFuzzyEnd());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertEquals("Expected EMBL format", locStr, LocationUtils
				.locationToEmblFormat(locs));
		assertTrue("Expected fuzzy location",LocationUtils.isLocationFuzzy(loc));
	}


	public void testListParseSimpleSingle1() throws Exception {
		String locStr = "<1..3";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 3, locs.getMax());
		assertTrue("Expected fuzzy start",locs.getMinPosition().getFuzzyStart());
		assertFalse("Expected fuzzy end",locs.getMaxPosition().getFuzzyEnd());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertTrue("Expected fuzzy location",LocationUtils.isLocationFuzzy(locs));

		Iterator<RichLocation> locIter = locs.blockIterator();
		RichLocation loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 3, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertTrue("Expected fuzzy start",locs.getMinPosition().getFuzzyStart());
		assertFalse("Expected fuzzy end",locs.getMaxPosition().getFuzzyEnd());
		assertEquals("Expected EMBL format", locStr, LocationUtils
				.locationToEmblFormat(locs));
		assertTrue("Expected fuzzy location",LocationUtils.isLocationFuzzy(loc));

	}

	public void testListParseSimpleSingle2() throws Exception {
		String locStr = "1..>3";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 3, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected fuzzy start",locs.getMinPosition().getFuzzyStart());
		assertTrue("Expected fuzzy end",locs.getMaxPosition().getFuzzyEnd());
		assertTrue("Expected fuzzy location",LocationUtils.isLocationFuzzy(locs));

		Iterator<RichLocation> locIter = locs.blockIterator();
		RichLocation loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 3, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertEquals("Expected EMBL format", locStr, LocationUtils
				.locationToEmblFormat(locs));
		assertFalse("Expected fuzzy start",locs.getMinPosition().getFuzzyStart());
		assertTrue("Expected fuzzy end",locs.getMaxPosition().getFuzzyEnd());
		assertTrue("Expected fuzzy location",LocationUtils.isLocationFuzzy(locs));

	}

	public void testListParseSimpleSingle3() throws Exception {
		String locStr = "<1..>3";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 3, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertTrue("Expected fuzzy start",locs.getMinPosition().getFuzzyStart());
		assertTrue("Expected fuzzy end",locs.getMaxPosition().getFuzzyEnd());
		assertTrue("Expected fuzzy location",LocationUtils.isLocationFuzzy(locs));

		Iterator<RichLocation> locIter = locs.blockIterator();
		RichLocation loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 3, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertEquals("Expected EMBL format", locStr, LocationUtils
				.locationToEmblFormat(locs));
		assertTrue("Expected fuzzy start",locs.getMinPosition().getFuzzyStart());
		assertTrue("Expected fuzzy end",locs.getMaxPosition().getFuzzyEnd());
		assertTrue("Expected fuzzy location",LocationUtils.isLocationFuzzy(loc));

	}

	public void testListParseSimplePointC() throws Exception {
		String locStr = "complement(<1)";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertTrue("Expected fuzzy start",locs.getMinPosition().getFuzzyStart());
		assertEquals("Expected end", 1, locs.getMax());
		assertFalse("Expected fuzzy end",locs.getMaxPosition().getFuzzyEnd());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertTrue("Expected fuzzy location",LocationUtils.isLocationFuzzy(locs));

		Iterator<RichLocation> locIter = locs.blockIterator();
		RichLocation loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 1, locs.getMin());
		assertTrue("Expected fuzzy start",locs.getMinPosition().getFuzzyStart());
		assertEquals("Expected end", 1, locs.getMax());
		assertFalse("Expected fuzzy end",locs.getMaxPosition().getFuzzyEnd());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertEquals("Expected EMBL format", locStr, LocationUtils
				.locationToEmblFormat(locs));
		assertTrue("Expected fuzzy location",LocationUtils.isLocationFuzzy(locs));

	}


	public void testListParseSimpleSingle1C() throws Exception {
		String locStr = "complement(<1..3)";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 3, locs.getMax());
		assertTrue("Expected fuzzy start",locs.getMinPosition().getFuzzyStart());
		assertFalse("Expected fuzzy end",locs.getMaxPosition().getFuzzyEnd());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertTrue("Expected fuzzy location",LocationUtils.isLocationFuzzy(locs));

		Iterator<RichLocation> locIter = locs.blockIterator();
		RichLocation loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 3, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertTrue("Expected fuzzy start",locs.getMinPosition().getFuzzyStart());
		assertFalse("Expected fuzzy end",locs.getMaxPosition().getFuzzyEnd());
		assertEquals("Expected EMBL format", locStr, LocationUtils
				.locationToEmblFormat(locs));
		assertTrue("Expected fuzzy location",LocationUtils.isLocationFuzzy(loc));

	}

	public void testListParseSimpleSingle2C() throws Exception {
		String locStr = "complement(1..>3)";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 3, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected fuzzy start",locs.getMinPosition().getFuzzyStart());
		assertTrue("Expected fuzzy end",locs.getMaxPosition().getFuzzyEnd());
		assertTrue("Expected fuzzy location",LocationUtils.isLocationFuzzy(locs));

		Iterator<RichLocation> locIter = locs.blockIterator();
		RichLocation loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 3, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertEquals("Expected EMBL format", locStr, LocationUtils
				.locationToEmblFormat(locs));
		assertFalse("Expected fuzzy start",locs.getMinPosition().getFuzzyStart());
		assertTrue("Expected fuzzy end",locs.getMaxPosition().getFuzzyEnd());
		assertTrue("Expected fuzzy location",LocationUtils.isLocationFuzzy(loc));

	}

	public void testListParseSimpleSingle3C() throws Exception {
		String locStr = "complement(<1..>3)";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 3, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertTrue("Expected fuzzy start",locs.getMinPosition().getFuzzyStart());
		assertTrue("Expected fuzzy end",locs.getMaxPosition().getFuzzyEnd());
		assertTrue("Expected fuzzy location",LocationUtils.isLocationFuzzy(locs));
		Iterator<RichLocation> locIter = locs.blockIterator();
		RichLocation loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 1, locs.getMin());
		assertEquals("Expected end", 3, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.NEGATIVE_STRAND,
				locs.getStrand());
		assertEquals("Expected EMBL format", locStr, LocationUtils
				.locationToEmblFormat(locs));
		assertTrue("Expected fuzzy start",locs.getMinPosition().getFuzzyStart());
		assertTrue("Expected fuzzy end",locs.getMaxPosition().getFuzzyEnd());
		assertTrue("Expected fuzzy location",LocationUtils.isLocationFuzzy(loc));

	}

	public void testListParseComplex() throws Exception {
		String locStr = "join(<123..456,789..>1000)";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 123, locs.getMin());
		assertEquals("Expected end", 1000, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertTrue("Expected fuzzy start",locs.getMinPosition().getFuzzyStart());
		assertTrue("Expected fuzzy end",locs.getMaxPosition().getFuzzyEnd());
		assertTrue("Expected fuzzy location",LocationUtils.isLocationFuzzy(locs));

		Iterator<RichLocation> locIter = locs.blockIterator();
		RichLocation loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 123, loc.getMin());
		assertEquals("Expected end", 456, loc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				loc.getStrand());
		assertTrue("Expected fuzzy start",loc.getMinPosition().getFuzzyStart());
		assertFalse("Expected fuzzy end",loc.getMaxPosition().getFuzzyEnd());
		loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 789, loc.getMin());
		assertEquals("Expected end", 1000, loc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				loc.getStrand());
		assertEquals("Expected EMBL format", locStr, LocationUtils
				.locationToEmblFormat(locs));
		assertFalse("Expected fuzzy start",loc.getMinPosition().getFuzzyStart());
		assertTrue("Expected fuzzy end",loc.getMaxPosition().getFuzzyEnd());
		assertTrue("Expected fuzzy location",LocationUtils.isLocationFuzzy(loc));

	}

	public void testListParseComplex2() throws Exception {
		String locStr = "join(123..>456,<789..1000)";
		RichLocation locs = LocationUtils.parseEmblLocation(locStr);
		assertEquals("Expected start", 123, locs.getMin());
		assertEquals("Expected end", 1000, locs.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				locs.getStrand());
		assertFalse("Expected fuzzy start",locs.getMinPosition().getFuzzyStart());
		assertFalse("Expected fuzzy end",locs.getMaxPosition().getFuzzyEnd());
		assertTrue("Expected fuzzy location",LocationUtils.isLocationFuzzy(locs));

		Iterator<RichLocation> locIter = locs.blockIterator();
		RichLocation loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 123, loc.getMin());
		assertEquals("Expected end", 456, loc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				loc.getStrand());
		assertFalse("Expected fuzzy start",loc.getMinPosition().getFuzzyStart());
		assertTrue("Expected fuzzy end",loc.getMaxPosition().getFuzzyEnd());
		loc = locIter.next();
		assertNotNull(loc);
		assertEquals("Expected start", 789, loc.getMin());
		assertEquals("Expected end", 1000, loc.getMax());
		assertEquals("Expected strand", RichLocation.Strand.POSITIVE_STRAND,
				loc.getStrand());
		assertEquals("Expected EMBL format", locStr, LocationUtils
				.locationToEmblFormat(locs));
		assertTrue("Expected fuzzy start",loc.getMinPosition().getFuzzyStart());
		assertFalse("Expected fuzzy end",loc.getMaxPosition().getFuzzyEnd());
		assertTrue("Expected fuzzy location",LocationUtils.isLocationFuzzy(locs));

	}


}
