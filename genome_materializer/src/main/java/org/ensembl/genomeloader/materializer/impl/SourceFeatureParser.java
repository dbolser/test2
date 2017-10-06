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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.materializer.EnaParsingException;
import org.ensembl.genomeloader.materializer.impl.XomUtils.ElementsIterable;
import org.ensembl.genomeloader.metadata.GenomeMetaData;
import org.ensembl.genomeloader.metadata.GenomeMetaData.OrganismNameType;
import org.ensembl.genomeloader.metadata.GenomicComponentMetaData.GenomicComponentType;
import org.ensembl.genomeloader.metadata.GenomicComponentMetaData;
import org.ensembl.genomeloader.util.collections.CollectionUtils;

import nu.xom.Element;

/**
 * @author dstaines
 * 
 */
public class SourceFeatureParser {

    public void parseFeature(GenomicComponentMetaData md, Element element) {

        GenomeMetaData gmd = md.getGenomeMetaData();

        final Element taxonElement = getFirstChild(element, "taxon");
        if (taxonElement == null) {
            throw new EnaParsingException("Source element does not contain a taxon child");
        }
        final Map<String, List<String>> qualifiers = XmlEnaFeatureParser.getQualifiers(element);

        String taxId = taxonElement.getAttributeValue("taxId");
        String scientificName = taxonElement.getAttributeValue("scientificName");

        String name = scientificName;
        final String strain = CollectionUtils.getFirstElement(qualifiers.get("strain"), null);
        if (!StringUtils.isEmpty(strain) && !name.contains(strain)) {
            name = name + " str. " + strain;
        }

        final Element lineageElem = taxonElement.getFirstChildElement("lineage");
        if (lineageElem == null) {
            LogFactory.getLog(this.getClass()).warn("No lineage element found for taxon " + taxId);
            if (gmd.getSuperregnum() == null)
                gmd.setSuperregnum(GenomeMetaData.DEFAULT_SUPERREGNUM);
        } else {
            final List<String> lineage = CollectionUtils.createArrayList();
            for (final Element lin : new ElementsIterable(lineageElem.getChildElements("taxon"))) {
                lineage.add(lin.getAttributeValue("scientificName"));
            }
            lineage.add(name);
            if (gmd.getLineage() == null || gmd.getLineage().isEmpty())
                gmd.setLineage(lineage);
            if (gmd.getSuperregnum() == null || GenomeMetaData.DEFAULT_SUPERREGNUM.equals(gmd.getSuperregnum()))
                gmd.setSuperregnum(CollectionUtils.getFirstElement(lineage, GenomeMetaData.DEFAULT_SUPERREGNUM));

        }

        final String serotype = CollectionUtils.getFirstElement(qualifiers.get("serotype"), null);
        if (!StringUtils.isEmpty(serotype) && gmd.getOrganismName(OrganismNameType.SEROTYPE) == null) {
            gmd.setOrganismName(OrganismNameType.SEROTYPE, serotype);
        }
        if (!StringUtils.isEmpty(strain) && gmd.getOrganismName(OrganismNameType.STRAIN) == null) {
            gmd.setOrganismName(OrganismNameType.STRAIN, strain);
        }
        final String substrain = CollectionUtils.getFirstElement(qualifiers.get("sub_strain"), null);
        if (!StringUtils.isEmpty(substrain) && gmd.getOrganismName(OrganismNameType.SUBSTRAIN) == null) {
            gmd.setOrganismName(OrganismNameType.SUBSTRAIN, substrain);
        }
        final String chromosome = CollectionUtils.getFirstElement(qualifiers.get("chromosome"), null);
        if (!StringUtils.isEmpty(chromosome)) {
            md.setComponentType(GenomicComponentType.CHROMOSOME);
            md.setName(chromosome);
        }
    }

}
