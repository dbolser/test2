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

import org.ensembl.genomeloader.materializer.EnaParser;
import org.ensembl.genomeloader.materializer.EnaXmlRetriever;
import org.ensembl.genomeloader.model.impl.GenomicComponentImpl;
import org.ensembl.genomeloader.xrefregistry.DatabaseReferenceTypeRegistry;

import nu.xom.Element;

/**
 * Minimal parser that only deals with parsing the sequence and CON of an entry
 * 
 * @author dstaines
 *
 */
public class EnaContigParser extends EnaParser {

    public EnaContigParser(EnaXmlRetriever retriever, DatabaseReferenceTypeRegistry registry) {
        super(retriever, registry);
    }

    @Override
    protected void parseFeatures(GenomicComponentImpl component, Element entryElem) {
        // do nothing with features
    }

}
