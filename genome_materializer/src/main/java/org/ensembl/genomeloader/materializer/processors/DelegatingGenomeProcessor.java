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

package org.ensembl.genomeloader.materializer.processors;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.model.Genome;
import org.ensembl.genomeloader.util.collections.CollectionUtils;

public class DelegatingGenomeProcessor implements GenomeProcessor {

	private Log log;

	protected Log getLog() {
		if (log == null) {
			log = LogFactory.getLog(this.getClass());
		}
		return log;
	}

	private final List<GenomeProcessor> processors;

	public DelegatingGenomeProcessor(GenomeProcessor... processors) {
		this(CollectionUtils.createArrayList(processors));
	}

	public DelegatingGenomeProcessor(List<GenomeProcessor> processors) {
		this.processors = processors;
	}

	public void processGenome(Genome genome) {
		getLog().info("Processing genome " + genome.getId());
		for (GenomeProcessor p : processors) {
			getLog().info("Running processor " + p.getClass().getSimpleName());
			p.processGenome(genome);
		}
		getLog().info("Finished processing genome " + genome.getId());
	}

	protected void addProcessor(GenomeProcessor processor) {
		processors.add(processor);
	}

}
