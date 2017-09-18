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
 * File: LocationUtils.java
 * Created by: dstaines
 * Created on: Oct 2, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.util.biojava;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.biojava.bio.symbol.CircularLocation;
import org.biojava.bio.symbol.FuzzyLocation;
import org.biojava.bio.symbol.FuzzyPointLocation;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.MergeLocation;
import org.biojava.bio.symbol.PointLocation;
import org.biojava.bio.symbol.RangeLocation;
import org.biojavax.bio.seq.MultiSourceCompoundRichLocation;
import org.biojavax.bio.seq.Position;
import org.biojavax.bio.seq.RichLocation;
import org.biojavax.bio.seq.RichLocation.Strand;
import org.ensembl.genomeloader.util.collections.CollectionUtils;
import org.biojavax.bio.seq.SimplePosition;
import org.biojavax.bio.seq.SimpleRichLocation;

/**
 *
 * Some useful tools for working with biojava Locations. Deals with locations
 * centered around the RichLocation implementation
 *
 * @author dstaines
 *
 */
public class LocationUtils {

	public static class ComplementStrandLocationComparator implements Comparator {
		
		public int compare(Object o1, Object o2) {
			int d = 0;
			
			Location l1 = (Location) o1;
			Location l2 = (Location) o2;

			Iterator i1 = l1.blockIterator();
			Iterator i2 = l2.blockIterator();

			while (i1.hasNext() && i2.hasNext()) {
				Location li1 = (Location) i1.next();
				Location li2 = (Location) i2.next();

				d = li1.getMax() - li2.getMax();
				if (d != 0) {
					return d;
				}
				d = li1.getMin() - li2.getMin();
				if (d != 0) {
					return d;
				}
			}
			if (i2.hasNext()) {
				return 1;
			} else if (i1.hasNext()) {
				return -1;
			}

			return 0;
		}

	}
	
	public static final Comparator complementOrder = new ComplementStrandLocationComparator();

	public static final String INTERBASE = "interbase";
	public static final String INTERBASE_SYM = "^";

	/**
	 * @param location1
	 *            first location to test
	 * @param location2
	 *            second location to test
	 * @return true if locations are on same strand and start of one is the next
	 *         base after the end of another
	 */
	public static boolean adjacent(RichLocation location1,
			RichLocation location2) {
		boolean adjacent = false;
		if (location1.getStrand() == location2.getStrand()) {
			adjacent = (location1.getMin() == location2.getMax() + 1 || location2
					.getMin() == location1.getMax() + 1);
		}
		return adjacent;
	}

	/**
	 * Helper method to see if two locations have identical outer coordinates
	 *
	 * @param location1
	 *            first location to compare
	 * @param location2
	 *            second location to compare
	 * @return true if outer min, max and strand are equal
	 */
	public static boolean outerCoordinatesEquals(RichLocation location1,
			RichLocation location2) {
		return location1.getMax() == location2.getMax()
				&& location1.getMin() == location2.getMin()
				&& location1.getStrand() == location2.getStrand();
	}

	/**
	 * Check to see if the two outer locations overlap in strand:
	 * <ul>
	 * <li>Have same strand (cannot be unknown)
	 * <li>Start/end overlap
	 * <li>Starts are in frame (multiples of 3)
	 *
	 * @param location1
	 *            first location to test
	 * @param location2
	 *            second location to test
	 * @return true if overlapping in frame
	 */
	protected static boolean outerOverlapsInFrame(RichLocation location1,
			int offset1, RichLocation location2, int offset2) {
		boolean overlaps = false;
		if (location1.getStrand() != RichLocation.Strand.UNKNOWN_STRAND
				&& location1.getStrand() == location2.getStrand()) {
			if (location1.overlaps(location2)) {
				// we want to know the difference between the start of the
				// overlaps:
				// - cumulative distance to start of 1
				// - cumulative distance to same location in 2
				int diff = 0;
				int start1 = 0;
				int start2 = 0;
				if (location1.getStrand() == RichLocation.Strand.POSITIVE_STRAND) {
					int min1 = getAdjustedCircularLocation(location1.getMin(),
							location1);
					int min2 = getAdjustedCircularLocation(location2.getMin(),
							location2);
					if (min1 > min2) {
						start2 = min1 - min2 + offset2;
						start1 = offset1;
					} else {
						start2 = offset2;
						start1 = min2 - min1 + offset1;
					}
					diff = start1 - start2;
				} else {
					int max1 = getAdjustedCircularLocation(location1.getMax(),
							location1);
					int max2 = getAdjustedCircularLocation(location2.getMax(),
							location2);
					if (max1 > max2) {
						start1 = offset1 + (max1 - max2);
						start2 = offset2;
					} else {
						start2 = offset2 + (max2 - max1);
						start1 = offset1;
					}
					diff = start1 - start2;
				}
				if ((diff % 3) == 0) {
					overlaps = true;
				}
			}
		}
		return overlaps;
	}

	/**
	 * @param location1
	 * @param location2
	 * @return true if outer bounds of location2 overlap with and are in frame
	 *         with outer bounds of location1
	 */
	public static boolean outerOverlapsInFrame(RichLocation location1,
			RichLocation location2) {
		return outerOverlapsInFrame(location1, 0, location2, 0);
	}

	/**
	 * @param location1
	 * @param location2
	 * @return true if any part of location2 overlaps in frame with location1
	 */
	public static boolean overlapsInFrame(RichLocation location1,
			RichLocation location2) {
		if (location1.getCircularLength() != location2.getCircularLength()) {
			return false;
		} else if (location1.getStrand() != RichLocation.Strand.UNKNOWN_STRAND
				&& location2.getStrand() != RichLocation.Strand.UNKNOWN_STRAND
				&& location1.getStrand() != location2.getStrand()) {
			// if on different strands, cannot overlap
			return false;
		} else if (location1.getCircularLength() < 1
				&& location1.getStrand() != RichLocation.Strand.UNKNOWN_STRAND
				&& location2.getStrand() != RichLocation.Strand.UNKNOWN_STRAND
				&& !outerOverlaps(location1, location2)) {
			// shortcut - for non-circular locations on the same strand, check
			// outer
			// overlap first
			return false;
		} else if (location1.getCircularLength() > 0
				&& location1.getMin() < location1.getMax()
				&& location2.getMin() < location2.getMax()
				&& location1.getStrand() != RichLocation.Strand.UNKNOWN_STRAND
				&& location2.getStrand() != RichLocation.Strand.UNKNOWN_STRAND
				&& !outerOverlaps(location1, location2)) {
			// shortcut - for circular locations that do not overlap the origin,
			// check the outer bounds first
			return false;
		} else {
			return overlapsInFrame(location1, 0, location2, 0);
		}
	}

	/**
	 * @param location1
	 * @param location2
	 * @return true if either outer bound of either location fall between outer
	 *         bounds of the other (ignores frame and inner joins)
	 */
	public static boolean outerOverlaps(RichLocation location1,
			RichLocation location2) {
		return outerContains(location1, location2.getMin())
				|| outerContains(location1, location2.getMax())
				|| outerContains(location2, location1.getMin())
				|| outerContains(location2, location1.getMax());
	}

