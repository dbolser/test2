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
 * File: Identifiable.java
 * Created by: dstaines
 * Created on: Mar 20, 2008
 * CVS:  $$
 */
package org.ensembl.genomeloader.genomebuilder.model;

/**
 * Interface specifying methods for getting/setting an identifying identifier
 * for an entity
 *
 * @author dstaines
 *
 */
public interface Identifiable {

	/**
	 * @return identifying identifier for entity
	 */
	public String getIdentifyingId();

	/**
	 * @param identifyingId
	 *            identifying identifier for entity
	 */
	public void setIdentifyingId(String identifyingId);

}
