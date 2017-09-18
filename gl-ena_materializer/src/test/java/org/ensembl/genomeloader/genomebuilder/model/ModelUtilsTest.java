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
 * File: ModelUtilsTest.java
 * Created by: dstaines
 * Created on: Aug 21, 2009
 * CVS:  $$
 */
package org.ensembl.genomeloader.genomebuilder.model;

import static junit.framework.Assert.assertEquals;

import org.biojava.bio.seq.RNATools;
import org.ensembl.genomeloader.genomebuilder.model.EntityLocation;
import org.ensembl.genomeloader.genomebuilder.model.ModelUtils;
import org.ensembl.genomeloader.genomebuilder.model.impl.DelegatingEntityLocation;
import org.ensembl.genomeloader.genomebuilder.model.sequence.Sequence;
import org.ensembl.genomeloader.util.biojava.LocationUtils;
import org.junit.Test;

/**
 * @author dstaines
 * 
 */
public class ModelUtilsTest {

	@Test
	public void testAdjustOverlap() {
		final EntityLocation loc = new DelegatingEntityLocation(LocationUtils.parseEmblLocation("1..6,6..11"));
		final Sequence seq = new Sequence("ATGATGATGAT");
		final EntityLocation newLoc = ModelUtils.resolveOverlap(seq, RNATools.getGeneticCode(11), loc);
		System.out.println(newLoc);
		assertEquals("join(1..6,9..11)",LocationUtils.locationToEmblFormat(newLoc));
		assertEquals(1,newLoc.getInsertions().size());
		assertEquals("MMDD",ModelUtils.getTranslation(seq, newLoc, 11));
	}

	@Test
	public void testAdjustNonTripletOverlap() {
		final EntityLocation loc = new DelegatingEntityLocation(LocationUtils.parseEmblLocation("1..7,7..11"));
		final Sequence seq = new Sequence("ATGATGATGAT");
		final EntityLocation newLoc = ModelUtils.resolveOverlap(seq, RNATools.getGeneticCode(11), loc);
		System.out.println(newLoc);
		assertEquals("join(1..6,9..11)",LocationUtils.locationToEmblFormat(newLoc));
		assertEquals(1,newLoc.getInsertions().size());
		assertEquals("MMND",ModelUtils.getTranslation(seq, newLoc, 11));
	}

	@Test
	public void testAdjustOverlapC() {
		final EntityLocation loc = new DelegatingEntityLocation(LocationUtils.parseEmblLocation("complement(join(2..7,7..12))"));
		final Sequence seq = new Sequence("CATCATCATCAT");
		final EntityLocation newLoc = ModelUtils.resolveOverlap(seq, RNATools.getGeneticCode(11), loc);
		System.out.println(newLoc);
		assertEquals("complement(join(2..4,7..12))",LocationUtils.locationToEmblFormat(newLoc));
		assertEquals(1,newLoc.getInsertions().size());
		assertEquals("MMDD",ModelUtils.getTranslation(seq, newLoc, 11));
	}

	@Test
	public void testAdjustNonTripletOverlapC() {
		final EntityLocation loc = new DelegatingEntityLocation(LocationUtils.parseEmblLocation("complement(join(2..6,6..12))"));
		final Sequence seq = new Sequence("CATCATCATCAT");
		final EntityLocation newLoc = ModelUtils.resolveOverlap(seq, RNATools.getGeneticCode(11), loc);
		System.out.println(newLoc);
		assertEquals("complement(join(2..4,7..12))",LocationUtils.locationToEmblFormat(newLoc));
		assertEquals(1,newLoc.getInsertions().size());
		assertEquals("MMND",ModelUtils.getTranslation(seq, newLoc, 11));
	}
	
	@Test
	public void testAdjustOverlapLong() {
		final EntityLocation loc = new DelegatingEntityLocation(LocationUtils.parseEmblLocation("1..9,6..14"));
		final Sequence seq = new Sequence("ATGATGATGATGAT");
		final EntityLocation newLoc = ModelUtils.resolveOverlap(seq, RNATools.getGeneticCode(11), loc);
		System.out.println(newLoc);
		assertEquals("join(1..9,12..14)",LocationUtils.locationToEmblFormat(newLoc));
		assertEquals(1,newLoc.getInsertions().size());
		assertEquals("MMMDDD",ModelUtils.getTranslation(seq, newLoc, 11));
	}
	
	@Test
	public void testAdjustOverlapInternal() {
		final EntityLocation loc = new DelegatingEntityLocation(LocationUtils.parseEmblLocation("1..9,4..6"));
		final Sequence seq = new Sequence("ATGATGATGATGAT");
		final EntityLocation newLoc = ModelUtils.resolveOverlap(seq, RNATools.getGeneticCode(11), loc);
		System.out.println(newLoc);
		assertEquals("1..9",LocationUtils.locationToEmblFormat(newLoc));
		assertEquals(1,newLoc.getInsertions().size());
		assertEquals("MMMM",ModelUtils.getTranslation(seq, newLoc, 11));
	}

	@Test
	public void testAdjustOverlapInternal2() {
		final EntityLocation loc = new DelegatingEntityLocation(LocationUtils.parseEmblLocation("1..8,4..7"));
		final Sequence seq = new Sequence("ATGATGATGATGAT");
		final EntityLocation newLoc = ModelUtils.resolveOverlap(seq, RNATools.getGeneticCode(11), loc);
		System.out.println(newLoc);
		assertEquals("1..6",LocationUtils.locationToEmblFormat(newLoc));
		assertEquals(1,newLoc.getInsertions().size());
		assertEquals("MMI*",ModelUtils.getTranslation(seq, newLoc, 11));
	}
	
