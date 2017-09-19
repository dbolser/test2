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
 * File: BaseModelUtils.java
 * Created by: dstaines
 * Created on: Dec 2, 2008
 * CVS:  $$
 */
package org.ensembl.genomeloader.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biojava.bio.seq.RNATools;
import org.biojava.bio.symbol.TranslationTable;
import org.biojavax.bio.seq.RichLocation;
import org.biojavax.bio.seq.RichLocation.Strand;
import org.biojavax.bio.seq.SimplePosition;
import org.biojavax.bio.seq.SimpleRichLocation;
import org.ensembl.genomeloader.model.EntityLocation.MappingState;
import org.ensembl.genomeloader.model.impl.DelegatingEntityLocation;
import org.ensembl.genomeloader.model.sequence.Sequence;
import org.ensembl.genomeloader.model.sequence.SequenceTranslationException;
import org.ensembl.genomeloader.model.sequence.SequenceUtils;
import org.ensembl.genomeloader.services.ServiceUncheckedException;
import org.ensembl.genomeloader.util.biojava.FeatureLocationNotFoundException;
import org.ensembl.genomeloader.util.biojava.LocationException;
import org.ensembl.genomeloader.util.biojava.LocationUtils;
import org.ensembl.genomeloader.util.collections.CollectionUtils;

/**
 * @author dstaines
 * 
 */
public class ModelUtils {

	protected static Log log;

	protected static Log getLog() {
		if (log == null)
			log = LogFactory.getLog(ModelUtils.class);
		return log;
	}

	/**
	 * Tests if the collection of {@link Locatable} objects are on the same
	 * strand
	 * 
	 * @param locatables
	 * @return true if all are on the same strand
	 */
	public static boolean featuresSameStrand(
			Collection<? extends Locatable> locatables) {
		Strand s = null;
		boolean sameStrand = true;
		for (final Locatable loc : locatables) {
			if (s == null) {
				s = loc.getLocation().getStrand();
			} else if (loc.getLocation().getStrand() != s) {
				sameStrand = false;
				break;
			}
		}
		return sameStrand;
	}

