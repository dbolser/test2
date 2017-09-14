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
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.genomebuilder.model;

/**
 * Interface representing properties of a ProteinFeature
 *
 * @author dstaines
 *
 */
public interface ProteinFeature extends Integr8ModelComponent, Locatable, CrossReferenced {

	/**
	 * @return defined type of feature
	 */
	public ProteinFeatureType getType();

	public void setType(ProteinFeatureType type);

	/**
	 * @return protein coordinate of N-terminus of feature
	 */
	public int getStart();

	public void setStart(int start);

	/**
	 * @return protein coordinate of C-terminus of feature
	 */
	public int getEnd();

	public void setEnd(int end);

	/**
	 * @return flag indicating source of feature
	 */
	public ProteinFeatureSource getSource();

	public void setSource(ProteinFeatureSource source);

	/**
	 * @return optional confidence score
	 */
	public Double getScore();

	public void setScore(Double score);
	
	public String getId();
	
	public void setId(String id);

	public String getName();
	
	public void setName(String name);

}
