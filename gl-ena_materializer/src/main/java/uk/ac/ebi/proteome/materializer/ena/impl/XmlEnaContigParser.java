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

package uk.ac.ebi.proteome.materializer.ena.impl;

import java.util.concurrent.Executor;

import nu.xom.Element;
import uk.ac.ebi.proteome.genomebuilder.model.impl.GenomicComponentImpl;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;

/**
 * Minimal parser that only deals with parsing the sequence and CON of an entry
 * @author dstaines
 *
 */
public class XmlEnaContigParser extends XmlEnaComponentParser {

	public XmlEnaContigParser(Executor executor,
			DatabaseReferenceTypeRegistry registry) {
		super(executor, registry);
	}

	@Override
	protected void parseFeatures(GenomicComponentImpl component,
			Element entryElem) {
		// do nothing with features
	}

}
