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
 * File: SequenceUtils.java
 * Created by: dstaines
 * Created on: Nov 28, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.genomebuilder.model.sequence;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.RNATools;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.symbol.AtomicSymbol;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.symbol.SymbolListViews;
import org.biojava.bio.symbol.TranslationTable;
import org.biojavax.bio.seq.RichLocation;
import org.ensembl.genomeloader.util.biojava.LocationUtils;
import org.ensembl.genomeloader.util.collections.CollectionUtils;

/**
 * Collection of general use helper utilities for working with sequences
 *
 * @author dstaines
 *
 */
public class SequenceUtils {
	private static Log log;

	private static Log getLog() {
		if (log == null) {
			log = LogFactory.getLog(SequenceUtils.class);
		}
		return log;
	}

	public static final String STOP = "*";
	public static final String STOP_TLA = "Ter";
	public static final String OTHER_AA = "X";
	public static final String OTHER_TLA = "Xaa";

	/**
	 * Check the sequence for internal stop codons
	 *
	 * @param sequence
	 *            string representing amino acid sequence
	 * @return
	 */
	public static boolean checkTranslationStopCodons(String sequence) {
		int i = sequence.indexOf(STOP);
		if (i == -1) {
			// no stop codon found
			return true;
		} else {
			if (i == (sequence.length() - 1)) {
				// stop codon is at end
				return true;
			} else {
				// stop codon is internal
				getLog().warn(
						"Sequence has internal stop codon at position "
								+ (i + 1) + ": " + sequence);
				return false;
			}
		}
	}

	/**
	 * Translate sequence corresponding to location and check for internal stop
	 * codons
	 *
	 * @param seq
	 * @param location
	 * @return true
	 */
	public static boolean checkTranslation(Sequence seq, RichLocation location,
			TranslationTable table) {
		return checkTranslationStopCodons(getTranslatedSequence(seq, location,
				table));
	}

	public static boolean checkTranslation(Sequence seq, RichLocation location) {
		return checkTranslation(seq, location, UNIVERSAL);
	}

	/**
	 * Extract and translate a portion of coding sequence
	 *
	 * @param seq
	 *            genomic sequence
	 * @param location
	 *            location of coding region
	 * @return protein sequence
	 */
	public static String getTranslatedSequence(Sequence seq,
			RichLocation location) {
		return getTranslatedSequence(seq, location, UNIVERSAL);
	}

	public static String getTranslatedSequence(Sequence seq,
			RichLocation location, TranslationTable table) {
		return getTranslatedSequence(table, SequenceUtils
				.getSequenceStringForLocation(seq, location));
	}

	public static String getTranslatedSequence(TranslationTable table,
			String seqStr) {
		if ((seqStr.length() % 3) != 0) {
			String msg = "Sequence does not encode a triplet";
			getLog().error(msg);
			throw new SequenceTranslationException(msg);
		} else {
			String aaSeq = null;
			try {
				getLog().debug("Translating " + seqStr.length() + " sequence");
				aaSeq = SequenceUtils.getSequenceTranslation(seqStr, table);
			} catch (IllegalSymbolException e) {
				String msg = "Could not translate sequence";
				getLog().error(msg, e);
				throw new SequenceTranslationException(msg, e);
			} catch (IllegalAlphabetException e) {
				String msg = "Could not translate sequence";
				getLog().error(msg, e);
				throw new SequenceTranslationException(msg, e);
			}
			if (StringUtils.isEmpty(aaSeq)) {
				String msg = "Empty translated sequence";
				getLog().error(msg);
				throw new SequenceTranslationException(msg);
			} else {
				return aaSeq;
			}
		}
	}

	/**
	 * Extract the sequence corresponding to a supplied location as a string
	 *
	 * @param sequence
	 * @param location
	 * @return sequence string
	 * @throws IllegalSymbolException
	 * @throws IllegalAlphabetException
	 */
	public static String getSequenceStringForLocation(Sequence sequence,
			RichLocation location) {
		try {
			StringBuilder subseq = new StringBuilder();
			// 1.2 substring nucleotide sequence given coords
			List<RichLocation> locs = LocationUtils.flatten(location);
			// shuffle round split locations overlapping origin if not already
			// shuffled
			if (location.getCircularLength() != -1 && locs.size() > 1) {
				RichLocation loc1 = locs.get(0);
				RichLocation loc2 = locs.get(locs.size() - 1);
				if (loc2.getMax() == location.getCircularLength()
						&& loc1.getMin() == 1
						&& loc1.getStrand() == loc2.getStrand()) {
					locs.remove(loc2);
					locs.add(0, loc2);
				}
			}
			for (RichLocation subLoc : locs) {
				int len = subLoc.getMax() - subLoc.getMin() + 1;
				if (len < 1) {
					throw new SequenceParserUncheckedException(
							"Cannot extract a subsequence with length less than zero for location "
									+ location);
				}
				subseq.append(sequence.getSequence(subLoc.getMin(), len));
			}
			SymbolList symL = DNATools.createDNA(subseq.toString().trim());
			// 1.3 reverse complement if needed
			if (RichLocation.Strand.NEGATIVE_STRAND == location.getStrand()) {
				// reverse complement it
				symL = DNATools.reverseComplement(symL);
			}
			return symL.seqString();
		} catch (IllegalSymbolException e) {
			String msg = "Could not extract sequence for location " + location;
			getLog().error(msg, e);
			throw new SequenceTranslationException(msg, e);
		} catch (IllegalAlphabetException e) {
			String msg = "Could not extract sequence for location " + location;
			getLog().error(msg, e);
			throw new SequenceTranslationException(msg, e);
		} catch (StringIndexOutOfBoundsException e) {
			String msg = "Could not extract sequence for location " + location;
			getLog().error(msg, e);
			throw new SequenceTranslationException(msg, e);
		}
	}