	private static boolean outerContains(RichLocation loc, int point) {
		return point >= loc.getMin() && point <= loc.getMax();
	}

	/**
	 * Check to see if the two locations have any overlapping component
	 * locations
	 *
	 * @param location1
	 *            first location to test
	 * @param location2
	 *            second location to test
	 * @return true if one or more sub-location of location1 overlaps one or
	 *         more sub-location of location2 in frame
	 */
	public static boolean overlapsInFrame(RichLocation location1, int offset1,
			RichLocation location2, int offset2) {
		for (RichLocation sloc1 : sortLocation(location1)) {
			if (sloc1.equals(location1)) {
				// same object therefore we've reached the bottom of the
				// compound location stack
				for (RichLocation sloc2 : sortLocation(location2)) {
					if (sloc2.equals(location2)) {
						// location2 is not a composite
						if (outerOverlapsInFrame(sloc1, offset1, sloc2, offset2)) {
							return true;
						}
					} else {
						// location2 is a composite
						if (overlapsInFrame(sloc1, offset1, sloc2, offset2)) {
							return true;
						}
					}
					// increment offset2 by sloc2 length
					offset2 += getLocationLength(sloc2);
				}
			} else {
				// sub-location is itself compound so recurse further
				if (overlapsInFrame(sloc1, offset1, location2, offset2)) {
					return true;
				}
			}
			// increment offset1 by sloc1 length
			offset1 += getLocationLength(sloc1);
		}
		return false;
	}

	/**
	 * Method to merge two locations into a new RichLocation, collapsing if
	 * required
	 */
	public static RichLocation mergeLocations(RichLocation location1,
			RichLocation location2) {
		// must be on same strand!
		List<RichLocation> locList = Arrays.asList(new RichLocation[] {
				location1, location2 });
		return mergeLocations(locList);
	}

	/**
	 * Method to merge set of locations into a new RichLocation, collapsing if
	 * required
	 *
	 * @param locs
	 * @return
	 */
	public static RichLocation mergeLocations(Collection<RichLocation> locs) {
		List<RichLocation> locList = new ArrayList<RichLocation>(locs);
		Collections.sort(locList, RichLocation.naturalOrder);
		Collection<RichLocation> mergedLocs = merge(locList);
		RichLocation mergedLoc = construct(mergedLocs);
		return mergedLoc;
	}

	public static String locationToEmblFormat(RichLocation location) {
		StringBuilder s = new StringBuilder();
		if (isComplement(location)) {
			s.append("complement(");
		}
		boolean isJoin = hasInnerLocations(location);
		if (isJoin)
			s.append("join(");
		int i = 0;
		for (Iterator<RichLocation> iter = location.blockIterator(); iter
				.hasNext();) {
			RichLocation loc = iter.next();
			if (loc != null) {
				if (i++ > 0) {
					s.append(',');
				}
				if (location.getStrand() == RichLocation.Strand.UNKNOWN_STRAND
						&& isComplement(loc)) {
					s.append("complement(");
					if (loc.getMinPosition().getFuzzyStart()) {
						s.append('<');
					}
					s.append(loc.getMin());
					if (loc.getMax() != loc.getMin()) {
						s.append("..");
						if (loc.getMaxPosition().getFuzzyEnd()) {
							s.append('>');
						}
						s.append(loc.getMax());
					}
					s.append(")");
				} else {
					if (loc.getMinPosition().getFuzzyStart()) {
						s.append('<');
					}
					s.append(loc.getMin());
					if (loc.getMax() != loc.getMin()) {
						s.append("..");
						if (loc.getMaxPosition().getFuzzyEnd()) {
							s.append('>');
						}
						s.append(loc.getMax());
					}
				}
			}
		}
		if (isComplement(location)) {
			s.append(")");
		}
		if (isJoin)
			s.append(")");
		return s.toString();
	}

	/**
	 * Build a RichLocation given the outer coordinates and optional set of
	 * inner coordinates
	 *
	 * @param outerStart
	 *            outer start limit
	 * @param outerEnd
	 *            outer end limit
	 * @param length
	 *            length of sequence to which this belongs
	 * @param outerComplement
	 *            true if location is on reverse strand
	 * @param innerLocs
	 *            optional serialized inner location set in EMBL-format
	 * @return location
	 */
	public static RichLocation buildLocation(int outerStart, int outerEnd,
			int length, boolean outerComplement, String innerLocs) {
		RichLocation.Strand strand = (outerComplement ? RichLocation.Strand.NEGATIVE_STRAND
				: RichLocation.Strand.POSITIVE_STRAND);
		return buildLocation(outerStart, outerEnd, length, strand, innerLocs);
	}

	public static RichLocation buildLocation(int outerStart, int outerEnd,
			int length, RichLocation.Strand strand, String innerLocs) {
		RichLocation loc = null;
		if (StringUtils.isEmpty(innerLocs)) {
			loc = buildSimpleLocation(outerStart, outerEnd, length,
					strand == RichLocation.Strand.NEGATIVE_STRAND);
		} else {
			// parse locs and use to build a location
			loc = parseEmblLocation(innerLocs);
			// perform sanity check vs outer stuff
			if (loc.getMin() != outerStart || loc.getMax() != outerEnd
					|| (loc.getStrand() != strand)) {
				String msg = "Constructed location " + loc
						+ " from inner location " + innerLocs
						+ " does not match the outer coordinates " + outerStart
						+ "-" + outerEnd + " (" + strand + ")";
				throw new LocationException(msg);
			}
		}
		if (loc.getMin() > loc.getMax()) {
			loc.setCircularLength(length);
		}
		return loc;
	}

	/**
	 *
	 * @param start
	 * @param end
	 * @param length
	 * @param complement
	 * @return
	 */

	public static RichLocation buildCircularLocation(int start, int end,
			int length, boolean complement) {
		RichLocation.Strand strand = (complement ? RichLocation.Strand.NEGATIVE_STRAND
				: RichLocation.Strand.POSITIVE_STRAND);
		return buildCircularLocation(start, end, length, strand);
	}

	/**
	 *
	 * @param start
	 * @param end
	 * @param length
	 * @param strand
	 * @return
	 */

	public static RichLocation buildCircularLocation(int start, int end,
			int length, RichLocation.Strand strand) {

		RichLocation loc1 = LocationUtils.buildSimpleLocation(1, start, length,
				strand == RichLocation.Strand.NEGATIVE_STRAND);
		loc1.setCircularLength(length);

		RichLocation loc2 = LocationUtils.buildSimpleLocation(end, length,
				length, strand == RichLocation.Strand.NEGATIVE_STRAND);
		loc2.setCircularLength(length);

		RichLocation loc = LocationUtils.mergeLocations(loc1, loc2);
		loc.setCircularLength(length);

		return loc;
	}

	private static Pattern EMBL_JOIN = Pattern
			.compile("^(join|order)\\((.*?)\\)$");
	private static Pattern EMBL_COMP_JOIN = Pattern
			.compile("^complement\\((join|order)\\((.*?)\\)\\)$");
	private static Pattern EMBL_LOC = Pattern
			.compile("^(complement\\()?([<>]?)([0-9]+)(([.^][.]?)([<>]?)([0-9]+))?(\\))?$");

