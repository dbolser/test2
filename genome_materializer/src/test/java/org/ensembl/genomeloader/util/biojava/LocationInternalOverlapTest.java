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
 * File: LocationInternalOverlapTest.java
 * Created by: dstaines
 * Created on: Aug 27, 2009
 * CVS:  $$
 */
package org.ensembl.genomeloader.util.biojava;

import org.biojavax.bio.seq.RichLocation;
import org.ensembl.genomeloader.util.biojava.LocationUtils;

import junit.framework.TestCase;

/**
 * @author dstaines
 *
 */
public class LocationInternalOverlapTest extends TestCase {

	public void testSimpleLocation() {
		RichLocation loc = LocationUtils.parseEmblLocation("1..10");
		assertFalse(loc+" does not overlap",LocationUtils.hasInternalOverlap(loc));
	}

	public void testSimpleComplementLocation() {
		RichLocation loc = LocationUtils.parseEmblLocation("complement(1..10)");
		assertFalse(loc+" does not overlap",LocationUtils.hasInternalOverlap(loc));
	}

	public void testJoinedLocation() {
		RichLocation loc = LocationUtils.parseEmblLocation("1..10,11..20");
		assertFalse(loc+" does not overlap",LocationUtils.hasInternalOverlap(loc));
	}
	public void testJoinedComplementLocation() {
		RichLocation loc = LocationUtils.parseEmblLocation("complement(1..10,11..20)");
		assertFalse(loc+" does not overlap",LocationUtils.hasInternalOverlap(loc));
	}
	public void testJoinedOverlapLocation() {
		RichLocation loc = LocationUtils.parseEmblLocation("1..10,10..20");
		assertTrue(loc+" does not overlap",LocationUtils.hasInternalOverlap(loc));
	}
	public void testJoinedOverlapComplementLocation() {
		RichLocation loc = LocationUtils.parseEmblLocation("complement(1..10,10..20)");
		assertTrue(loc+" does not overlap",LocationUtils.hasInternalOverlap(loc));
	}
	public void testJoinedOverlap2Location() {
		RichLocation loc = LocationUtils.parseEmblLocation("1..10,9..20");
		assertTrue(loc+" does not overlap",LocationUtils.hasInternalOverlap(loc));
	}
	public void testJoinedOverlap2ComplementLocation() {
		RichLocation loc = LocationUtils.parseEmblLocation("complement(1..10,9..20)");
		assertTrue(loc+" does not overlap",LocationUtils.hasInternalOverlap(loc));
	}

}