	private static TranslationTable UNIVERSAL = RNATools
			.getGeneticCode(TranslationTable.UNIVERSAL);

	public static String getSequenceTranslation(String seq)
			throws IllegalSymbolException, IllegalAlphabetException {
		return getSequenceTranslation(seq, UNIVERSAL);
	}

	/**
	 * Translate the attached sequence
	 *
	 * @param seq
	 * @return protein sequence
	 * @throws IllegalSymbolException
	 * @throws IllegalAlphabetException
	 */
	public static String getSequenceTranslation(String seq,
			TranslationTable table) throws IllegalSymbolException,
			IllegalAlphabetException {
		SymbolList symL = DNATools.createDNA(seq);
		// 1.4 translate
		// transcribe to RNA (after biojava 1.4 use this method instead)
		symL = DNATools.toRNA(symL);
		// translate to protein
		// symL = RNATools.translate(symL);
		// veiw the RNA sequence as codons, this is done internally by
		// RNATool.translate()
		symL = SymbolListViews.windowedSymbolList(symL, 3);
		// translate using supplied table
		SymbolList protein = SymbolListViews.translate(symL, table);
		return protein.seqString();
	}

	/**
	 * Extract the sequence corresponding to a supplied location
	 *
	 * @param sequence
	 * @param location
	 * @return sequence as object
	 * @throws IllegalSymbolException
	 * @throws IllegalAlphabetException
	 */
	public static Sequence getSequenceForLocation(Sequence sequence,
			RichLocation location) throws IllegalSymbolException,
			IllegalAlphabetException {
		Sequence seq = new Sequence();
		seq.appendSequence(getSequenceStringForLocation(sequence, location));
		return seq;
	}

	private static Map<String, String> aminoAcids = null;

	private static Map<String, String> getAminoAcids() {
		if (aminoAcids == null) {
			try {
				aminoAcids = CollectionUtils.createHashMap(23);
				FiniteAlphabet a = ProteinTools.getTAlphabet();
				SymbolTokenization st = a.getTokenization("token");
				for (Iterator i = a.iterator(); i.hasNext();) {
					AtomicSymbol s = (AtomicSymbol) i.next();
					aminoAcids.put(StringUtils.capitalize(s.getName()
							.toLowerCase()), st.tokenizeSymbol(s));
				}
				aminoAcids.put(OTHER_TLA, OTHER_AA);
				aminoAcids.put(STOP_TLA, STOP);
				aminoAcids.put("Asx", "B");
				aminoAcids.put("Glx", "Z");
			} catch (BioException e) {
				throw new SequenceParserUncheckedException(
						"Could not retrieve list of amino acids codes", e);
			}
		}
		return aminoAcids;
	}

	/**
	 * Convert a TLA amino acid code to the single letter equivalent
	 *
	 * @param tla
	 *            amino acid TLA
	 * @return single amino acid code
	 * @throws BioException
	 */
	public static String aminoAcidTlaToSingle(String tla) {
		String aa = getAminoAcids().get(
				StringUtils.capitalize(tla.toLowerCase()));
		if (aa == null)
			aa = OTHER_AA;
		return aa;
	}

	/**
	 * Convert a single amino acid code to the TLA equivalent
	 *
	 * @param aa
	 *            single amino acid code
	 * @return amino acid TLA or
	 * @throws BioException
	 */
	public static String aminoAcidSingleToTla(String aa) {
		String tla = OTHER_TLA;
		for (Entry<String, String> e : getAminoAcids().entrySet()) {
			if (e.getValue().equalsIgnoreCase(aa)) {
				tla = e.getKey();
				break;
			}
		}
		return tla;
	}

	/**
	 * Convert a single amino acid code to the TLA equivalent
	 *
	 * @param aa
	 *            single amino acid code
	 * @return amino acid TLA or
	 * @throws BioException
	 */
	public static String aminoAcidSingleToTla(Character aa) {
		return aminoAcidSingleToTla(String.valueOf(aa));
	}

	/**
	 * Reverse complement the supplied sequence string
	 *
	 * @param seq
	 *            DNA sequence as string
	 * @return reverse complement of supplied DNA string
	 * @throws IllegalAlphabetException
	 * @throws IllegalSymbolException
	 */
	public static String reverseComplement(String seq)
			throws IllegalAlphabetException, IllegalSymbolException {
		return DNATools.reverseComplement(DNATools.createDNA(seq)).seqString();
	}

	public static Sequence reverseComplement(Sequence seq) {
		Sequence newSeq = new Sequence(seq);
		seq.setLength(0);
		try {
			newSeq.appendSequence(DNATools.reverseComplement(
					DNATools.createDNA(seq.getSequence())).seqString());
		} catch (IllegalAlphabetException e) {
			throw new SequenceParserUncheckedException(
					"Could not revcomp sequence " + seq, e);
		} catch (IllegalSymbolException e) {
			throw new SequenceParserUncheckedException(
					"Could not revcomp sequence " + seq, e);
		}
		return newSeq;
	}

}
