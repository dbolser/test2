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

package org.ensembl.genomeloader.genomebuilder.validator.impl;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.ensembl.genomeloader.genomebuilder.model.Genome;
import org.ensembl.genomeloader.genomebuilder.validator.GenomeValidationException;
import org.ensembl.genomeloader.genomebuilder.validator.GenomeValidator;

/**
 * Validator that checks the genome description does not match a blacklist
 * 
 * @author dstaines
 * 
 */
public class GenomeDescriptionValidator implements GenomeValidator {

	private Pattern[] PATTERNS = {
			Pattern.compile(".*metagenom.*", Pattern.CASE_INSENSITIVE),
			Pattern.compile(".*Targeted Locus.*", Pattern.CASE_INSENSITIVE),
			Pattern.compile(".*Partial Genome.*", Pattern.CASE_INSENSITIVE) };

	public void validateGenome(Genome genome) throws GenomeValidationException {
		if (!StringUtils.isEmpty(genome.getDescription())) {
			for (Pattern p : PATTERNS) {
				if (p.matcher(genome.getDescription()).matches()) {
					throw new GenomeValidationGeneCountException("Genome "
							+ genome.getName()
							+ " has blacklisted description \""
							+ genome.getDescription() + "\" (matches /"
							+ p.pattern() + "/)");
				}
			}
		}
	}

}
