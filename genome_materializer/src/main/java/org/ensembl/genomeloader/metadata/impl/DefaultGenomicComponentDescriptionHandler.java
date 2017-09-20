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

package org.ensembl.genomeloader.metadata.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.metadata.GenomeMetaData;
import org.ensembl.genomeloader.metadata.GenomicComponentDescriptionHandler;
import org.ensembl.genomeloader.metadata.GenomicComponentMetaData;
import org.ensembl.genomeloader.metadata.GenomicComponentMetaData.GenomicComponentType;

public class DefaultGenomicComponentDescriptionHandler implements GenomicComponentDescriptionHandler, Serializable {

    private static final long serialVersionUID = 3021506479423171650L;
    protected final static Pattern WGS_DES = Pattern.compile(".*? +[^ ]+ +([^ ,]+), whole genome shotgun.*",
            Pattern.CASE_INSENSITIVE);
    protected final static Pattern CHR_DES = Pattern.compile(".* chromosome[ :]+([^ ,.]+)?.*",
            Pattern.CASE_INSENSITIVE);
    protected final static Pattern LG_DES = Pattern.compile(".* linkage group ([^ ,.]+)?.*", Pattern.CASE_INSENSITIVE);
    protected final static Pattern LG2_DES = Pattern.compile(".* linkage group LG ([^ ,.]+)?.*",
            Pattern.CASE_INSENSITIVE);
    protected final static Pattern PLA2_DES = Pattern.compile(".*plasmid[ :]+([^, ]+).*", Pattern.CASE_INSENSITIVE);
    protected final static Pattern PLA3_DES = Pattern.compile(".*[: ]+([^ :]+) plasmid.*", Pattern.CASE_INSENSITIVE);
    protected final static Pattern PLA_DES = Pattern.compile(".*[: ]+([^ ]+plasmid) .*", Pattern.CASE_INSENSITIVE);
    protected final static Pattern MIT_DES = Pattern.compile(".*()(mitochondrial|mitochondrion).*",
            Pattern.CASE_INSENSITIVE);
    protected final static Pattern CHL_DES = Pattern.compile(".*()(chloro)?plast(id)?.*", Pattern.CASE_INSENSITIVE);
    protected final static Pattern SEG_DES = Pattern.compile(".* segment[ :]+(.+)", Pattern.CASE_INSENSITIVE);
    protected final static Pattern SCA_DES = Pattern.compile(".* scaffold[ :]+([^ ,]+).*", Pattern.CASE_INSENSITIVE);
    protected final static Pattern CON_DES = Pattern.compile(".* contig[ :]+([^ ,]+).*", Pattern.CASE_INSENSITIVE);
    protected final static Pattern GEN_DES = Pattern.compile(".* complete genome.*", Pattern.CASE_INSENSITIVE);
    protected final static Pattern CHR2_DES = Pattern.compile(".* complete chromosome.*", Pattern.CASE_INSENSITIVE);
    protected final static Pattern CHR3_DES = Pattern.compile(".* chromosome complete sequence.*",
            Pattern.CASE_INSENSITIVE);

