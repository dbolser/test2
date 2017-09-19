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
 * File: LocationModificationTest.java
 * Created by: dstaines
 * Created on: Nov 26, 2008
 * CVS:  $$
 */
package org.ensembl.genomeloader.util.biojava;

import org.biojavax.bio.seq.RichLocation;
import org.biojavax.bio.seq.RichLocation.Strand;

import junit.framework.TestCase;

/**
 * @author dstaines
 *
 */
public class LocationModificationTest extends TestCase {

	public void testShiftStartUp() {
		RichLocation loc = LocationUtils.parseEmblLocation("100..200");
		loc = LocationUtils.adjustLocationStart(loc, -3);
		assertEquals("Expected start", 103, loc.getMin());
		assertEquals("Expected end", 200, loc.getMax());
		assertEquals("Expected strand", Strand.POSITIVE_STRAND, loc.getStrand());
	}

	public void testShiftStartUpComp() {
		RichLocation loc = LocationUtils
				.parseEmblLocation("complement(100..200)");
		loc = LocationUtils.adjustLocationStart(loc, -3);
		assertEquals("Expected start", 100, loc.getMin());
		assertEquals("Expected end", 197, loc.getMax());
		assertEquals("Expected strand", Strand.NEGATIVE_STRAND, loc.getStrand());
	}

	public void testShiftStartDown() {
		RichLocation loc = LocationUtils.parseEmblLocation("100..200");
		loc = LocationUtils.adjustLocationStart(loc, 3);
		assertEquals("Expected start", 97, loc.getMin());
		assertEquals("Expected end", 200, loc.getMax());
		assertEquals("Expected strand", Strand.POSITIVE_STRAND, loc.getStrand());
	}

	public void testShiftStartDownComp() {
		RichLocation loc = LocationUtils
				.parseEmblLocation("complement(100..200)");
		loc = LocationUtils.adjustLocationStart(loc, 3);
		assertEquals("Expected start", 100, loc.getMin());
		assertEquals("Expected end", 203, loc.getMax());
		assertEquals("Expected strand", Strand.NEGATIVE_STRAND, loc.getStrand());
	}

	public void testShiftEndUp() {
		RichLocation loc = LocationUtils.parseEmblLocation("100..200");
		loc = LocationUtils.adjustLocationEnd(loc, -3);
		assertEquals("Expected start", 100, loc.getMin());
		assertEquals("Expected end", 197, loc.getMax());
		assertEquals("Expected strand", Strand.POSITIVE_STRAND, loc.getStrand());
	}

	public void testShiftEndUpComp() {
		RichLocation loc = LocationUtils
				.parseEmblLocation("complement(100..200)");
		loc = LocationUtils.adjustLocationEnd(loc, -3);
		assertEquals("Expected start", 103, loc.getMin());
		assertEquals("Expected end", 200, loc.getMax());
		assertEquals("Expected strand", Strand.NEGATIVE_STRAND, loc.getStrand());
	}

	public void testShiftEndDown() {
		RichLocation loc = LocationUtils.parseEmblLocation("100..200");
		loc = LocationUtils.adjustLocationEnd(loc, 3);
		assertEquals("Expected start", 100, loc.getMin());
		assertEquals("Expected end", 203, loc.getMax());
		assertEquals("Expected strand", Strand.POSITIVE_STRAND, loc.getStrand());
	}

	public void testShiftEndDownComp() {
		RichLocation loc = LocationUtils
				.parseEmblLocation("complement(100..200)");
		loc = LocationUtils.adjustLocationEnd(loc, 3);
		assertEquals("Expected start", 97, loc.getMin());
		assertEquals("Expected end", 200, loc.getMax());
		assertEquals("Expected strand", Strand.NEGATIVE_STRAND, loc.getStrand());
	}

}
