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
 * File: Transcript.java
 * Created by: dstaines
 * Created on: Oct 4, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.genomebuilder.model;

import java.util.Set;

/**
 * Class associating one or more {@link Protein} objects that are
 * co-transcribed. Can optionally be associated with an {@link Operon}.
 *
 * @author dstaines
 *
 */
public interface Transcript extends Integr8ModelComponent, Locatable,
		CrossReferenced, Identifiable {

	/**
	 * @return optional consensus use name for transcript
	 */
	public String getName();

	/**
	 * @return set of proteins encoded by this transcript
	 */
	public Set<Protein> getProteins();

	public void addProtein(Protein protein);

	/**
	 * @return optional operon to which this transcript belongs
	 */
	public Operon getOperon();

	public void setOperon(Operon operon);

	/**
	 * @return optional name of promoter
	 */
	public String getPromoter();

	public void setPromoter(String string);

	/**
	 * @return optional name of co-transcribed unit
	 */
	public String getCoTranscribedUnit();

	public void setCoTranscribedUnit(String string);

}
