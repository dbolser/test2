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

package org.ensembl.genomeloader.validator.impl;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.model.Genome;
import org.ensembl.genomeloader.validator.GenomeValidationException;
import org.ensembl.genomeloader.validator.GenomeValidator;

/**
 * Validator that accepts a set of validators which are then called in turn
 * @author dstaines
 *
 */
public class DelegatingGenomeValidator implements GenomeValidator {

	private Log log;
	protected Log getLog() {
		if(log==null) log = LogFactory.getLog(this.getClass());
		return log;
	}
	
	protected final Collection<GenomeValidator> validators;

	public DelegatingGenomeValidator(Collection<GenomeValidator> validators) {
		this.validators = validators;
	}

	public DelegatingGenomeValidator(GenomeValidator... validators) {
		this(Arrays.asList(validators));
	}

	public void validateGenome(Genome genome) throws GenomeValidationException {
		for(GenomeValidator validator: validators) {
			validator.validateGenome(genome);
		}
	}

}
