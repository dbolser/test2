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
 * File: LocationOverlapTest.java
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
public class LocationOuterOverlapTest extends TestCase {

	/**
	 * @param name
	 */
	public LocationOuterOverlapTest(String name) {
		super(name);
	}

	public void testSimpleMatch1() throws Exception {
		RichLocation location1 = LocationUtils.buildSimpleLocation(1, 10, 100,
				false);
		RichLocation location2 = LocationUtils.buildSimpleLocation(1, 10, 100,
				false);
		assertTrue("Expected match", LocationUtils.outerCoordinatesEquals(
				location1, location2));
	}

	public void testSimpleMatch2() throws Exception {
		RichLocation location1 = LocationUtils.buildSimpleLocation(1, 10, 100,
				false);
		RichLocation location2 = LocationUtils.buildSimpleLocation(1, 9, 100,
				false);
		assertFalse("Expected no match", LocationUtils.outerCoordinatesEquals(
				location1, location2));
	}

	public void testSimpleMatch3() throws Exception {
		RichLocation location1 = LocationUtils.buildSimpleLocation(1, 10, 100,
				false);
		RichLocation location2 = LocationUtils.buildSimpleLocation(1, 10, 100,
				true);
		assertFalse("Expected no match", LocationUtils.outerCoordinatesEquals(
				location1, location2));
	}

	public void testComplexMatch1() throws Exception {
		RichLocation location1 = LocationUtils
				.parseEmblLocation("join(1..10,90..100)");
		RichLocation location2 = LocationUtils
				.parseEmblLocation("join(1..10,90..100)");
		assertTrue("Expected match", LocationUtils.outerCoordinatesEquals(
				location1, location2));
	}

	public void testComplexMatch2() throws Exception {
		RichLocation location1 = LocationUtils
				.parseEmblLocation("join(1..10,90..100)");
		RichLocation location2 = LocationUtils
				.parseEmblLocation("join(1..100)");
		assertTrue("Expected match", LocationUtils.outerCoordinatesEquals(
				location1, location2));
	}

	public void testComplexMatch3() throws Exception {
		RichLocation location1 = LocationUtils
				.parseEmblLocation("join(1..10,90..100)");
		RichLocation location2 = LocationUtils.parseEmblLocation("join(1..99)");
		assertFalse("Expected match", LocationUtils.outerCoordinatesEquals(
				location1, location2));
	}

	public void testComplexMatch4() throws Exception {
		RichLocation location1 = LocationUtils
				.parseEmblLocation("join(1..10,90..100)");
		RichLocation location2 = LocationUtils
				.parseEmblLocation("join(1..10,30..40,90..100)");
		assertTrue("Expected match", LocationUtils.outerCoordinatesEquals(
				location1, location2));
	}

	public void testSubsumes1() throws Exception {
		RichLocation location1 = LocationUtils
				.parseEmblLocation("join(1..100)");
		RichLocation location2 = LocationUtils
				.parseEmblLocation("join(20..30)");
		assertTrue("Expected match", LocationUtils.outerOverlaps(location1,
				location2));
	}

	public void testSubsumes2() throws Exception {
		RichLocation location1 = LocationUtils
				.parseEmblLocation("join(1..10,40..100)");
		RichLocation location2 = LocationUtils
				.parseEmblLocation("join(20..30)");
		assertTrue("Expected match", LocationUtils.outerOverlaps(location1,
				location2));
	}


}
