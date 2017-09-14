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
 * File: RunIdentifier.java
 * Created by: dstaines
 * Created on: Oct 7, 2008
 * CVS:  $$
 */
package uk.ac.ebi.proteome.materializer.version;

import uk.ac.ebi.proteome.genomebuilder.metadata.GenomeIdentifier;
import uk.ac.ebi.proteome.genomebuilder.metadata.GenomeMetaData;
import uk.ac.ebi.proteome.genomebuilder.metadata.GenomicComponentIdentifier;
import uk.ac.ebi.proteome.genomebuilder.metadata.GenomicComponentMetaData;

/**
 * @author dstaines
 * 
 */
public class RunIdentifier {
	public final static void main(String[] args) throws Throwable {

		if (true) {
			GenomeIdentifier idfer = GenomeIdentifier
					.build("jdbc:oracle:thin:integr8/textme@localhost:15510:DEV10");
			GenomeMetaData md = idfer.getMetaDataForIdentifier("18");
			System.out.println(md.getIdentifier());
		} else {
			GenomicComponentIdentifier idfer = GenomicComponentIdentifier
					.build("jdbc:oracle:thin:integr8/textme@localhost:15510:DEV10");
			GenomicComponentMetaData md = idfer.getMetaDataForIdentifier("44");
			System.out.println(md.getIdentifier());
		}
		// for(GenomicComponentMetaData md: idfer.getAllMetaData()) {
		// System.out.println(md.getIdentifier());
		// }

	}
}
