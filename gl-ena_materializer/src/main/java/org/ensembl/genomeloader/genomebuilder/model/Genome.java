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
 * File: Genome.java
 * Created by: dstaines
 * Created on: Sep 24, 2008
 * CVS:  $$
 */
package org.ensembl.genomeloader.genomebuilder.model;

import java.util.Collection;
import java.util.List;

import org.ensembl.genomeloader.genomebuilder.metadata.DataItem;

/**
 * Interface adding some information on components and versions for a genome
 *
 * @author dstaines
 *
 */
public interface Genome extends GenomeInfo, Integr8ModelComponent, CrossReferenced {

    public Collection<DataItem> getDataItems();

    public void addDataItem(DataItem item);

    public List<GenomicComponent> getGenomicComponents();

    public void addGenomicComponent(GenomicComponent component);

}
