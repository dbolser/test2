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
 * File: TaskDefinition.java
 * Created by: dstaines
 * Created on: Jan 25, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.resolver;

import java.io.Serializable;

/**
 * Bean containing minimal metadata representing a task
 * 
 * @author dstaines
 */
public class TaskDefinition implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	private String description = "";

	/**
	 * Simple description
	 * 
	 * @return
	 */
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Name identifying this task, for use in resolution
	 * 
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static TaskDefinition forName(String name) {
		return new TaskDefinition(name);
	}

	public TaskDefinition() {
	}

	public TaskDefinition(String name) {
		this.name = name;
	}

}
