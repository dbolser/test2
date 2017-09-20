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
 * File: CdsFeatureParser.java
 * Created by: dstaines
 * Created on: Mar 25, 2010
 * CVS:  $$
 */
package org.ensembl.genomeloader.materializer.impl;

import static org.ensembl.genomeloader.materializer.impl.XomUtils.getFirstChild;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ensembl.genomeloader.materializer.EnaParsingException;
import org.ensembl.genomeloader.materializer.impl.XomUtils.ElementsIterable;
import org.ensembl.genomeloader.metadata.GenomeMetaData;
import org.ensembl.genomeloader.model.impl.GenomicComponentImpl;
import org.ensembl.genomeloader.util.collections.CollectionUtils;
import org.ensembl.genomeloader.xrefregistry.DatabaseReferenceTypeRegistry;

import nu.xom.Element;

/**
 * @author dstaines
 * 
 */
public class SourceFeatureParser extends XmlEnaFeatureParser {

    public SourceFeatureParser(DatabaseReferenceTypeRegistry registry) {
        super(registry);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ensembl.genomeloader.materializer.ena.impl.XmlEnaFeatureParser#
     * dependsOn()
     */
    public List<Class<? extends XmlEnaFeatureParser>> dependsOn() {
        return Collections.EMPTY_LIST;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ensembl.genomeloader.materializer.ena.impl.XmlEnaFeatureParser#
     * parseFeature
     * (org.ensembl.genomeloader.genomebuilder.model.impl.GenomicComponentImpl,
     * nu.xom.Element)
     */
    public void parseFeature(GenomicComponentImpl component, Element element) {
        final Element taxonElement = getFirstChild(element, "taxon");
        if (taxonElement == null) {
            throw new EnaParsingException("Source element does not contain a taxon child");
        }
        final Map<String, List<String>> qualifiers = XmlEnaFeatureParser.getQualifiers(element);

        String taxId = taxonElement.getAttributeValue("taxId");
        component.getSourceMetaData().put("taxon_id", taxId);
        String scientificName = taxonElement.getAttributeValue("scientificName");
        component.getSourceMetaData().put("scientific_name", scientificName);

        String name = scientificName;
        final String strain = CollectionUtils.getFirstElement(qualifiers.get("strain"), null);
        if (!StringUtils.isEmpty(strain) && !name.contains(strain)) {
            name = name + " str. " + strain;
        }

        final Element lineageElem = taxonElement.getFirstChildElement("lineage");
        if (lineageElem == null) {
            getLog().warn("No lineage element found for taxon " + taxId);
            component.getSourceMetaData().put("superregnum", GenomeMetaData.DEFAULT_SUPERREGNUM);
        } else {
            final List<String> lineage = CollectionUtils.createArrayList();
            for (final Element lin : new ElementsIterable(lineageElem.getChildElements("taxon"))) {
                lineage.add(lin.getAttributeValue("scientificName"));
            }
            lineage.add(name);
            component.getSourceMetaData().put("lineage", lineage);
            component.getSourceMetaData().put("superregnum",
                    CollectionUtils.getFirstElement(lineage, GenomeMetaData.DEFAULT_SUPERREGNUM));
        }
        component.getSourceMetaData().put("name", name);

        final String serotype = CollectionUtils.getFirstElement(qualifiers.get("serotype"), null);
        if (!StringUtils.isEmpty(serotype)) {
            component.getSourceMetaData().put("serotype", serotype);
        }
        if (!StringUtils.isEmpty(strain)) {
            component.getSourceMetaData().put("strain", strain);
        }
        final String substrain = CollectionUtils.getFirstElement(qualifiers.get("sub_strain"), null);
        if (!StringUtils.isEmpty(substrain)) {
            component.getSourceMetaData().put("substrain", substrain);
        }

    }

}
