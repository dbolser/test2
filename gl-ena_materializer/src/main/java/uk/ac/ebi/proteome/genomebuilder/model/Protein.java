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
 * File: Protein.java
 * Created by: dstaines
 * Created on: Oct 4, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.genomebuilder.model;

import java.util.Set;

/**
 * Interface specifying information about a protein-coding region of a
 * {@link Gene}
 *
 * @author dstaines
 */
public interface Protein extends Integr8ModelComponent, Locatable,
		CrossReferenced, Identifiable {

	/**
	 * @return accession or isoform ID of protein encoded
	 */
	public String getUniprotKbId();

	/**
	 * @return consensus use name of protein
	 */
	public String getName();

	/**
	 * @return one or more transcripts that contain this region
	 */
	public Set<Transcript> getTranscripts();

	public void addTranscript(Transcript transcript);

	/**
	 * @return set of sequence features within this protein coding region
	 */
	public Set<ProteinFeature> getProteinFeatures();

	public void addProteinFeature(ProteinFeature feature);

	/**
	 * @return phase in which to start translation
	 */
	public int getCodonStart();

	/**
	 * @return true if sequence encoded by this object does not code
	 */
	public boolean isPseudo();
	
}
