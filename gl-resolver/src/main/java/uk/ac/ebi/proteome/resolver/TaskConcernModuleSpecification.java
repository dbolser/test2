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
 * File: TaskComponentModuleSpecification.java
 * Created by: dstaines
 * Created on: Feb 8, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.resolver;

import java.io.Serializable;

/**
 * @author dstaines
 * 
 */
public class TaskConcernModuleSpecification implements Serializable {

	private static final long serialVersionUID = 1L;

	private String task;

	private String concern;

	private ModuleSpecification moduleSpecification;

	public TaskConcernModuleSpecification() {
	}

	public TaskConcernModuleSpecification(String task, String concern) {
		this.task = task;
		this.concern = concern;
	}

	public String getConcern() {
		return this.concern;
	}

	public void setConcern(String concern) {
		this.concern = concern;
	}

	public ModuleSpecification getModuleSpecification() {
		return this.moduleSpecification;
	}

	public void setModuleSpecification(ModuleSpecification moduleSpecification) {
		this.moduleSpecification = moduleSpecification;
	}

	public String getTask() {
		return this.task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("<taskConcernSpecification task=\"");
		s.append(task);
		s.append(" concern=\"");
		s.append(concern);
		s.append("\">\n");
		s.append(moduleSpecification != null ? moduleSpecification.toString()
				+ "\n" : "");
		s.append("</taskConcernSpecification>");
		return s.toString();
	}

}
