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
 * File: SerializableObjectHolder.java
 * Created by: dstaines
 * Created on: Dec 1, 2006
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.util;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author dstaines
 * 
 */
public class SerializableObjectHolder implements Serializable {

	private static final long serialVersionUID = 1L;

	private String serializedObject;

	public SerializableObjectHolder(Object o) {
		set(o);
	}

	public void set(Object o) {
		if (o == null) {
			throw new UtilUncheckedException("Cannot serialize null object");
		} else {
			if (Serializable.class.isAssignableFrom(o.getClass())) {
				try {
					serializedObject = EncodingUtil.serialiseObject(o);
				} catch (IOException e) {
					throw new UtilUncheckedException(
							"Could not serialize object " + o, e);
				}
			} else {
				throw new UtilUncheckedException("Object of class "
						+ o.getClass().getName() + " cannot be serialized");
			}
		}
	}

	public Serializable get() {
		try {
			return (Serializable) (EncodingUtil
					.deserialiseObject(serializedObject));
		} catch (ClassNotFoundException e) {
			throw new UtilUncheckedException(
					"Could not deserialize object of unknown class", e);
		} catch (IOException e) {
			throw new UtilUncheckedException("Could not deserialize object");
		}
	}
}
