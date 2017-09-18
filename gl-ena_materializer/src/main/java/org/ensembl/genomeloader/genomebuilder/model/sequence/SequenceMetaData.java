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
 * File: SequenceMetaData.java
 * Created by: dstaines
 * Created on: Mar 9, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.genomebuilder.model.sequence;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.ensembl.genomeloader.genomebuilder.metadata.EntityMetaData;

/**
 * @author dstaines
 *
 */
public class SequenceMetaData extends EntityMetaData {

    private static final long serialVersionUID = 1L;

    public SequenceMetaData() {
        super();
    }

    /**
     * @param source
     * @param identifier
     */
    public SequenceMetaData(String source, String identifier) {
        super(source, identifier);
    }

    private Map<String, String> properties = null;

    public Map<String, String> getProperties() {
        if (properties == null) {
            properties = new HashMap<String, String>();
        }
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public void setProperty(String key, String value) {
        getProperties().put(key, value);
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

}
