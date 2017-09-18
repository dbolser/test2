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

package org.ensembl.genomeloader.materializer.impl;

import org.ensembl.genomeloader.genomebuilder.model.GenomeInfo;
import org.ensembl.genomeloader.genomebuilder.model.impl.GenomeInfoImpl;
import org.ensembl.genomeloader.materializer.EnaParser;
import org.ensembl.genomeloader.materializer.EnaParsingException;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

public class XmlEnaGenomeInfoParser extends AbstractXmlEnaParser<GenomeInfo>
		implements EnaParser<GenomeInfo> {

	private final SourceElementParser parser = new SourceElementParser();

	@Override
	public GenomeInfoImpl parse(Document doc) {
		// get the source element
		Nodes srcs = doc.query("//feature[@name='source']");
		if (srcs.size() != 1) {
			throw new EnaParsingException(
					"Document should contain only one source feature");
		}
		Element src = (Element) srcs.get(0);
		return parser.getGenomeInfo(src);

	}

}