	public static RichLocation parseEmblLocation(String location) {
		List<RichLocation> locs = CollectionUtils.createArrayList();
		Matcher m1 = EMBL_JOIN.matcher(location);
		boolean outerComp = false;
		if (!m1.matches()) {
			m1 = EMBL_COMP_JOIN.matcher(location);
			if (m1.matches()) {
				outerComp = true;
				location = m1.group(2);
			}
		} else {
			location = m1.group(2);
		}
		int rank = 0;
		for (String locStr : location.split(",")) {
			if (!StringUtils.isEmpty(locStr)) {
				m1 = EMBL_LOC.matcher(locStr);
				int start = 0;
				int end = 0;
				boolean fuzzystart = false;
				boolean fuzzyend = false;
				boolean complement = false;
				String operator = null;
				if (m1.matches() && m1.groupCount() == 8) {
					if (!StringUtils.isEmpty(m1.group(1))) {
						complement = true;
					}
					start = Integer.parseInt(m1.group(3));
					operator = m1.group(6);
					if (StringUtils.isEmpty(m1.group(7)))
						end = start;
					else
						end = Integer.parseInt(m1.group(7));
					if (!StringUtils.isEmpty(m1.group(2))) {
						if (m1.group(2).equals(">") && start == end) {
							fuzzyend = true;
						} else {
							fuzzystart = true;
						}
					}
					if (!StringUtils.isEmpty(m1.group(6))) {
						if (m1.group(6).equals("<") && start == end) {
							fuzzystart = true;
						} else {
							fuzzyend = true;
						}
					}
				} else {
					throw new LocationException("Could not parse location "
							+ locStr + " from " + location);
				}
				RichLocation.Strand strand = complement ^ outerComp ? RichLocation.Strand.NEGATIVE_STRAND
						: RichLocation.Strand.POSITIVE_STRAND;
				// RichLocation.Strand strand = complement ?
				// RichLocation.Strand.NEGATIVE_STRAND
				// : RichLocation.Strand.POSITIVE_STRAND;
				SimplePosition startPos = new SimplePosition(fuzzystart, false,
						start);
				SimplePosition endPos = new SimplePosition(false, fuzzyend, end);
				SimpleRichLocation simpleRichLocation = new SimpleRichLocation(
						startPos, endPos, ++rank, strand);
				if (INTERBASE_SYM.equals(operator)) {
					simpleRichLocation.getNoteSet().add(INTERBASE);
				}
				locs.add(simpleRichLocation);
			}
		}
		return construct(locs);
	}

	public static RichLocation buildSimpleLocation(int start,
			boolean fuzzyStart, int end, boolean fuzzyEnd, int limit,
			boolean complement) {
		// if end has not been initialised, set default value
		if ((start > 0) && (end <= 0)) {

			if (complement) {

				end = start - 100;

			} else {

				end = start + 100;
			}
		}

		// co-ordinates exceed limits, make a fix on the assumption that genomes
		// are circular.
		if (limit != 0 && start > limit) {

			start = start - limit;
		}

		if (limit != 0 && end > limit) {

			end = end - limit;
		}

		return new SimpleRichLocation(new SimplePosition(fuzzyStart, false,
				start), new SimplePosition(false, fuzzyEnd, end), 1,
				(complement ? RichLocation.Strand.NEGATIVE_STRAND
						: RichLocation.Strand.POSITIVE_STRAND));
	}

	public static RichLocation buildSimpleLocation(int start, int end,
			boolean complement) {
		return buildSimpleLocation(start, false, end, false, 0, complement);
	}

	public static RichLocation buildSimpleLocation(int start, int end,
			int limit, boolean complement) {
		return buildSimpleLocation(start, false, end, false, limit, complement);
	}

	/**
	 * Takes a set of locations and tries to merge all pairs where the union
	 * operation results in a simple rich location, not a compound one. Note
	 * that this is a fixed and extended version of the BioJava
	 * {@link RichLocation.Tools#merge(Collection)} method
	 *
	 * @param members
	 *            the members to merge
	 * @return the resulting merged set, which may have only one location in it.
	 */
	public static Collection<RichLocation> merge(Collection<RichLocation> locs) {
		if (locs.size() < 2) {
			return locs;
		} else {
			List<RichLocation> membersList = new ArrayList<RichLocation>(
					RichLocation.Tools.flatten(locs));
			Collection<RichLocation> mergedLocs = RichLocation.Tools
					.merge(locs);
			if (membersList.size() > 1) {
				for (int p = 0; p < (membersList.size() - 1); p++) {
					RichLocation parent = (RichLocation) membersList.get(p);
					for (int c = p + 1; c < membersList.size(); c++) {
						RichLocation child = (RichLocation) membersList.get(c);
						RichLocation union = (RichLocation) parent.union(child);
						if (MultiSourceCompoundRichLocation.class
								.isAssignableFrom(union.getClass())) {
							union = new TranssplicedCompoundRichLocation(
									(MultiSourceCompoundRichLocation) union);
						}
						union.setCircularLength(CollectionUtils
								.getFirstElement(locs, null)
								.getCircularLength());
						// if parent can merge with child
						if (union.isContiguous()) {
							// replace parent with union
							membersList.set(p, union);
							// remove child
							membersList.remove(c);
							// check all children again
							c = p;
							parent = union;
						}
					}
				}
				// now adjust for circular locs
				if (membersList.size() > 0) {
					int circularLength = membersList.get(0).getCircularLength();
					if (circularLength != -1) {
						RichLocation loc1 = membersList.get(0);
						RichLocation loc2 = membersList
								.get(membersList.size() - 1);
						if (loc2.getMax() == circularLength
								&& loc1.getMin() == 1
								&& loc1.getStrand() == loc2.getStrand()) {
							membersList.remove(loc2);
							membersList.add(0, loc2);
						}
					}
				}
				int rank = 0;
				for (RichLocation loc : membersList) {
					loc.setRank(++rank);
				}
			}
			return membersList;
		}
	}

	public static RichLocation construct(RichLocation... locs) {
		return construct(Arrays.asList(locs));
	}