    protected static final Map<Pattern, String> defaultMap = new HashMap<Pattern, String>() {
        {
            put(CHR_DES, CHROMOSOME);
            put(LG_DES, CHROMOSOME);
            put(PLA_DES, StringUtils.EMPTY);
            put(PLA2_DES, StringUtils.EMPTY);
            put(PLA3_DES, StringUtils.EMPTY);
            put(MIT_DES, MITOCHONDRION);
            put(CHL_DES, PLASTID);
            put(SEG_DES, StringUtils.EMPTY);
            put(SCA_DES, StringUtils.EMPTY);
            put(CON_DES, StringUtils.EMPTY);
            put(WGS_DES, StringUtils.EMPTY);
        }
    };
    protected static final Map<Pattern, GenomicComponentType> typeMap = new HashMap<Pattern, GenomicComponentType>() {
        {
            put(CHR_DES, GenomicComponentType.CHROMOSOME);
            put(LG2_DES, GenomicComponentType.CHROMOSOME);
            put(LG_DES, GenomicComponentType.CHROMOSOME);
            put(PLA_DES, GenomicComponentType.PLASMID);
            put(PLA2_DES, GenomicComponentType.PLASMID);
            put(PLA3_DES, GenomicComponentType.PLASMID);
            put(MIT_DES, GenomicComponentType.CHROMOSOME);
            put(CHL_DES, GenomicComponentType.CHROMOSOME);
            put(SEG_DES, GenomicComponentType.SUPERCONTIG);
            put(SCA_DES, GenomicComponentType.SUPERCONTIG);
            put(CON_DES, GenomicComponentType.SUPERCONTIG);
            put(WGS_DES, GenomicComponentType.SUPERCONTIG);
            put(GEN_DES, GenomicComponentType.CHROMOSOME);
            put(CHR2_DES, GenomicComponentType.CHROMOSOME);
            put(CHR3_DES, GenomicComponentType.CHROMOSOME);
        }

    };

    /**
     * Parse out the description using the supplied pattern
     * 
     * @param md
     * @param description
     * @param pattern
     * @param defaultName
     */
    protected boolean parseDescription(GenomicComponentMetaData md, String description, Pattern pattern,
            String defaultName) {
        boolean success = false;
        if (md.getComponentType() == null) {
            final Matcher m = pattern.matcher(description);
            if (m.matches()) {
                md.setComponentType(typeMap.get(pattern));
                if (md.getComponentType() == null) {
                    LogFactory.getLog(GenomicComponentMetaData.class).warn("Can't find type for pattern " + pattern);
                }
                if (m.groupCount() >= 1 && !StringUtils.isEmpty(m.group(1))) {
                    md.setName(m.group(1));
                } else {
                    md.setName(defaultName);
                }
                success = true;
            }
        }
        return success;
    }

    public void parseComponentDescription(GenomicComponentMetaData md) {
        parseComponentDescription(md, md.getDescription());
    }

    protected final static Pattern[] PATTERNS = { LG2_DES, LG_DES, PLA_DES, PLA2_DES, PLA3_DES, MIT_DES, CHL_DES,
            SEG_DES, SCA_DES, CON_DES, CHR_DES, WGS_DES };

    /**
     * Attempt to parse out the description using a series of patterns for both
     * proteomes and ENA descriptions
     * 
     * @param md
     * @param description
     */
    public void parseComponentDescription(GenomicComponentMetaData md, String description) {
        boolean parsed = false;
        if ((GenomeMetaData.BAC_SUPERREGNUM.equalsIgnoreCase(md.getGenomeMetaData().getSuperregnum())
                        || GenomeMetaData.EUBAC_SUPERREGNUM.equalsIgnoreCase(md.getGenomeMetaData().getSuperregnum())
                        || GenomeMetaData.ARC_SUPERREGNUM.equalsIgnoreCase(md.getGenomeMetaData().getSuperregnum()))
                && !description.matches(".*[Pp]lasmid.*")) {
            parsed = parseDescription(md, description, GEN_DES, CHROMOSOME);
            if (!parsed) {
                parsed = parseDescription(md, description, CHR2_DES, CHROMOSOME);
            }
            if (!parsed) {
                parsed = parseDescription(md, description, CHR3_DES, CHROMOSOME);
            }
        } else {
            // do nothing
        }
        if (!parsed) {
            for (final Pattern pattern : PATTERNS) {
                if (parseDescription(md, description, pattern, defaultMap.get(pattern))) {
                    break;
                }
            }
        }
        if (StringUtils.isEmpty(md.getName())) {
            md.setName(md.getAccession());
        }
        if (md.getComponentType() != GenomicComponentType.CHROMOSOME
                && md.getName().toLowerCase().matches(CHROMOSOME.toLowerCase())) {
            md.setComponentType(GenomicComponentType.CHROMOSOME);
        }
        if (md.getComponentType() == null) {
            md.setComponentType(GenomicComponentType.SUPERCONTIG);
        }

    }

}
