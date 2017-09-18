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
 * File: Component.java
 * Created by: dstaines
 * Created on: Jan 25, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.metadata;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Bean class representing minimal meta data about an entity, sufficient for its
 * materialization. Contains a set of data items objects representing teh
 * backing data
 *
 * @author dstaines
 */
public class EntityMetaData implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private Set<DataItem> dataItems = null;

    @JsonIgnore
    private String dataType;

    private String identifier;

    @JsonIgnore
    private boolean updated;

    @JsonIgnore
    private boolean deleted;

    /**
     * @return true if the data has been deleted since last seen
     */
    public boolean isDeleted() {
        return this.deleted;
    }

    /**
     * @param deleted
     *            set to true if the data has been deleted since last seen
     */
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * @return true if the data has been updated since last seen
     */
    public boolean isUpdated() {
        return this.updated;
    }

    /**
     * @param updated
     *            set to true if the data has been updated since last seen
     */
    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public EntityMetaData() {
    }

    public EntityMetaData(String dataType, String identifier) {
        this.dataType = dataType;
        this.identifier = identifier;
    }

    /**
     * Add an object representing a data item that is used to construct this
     * entity
     *
     * @param dataItem
     *            item describing source and section of the data
     */
    public void addSource(DataItem dataItem) {
        getDataItems().add(dataItem);
    }

    /**
     * @return set of data items attached to this metadata
     */
    public Set<DataItem> getDataItems() {
        if (dataItems == null) {
            dataItems = new HashSet<DataItem>();
        }
        return dataItems;
    }

    /**
     * @return string representing the type and origin of entity data
     *         represented
     */
    public String getDataType() {
        return this.dataType;
    }

    /**
     * @return (optional) string representing a unique identifer for this entity
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * @param dataItems
     *            set of individual data items that contruibute to this entity
     */
    public void setDataItems(Set<DataItem> dataItems) {
        this.dataItems = dataItems;
    }

    public void addDataItem(DataItem dataItem) {
        getDataItems().add(dataItem);
    }

    /**
     * @param dataType
     *            type of data represented
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     * @param identifier
     *            unique identifier for this entity
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @JsonIgnore
    private final transient ObjectMapper mapper = new ObjectMapper();

    @Override
    public String toString() {
        try {
            mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
