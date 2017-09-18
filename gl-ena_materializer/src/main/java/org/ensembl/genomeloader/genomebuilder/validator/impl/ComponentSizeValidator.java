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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.genomebuilder.model.Genome;
import org.ensembl.genomeloader.genomebuilder.model.GenomicComponent;
import org.ensembl.genomeloader.genomebuilder.validator.ComponentSizeValidationException;
import org.ensembl.genomeloader.genomebuilder.validator.GenomeValidationException;
import org.ensembl.genomeloader.genomebuilder.validator.GenomeValidator;

/**
 * Class to check if component sequence length matches the annotated length
 * 
 * @author dstaines
 * 
 */
public class ComponentSizeValidator implements GenomeValidator {

	private Log log;

	protected Log getLog() {
		if (log == null)
			log = LogFactory.getLog(this.getClass());
		return log;
	}

	public void validateGenome(Genome genome) throws GenomeValidationException {
		for (GenomicComponent component : genome.getGenomicComponents()) {
			if (component.getSequence() != null) {
				long slen = component.getSequence().getLength();
				int mlen = component.getMetaData().getLength();
				if (mlen != slen) {
					throw new ComponentSizeValidationException("Component "
							+ component.getAccession()
							+ " is annotated with sequence length " + mlen
							+ " but has actual sequence of length " + slen);
				}
			}
		}
	}

}