	public static class MinMaxLocationComparator implements
			Comparator<EntityLocation> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(EntityLocation o1, EntityLocation o2) {
			if (o1.getMin() > o2.getMin()) {
				return 1;
			} else if (o1.getMin() < o2.getMin()) {
				return -1;
			} else {
				if (o1.getMax() > o2.getMax()) {
					return 1;
				} else if (o1.getMax() == o2.getMax()) {
					return 0;
				} else {
					return -1;
				}
			}
		}

	}

	public static boolean parseBoolean(String boole) {
		if ("Y".equals(boole)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Utility to compare whether two genes are the same based on their
	 * annotation
	 * 
	 * @param gene1
	 *            first gene to compare
	 * @param gene2
	 *            second gene to compare
	 * @return true if genes have identical annotation
	 */
	public static boolean compareGeneByAnnotation(AnnotatedGene gene1,
			AnnotatedGene gene2) {
		boolean equal = false;

		if (gene1 == null || gene2 == null) {
			return equal;
		}

		// 1. if their IDs are set, are they the same?
		if (gene1.getIdentifyingId() != null
				&& gene2.getIdentifyingId() != null) {
			if (gene1.getIdentifyingId().equals(gene2.getIdentifyingId())) {
				equal = true;
			}
		}
		// 2. now compare their gene maps
		final Map<GeneNameType, List<GeneName>> map1 = gene1.getNameMap();
		final Map<GeneNameType, List<GeneName>> map2 = gene2.getNameMap();
		if (map1 != null && map2 != null) {
			if (map1.equals(map2)) {
				equal = true;
			}
		}
		return equal;
	}

	/**
	 * Compare two annotated genes by ordered locus name
	 * 
	 * @author dstaines
	 */
	public static class GeneOlnComparator implements Comparator<AnnotatedGene> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(AnnotatedGene o1, AnnotatedGene o2) {
			if ((!(o1 instanceof AnnotatedGene))
					|| (!(o2 instanceof AnnotatedGene))) {

				return 0;
			}

			final List<GeneName> g1Olns = o1.getNameMap().get(
					GeneNameType.ORDEREDLOCUSNAMES);
			final List<GeneName> g2Olns = o2.getNameMap().get(
					GeneNameType.ORDEREDLOCUSNAMES);

			if ((g1Olns.size() != 1) || (g2Olns.size() != 1)) {
				return 0;
			}

			final GeneName oln1 = g1Olns.iterator().next();
			final GeneName oln2 = g2Olns.iterator().next();

			return oln1.getName().compareTo(oln2.getName());
		}

	}

	public static final class GenomicComponentRankComparator implements
			Comparator<GenomicComponent> {

		public int compare(GenomicComponent o1, GenomicComponent o2) {
			return Integer.valueOf(
					o1.getMetaData().getComponentType().getRank()).compareTo(
					o2.getMetaData().getComponentType().getRank());
		}
	};

	public static boolean containsName(AnnotatedGene gene, String geneName) {
		boolean found = false;
		for (final List<GeneName> names : gene.getNameMap().values()) {
			for (final GeneName name : names) {
				if (name.getName() == geneName) {
					found = true;
					break;
				}
			}
			if (found)
				break;
		}
		return found;
	}

	/**
	 * Method to determine and set the protein start/end coordinates of a
	 * {@link ProteinFeature} object from its {@link RichLocation}, given the
	 * coordinates of the encoding CDS as a {@link RichLocation}
	 * 
	 * @param feature
	 *            object containing feature and its location
	 * @param location
	 *            CDs coordinates
	 */
	public static void setFeatureCoordinates(ProteinFeature feature,
			RichLocation location) {

		final RichLocation relativeLoc = LocationUtils.getProteinSubLocation(
				feature.getLocation(), location);
		feature.setStart(relativeLoc.getMin());
		feature.setEnd(relativeLoc.getMax());
	}

	/**
	 * Derive the offset of the start of the inner location relative to the
	 * outer location
	 * 
	 * @param loc
	 *            inner location
	 * @param outerLoc
	 *            enclosing location
	 * @return offset of start of inner location
	 * @deprecated
	 */
	@Deprecated
	public static int getStartOffset(RichLocation loc, RichLocation outerLoc) {
		final RichLocation loc1 = LocationUtils.sortLocation(loc).get(0);
		final Iterator<RichLocation> outerLocsI = LocationUtils.sortLocation(
				outerLoc).iterator();
		int offset = 0;
		while (outerLocsI.hasNext()) {
			final RichLocation loc2 = outerLocsI.next();
			// is start of loc1 contained in loc2?
			if (loc2.contains(loc1.getMin())) {
				// if yes, add diff of loc1 and loc2 to offset and break
				if (loc2.getStrand() == RichLocation.Strand.NEGATIVE_STRAND) {
					offset += loc2.getMax() - loc1.getMax() + 1;
				} else {
					offset += loc1.getMin() - loc2.getMin() + 1;
				}
				break;
			} else {
				// if no, add length of loc2 to offset
				offset += loc2.getMax() - loc2.getMin() + 1;
			}
		}
		return offset;
	}

	public static boolean checkCdsTranslation(GenomicComponent component,
			EntityLocation location, int proteinLength) {
		return checkTranslation(component.getSequence(), proteinLength,
				location, component.getMetaData().getGeneticCode());
	}

	public static boolean checkTranslation(Sequence genomicSequence,
			int proteinLength, EntityLocation loc, int code) {
		boolean translates = true;
		if (SequenceUtils.checkTranslation(genomicSequence, loc,
				RNATools.getGeneticCode(code))) {
			// now check length and insertions
			final int locLen = getEntityLocationLength(loc);
			final int protCodeLen = (proteinLength + 1) * 3;
			if (locLen != protCodeLen) {
				getLog().warn(
						"Location " + loc + " of length " + locLen
								+ " does not match protein coding length "
								+ protCodeLen + " for protein of length "
								+ proteinLength);
			}
		} else {
			translates = false;
		}
		return translates;
	}

	public static EntityLocation trimStopCodon(EntityLocation loc,
			GenomicComponent component) {
		final String pSeq = SequenceUtils.getTranslatedSequence(RNATools
				.getGeneticCode(component.getMetaData().getGeneticCode()),
				SequenceUtils.getSequenceStringForLocation(
						component.getSequence(), loc));
		if (pSeq.indexOf(SequenceUtils.STOP) == pSeq.length() - 1) {
			getLog().info("Triming internal trailing stop from location " + loc);
			final List<RichLocation> locs = LocationUtils.sortLocation(loc);
			final RichLocation lastLoc = CollectionUtils.getLastElement(locs,
					null);
			final RichLocation newLastLoc = LocationUtils.adjustLocationEnd(
					lastLoc, -3);
			locs.set(locs.size() - 1, newLastLoc);
			loc = new DelegatingEntityLocation(loc,
					LocationUtils.construct(locs));
		}
		return loc;
	}

	public static EntityLocation adjustForCodonStart(EntityLocation location,
			int codonStart) {
		final int frame = codonStart - 1;
		if (frame > 0) {
			location = new DelegatingEntityLocation(location,
					LocationUtils.adjustLocationStart(location, -frame));
		}
		return location;
	}

	/**
	 * @param loc
	 *            location to check
	 * @return length of joined locations, including insertions
	 */
	public static int getEntityLocationLength(EntityLocation loc) {
		int locLen = LocationUtils.getLocationLength(loc);
		for (final EntityLocationInsertion ins : loc.getInsertions()) {
			locLen += Math.abs(ins.getStart() - ins.getStop() + 1);
		}
		return locLen;
	}

	public static EntityLocation mergeLocations(Collection<EntityLocation> locs) {
		MappingState state = null;
		final Collection<EntityLocationException> exceptions = CollectionUtils
				.createArrayList();
		final Collection<EntityLocationInsertion> insertions = CollectionUtils
				.createArrayList();
		int circLen = -1;
		final Set<RichLocation> rLocs = CollectionUtils.createHashSet();
		for (final EntityLocation loc : locs) {
			circLen = loc.getCircularLength();
			if (state == null) {
				state = loc.getState();
			} else {
				state = MappingState.findConsensus(state, loc.getState());
			}
			rLocs.add(loc);
			exceptions.addAll(loc.getExceptions());
			insertions.addAll(loc.getInsertions());
		}
		final EntityLocation loc = new DelegatingEntityLocation(
				LocationUtils.mergeLocations(rLocs), state);
		loc.getExceptions().addAll(exceptions);
		loc.getInsertions().addAll(insertions);
		loc.setCircularLength(circLen);
		return loc;
	}

	/**
	 * @param locs
	 * @return merged location
	 */
	public static EntityLocation mergeLocations(EntityLocation... locs) {
		return mergeLocations(Arrays.asList(locs));
	}

	private static void mergeInsertions(EntityLocation loc1, EntityLocation loc) {
		for (final EntityLocationInsertion ins : loc1.getInsertions()) {
			if (!loc.getInsertions().contains(ins)) {
				loc.addInsertion(ins);
			}
		}
	}

	private static void mergeExceptions(EntityLocation loc1, EntityLocation loc) {
		for (final EntityLocationInsertion ins : loc1.getInsertions()) {
			if (!loc.getInsertions().contains(ins)) {
				loc.addInsertion(ins);
			}
		}
	}

	public static List<EntityLocation> decomposeLocation(EntityLocation location) {
		final List<EntityLocation> locs = CollectionUtils.createArrayList();
		if (LocationUtils.hasInnerLocations(location)) {
			RichLocation subLoc = null;
			for (final Iterator<RichLocation> locI = location.blockIterator(); locI
					.hasNext();) {
				subLoc = locI.next();
				if (LocationUtils.hasInnerLocations(subLoc)) {
					locs.addAll(decomposeLocation(location));
				} else {
					final EntityLocation eSubLoc = new DelegatingEntityLocation(
							subLoc, location.getState());
					assignInsertions(location, eSubLoc);
					assignExceptions(location, eSubLoc);
					locs.add(eSubLoc);
				}
			}
		} else {
			locs.add(location);
		}
		return locs;
	}

	/**
	 * @param location
	 * @param subLoc
	 */
	private static void assignExceptions(EntityLocation src, EntityLocation dest) {
		for (final EntityLocationException ex : src.getExceptions()) {
			if (locationContainsModification(dest, ex)) {
				dest.addException(ex);
			}
		}
	}

	/**
	 * @param location
	 * @param subLoc
	 */
	private static void assignInsertions(EntityLocation src, EntityLocation dest) {
		for (final EntityLocationInsertion ins : src.getInsertions()) {
			if (locationContainsModification(dest, ins)) {
				dest.addInsertion(ins);
			}
		}
	}

	/**
	 * @param location
	 * @param modification
	 * @return true if location contains part or all of modification
	 */
	public static boolean locationContainsModification(EntityLocation location,
			EntityLocationModifier modification) {
		return (modification.getStart() >= location.getMin() && modification
				.getStart() <= location.getMax())
				|| (modification.getStop() >= location.getMin() && modification
						.getStop() <= location.getMax());
	}

	/**
	 * Copy relevant modifiers from one location to another where the location
	 * contains them
	 * 
	 * @param src
	 *            location containing modifiers to copy
	 * @param dest
	 *            target for modifiers
	 */
	public static void assignLocationModifiers(EntityLocation src,
			EntityLocation dest) {
		for (final EntityLocation loc : decomposeLocation(dest)) {
			assignInsertions(src, loc);
			assignExceptions(src, loc);
		}
	}

	/**
	 * Check whether location translates cleanly
	 * 
	 * @param component
	 *            component to which location belongs
	 * @param location
	 *            protein coding location
	 * @return true if subsequence from component translates cleanly
	 */
	public static boolean checkTranslation(GenomicComponent component,
			EntityLocation location) {
		return checkTranslation(component.getSequence(), location, component
				.getMetaData().getGeneticCode());
	}

	public static boolean checkTranslation(Sequence seq,
			EntityLocation location, int translationTable) {
		try {
			return SequenceUtils.checkTranslationStopCodons(getTranslation(seq,
					location, translationTable));
		} catch (final SequenceTranslationException e) {
			getLog().debug(
					"Sequence did not translate using table "
							+ translationTable, e);
			return false;
		}
	}

	public static String getTranslation(GenomicComponent component,
			EntityLocation location) {
		return getTranslation(component.getSequence(), location, component
				.getMetaData().getGeneticCode());
	}

	public static String getTranslation(Sequence seq, EntityLocation location,
			int translationTable) {
		final String dnaSeq = SequenceUtils.getSequenceStringForLocation(seq,
				location);
		String aaSeq = SequenceUtils.getTranslatedSequence(
				RNATools.getGeneticCode(translationTable), dnaSeq);
		// modify with exceptions
		aaSeq = modifySequence(location, aaSeq);
		return aaSeq;
	}

	/**
	 * @param location
	 * @param seq
	 * @return
	 */
	protected static String modifySequence(EntityLocation location, String seq) {
		final StringBuilder s = new StringBuilder(seq);
		// apply exceptions
		for (final EntityLocationException exception : location.getExceptions()) {
			final RichLocation exLoc = getModificationLocation(exception,
					location);
			s.replace(exLoc.getMin(), exLoc.getMax(), exception.getProteinSeq());
		}
		// sort insertions and apply from C-terminus
		final List<EntityLocationInsertion> list = new ArrayList<EntityLocationInsertion>(
				location.getInsertions());
		Collections.sort(list,
				new EntityLocationModifierComparator<EntityLocationInsertion>(
						LocationUtils.isComplement(location)));
		for (final EntityLocationInsertion insertion : list) {
			final RichLocation exLoc = getModificationLocation(insertion,
					location);
			s.insert(exLoc.getMin(), insertion.getProteinSeq());
		}
		return s.toString();
	}

	protected static class EntityLocationModifierComparator<T extends EntityLocationModifier>
			implements Comparator<T> {

		private final boolean isComplement;

		public EntityLocationModifierComparator(boolean isComplement) {
			this.isComplement = isComplement;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(T o1, T o2) {
			int val = 0;
			if (o1.getStart() < o2.getStart()) {
				val = 1;
			} else if (o1.getStart() > o2.getStart()) {
				val = -1;
			}
			return isComplement ? (val * -1) : val;
		}

	}

	protected static RichLocation getModificationLocation(
			EntityLocationModifier mod, EntityLocation location) {
		RichLocation loc = new SimpleRichLocation(new SimplePosition(Math.min(
				mod.getStart(), mod.getStop())), new SimplePosition(Math.min(
				mod.getStart(), mod.getStop())), 1, location.getStrand());
		loc = LocationUtils.getRelativeLocation(loc, location);
		return cdsToProteinLocation(loc);
	}

	public static RichLocation cdsToProteinLocation(RichLocation loc) {
		return new SimpleRichLocation(new SimplePosition(
				((loc.getMin() - 1) / 3) + 1), new SimplePosition(
				((loc.getMax() - 1) / 3) + 1), 1);
	}

	private static Pattern exceptionPattern = Pattern
			.compile("pos=([^,]+),aa=(.*)");

	/**
	 * @param exception
	 *            translation exception in mirror format
	 * @return exception object
	 */
	public static EntityLocationException parseException(String exception) {
		final Matcher mat = exceptionPattern.matcher(exception);
		if (!mat.matches()) {
			throw new ServiceUncheckedException(
					"Could not parse trans_exception " + exception);
		} else {
			final RichLocation loc = LocationUtils.parseEmblLocation(mat
					.group(1));
			return new EntityLocationException(loc.getMin(), loc.getMax(),
					mat.group(2), mat.group(2));
		}
	}

	private static String proteinIdRE = "^([^.]+).?(.*)$";

	public static String removeProteinIdVersion(String proteinId) {
		return proteinId.replaceAll(proteinIdRE, "$1");
	}

	public static EntityLocation getOuterLocation(
			Collection<EntityLocation> locs) {
		final EntityLocation mLoc = mergeLocations(locs);
		RichLocation sloc = new SimpleRichLocation(mLoc.getMinPosition(),
				mLoc.getMaxPosition(), 1, mLoc.getStrand());
		final int circLen = mLoc.getCircularLength();
		sloc.setCircularLength(circLen);
		if (circLen > 0) {
			sloc = LocationUtils.splitCircularLocation(sloc);
		}
		final EntityLocation loc = new DelegatingEntityLocation(sloc,
				mLoc.getState());
		return loc;
	}

	/**
	 * Test location for internal overlap
	 * 
	 * @param location
	 * @return true if internal overlap has been found
	 */
	public static boolean hasInternalOverlap(RichLocation location) {
		boolean hasOverlap = false;
		RichLocation lastLoc = null;
		for (final RichLocation loc : LocationUtils.sortLocation(location)) {
			if (lastLoc != null) {
				if (loc.overlaps(lastLoc)) {
					hasOverlap = true;
					break;
				}
			}
			lastLoc = loc;
		}
		return hasOverlap;
	}

	public static class LocatableComparatorMinOnly implements
			Comparator<Locatable> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Locatable o1, Locatable o2) {
			final int l1Min = o1.getLocation().getMin();
			final int l2Min = o2.getLocation().getMin();
			int cmp = 0;
			if (l2Min < l1Min) {
				cmp = 1;
			} else if (l1Min < l2Min) {
				cmp = -1;
			} else {
				final int l1Max = o1.getLocation().getMax();
				final int l2Max = o2.getLocation().getMax();
				if (l2Max < l1Max) {
					cmp = 1;
				} else {
					cmp = -1;
				}
			}
			return cmp;
		}
	}

	public static class LocatableComparator implements Comparator<Locatable> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Locatable o1, Locatable o2) {

			if (o1.getLocation().getStrand() != o2.getLocation().getStrand()) {
				throw new IllegalArgumentException("Cannot compare locations "
						+ o1.getLocation() + " and " + o2.getLocation()
						+ " as they are on different strands");
			}

			final int l1Min = o1.getLocation().getMin();
			final int l2Min = o2.getLocation().getMin();
			int cmp = 0;
			if (l2Min < l1Min) {

				cmp = 1;

			} else if (l1Min < l2Min) {

				cmp = -1;

			} else {
				final int l1Max = o1.getLocation().getMax();
				final int l2Max = o2.getLocation().getMax();
				if (l2Max < l1Max) {

					cmp = 1;

				} else {

					cmp = -1;
				}
			}

			return cmp;
			// return ((o1.getLocation().getStrand() ==
			// RichLocation.Strand.NEGATIVE_STRAND) ? (-1 * cmp)
			// : cmp);
		}

	}

	public static TranslationStatus checkTranslation(
			GenomicComponent component, Protein protein) {
		return checkTranslation(component, protein.getLocation(),
				protein.getCodonStart());
	}

	public static TranslationStatus checkTranslation(
			GenomicComponent component, EntityLocation location, int codonStart) {
		TranslationStatus status = TranslationStatus.UNKNOWN;

		final EntityLocation newLoc = adjustForCodonStart(location, codonStart);

		if (checkTranslation(component, newLoc)) {
			status = TranslationStatus.TRANSLATABLE;
		} else {
			status = TranslationStatus.UNTRANSLATABLE;
		}

		if (status == TranslationStatus.UNTRANSLATABLE
				&& LocationUtils.isLocationFuzzy(location)) {
			status = TranslationStatus.INDETERMINATE;
		}

		return status;
	}

	/**
	 * utility to return a {@link RichLocation} object representing the part of
	 * the genome encoding the protein
	 * 
	 * @param location
	 *            genomic location of protein coding sequence
	 * @param pStart
	 *            start of protein feature
	 * @param pStop
	 *            stop of protein feature
	 * @return genomic location of protein feature coding sequence
	 * @throws FeatureLocationNotFoundException
	 *             when feature coordinates not found within location
	 */
	public static RichLocation getLocationForProteinFeature(
			final EntityLocation location, int pStart, int pStop) {
		// Aim: modify pstart and pstop to take account of any insertions
		// iterate over insert in coding order
		Collections.sort(location.getInsertions(),
				new Comparator<EntityLocationInsertion>() {
					public int compare(EntityLocationInsertion o1,
							EntityLocationInsertion o2) {
						int order = 0;
						if (o1.getStart() > o1.getStart()) {
							order = 1;
						} else if (o1.getStart() < o1.getStart()) {
							order = -1;
						}
						return order
								* (location.getStrand() == RichLocation.Strand.NEGATIVE_STRAND ? -1
										: 1);
					}
				});
		for (final EntityLocationInsertion insert : location.getInsertions()) {
			// find out where our insert sits in the protein sequence
			final RichLocation insertLoc = new SimpleRichLocation(
					new SimplePosition(insert.getStart()), new SimplePosition(
							insert.getStop()), 1, location.getStrand());
			final RichLocation insertpLoc = LocationUtils
					.getProteinSubLocation(insertLoc, location);
			final int insertLocStart = insertpLoc.getMin();
			// RichLocation insertLoc2 =
			// LocationUtils.getRelativeLocation(insertLoc, location);
			// insertLocStart = (insertpLoc.getMin() +2 )/3;
			// //System.out.println(insertLoc+" vs "+insertpLoc+" vs
			// "+insertLoc2+"="+insertLocStart);
			final int insertLen = insert.getProteinSeq().length();
			// adjust the feature start/stop by protein length if the insert
			// occurs before it in the protein seq
			if (insertLocStart < pStart) {
				pStart -= insertLen;
				if (pStart < insertLocStart) {
					pStart = insertLocStart;
				}
			}
			if (insertLocStart <= pStop) {
				pStop -= insertLen;
				if (pStop < insertLocStart) {
					pStop = insertLocStart - 1;
				}
			}
		}
		// use modified pstart/pstop to get genomic coordinates
		return LocationUtils.getLocationForProteinFeature(location, pStart,
				pStop);
	}

	public static EntityLocation resolveNoncodingOverlap(
			GenomicComponent component, EntityLocation location) {
		return resolveNoncodingOverlap(component.getSequence(), location);
	}

	public static EntityLocation resolveNoncodingOverlap(Sequence sequence,
			EntityLocation location) {
		RichLocation lastLoc = null;
		final List<EntityLocationInsertion> insertions = CollectionUtils
				.createArrayList();
		final List<RichLocation> locations = CollectionUtils.createArrayList();
		int rank = 0;
		for (RichLocation nextLoc : LocationUtils.sortLocation(location)) {
			if (lastLoc != null && nextLoc.overlaps(lastLoc)) {
				final int overlap = lastLoc.getMax() - nextLoc.getMin() + 1;
				if(nextLoc.getMax() == lastLoc.getMax() && nextLoc.getMin() == lastLoc.getMin()) {
					nextLoc = null;
				} else if (overlap <= (nextLoc.getMax() - nextLoc.getMin() + 1)) {
					nextLoc = LocationUtils.adjustLocationStart(nextLoc,
							-overlap);
				} else {
					nextLoc = null;
				}
				insertions.add(new EntityLocationInsertion(lastLoc.getMax(),
						lastLoc.getMax(), sequence.getSequence(
								lastLoc.getMax(), overlap)));
			}
			lastLoc = nextLoc;
			if (lastLoc != null) {
				lastLoc.setRank(rank++);
				locations.add(lastLoc);
			}
		}
		// simple code to remove overlap
		final DelegatingEntityLocation resolvedLoc = new DelegatingEntityLocation(
				location, LocationUtils.construct(LocationUtils
						.sortLocations(locations)));
		resolvedLoc.getInsertions().addAll(insertions);
		return resolvedLoc;
	}

	public static EntityLocation resolveOverlap(GenomicComponent component,
			EntityLocation location) {
		return resolveOverlap(component.getSequence(),
				RNATools.getGeneticCode(component.getMetaData()
						.getGeneticCode()), location);
	}

	public static EntityLocation resolveOverlap(Sequence seq,
			TranslationTable table, EntityLocation location) {
		RichLocation lastLoc = null;
		final List<RichLocation> locations = CollectionUtils.createArrayList();
		final List<EntityLocationInsertion> insertions = CollectionUtils
				.createArrayList();
		int cumulativeCount = 0;
		for (RichLocation nextLoc : LocationUtils.sortLocation(location)) {
			getLog().trace("NEXTLOC:" + nextLoc);
			if (lastLoc != null && nextLoc.overlaps(lastLoc)) {
				locations.remove(lastLoc);
				getLog().trace(
						"Overlap found with " + nextLoc + " and " + lastLoc);
				Sequence insertSeq = new Sequence();
				// work out how much there is left at the end of lastLoc
				final int phase = cumulativeCount % 3;
				if (!LocationUtils.isComplement(lastLoc)) {
					getLog().trace("Trimming by " + phase);
					if (phase > 0) {
						// move the trimmed sequence to an insert
						insertSeq.appendSequence(seq.getSequence(
								lastLoc.getMax() - phase + 1, phase));
						lastLoc = LocationUtils.adjustLocationEnd(lastLoc,
								-phase);
						getLog().trace("LASTLOC now:" + lastLoc);
						// move the trimmed sequence to an insert
						insertSeq.appendSequence(seq.getSequence(
								lastLoc.getMin(), 3 - phase));
						nextLoc = LocationUtils.adjustLocationStart(nextLoc,
								-(3 - phase));
						getLog().trace("NEXTLOC now:" + nextLoc);
					}
					// now work out if an overlap remains and if so, move
					// nextLoc by
					// a multiple of three over the overlap
					final int overlap = lastLoc.getMax() - nextLoc.getMin() + 1;
					if (overlap > 0) {
						getLog().trace(
								"Overlap of " + overlap
										+ " still found between " + nextLoc
										+ " and " + lastLoc);
						final int n = 3 * (1 + (overlap / 3));
						final int locationLength = LocationUtils
								.getLocationLength(nextLoc);
						if (n == locationLength) {
							insertSeq.appendSequence(seq.getSequence(
									nextLoc.getMin(), n));
							getLog().trace(
									"Ditching nextloc as it is the same as the overlap trim:"
											+ nextLoc);
							nextLoc = null;
						} else if (n > locationLength) {
							insertSeq.appendSequence(seq.getSequence(
									nextLoc.getMin(), locationLength));
							getLog().trace(
									"Ditching nextloc as it is greater as the overlap trim:"
											+ nextLoc);
							nextLoc = null;
						} else {
							getLog().trace("Trimming by " + n);
							insertSeq.appendSequence(seq.getSequence(
									nextLoc.getMin(), n));
							nextLoc = LocationUtils.adjustLocationStart(
									nextLoc, -n);
							getLog().trace("NEXTLOC now:" + nextLoc);
						}
					}
				} else {
					// TODO fix for complement now
					// same logic as before, but everything is reversed!
					getLog().trace("Trimming to triplet by " + phase);
					String seqStr = "";
					if (phase > 0) {
						// trim start of lastLoc
						// move the trimmed sequence to an insert - may need to
						// deal with rev/comp
						String trimmed = seq.getSequence(lastLoc.getMin(),
								phase);
						getLog().trace(
								"Trimmed " + trimmed + " from start of "
										+ lastLoc);
						seqStr = trimmed + seqStr;
						lastLoc = LocationUtils.adjustLocationEnd(lastLoc,
								-phase);
						getLog().trace("LASTLOC now:" + lastLoc);
						// trim end of lastloc
						// move the trimmed sequence to an insert
						final int trimLoc = nextLoc.getMax() - (3 - phase) + 1;
						trimmed = seq.getSequence(trimLoc, (3 - phase));
						getLog().trace(
								"Trimmed " + trimLoc + " => " + trimmed
										+ " from end of " + nextLoc);
						seqStr = trimmed + seqStr;
						nextLoc = LocationUtils.adjustLocationStart(nextLoc,
								-(3 - phase));
						getLog().trace("NEXTLOC now:" + nextLoc);
					}
					// now work out if an overlap remains and if so, move
					// nextLoc by
					// a multiple of three over the overlap
					final int overlap = nextLoc.getMax() - lastLoc.getMin() + 1;
					if (overlap > 0) {
						getLog().trace(
								"Overlap of " + overlap
										+ " still found between " + nextLoc
										+ " and " + lastLoc);
						final int n = 3 * (1 + (overlap / 3));
						getLog().trace("Trimming overlap by " + n);
						final int locationLength = LocationUtils
								.getLocationLength(nextLoc);
						if (n == locationLength) {
							seqStr = seq.getSequence(nextLoc.getMax() - n + 1,
									n) + seqStr;
							getLog().trace(
									"Ditching nextloc as it is the same as the overlap trim:"
											+ nextLoc);
							nextLoc = null;
						} else if (n > locationLength) {
							throw new LocationException(
									"Overlap trim "
											+ n
											+ " is greater than the overlapping segment "
											+ nextLoc);
						} else {
							seqStr = seq.getSequence(nextLoc.getMax() - n + 1,
									n) + seqStr;
							nextLoc = LocationUtils.adjustLocationStart(
									nextLoc, -n);
							getLog().trace("NEXTLOC now:" + nextLoc);
						}
					}
					getLog().trace("Seq is now " + seqStr);
					insertSeq = SequenceUtils.reverseComplement(new Sequence(
							seqStr));
				}
				locations.add(lastLoc);
				getLog().trace("Sequence is " + insertSeq.getSequence());
				final String aaSeq = SequenceUtils.getTranslatedSequence(table,
						insertSeq.getSequence());
				getLog().trace("AA sequence is " + aaSeq);
				final int insPos = LocationUtils.isComplement(lastLoc) ? lastLoc
						.getMin() : lastLoc.getMax();
				insertions.add(new EntityLocationInsertion(insPos, insPos,
						aaSeq));
			}
			if (nextLoc != null) {
				cumulativeCount += LocationUtils.getLocationLength(nextLoc);
				lastLoc = nextLoc;
				locations.add(nextLoc);
			}
		}
		final DelegatingEntityLocation resolvedLoc = new DelegatingEntityLocation(
				location, LocationUtils.construct(locations));
		resolvedLoc.getInsertions().addAll(insertions);
		getLog().trace("Location is now " + resolvedLoc);
		return resolvedLoc;
	}

	public static DatabaseReference getReferenceForTypeAndId(
			CrossReferenced referee, DatabaseReferenceType type, String id) {
		DatabaseReference ref = null;
		for (final DatabaseReference r : getReferencesForType(referee, type)) {
			if (r.getPrimaryIdentifier().equals(id)) {
				ref = r;
				break;
			}
		}
		return ref;
	}

	public static boolean hasReferenceForType(CrossReferenced referee,
			DatabaseReferenceType type) {
		return getReferencesForType(referee, type).size() > 0;
	}

	public static DatabaseReference getReferenceForType(
			CrossReferenced referee, DatabaseReferenceType type) {
		return CollectionUtils.getFirstElement(
				getReferencesForType(referee, type), null);
	}

	public static Collection<DatabaseReference> getReferencesForType(
			CrossReferenced referee, DatabaseReferenceType type) {
		final Collection<DatabaseReference> refs = CollectionUtils
				.createHashSet();
		for (final DatabaseReference ref : referee.getDatabaseReferences()) {
			if (ref.getDatabaseReferenceType().equals(type)) {
				refs.add(ref);
			}
		}
		return refs;
	}

	public static Set<GeneName> getNames(AnnotatedGene gene) {
		final Set<GeneName> names = CollectionUtils.createHashSet();
		for (final List<GeneName> nameSets : gene.getNameMap().values()) {
			names.addAll(nameSets);
		}
		return names;
	}

}
