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
 * File: Operon.java
 * Created by: dstaines
 * Created on: Dec 4, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.genomebuilder.model;

/**
 * Interface specifying minimal information about an operon to which one or more
 * {@link Transcript} objects can belong
 *
 * @author dstaines
 *
 */
public interface Operon extends Locatable, Integr8ModelComponent,
		CrossReferenced {

	/**
	 * @return agreed name of operon
	 */
	public abstract String getName();

	public abstract void setName(String name);

	/**
	 * @return string representing unique identifier for operon
	 */
	public abstract String getOperonId();

	public abstract void setOperonId(String operonId);

}
