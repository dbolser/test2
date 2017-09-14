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
 * File: ActorSpecification.java
 * Created by: dstaines
 * Created on: Feb 6, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.resolver;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * @author dstaines
 * 
 */
public class ModuleSpecification implements Serializable {

	private static final long serialVersionUID = 1L;

	private String className;

	private String name;

	private Properties properties = new Properties();

	public String getClassName() {
		return this.className;
	}

	public String getName() {
		return this.name;
	}

	public Properties getProperties() {
		return this.properties;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("<moduleSpecification name=\"");
		s.append(name);
		s.append("\" className=\"");
		s.append(className);
		s.append("\">\n\t<properties>\n");
		for (Entry e : properties.entrySet()) {
			s.append("\t\t<property key=\"");
			s.append(e.getKey());
			s.append("\">");
			s.append(e.getValue());
			s.append("</property>\n");
		}
		s.append("\t</properties>\n</moduleSpecification>");
		return s.toString();
	}

}