	@Test
	public void testAdjustOverlapMultiple() {
		final EntityLocation loc = new DelegatingEntityLocation(LocationUtils.parseEmblLocation("join(1..3,7..9,9..14)"));
		final Sequence seq = new Sequence("ATGATGATGATGATGATGATG");
		final EntityLocation newLoc = ModelUtils.resolveOverlap(seq, RNATools.getGeneticCode(11), loc);
		System.out.println(newLoc);
		assertEquals("join(1..3,7..9,12..14)",LocationUtils.locationToEmblFormat(newLoc));
		assertEquals(1,newLoc.getInsertions().size());
		assertEquals("MMDD",ModelUtils.getTranslation(seq, newLoc, 11));
	}
	
	@Test
	public void testAdjustOverlapMultipleShort() {
		final EntityLocation loc = new DelegatingEntityLocation(LocationUtils.parseEmblLocation("join(1..3,3..5,5..7)"));
		final Sequence seq = new Sequence("ATGATGATGATGATGATGATG");
		final EntityLocation newLoc = ModelUtils.resolveOverlap(seq, RNATools.getGeneticCode(11), loc);
		System.out.println(newLoc);
		assertEquals("join(1..3,5..7)",LocationUtils.locationToEmblFormat(newLoc));
		assertEquals(1,newLoc.getInsertions().size());
		assertEquals("MD*",ModelUtils.getTranslation(seq, newLoc, 11));
	}

	@Test
	public void testAdjustOverlapMultipleShortC() {
		final EntityLocation loc = new DelegatingEntityLocation(LocationUtils.parseEmblLocation("complement(join(1..3,3..5,5..7))"));
		final Sequence seq = new Sequence("ATGATGATGATGATGATGATG");
		final EntityLocation newLoc = ModelUtils.resolveOverlap(seq, RNATools.getGeneticCode(11), loc);
		System.out.println(newLoc);
		assertEquals("complement(join(1..3,5..7))",LocationUtils.locationToEmblFormat(newLoc));
		assertEquals(1,newLoc.getInsertions().size());
		assertEquals("SIH",ModelUtils.getTranslation(seq, newLoc, 11));
	}

	@Test
	public void testAdjustOverlapReal() {
		// mimics 973271..974294,974293..974369
		final EntityLocation loc = new DelegatingEntityLocation(
				LocationUtils.parseEmblLocation("complement(join(1..4,3..7))"));
		final Sequence seq = new Sequence("ATGATGA");
		final EntityLocation newLoc = ModelUtils.resolveOverlap(seq,
				RNATools.getGeneticCode(11), loc);
		System.out.println(newLoc);
		assertEquals("complement(join(1..3,5..7))",
				LocationUtils.locationToEmblFormat(newLoc));
		assertEquals(1, newLoc.getInsertions().size());
		assertEquals("SSH", ModelUtils.getTranslation(seq, newLoc, 11));
	}

	@Test
	public void testAdjustNonCodingOverlap() {
		final EntityLocation loc = new DelegatingEntityLocation(
				LocationUtils.parseEmblLocation("join(1..4,3..7)"));
		final Sequence seq = new Sequence("ATGATGA");
		final EntityLocation newLoc = ModelUtils.resolveNoncodingOverlap(seq,
				loc);
		System.out.println(newLoc);
		assertEquals("join(1..4,5..7)",
				LocationUtils.locationToEmblFormat(newLoc));
		assertEquals(1, newLoc.getInsertions().size());
	}

	@Test
	public void testAdjustNonCodingOverlapEnclosed() {
		final EntityLocation loc = new DelegatingEntityLocation(
				LocationUtils.parseEmblLocation("join(1..2,1..4)"));
		final Sequence seq = new Sequence("ATGATGA");
		final EntityLocation newLoc = ModelUtils.resolveNoncodingOverlap(seq,
				loc);
		System.out.println(newLoc);
		assertEquals("join(1..2,3..4)",
				LocationUtils.locationToEmblFormat(newLoc));
		assertEquals(1, newLoc.getInsertions().size());
	}

	@Test
	public void testAdjustNonCodingOverlapEnclosed2() {
		final EntityLocation loc = new DelegatingEntityLocation(
				LocationUtils.parseEmblLocation("join(1..4,1..2)"));
		final Sequence seq = new Sequence("ATGATGA");
		final EntityLocation newLoc = ModelUtils.resolveNoncodingOverlap(seq,
				loc);
		System.out.println(newLoc);
		assertEquals("join(1..2,3..4)",
				LocationUtils.locationToEmblFormat(newLoc));
		assertEquals(1, newLoc.getInsertions().size());
	}
	
	@Test
	public void testAdjustNonCodingOverlapComplete() {
		final EntityLocation loc = new DelegatingEntityLocation(
				LocationUtils.parseEmblLocation("join(1..4,1..4)"));
		final Sequence seq = new Sequence("ATGATGA");
		final EntityLocation newLoc = ModelUtils.resolveNoncodingOverlap(seq,
				loc);
		System.out.println(newLoc);
		assertEquals("1..4",
				LocationUtils.locationToEmblFormat(newLoc));
		assertEquals(1, newLoc.getInsertions().size());
	}
	@Test
	public void testAdjustNonCodingOverlapCompleteComplement() {
		final EntityLocation loc = new DelegatingEntityLocation(
				LocationUtils.parseEmblLocation("complement(join(1..4,1..4))"));
		final Sequence seq = new Sequence("ATGATGA");
		final EntityLocation newLoc = ModelUtils.resolveNoncodingOverlap(seq,
				loc);
		System.out.println(newLoc);
		assertEquals("complement(1..4)",
				LocationUtils.locationToEmblFormat(newLoc));
		assertEquals(1, newLoc.getInsertions().size());
	}
}
