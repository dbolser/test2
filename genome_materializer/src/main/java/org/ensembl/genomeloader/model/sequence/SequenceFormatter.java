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
 * File: SequenceFormatter.java
 * Created by: dstaines
 * Created on: Feb 19, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.model.sequence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

/**
 * Base class for transformation of sequences into output strings of the correct
 * format
 *
 * @author dstaines
 *
 */
public abstract class SequenceFormatter {

	/**
	 * Write the supplied sequences as strings to the specified file
	 *
	 * @param sequences
	 * @param file
	 * @throws IOException
	 */
	public void writeSequences(Collection<Sequence> sequences, String file)
			throws IOException {
		Writer writer = new FileWriter(file);
		writeSequences(sequences, writer);
		writer.close();
	}

	/**
	 * Write the sequences as strings to the supplied writer
	 *
	 * @param sequences
	 * @param writer
	 * @throws IOException
	 */
	public void writeSequences(Collection<Sequence> sequences, Writer writer)
			throws IOException {
		BufferedWriter out = new BufferedWriter(writer);
		for (Sequence seq : sequences) {
			writeSequence(seq, writer);
		}
		out.close();
	}

	/**
	 * Write a single sequence to the specified writer, followed by a newline
	 *
	 * @param seq
	 * @param writer
	 * @throws IOException
	 */
	public void writeSequence(Sequence seq, Writer writer) throws IOException {
		writer.write(sequenceToString(seq));
		writer.write('\n');
	}

	/**
	 * Write a single sequence to the specified file
	 *
	 * @param seq
	 * @param fileName
	 * @throws IOException
	 */
	public void writeSequence(Sequence seq, File file) throws IOException {
		Writer writer = new FileWriter(file);
		writeSequence(seq, writer);
		writer.close();
	}

	/**
	 * Abstract method to be overridden by concrete classes to turn a sequence
	 * object into a string of the correct format
	 *
	 * @param sequence
	 * @return
	 */
	public abstract String sequenceToString(Sequence sequence);

	/**
	 * Split a string up into columns of a certain width
	 *
	 * @param seq
	 * @param cols
	 * @return
	 */
	protected static String formatInColumns(String seq, int cols) {
		StringBuilder s2 = new StringBuilder();
		for (int i = 0; i < seq.length(); i += cols) {
			if (i + cols > seq.length()) {
				s2.append(seq.substring(i));
			} else {
				s2.append(seq.substring(i, i + cols));
			}
			s2.append('\n');
		}
		return s2.toString();
	}

	/**
	 * Turn a properties map and a scope variable into a string of the form
	 * {KEY=value x=y}
	 *
	 * @param props
	 * @param scope
	 * @return
	 */
	protected static String formatProperties(Map<String, String> props) {
		Collection<String> propStrs = new ArrayList<String>();
		if (props != null) {
			for (Entry<String, String> e : props.entrySet()) {
				propStrs.add(e.getKey() + "=\"" + e.getValue() + "\"");
			}
		}
		if (propStrs.size() == 0) {
			return StringUtils.EMPTY;
		} else {
			return "{" + StringUtils.join(propStrs.toArray(), ' ') + "} ";
		}
	}

}
