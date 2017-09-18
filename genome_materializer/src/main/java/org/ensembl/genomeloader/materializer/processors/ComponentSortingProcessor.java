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

import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.materializer.EnaGenomeConfig;
import org.ensembl.genomeloader.materializer.EnaParsingException;
import org.ensembl.genomeloader.metadata.GenomicComponentDescriptionHandler;
import org.ensembl.genomeloader.metadata.GenomicComponentMetaData.GenomicComponentType;
import org.ensembl.genomeloader.model.Genome;
import org.ensembl.genomeloader.model.GenomicComponent;

/**
 * Processor that sorts components according to specific rules
 * 
 * @author dstaines
 *
 */
public class ComponentSortingProcessor implements GenomeProcessor {

	public static enum ComponentSorter {
		AUTOMATIC("automatic", automaticComponentComparator), ALPHABETICAL(
				"alphabetical", alphabeticalComponentComparator), NUMERICAL(
				"numerical", numericalComponentComparator), LENGTH("length",
				lengthComponentComparator);
		final Comparator<GenomicComponent> comparator;
		private final String name;

		private ComponentSorter(String name,
				Comparator<GenomicComponent> comparator) {
			this.comparator = comparator;
			this.name = name;
		}

		public static ComponentSorter getComponentSorter(String name) {
			ComponentSorter cs = null;
			for (final ComponentSorter c : ComponentSorter.values()) {
				if (c.name.equalsIgnoreCase(name)) {
					cs = c;
					break;
				}
			}
			return cs;
		}
	}

	public static Comparator<GenomicComponent> alphabeticalComponentComparator = new Comparator<GenomicComponent>() {
		public int compare(GenomicComponent o1, GenomicComponent o2) {
			return o1.getMetaData().getName()
					.compareToIgnoreCase(o2.getMetaData().getName());
		}
	};

	public static Comparator<GenomicComponent> numericalComponentComparator = new Comparator<GenomicComponent>() {
		public int compare(GenomicComponent o1, GenomicComponent o2) {
			return Integer.valueOf(o1.getMetaData().getName()).compareTo(
					Integer.valueOf(o2.getMetaData().getName()));
		}
	};

	public static Comparator<GenomicComponent> lengthComponentComparator = new Comparator<GenomicComponent>() {
		public int compare(GenomicComponent o1, GenomicComponent o2) {
			return Integer.valueOf(o1.getMetaData().getLength()).compareTo(
					Integer.valueOf(o2.getMetaData().getLength()));
		}
	};

	public static Comparator<GenomicComponent> automaticComponentComparator = new Comparator<GenomicComponent>() {
		public int compare(GenomicComponent o1, GenomicComponent o2) {
			int cmp = 0;
			final GenomicComponentType t1 = o1.getMetaData().getComponentType();
			final GenomicComponentType t2 = o2.getMetaData().getComponentType();
			final String n1 = o1.getMetaData().getName();
			final String n2 = o2.getMetaData().getName();
			if (t1.equals(t2)) {
				// is one of them a mitochondrion?
				if (n1.equals(GenomicComponentDescriptionHandler.MITOCHONDRION)
						&& n2.equals(GenomicComponentDescriptionHandler.PLASTID)) {
					cmp = n2.equals(GenomicComponentDescriptionHandler.PLASTID) ? 1
							: -1;
				} else if (n2
						.equals(GenomicComponentDescriptionHandler.MITOCHONDRION)) {
					cmp = n1.equals(GenomicComponentDescriptionHandler.PLASTID) ? -1
							: 1;
				} else if (n1
						.equals(GenomicComponentDescriptionHandler.PLASTID)) {
					cmp = n2.equals(GenomicComponentDescriptionHandler.MITOCHONDRION) ? -1
							: 1;
				} else if (n2
						.equals(GenomicComponentDescriptionHandler.PLASTID)) {
					cmp = n2.equals(GenomicComponentDescriptionHandler.MITOCHONDRION) ? 1
							: -1;
				} else if (t1.equals(GenomicComponentType.CHROMOSOME)
						&& (StringUtils.isNumeric(n1) && StringUtils
								.isNumeric(n2))) {
					cmp = Integer.valueOf(n1).compareTo(Integer.valueOf(n2));
				} else {
					return n1.compareToIgnoreCase(n2);
				}
			} else if (t1.equals(GenomicComponentType.CHROMOSOME)) {
				// trumps t2
				cmp = 1;
			} else if (t2.equals(GenomicComponentType.CHROMOSOME)) {
				// trumps t1
				cmp = -1;
			} else if (t1.equals(GenomicComponentType.PLASMID)) {
				cmp = 1;
				// trumps t2
			} else if (t2.equals(GenomicComponentType.PLASMID)) {
				// trumps t1
				cmp = -1;
			} else {
				return n1.compareToIgnoreCase(n2);
			}
			return cmp;
		}
	};

	private Log log;

	protected Log getLog() {
		if (log == null) {
			log = LogFactory.getLog(this.getClass());
		}
		return log;
	}

	private final EnaGenomeConfig config;

	public ComponentSortingProcessor(EnaGenomeConfig config) {
		this.config = config;
	}

	public void processGenome(Genome genome) {
		Comparator<GenomicComponent> gc = null;
		if (!StringUtils.isEmpty(config.getComponentSorter())) {
			final ComponentSorter cs = ComponentSorter.getComponentSorter(config.getComponentSorter());
			if(cs==null) {
				throw new EnaParsingException("Could not find component sorter of type "+config.getComponentSorter());
			}
			gc = cs.comparator;
		}
		if(gc==null) {
			gc = automaticComponentComparator;
		} 
		Collections.sort(genome.getGenomicComponents(), gc);
	}
}
