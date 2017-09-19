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
public class LocationOuterIdentityTest extends TestCase {

	/**
	 * @param name
	 */
	public LocationOuterIdentityTest(String name) {
		super(name);
	}

	public void testSimpleOverlap1() throws Exception {
		RichLocation location1 = LocationUtils
				.buildSimpleLocation(1, 10, 100, false);
		RichLocation location2 = LocationUtils
				.buildSimpleLocation(1, 10, 100, false);
		assertTrue("Expected overlap", LocationUtils.outerOverlapsInFrame(location1, location2));
		assertTrue("Expected overlap", LocationUtils.overlapsInFrame(location1, location2));
	}

	public void testSimpleOverlap2() throws Exception {
		RichLocation location1 = LocationUtils
				.buildSimpleLocation(1, 10, 100, false);
		RichLocation location2 = LocationUtils
				.buildSimpleLocation(2, 10, 100, false);
		assertFalse("Expected no overlap", LocationUtils.outerOverlapsInFrame(location1, location2));
		assertFalse("Expected no overlap", LocationUtils.overlapsInFrame(location1, location2));
	}

	public void testSimpleOverlap3() throws Exception {
		RichLocation location1 = LocationUtils
				.buildSimpleLocation(1, 10, 100, false);
		RichLocation location2 = LocationUtils
				.buildSimpleLocation(3, 10, 100, false);
		assertFalse("Expected no overlap", LocationUtils.outerOverlapsInFrame(location1, location2));
		assertFalse("Expected no overlap", LocationUtils.overlapsInFrame(location1, location2));
	}

	public void testSimpleOverlap4() throws Exception {
		RichLocation location1 = LocationUtils
				.buildSimpleLocation(1, 10, 100, false);
		RichLocation location2 = LocationUtils
				.buildSimpleLocation(4, 10, 100, false);
		assertTrue("Expected overlap", LocationUtils.outerOverlapsInFrame(location1, location2));
		assertTrue("Expected overlap", LocationUtils.overlapsInFrame(location1, location2));
	}

	public void testSimpleOverlap5() throws Exception {
		RichLocation location1 = LocationUtils
				.buildSimpleLocation(1, 10, 100, false);
		RichLocation location2 = LocationUtils
				.buildSimpleLocation(1, 9, 100, false);
		assertTrue("Expected overlap", LocationUtils.outerOverlapsInFrame(location1, location2));
		assertTrue("Expected overlap", LocationUtils.overlapsInFrame(location1, location2));
	}

	public void testSimpleOverlap6() throws Exception {
		RichLocation location1 = LocationUtils
				.buildSimpleLocation(1, 10, 100, false);
		RichLocation location2 = LocationUtils
				.buildSimpleLocation(2, 9, 100, false);
		assertFalse("Expected no overlap", LocationUtils.outerOverlapsInFrame(location1, location2));
		assertFalse("Expected no overlap", LocationUtils.overlapsInFrame(location1, location2));
	}

	public void testSimpleOverlap7() throws Exception {
		RichLocation location1 = LocationUtils
				.buildSimpleLocation(1, 10, 100, false);
		RichLocation location2 = LocationUtils
				.buildSimpleLocation(3, 9, 100, false);
		assertFalse("Expected no overlap", LocationUtils.outerOverlapsInFrame(location1, location2));
		assertFalse("Expected no overlap", LocationUtils.overlapsInFrame(location1, location2));
	}

	public void testSimpleOverlap8() throws Exception {
		RichLocation location1 = LocationUtils
				.buildSimpleLocation(1, 10, 100, false);
		RichLocation location2 = LocationUtils
				.buildSimpleLocation(4, 9, 100, false);
		assertTrue("Expected overlap", LocationUtils.outerOverlapsInFrame(location1, location2));
		assertTrue("Expected overlap", LocationUtils.overlapsInFrame(location1, location2));
	}

	public void testSimpleOverlap9() throws Exception {
		RichLocation location1 = LocationUtils
				.buildSimpleLocation(1, 10, 100, false);
		RichLocation location2 = LocationUtils
				.buildSimpleLocation(1, 10, 100, true);
		assertFalse("Expected no overlap", LocationUtils.outerOverlapsInFrame(location1, location2));
		assertFalse("Expected no overlap", LocationUtils.overlapsInFrame(location1, location2));
	}

	public void testSimpleOverlap10() throws Exception {
		RichLocation location1 = LocationUtils
				.buildSimpleLocation(1, 10, 100, false);
		RichLocation location2 = LocationUtils
				.buildSimpleLocation(2, 10, 100, true);
		assertFalse("Expected no overlap", LocationUtils.outerOverlapsInFrame(location1, location2));
		assertFalse("Expected no overlap", LocationUtils.overlapsInFrame(location1, location2));
	}

	public void testSimpleOverlap11() throws Exception {
		RichLocation location1 = LocationUtils
				.buildSimpleLocation(1, 10, 100, false);
		RichLocation location2 = LocationUtils
				.buildSimpleLocation(3, 10, 100, true);
		assertFalse("Expected no overlap", LocationUtils.outerOverlapsInFrame(location1, location2));
		assertFalse("Expected no overlap", LocationUtils.overlapsInFrame(location1, location2));
	}

	public void testSimpleOverlap12() throws Exception {
		RichLocation location1 = LocationUtils
				.buildSimpleLocation(1, 10, 100, false);
		RichLocation location2 = LocationUtils
				.buildSimpleLocation(4, 10, 100, true);
		assertFalse("Expected no overlap", LocationUtils.outerOverlapsInFrame(location1, location2));
		assertFalse("Expected no overlap", LocationUtils.overlapsInFrame(location1, location2));
	}

}
