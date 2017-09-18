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

/**
 * File: CdsFeatureParser.java
 * Created by: dstaines
 * Created on: Mar 25, 2010
 * CVS:  $$
 */
package org.ensembl.genomeloader.materializer.impl;

import java.util.Collections;
import java.util.List;

import org.ensembl.genomeloader.genomebuilder.model.GenomeInfo;
import org.ensembl.genomeloader.genomebuilder.model.impl.GenomicComponentImpl;
import org.ensembl.genomeloader.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;

import nu.xom.Element;

/**
 * @author dstaines
 * 
 */
public class SourceFeatureParser extends XmlEnaFeatureParser {

	private final SourceElementParser parser;

	public SourceFeatureParser(DatabaseReferenceTypeRegistry registry) {
		super(registry);
		this.parser = new SourceElementParser();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.genomeloader.materializer.ena.impl.XmlEnaFeatureParser#dependsOn()
	 */
	public List<Class<? extends XmlEnaFeatureParser>> dependsOn() {
		return Collections.EMPTY_LIST;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.genomeloader.materializer.ena.impl.XmlEnaFeatureParser#parseFeature
	 * (org.ensembl.genomeloader.genomebuilder.model.impl.GenomicComponentImpl,
	 * nu.xom.Element)
	 */
	public void parseFeature(GenomicComponentImpl component, Element element) {
		GenomeInfo info = parser.getGenomeInfo(element);
		component.getMetaData().setGenomeInfo(info);
	}

}
