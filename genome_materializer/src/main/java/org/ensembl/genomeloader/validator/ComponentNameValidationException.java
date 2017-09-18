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

package org.ensembl.genomeloader.validator;

import java.util.List;
import java.util.Map;

import org.ensembl.genomeloader.model.GenomicComponent;

public class ComponentNameValidationException extends GenomeValidationException {

	private static final long serialVersionUID = 1L;

	final Map<String, List<GenomicComponent>> problems;

	public ComponentNameValidationException(String message,
			Map<String, List<GenomicComponent>> problems) {
		super(message);
		this.problems = problems;
	}

	public Map<String, List<GenomicComponent>> getProblems() {
		return this.problems;
	}

}
