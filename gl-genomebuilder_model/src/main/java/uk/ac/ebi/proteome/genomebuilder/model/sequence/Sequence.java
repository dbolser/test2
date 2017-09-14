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
 * File: Sequence.java
 * Created by: dstaines
 * Created on: Feb 9, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.genomebuilder.model.sequence;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * Very simple bean to hold information and sequence contents so it can be
 * written
 *
 * @author dstaines
 *
 */
public class Sequence extends SequenceInformation {

    public Sequence(String seqStr) {
        super();
        this.seqStr.append(seqStr);
    }

    public Sequence(SequenceInformation seq) {
        super(seq);
    }

    protected StringBuilder seqStr = new StringBuilder();

    /**
     * Data type name used by ComponentMetaData
     */
    public final static String DATA_TYPE = "sequence";

    protected String sequence;

    public static final String DNA_SEQ_SOURCE_TYPE = "dnaSeqSrc";

    public Sequence() {
        super();
    }

    public void appendSequence(String sequence) {
        seqStr.append(sequence);
    }

    public String getSequence() {
        return seqStr.toString();
    }

    public void setSequence(String sequence) {
        seqStr = new StringBuilder(sequence);
    }

    public String getSequence(int offset, int length) {
        String sq = null;
        if (length == -1) {
            sq = getSequence().substring(offset - 1);
        } else {
            sq = getSequence().substring(offset - 1, offset - 1 + length);
        }
        return sq;
    }

    public String toString() {
        return ReflectionToStringBuilder.reflectionToString(this);
    }

    @Override
    public long getLength() {
        return getSequence().length();
    }

}
