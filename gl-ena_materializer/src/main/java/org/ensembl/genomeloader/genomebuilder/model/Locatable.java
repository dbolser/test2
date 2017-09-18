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
 * File: Locatable.java
 * Created by: dstaines
 * Created on: Oct 4, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.genomebuilder.model;


/**
 * @author dstaines
 *
 */
public interface Locatable {

	public abstract EntityLocation getLocation();

	public abstract void setLocation(EntityLocation location);

}
