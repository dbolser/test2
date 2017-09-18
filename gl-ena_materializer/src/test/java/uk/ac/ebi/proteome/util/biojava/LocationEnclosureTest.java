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
 * File: LocationEnclosureTest.java
 * Created by: dstaines
 * Created on: Nov 10, 2008
 * CVS:  $$
 */
package uk.ac.ebi.proteome.util.biojava;

import org.biojavax.bio.seq.RichLocation;

import junit.framework.TestCase;

/**
 * @author dstaines
 *
 */
public class LocationEnclosureTest extends TestCase {

	public void testSimpleSuccess() {
		RichLocation loc1 = LocationUtils.parseEmblLocation("1..10");
		RichLocation loc2 = LocationUtils.parseEmblLocation("2..8");
		assertTrue(LocationUtils.encloses(loc1, loc2));
		assertFalse(LocationUtils.encloses(loc2, loc1));
	}

	public void testSimpleFail() {
		RichLocation loc1 = LocationUtils.parseEmblLocation("1..10");
		RichLocation loc2 = LocationUtils.parseEmblLocation("2..11");
		assertFalse(LocationUtils.encloses(loc1, loc2));
		assertFalse(LocationUtils.encloses(loc2, loc1));
	}

	public void testSimpleIdentical() {
		RichLocation loc1 = LocationUtils.parseEmblLocation("1..10");
		RichLocation loc2 = LocationUtils.parseEmblLocation("1..10");
		assertTrue(LocationUtils.encloses(loc1, loc2));
		assertTrue(LocationUtils.encloses(loc2, loc1));
	}

	public void testCompoundSuccess() {
		RichLocation loc1 = LocationUtils.parseEmblLocation("join(1..10,15..20)");
		RichLocation loc2 = LocationUtils.parseEmblLocation("join(2..10,15..18)");
		assertTrue(LocationUtils.encloses(loc1, loc2));
		assertFalse(LocationUtils.encloses(loc2, loc1));
	}

	public void testCompoundFail() {
		RichLocation loc1 = LocationUtils.parseEmblLocation("join(1..10,15..20)");
		RichLocation loc2 = LocationUtils.parseEmblLocation("join(2..11,16..21)");
		assertFalse(LocationUtils.encloses(loc1, loc2));
		assertFalse(LocationUtils.encloses(loc2, loc1));
	}

	public void testCompoundIdentical() {
		RichLocation loc1 = LocationUtils.parseEmblLocation("join(1..10,15..20)");
		RichLocation loc2 = LocationUtils.parseEmblLocation("join(1..10,15..20)");
		assertTrue(LocationUtils.encloses(loc1, loc2));
		assertTrue(LocationUtils.encloses(loc2, loc1));
	}

	public void testRealSuccess() {
		RichLocation loc1 = LocationUtils.parseEmblLocation("join(308485..308844,309288..310784,311439..312245)");
		RichLocation loc2 = LocationUtils.parseEmblLocation("join(309345..310784,311439..312245)");
		assertTrue(LocationUtils.encloses(loc1, loc2));
		assertFalse(LocationUtils.encloses(loc2, loc1));
	}



}
