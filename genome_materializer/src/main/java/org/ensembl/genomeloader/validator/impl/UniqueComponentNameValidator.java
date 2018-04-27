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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.model.Genome;
import org.ensembl.genomeloader.model.GenomicComponent;
import org.ensembl.genomeloader.model.ModelUtils.GenomicComponentRankComparator;
import org.ensembl.genomeloader.util.collections.CollectionUtils;
import org.ensembl.genomeloader.validator.ComponentNameValidationException;
import org.ensembl.genomeloader.validator.GenomeValidationException;
import org.ensembl.genomeloader.validator.GenomeValidator;

/**
 * Checks for components with duplicate names
 * 
 * @author dstaines
 *
 */
public class UniqueComponentNameValidator implements GenomeValidator {

	private final Log log = LogFactory
			.getLog(UniqueComponentNameValidator.class);

	public void validateGenome(Genome genome) throws GenomeValidationException {
		final Map<String, List<GenomicComponent>> names = CollectionUtils
				.createHashMap(genome.getGenomicComponents().size());
		for (final GenomicComponent component : genome.getGenomicComponents()) {
			final String name = component.getMetaData().getName();
			if (!names.containsKey(name)) {
				names.put(name, new ArrayList<GenomicComponent>(1));
			}
			names.get(name).add(component);
		}
		final Map<String, List<GenomicComponent>> problems = CollectionUtils
				.createHashMap();
		for (final Entry<String, List<GenomicComponent>> e : names.entrySet()) {
			final List<GenomicComponent> dups = e.getValue();
			if (dups.size() > 1) {
				log.warn("Multiple components found with name " + e.getKey());
				Collections.sort(dups, new GenomicComponentRankComparator());
				// if the first has a different type to the second, remove it
				// from the list of problem components so its name is preserved
				if (!dups.get(0).getMetaData().getComponentType()
						.equals(dups.get(1).getMetaData().getComponentType())) {
					dups.remove(0);
				}
				problems.put(e.getKey(), dups);
			}
		}
		if (problems.size() > 0) {
			throw new ComponentNameValidationException(
					"Components found with duplicate names", problems);
		}
	}
}
