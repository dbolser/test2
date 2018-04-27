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

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.model.Genome;
import org.ensembl.genomeloader.model.GenomicComponent;
import org.ensembl.genomeloader.util.collections.CollectionUtils;
import org.ensembl.genomeloader.validator.ComponentNameLengthValidationException;
import org.ensembl.genomeloader.validator.GenomeValidationException;
import org.ensembl.genomeloader.validator.GenomeValidator;

/**
 * Checks that components have valid names (content, length etc.)
 * @author dstaines
 *
 */
public class ComponentNameValidator implements GenomeValidator {

	private final Log log = LogFactory.getLog(ComponentNameValidator.class);

	public static final int MAX_NAME_LEN = 40;
	
	public void validateGenome(Genome genome) throws GenomeValidationException {
		Set<GenomicComponent> problems = CollectionUtils.createHashSet();
		for (GenomicComponent component : genome.getGenomicComponents()) {
			String name = component.getMetaData().getName();
			if (StringUtils.isEmpty(name)) {
				log.warn("Component found with empty name");
				problems.add(component);
			} else if (name.equals("0")) {
				log.warn("Component found with name " + name + " which is not supported by Ensembl");
				problems.add(component);
			} else if (name.length() > 40) {
				log.warn("Component found with name " + name + " over "
						+ MAX_NAME_LEN + " characters");
				problems.add(component);
			}
		}
		if (problems.size() > 0) {
			throw new ComponentNameLengthValidationException(
					"Components found with invalid names", problems);
		}
	}

}