	public static class RankedComparator implements Comparator<RichLocation> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(RichLocation o1, RichLocation o2) {
			Integer rank1 = new Integer(o1.getRank());
			return rank1.compareTo(o2.getRank());
		}

	}

	private static RankedComparator rankedComparator = new RankedComparator();

	public static RichLocation construct(Collection<RichLocation> locs) {
		if (locs.size() == 0) {
			return RichLocation.EMPTY_LOCATION;
		} else if (locs.size() == 1) {
			return locs.iterator().next();
		} else {
			// order by rank
			List<RichLocation> locsL = new ArrayList<RichLocation>(locs);
			Collections.sort(locsL, rankedComparator);
			if (locationsAreMixedStrand(locsL)) {
				return new TranssplicedCompoundRichLocation(locsL);
			} else {
				return new RankedCompoundRichLocation(locsL);
			}
		}
	}

	public static boolean locationsAreMixedStrand(Collection<RichLocation> locs) {
		RichLocation.Strand lastStrand = null;
		boolean mixed = false;
		for (RichLocation sloc : locs) {
			if (lastStrand != null && lastStrand != sloc.getStrand()) {
				mixed = true;
				break;
			}
			lastStrand = sloc.getStrand();
		}
		return mixed;
	}

	public static int getLocationLength(RichLocation location) {
		int len = 0;
		for (Iterator<RichLocation> iter = location.blockIterator(); iter
				.hasNext();) {
			RichLocation loc = iter.next();
			if (loc == location) {
				len += getLocationOuterLength(loc);
			} else {
				len += getLocationLength(loc);
			}
		}
		return len;
	}

	public static int getLocationOuterLength(RichLocation loc) {
		return Math.abs(loc.getMax() - loc.getMin()) + 1;
	}

	/**
	 * {@link Deprecated} as does not handle transsplicing properly
	 *
	 * @author dstaines
	 */
	@Deprecated
	protected static class LocationStrandAwareComparator implements
			Comparator<RichLocation> {

		public int compare(RichLocation o1, RichLocation o2) {

			if (o1.getStrand() == o2.getStrand()) {
				if (isComplement(o1)) {
					int max1 = LocationUtils.getAdjustedCircularLocation(
							o1.getMax(), o1);
					int max2 = LocationUtils.getAdjustedCircularLocation(
							o2.getMax(), o2);
					if (max1 > max2) {
						return -1;
					} else if (max1 < max2) {
						return 1;
					} else {
						return 0;
					}
				} else {
					int min1 = LocationUtils.getAdjustedCircularLocation(
							o1.getMin(), o1);
					int min2 = LocationUtils.getAdjustedCircularLocation(
							o2.getMin(), o2);
					if (min1 > min2) {
						return 1;
					} else if (min1 < min2) {
						return -1;
					} else {
						return 0;
					}
				}
			} else {
				throw new UnsupportedOperationException(
						"Cannot compare locations using different strand");
			}
		}

	}

	public static List<RichLocation> sortLocation(RichLocation location) {
		List<RichLocation> locs = flatten(location);
		if (locs.size() == 1)
			return locs;
		return sortLocations(locs);
	}

	public static List<RichLocation> sortLocations(List<RichLocation> locs) {
		// sort locations
		Collections.sort(locs, RichLocation.naturalOrder);
		// if last element is complement, reverse order
		RichLocation lastLoc = locs.get(locs.size() - 1);
		if (isComplement(locs.get(locs.size() - 1))) {
			Collections.reverse(locs);
		}
		// shuffle round split locations overlapping origin if not already
		// shuffled
		if (lastLoc.getCircularLength() != -1 && locs.size() > 1) {
			RichLocation loc1 = locs.get(0);
			RichLocation loc2 = locs.get(locs.size() - 1);
			if (loc2.getMax() == lastLoc.getCircularLength()
					&& loc1.getMin() == 1
					&& loc1.getStrand() == loc2.getStrand()) {
				locs.remove(loc2);
				locs.add(0, loc2);
			}
		}
		return locs;
	}

	/**
	 * @param location
	 *            location to flatten
	 * @return list of all sub-locations, sorted by natural order
	 */
	public static List<RichLocation> flatten(RichLocation location) {
		List<RichLocation> locs = CollectionUtils.createArrayList();
		if (!hasInnerLocations(location)) {
			locs.add(location);
		} else {
			for (Iterator<RichLocation> locI = location.blockIterator(); locI
					.hasNext();) {
				RichLocation loc = locI.next();
				if (hasInnerLocations(loc)) {
					locs.addAll(flatten(location));
				} else {
					loc.setCircularLength(location.getCircularLength());
					locs.add(loc);
				}
			}
			Collections.sort(locs, RichLocation.naturalOrder);
		}
		return locs;
	}

	/**
	 * Decoration of {@link #sortLocation(RichLocation)} which merges locations
	 * overlapping the origin and converts them to zero-based coordinates
	 *
	 * @param location
	 * @return list of rich locations
	 */
	protected static List<RichLocation> sortAndMergeLocation(
			RichLocation location) {
		List<RichLocation> locs = sortLocation(location);
		// merge together split locations to give negative coords for working
		// out relative stuff
		if (location.getCircularLength() != -1 && locs.size() > 1) {
			RichLocation loc1 = locs.get(0);
			RichLocation loc2 = locs.get(1);
			if (loc1.getMax() == location.getCircularLength()
					&& loc2.getMin() == 1) {
				int newStart = loc1.getMin() - location.getCircularLength();
				int newEnd = loc2.getMax();
				RichLocation loc = new SimpleRichLocation(
						new SimplePosition(loc1.getMinPosition()
								.getFuzzyStart(), false, newStart),
						new SimplePosition(false, loc2.getMaxPosition()
								.getFuzzyEnd(), newEnd), 1,
						location.getStrand());
				loc.setCircularLength(location.getCircularLength());
				locs.remove(1);
				locs.set(0, loc);
			}
		}
		return locs;
	}

	protected static RichLocation convertToRelativeCoordinates(
			RichLocation location) {
		if (location.getCircularLength() > 0) {
			int start = location.getMin() - location.getCircularLength();
			int end = location.getMax() - location.getCircularLength();
			RichLocation loc = new SimpleRichLocation(new SimplePosition(
					location.getMinPosition().getFuzzyStart(), location
							.getMinPosition().getFuzzyEnd(), start),
					new SimplePosition(location.getMaxPosition()
							.getFuzzyStart(), location.getMaxPosition()
							.getFuzzyEnd(), end), 1);
			loc.setCircularLength(location.getCircularLength());
			return loc;
		} else {
			return location;
		}
	}

	/**
	 * @param position
	 *            offset position
	 * @param location
	 *            location
	 * @return position relative to circular origin
	 */
	public static int getAdjustedCircularLocation(int position,
			RichLocation location) {
		if (location.getCircularLength() > 0
				&& location.getMin() > location.getMax()) {
			return position - location.getCircularLength();
		} else {
			return position;
		}
	}

	/**
	 * @param location
	 *            location to test
	 * @return true if location has at least one fuzzy start/end
	 */
	public static boolean isLocationFuzzy(RichLocation location) {
		boolean fuzzy = false;
		for (Iterator<RichLocation> locI = location.blockIterator(); locI
				.hasNext();) {
			RichLocation loc = locI.next();
			if (loc.getMinPosition().getFuzzyStart()
					|| loc.getMaxPosition().getFuzzyEnd()) {
				fuzzy = true;
				break;
			}
		}
		return fuzzy;
	}

	/**
	 * @param location
	 *            location to test
	 * @return true if location is compound
	 */
	public static boolean hasInnerLocations(RichLocation location) {
		return countInnerLocations(location) > 1;
	}

	public static int countInnerLocations(RichLocation location) {
		if (location.getClass().isAssignableFrom(RichLocation.class)) {
			return 1;
		}
		int i = 0;
		for (Iterator<RichLocation> locI = location.blockIterator(); locI
				.hasNext();) {
			locI.next();
			i++;
		}
		return i;
	}

	public static int getTripletStart(int pos) {
		return 1 + ((pos - 1) * 3);
	}

	public static int getTripletEnd(int pos) {
		return 3 + ((pos - 1) * 3);
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
			RichLocation location, int pStart, int pStop) {

		int start = getTripletStart(pStart); // start of triplet for start
		// position in joined sequence
		int end = getTripletEnd(pStop); // end of triplet for end position in
		// joined sequence

		return getGlobalLocation(location, new SimpleRichLocation(
				new SimplePosition(start), new SimplePosition(end), 1));
	}

	/**
	 * Convert a local location to the global equivalent
	 *
	 * @param reference
	 *            location with global coordinates
	 * @param subject
	 *            location with local coordinates
	 * @return global version of subject
	 */
	public static RichLocation getGlobalLocation(RichLocation reference,
			RichLocation subject) {

		if (hasInnerLocations(subject)) {

			RichLocation globalLocation = null;
			for (RichLocation subSubject : sortAndMergeLocation(subject)) {
				RichLocation globalSubSubject = getGlobalLocation(subSubject,
						reference);
				if (globalLocation == null) {
					globalLocation = globalSubSubject;
				} else {
					globalLocation = mergeLocations(globalLocation,
							globalSubSubject);
				}
			}
			return globalLocation;

		} else {

			Iterator<RichLocation> locI = LocationUtils.sortAndMergeLocation(
					reference).iterator();
			int rank = 0;

			List<RichLocation> subLocs = CollectionUtils.createArrayList();
			RichLocation refLoc = null;
			RichLocation lastRefLoc = null;

			boolean foundStart = false;
			boolean foundEnd = false;
			int offset = 0;
			boolean lastLoc = false;
			boolean firstLoc = true;
			while (locI.hasNext()) {

				boolean fuzzyStart = false;
				boolean fuzzyEnd = false;

				// remember the last reference join and note where we are
				if (refLoc != null) {
					firstLoc = false;
					lastRefLoc = refLoc;
				}

				refLoc = locI.next();
				lastLoc = !locI.hasNext();

				// if we've had more than one join before this one, set offset
				// correctly
				if (lastRefLoc != null) {
					// increase offset by length of last join
					offset += getLocationLength(lastRefLoc);
				}
				// 1. convert reference coords to local system
				int start = 1 + offset;
				int end = refLoc.getMax() - refLoc.getMin() + 1 + offset;

				// 2. note global coords
				int gStart = refLoc.getMin();
				int gEnd = refLoc.getMax();

				if (isComplement(refLoc)) {
					if (!foundStart) {
						if (subject.getMax() >= start
								&& (lastLoc || subject.getMax() <= end)) {
							gStart = refLoc.getMax()
									- (subject.getMax() - start);
							foundStart = true;
							fuzzyStart = subject.getMaxPosition().getFuzzyEnd();
						}
					}
					if (!foundEnd) {
						if (subject.getMin() <= end) {
							gEnd = refLoc.getMax() - (subject.getMin() - start);
							foundEnd = true;
							fuzzyEnd = subject.getMinPosition().getFuzzyStart();
						}
					}
				} else {
					if (!foundStart) {
						if (subject.getMin() <= end) {
							gStart = subject.getMin() - start + refLoc.getMin();
							foundStart = true;
							fuzzyStart = subject.getMinPosition()
									.getFuzzyStart();
						}
					}
					if (!foundEnd) {
						if (subject.getMax() >= start
								&& (lastLoc || subject.getMax() <= end)) {
							gEnd = subject.getMax() - start + refLoc.getMin();
							foundEnd = true;
							fuzzyEnd = subject.getMaxPosition().getFuzzyEnd();
						}
					}
				}

				if (foundStart || foundEnd) {
					// locations to our list (this includes inners)
					if (isComplement(reference)) {
						rank--;
					} else {
						rank++;
					}
					SimpleRichLocation newLoc = new SimpleRichLocation(
							new SimplePosition(fuzzyStart, false, gStart),
							new SimplePosition(false, fuzzyEnd, gEnd),
							rank,
							(isComplement(refLoc) == isComplement(subject)) ? Strand.POSITIVE_STRAND
									: Strand.NEGATIVE_STRAND);
					newLoc.setCircularLength(refLoc.getCircularLength());
					subLocs.add(newLoc);
				}
				if (foundStart && foundEnd)
					break;
			}
			return splitLocationAtOrigin(unite(subLocs));
		}
	}

	/**
	 * Convert a circular location with negative coordinates to a joined pair
	 * around the origin
	 *
	 * @param loc
	 *            location overlapping origin
	 * @return locations split by origin
	 */
	public static RichLocation splitLocationAtOrigin(RichLocation loc) {
		if (loc.getCircularLength() > 0 && loc.getMin() < 1 && loc.getMax() > 0) {
			RichLocation newLoc = new SimpleRichLocation(new SimplePosition(
					loc.getCircularLength() + loc.getMin()),
					new SimplePosition(loc.getCircularLength()), 2,
					loc.getStrand());
			newLoc.setCircularLength(loc.getCircularLength());
			RichLocation newLoc2 = new SimpleRichLocation(
					new SimplePosition(1), new SimplePosition(loc.getMax()), 1,
					loc.getStrand());
			newLoc2.setCircularLength(loc.getCircularLength());
			return mergeLocations(newLoc, newLoc2);
		} else if (loc.getCircularLength() > 0 && loc.getMin() < 1
				&& loc.getMax() < 1) {
			return new SimpleRichLocation(new SimplePosition(
					loc.getCircularLength() + loc.getMin()),
					new SimplePosition(loc.getCircularLength() + loc.getMax()),
					1, loc.getStrand());

		} else {
			return loc;
		}
	}

	/**
	 * Find the relative start and end coordinates of an enclosed location given
	 * the coordinates of the enclosing location skipping gaps in the reference
	 * where the subject can overlap reference gaps
	 *
	 * @param subject
	 *            inner location
	 * @param reference
	 *            enclosed location
	 * @return relative coordinates of inner location
	 */
	public static RichLocation getRelativeLocationWithAllGaps(
			RichLocation subject, RichLocation reference) {
		return getRelativeLocation(subject, reference, false, false);
	}

	/**
	 * Find the relative start and end coordinates of an enclosed location given
	 * the coordinates of the enclosing location, skipping gaps in the reference
	 * where the subject must not overlap reference gaps
	 *
	 * @param subject
	 *            inner location
	 * @param reference
	 *            enclosed location
	 * @return relative coordinates of inner location
	 */
	public static RichLocation getRelativeLocation(RichLocation subject,
			RichLocation reference) {
		return getRelativeLocation(subject, reference, false, true);
	}

	/**
	 * Find the relative start and end coordinates of an enclosed location given
	 * the coordinates of the enclosing location, retaining gaps in the
	 * reference and subject alike
	 *
	 * @param subject
	 *            inner location
	 * @param reference
	 *            enclosed location
	 * @return relative coordinates of inner location
	 */
	public static RichLocation getRelativeLocationGappedWithAllGaps(
			RichLocation subject, RichLocation reference) {
		return getRelativeLocation(subject, reference, true, false);
	}

	/**
	 * Find the relative start and end coordinates of an enclosed location given
	 * the coordinates of the enclosing location, using gaps only from the
	 * subject
	 *
	 * @param subject
	 *            inner location
	 * @param reference
	 *            enclosed location
	 * @return relative coordinates of inner location
	 */
	public static RichLocation getRelativeLocationGapped(RichLocation subject,
			RichLocation reference) {
		return getRelativeLocation(subject, reference, true, true);
	}

	public static RichLocation getRelativeLocation(RichLocation subject,
			RichLocation reference, boolean maintainGaps, boolean strict) {

		if (hasInnerLocations(subject)) {
			RichLocation relativeLocation = null;
			for (RichLocation relativeSubject : sortAndMergeLocation(subject)) {
				RichLocation relativeSubSubject = getRelativeLocation(
						relativeSubject, reference, maintainGaps, strict);
				if (relativeLocation == null) {
					relativeLocation = relativeSubSubject;
				} else {
					if (!maintainGaps) {
						relativeLocation = construct(RichLocation.Tools
								.flatten(unite(relativeLocation,
										relativeSubSubject)));
					} else {
						relativeLocation = construct(RichLocation.Tools
								.flatten(construct(new RichLocation[] {
										relativeLocation, relativeSubSubject })));
					}
				}
			}
			return relativeLocation;
		} else {

			Iterator<RichLocation> locI = LocationUtils.sortAndMergeLocation(
					reference).iterator();

			int offset = 0;
			int start = 0;
			int end = 0;
			boolean foundStart = false;
			boolean foundEnd = false;
			List<RichLocation> relativeLocations = CollectionUtils
					.createArrayList();
			int rank = 0;
			RichLocation refLoc = null;
			RichLocation lastRefLoc = null;
			boolean lastLoc = false;
			boolean firstLoc = true;
			boolean overlapsGap = false; // monitor if we overlap a gap at
			// any point

			while (locI.hasNext()) {

				boolean fuzzyStart = false;
				boolean fuzzyEnd = false;
				if (strict) {
					foundStart = false;
					foundEnd = false;
				}

				// remember the last reference join and note where we are
				if (refLoc != null) {
					firstLoc = false;
					lastRefLoc = refLoc;
				}

				refLoc = locI.next();
				lastLoc = !locI.hasNext();

				// convert to negative coordinates if
				// 1. reference is circular
				// 2. reference overlaps the origin
				// 3. subject
				if (refLoc.getCircularLength() != -1 && refLoc.getMin() < 1
						&& subject.getMin() > 0) {
					RichLocation newSubject = convertToRelativeCoordinates(subject);
					if (newSubject.overlaps(refLoc)) {
						subject = newSubject;
					}
				}

				// if we've had more than one join before this one, set offset
				// correctly
				if (lastRefLoc != null) {
					// increase offset by length of last join
					offset += getLocationLength(lastRefLoc);
					// if keeping the gaps, also increase by gap size
					if (maintainGaps) {
						if (isComplement(refLoc)) {
							offset += lastRefLoc.getMin() - refLoc.getMax() - 1;
						} else {
							offset += refLoc.getMin() - lastRefLoc.getMax() - 1;
						}
					}
				}

				// to record inner locations correctly, set default relative
				// coordinates of the reference
				start = offset + 1;
				end = getLocationLength(refLoc) + offset;

				if (isComplement(refLoc)) {
					// for a reverse strand reference, we use the min and max
					// inversely
					if (!foundStart) {
						if (subject.getMax() >= refLoc.getMin()
								&& (!strict || subject.getMax() <= refLoc
										.getMax())) {
							foundStart = true;
							start = refLoc.getMax() - subject.getMax() + 1
									+ offset;
							fuzzyStart = subject.getMaxPosition().getFuzzyEnd();
						}
					}
					if (!foundEnd) {
						if (subject.getMin() <= refLoc.getMax()
								&& ((!strict && lastLoc) || subject.getMin() >= refLoc
										.getMin())) {
							end = refLoc.getMax() - subject.getMin() + 1
									+ offset;
							fuzzyEnd = subject.getMinPosition().getFuzzyStart();
							foundEnd = true;
						}
					}

				} else {
					// for a forward strand reference, we use the min and max
					// correctly
					if (!foundStart) {
						// subject start must be downstream of the end of the
						// reference
						if (subject.getMin() <= refLoc.getMax()
								&& (!strict || subject.getMin() >= refLoc
										.getMin())) {
							// correct the start relative to the reference start
							start = subject.getMin() - refLoc.getMin() + 1
									+ offset;
							fuzzyStart = subject.getMinPosition()
									.getFuzzyStart();
							foundStart = true;
						}
					}
					if (!foundEnd) {
						// subject end must be upstream of the start of the
						// reference
						// AND either this is the last last, or downstream of
						// the reference end
						if ((subject.getMax() >= refLoc.getMin())
								&& ((lastLoc && !strict) || subject.getMax() <= refLoc
										.getMax())) {
							foundEnd = true;
							fuzzyEnd = subject.getMaxPosition().getFuzzyEnd();
							end = subject.getMax() - refLoc.getMin() + 1
									+ offset;
						}
					}
				}

				boolean addLoc = (foundStart || foundEnd);
				if (strict) {
					// if strict, both start AND end must be within
					if (foundStart && foundEnd) {
						addLoc = true;
					} else if (foundStart || foundEnd) {
						overlapsGap = true;
						addLoc = false;
					} else {
						addLoc = false;
					}
				}

				if (addLoc) {
					// if we found that start, we need to continue adding
					// locations to our list (this includes inners)
					SimpleRichLocation newLoc = new SimpleRichLocation(
							new SimplePosition(fuzzyStart, false, start),
							new SimplePosition(false, fuzzyEnd, end),
							++rank,
							(isComplement(refLoc) == isComplement(subject)) ? Strand.POSITIVE_STRAND
									: Strand.NEGATIVE_STRAND);
					newLoc.setCircularLength(refLoc.getCircularLength());
					relativeLocations.add(newLoc);
				}

				if (foundStart && foundEnd)
					break;

			}
			if (relativeLocations.size() == 0 && overlapsGap) {
				throw new FeatureLocationGapOverlapException("Subject "
						+ subject + " overlaps gap in reference " + refLoc);
			}
			if (!maintainGaps) {
				return unite(relativeLocations);
			} else {
				return construct(relativeLocations);
			}
		}
	}

	/**
	 * @param subject
	 * @return
	 */
	public static boolean isComplement(RichLocation subject) {
		return subject.getStrand() == RichLocation.Strand.NEGATIVE_STRAND;
	}

	/**
	 * Attempts to convert a plain Location into a RichLocation.
	 *
	 * @param l
	 * @param complement
	 * @return the converted location
	 * @throws IllegalArgumentException
	 */

	public static RichLocation enrich(Location l, boolean complement)
			throws IllegalArgumentException {
		RichLocation.Strand strand = (complement ? RichLocation.Strand.NEGATIVE_STRAND
				: RichLocation.Strand.POSITIVE_STRAND);
		return enrich(l, strand);
	}

	/**
	 * Attempts to convert a plain Location into a RichLocation.
	 *
	 * This is a rewritten version of RichLocation.Tools.enrich which doesn't
	 * cover CircularLocation
	 *
	 * @param l
	 *            the location to convert
	 * @param strand
	 * @return the converted location
	 * @throws IllegalArgumentException
	 */
	public static RichLocation enrich(Location l, RichLocation.Strand strand)
			throws IllegalArgumentException {

		// Dummy case where location is already enriched
		if (l instanceof RichLocation) {
			return (RichLocation) l;
		}
		// Compound case
		else if (l instanceof MergeLocation || !l.isContiguous()) {
			List members = new ArrayList();
			for (Iterator i = l.blockIterator(); i.hasNext();) {
				Location member = (Location) i.next();
				members.add(enrich(member, strand));
			}
			RichLocation convertedLocation = RichLocation.Tools
					.construct(members);
			if (l instanceof CircularLocation) {
				CircularLocation cLocation = (CircularLocation) l;
				convertedLocation.setCircularLength(cLocation.getLength());
			}
			return convertedLocation;
		}
		// Fuzzy single points
		else if (l instanceof FuzzyPointLocation) {
			FuzzyPointLocation f = (FuzzyPointLocation) l;
			Position pos = new SimplePosition(f.hasBoundedMin(),
					f.hasBoundedMax(), f.getMin(), f.getMax(),
					Position.IN_RANGE);
			return new SimpleRichLocation(pos, 0, strand); // 0 for no rank
		}
		// Fuzzy ranges
		else if (l instanceof FuzzyLocation) {
			FuzzyLocation f = (FuzzyLocation) l;
			Position start = new SimplePosition(!f.hasBoundedMin(), false,
					f.getMin());
			Position end = new SimplePosition(false, !f.hasBoundedMax(),
					f.getMax());
			return new SimpleRichLocation(start, end, 0, strand); // 0 for no
			// rank
		}
		// Normal ranges
		else if (l instanceof RangeLocation) {
			RangeLocation r = (RangeLocation) l;
			Position start = new SimplePosition(false, false, r.getMin());
			Position end = new SimplePosition(false, false, r.getMax());
			return new SimpleRichLocation(start, end, 0, strand); // 0 for no
			// rank
		}
		// Normal points
		else if (l instanceof PointLocation) {
			PointLocation p = (PointLocation) l;
			Position pos = new SimplePosition(false, false, p.getMin());
			return new SimpleRichLocation(pos, 0, strand); // 0 for no rank
		}
		// Empty locations
		else if (l.toString().equals("{}")) {
			return RichLocation.EMPTY_LOCATION;
		}
		// All other cases
		else {
			throw new IllegalArgumentException(
					"Unable to enrich locations of type " + l.getClass());
		}
	}

	/**
	 * Attempts to convert a fuzzy RichLocation into a FuzzyLocation or into a
	 * FuzzyPointLocation. Therefore, it does the contrary of enrich method, but
	 * only deals with FuzzyLocation
	 *
	 * @param l
	 *            the RichLocation to convert into a Location
	 * @return the converted location as a FuzzyLocation or as a
	 *         FuzzyPointLocation
	 * @throws IllegalArgumentException
	 *             if the input location doesn't have any fuzzy boundaries
	 */
	public static Location derichFuzzyLocation(RichLocation l)
			throws IllegalArgumentException {

		if ((!(l.getMinPosition().getFuzzyStart()))
				&& (!(l.getMaxPosition().getFuzzyEnd()))) {
			throw new IllegalArgumentException(
					"Operation not supported for non-fuzzy location of type "
							+ l.getClass());
		}

		int outerMin = -1;
		int outerMax = -1;
		int innerMin = l.getMinPosition().getEnd();
		int innerMax = l.getMaxPosition().getStart();
		if (l.getMaxPosition().getFuzzyEnd()) {
			outerMax = Integer.MAX_VALUE;
		} else {
			outerMax = l.getMaxPosition().getEnd();
		}

		if (l.getMinPosition().getFuzzyStart()) {
			outerMin = Integer.MIN_VALUE;
		} else {
			outerMin = l.getMinPosition().getStart();
		}

		if (innerMin != innerMax) {

			// It is a range location

			FuzzyLocation fuzzyLocation = new FuzzyLocation(outerMin, outerMax,
					innerMin, innerMax, l.getMinPosition().getFuzzyStart(), l
							.getMaxPosition().getFuzzyEnd(),
					FuzzyLocation.RESOLVE_INNER);

			return fuzzyLocation;
		} else {

			// TODO: Not sure what to do in the case, both, start and end are
			// fuzzy

			// It is a point location

			if (l.getMinPosition().getFuzzyStart()) {
				FuzzyPointLocation fuzzyLocation = new FuzzyPointLocation(
						outerMin, innerMin, FuzzyPointLocation.RESOLVE_MAX);
				return fuzzyLocation;
			} else if (l.getMinPosition().getFuzzyEnd()) {
				FuzzyPointLocation fuzzyLocation = new FuzzyPointLocation(
						innerMax, outerMax, FuzzyPointLocation.RESOLVE_MIN);
				return fuzzyLocation;
			} else {
				throw new IllegalArgumentException(
						"Operation not supported for point location with start and end being fuzzy, "
								+ "and of type " + l.getClass());
			}
		}

	}

	protected static RichLocation getProteinSubLocation(RichLocation subject,
			RichLocation reference, boolean strict) {
		RichLocation cLoc = getRelativeLocation(subject, reference, false,
				strict);
		// convert to triplet based
		// check start is triplet based
		int start = cLoc.getMin() - 1;
		if (start % 3 != 0) {
			throw new TripletLocationException("Start of location " + cLoc
					+ " is not triplet based");
		} else {
			start = (start / 3) + 1;
		}
		int end = start;
		if (cLoc.getMax() != cLoc.getMin()) {
			end = cLoc.getMax() - 3;
			if (end % 3 != 0) {
				throw new TripletLocationException("End of location " + cLoc
						+ " is not triplet based");
			} else {
				end = (end / 3) + 1;
			}
		}
		return new SimpleRichLocation(new SimplePosition(start),
				new SimplePosition(end), 1);
	}

	/**
	 * @param subject
	 *            genomic location of protein subsequence
	 * @param reference
	 *            genomic location of coding sequence
	 * @return protein coordinate based location of subsequence
	 */
	public static RichLocation getProteinSubLocationWithAllGaps(
			RichLocation subject, RichLocation reference) {
		return getProteinSubLocation(subject, reference, false);
	}

	/**
	 * @param subject
	 *            genomic location of protein subsequence
	 * @param reference
	 *            genomic location of coding sequence
	 * @return protein coordinate based location of subsequence
	 */
	public static RichLocation getProteinSubLocation(RichLocation subject,
			RichLocation reference) {
		return getProteinSubLocation(subject, reference, true);
	}

	public static RichLocation getOuterLocation(RichLocation location) {
		if (countInnerLocations(location) > 1) {
			RichLocation loc = new SimpleRichLocation(new SimplePosition(
					location.getMin()), new SimplePosition(location.getMax()),
					1, location.getStrand());
			loc.setCircularLength(location.getCircularLength());
			return loc;
		} else {
			return location;
		}
	}

	/**
	 * Reduce the supplied location by merging overlapping or abutting locations
	 *
	 * @param location
	 *            set to unite
	 * @return united set
	 */
	public static RichLocation unite(RichLocation location) {
		if (!hasInnerLocations(location)) {
			return location;
		}
		RichLocation loc = null;
		for (RichLocation subLoc : sortLocation(location)) {
			subLoc = unite(subLoc);
			if (loc == null) {
				loc = unite(subLoc);
			} else {
				loc = unite(subLoc, loc);
			}
		}
		return loc;
	}

	/**
	 * Reduce the set of supplied locations by merging overlapping or abutting
	 * locations
	 *
	 * @param locations
	 *            set to unite
	 * @return united set
	 */
	public static RichLocation unite(RichLocation... locations) {
		List<RichLocation> locs = Arrays.asList(locations);
		return unite(locs);
	}

	/**
	 * Reduce the set of supplied locations by merging abutting locations
	 *
	 * @param locs
	 *            set to unite
	 * @return united set
	 */
	public static RichLocation unite(List<RichLocation> iLocs) {
		List<RichLocation> locs = CollectionUtils.createArrayList();
		for (RichLocation loc : iLocs) {
			locs.addAll(sortLocation(loc));
		}
		Collections.sort(locs, RichLocation.naturalOrder);
		List<RichLocation> out = CollectionUtils.createArrayList();
		int n = -1; // out counter
		for (int i = 0; i < locs.size(); i++) {
			if (i == 0) {
				// always copy the first one over
				out.add(++n, locs.get(i));
			} else {
				RichLocation last = out.get(n);
				RichLocation next = locs.get(i);
				if (last.getMax() + 1 == next.getMin()) {
					out.set(n, new SimpleRichLocation(last.getMinPosition(),
							next.getMaxPosition(), n, last.getStrand()));
				} else {
					out.add(++n, locs.get(i));
				}
			}
		}
		// for (Iterator<RichLocation> locI = locs.iterator(); locI.hasNext();)
		// {
		// RichLocation curr = locI.next();
		// if (locI.hasNext()) {
		// RichLocation next = locI.next();
		// }
		// if (next.getStrand() == curr.getStrand()
		// && next.getMin() - 1 == curr.getMax()) {
		// out.add(new SimpleRichLocation(curr.getMinPosition(), next
		// .getMaxPosition(), ++rank, curr.getStrand()));
		// } else if (next.getStrand() == curr.getStrand()
		// && next.getMax() == curr.getMin()-1) {
		// out.add(new SimpleRichLocation(curr.getMaxPosition(), next
		// .getMinPosition(), ++rank, curr.getStrand()));
		// } else {
		// curr.setRank(++rank);
		// out.add(curr);
		// next.setRank(++rank);
		// out.add(next);
		// }
		// } else {
		// curr.setRank(++rank);
		// out.add(curr);
		// }
		// }
		return construct(out);
	}

	/**
	 * Convert a location of the form n..m into the form n..x,1..m where x is
	 * the circular lenght
	 *
	 * @param outer
	 *            circular location with outer bounds min>max
	 * @return compound location split over origin
	 */
	public static RichLocation splitCircularLocation(RichLocation outer) {
		if (outer.getCircularLength() > 0) {
			if (outer.getMax() > outer.getMin()) {
				return outer;
			} else {
				List<RichLocation> locs = Arrays.asList(new RichLocation[] {
						new SimpleRichLocation(outer.getMinPosition(),
								new SimplePosition(outer.getCircularLength()),
								1, outer.getStrand()),
						new SimpleRichLocation(new SimplePosition(1), outer
								.getMaxPosition(), 2, outer.getStrand()) });
				RichLocation newLoc = new RankedCompoundRichLocation(locs);
				newLoc.setCircularLength(outer.getCircularLength());
				return newLoc;
			}
		} else {
			return outer;
		}
	}

	public static boolean encloses(RichLocation outer, RichLocation inner) {
		boolean startFound = false;
		boolean endFound = false;
		for (RichLocation loc : sortLocation(outer)) {
			if (startFound && endFound)
				break;
			if (loc.contains(inner.getMin())) {
				startFound = true;
			}
			if (loc.contains(inner.getMax())) {
				endFound = true;
			}
		}
		return startFound && endFound;
	}

	/**
	 * @param loc
	 *            location to modify end of
	 * @param shift
	 *            amount to shift end by (relative to size of location, so -3
	 *            means shrink by 3, 3 means grow by 3)
	 * @return modified location
	 */
	public static RichLocation adjustLocationEnd(RichLocation loc, int shift) {
		return adjustLocation(loc, shift, true);
	}

	/**
	 * @param loc
	 *            location to modify start of
	 * @param shift
	 *            amount to shift start by (relative to size of location, so -3
	 *            means shrink by 3, 3 means grow by 3)
	 * @return modified location
	 */
	public static RichLocation adjustLocationStart(RichLocation loc, int shift) {
		return adjustLocation(loc, shift, false);
	}

	protected static RichLocation adjustLocation(RichLocation loc, int shift,
			boolean end) {
		List<RichLocation> locs = LocationUtils.sortLocation(loc);
		if (locs.size() > 1) {
			loc = end ? CollectionUtils.getLastElement(locs, null)
					: CollectionUtils.getFirstElement(locs, null);
		}
		Position min = loc.getMinPosition();
		Position max = loc.getMaxPosition();
		boolean isComp = LocationUtils.isComplement(loc);
		if ((end && isComp) || (!end && !isComp)) {
			min = new SimplePosition(false, false, min.getStart() - shift,
					min.getEnd() - shift, null);
		} else {
			max = new SimplePosition(false, false, max.getStart() + shift,
					max.getEnd() + shift, null);
		}
		RichLocation newLastLoc = new SimpleRichLocation(min, max,
				loc.getRank(), loc.getStrand());
		newLastLoc.setCircularLength(loc.getCircularLength());
		if (locs.size() > 1) {
			if (end) {
				locs.set(locs.size() - 1, newLastLoc);
			} else {
				locs.set(0, newLastLoc);
			}
			return LocationUtils.construct(locs);
		} else {
			return newLastLoc;
		}
	}

	/**
	 * @param location1
	 *            location to test against
	 * @param location2
	 *            location to test with
	 * @return true if location2 is wholly contained with location1
	 */
	public static boolean contains(RichLocation location1,
			RichLocation location2) {
		boolean contains = true;
		for (RichLocation loc : flatten(location2)) {
			if (!location1.contains(loc)) {
				contains = false;
				break;
			}
		}
		return contains;
	}

	/**
	 * Test if location contains inner locations which overlap
	 *
	 * @param loc
	 * @return true if at least one inner join overlaps
	 */
	public static boolean hasInternalOverlap(RichLocation testLoc) {
		boolean hasOverlap = false;
		RichLocation lastLoc = null;
		for (RichLocation loc : sortLocation(testLoc)) {
			if (lastLoc != null && loc.overlaps(lastLoc)) {
				hasOverlap = true;
				break;
			}
			lastLoc = loc;
		}
		return hasOverlap;
	}

}
