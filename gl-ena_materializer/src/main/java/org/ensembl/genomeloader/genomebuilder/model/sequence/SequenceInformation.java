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
 * File: SequenceInformation.java
 * Created by: dstaines
 * Created on: Feb 9, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.genomebuilder.model.sequence;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Basic class encapsulating information/annotation about a sequence
 *
 * @author dstaines
 *
 */
public class SequenceInformation {

	public static final String PROPERTY_ACCESSION = "accession";

	public static final String PROPERTY_CRC64 = "crc64";

	public static final String PROPERTY_UPI = "upi";

	public static final String PROPERTY_KINGDOM = "kingdom";

	public static final String PROPERTY_CHROMOSOME = "chromosome";

	public static final String PROPERTY_SUPERREGNUM = "superregnum";

	public static final String PROPERTY_TYPE = "type";

	public static final String PROPERTY_SCOPE = "scope";

	public static final String PROPERTY_ORGANELLE = "organelle";

	public static final String PROPERTY_HOST_SUPERREGNUM = "host_superregnum";

	public static final String PROPERTY_VERSION = "version";

	public static final String PROPERTY_DATE = "date";

	public SequenceInformation() {
	}

	public SequenceInformation(SequenceInformation seq) {
		this.setDescription(seq.getDescription());
		this.setIdentifier(seq.getIdentifier());
		this.setProperties(seq.getProperties());
		this.setLength(seq.getLength());
	}

	/**
	 * Short, pithy description
	 */
	private String description;

	/**
	 * Identifier string, including any source info
	 */
	private String identifier;

	/**
	 * Length of the sequence
	 */
	private long length;

	/**
	 * Additional semi-structured properties
	 */
	private Map<String, String> properties;

	/**
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return this.identifier;
	}

	private static Pattern idPattern1 = Pattern.compile("([^:]+):([^:]+)");

	private static Pattern idPattern2 = Pattern
			.compile("[^|]+\\|[^|]+\\|[^|]+");

	/**
	 * @param id
	 * @return
	 */
	public static String getBaseId(String id) {
		Matcher mat = idPattern1.matcher(id);
		if (mat.matches()) {
			return mat.group(2);
		} else {
			mat = idPattern2.matcher(id);
			if (mat.matches()) {
				return mat.group(3);
			} else {
				return id;
			}
		}
	}

	/**
	 * @param id
	 * @return
	 */
	public static String getDatabase(String id) {
		Matcher mat = idPattern1.matcher(id);
		if (mat.matches()) {
			return mat.group(1);
		} else {
			mat = idPattern2.matcher(id);
			if (mat.matches()) {
				return mat.group(1);
			} else {
				return id;
			}
		}
	}

	/**
	 * @return the length
	 */
	public long getLength() {
		return this.length;
	}

	/**
	 * @return the properties
	 */
	public Map<String, String> getProperties() {
		if (this.properties == null) {
			this.properties = new HashMap<String, String>();
		}
		return this.properties;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param identifier
	 *            the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * @param length
	 *            the length to set
	 */
	public void setLength(long length) {
		this.length = length;
	}

	/**
	 * @param properties
	 *            the properties to set
	 */
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

}
