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

package org.ensembl.genomeloader.materializer.impl;

import static org.ensembl.genomeloader.materializer.impl.XomUtils.getFirstChild;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.genomebuilder.model.GenomeInfo;
import org.ensembl.genomeloader.genomebuilder.model.GenomeInfo.OrganismNameType;
import org.ensembl.genomeloader.genomebuilder.model.impl.GenomeInfoImpl;
import org.ensembl.genomeloader.materializer.EnaParsingException;
import org.ensembl.genomeloader.materializer.impl.XomUtils.ElementsIterable;
import org.ensembl.genomeloader.util.collections.CollectionUtils;

import nu.xom.Element;

public class SourceElementParser {

    private Log log;

    private Log getLog() {
        if (log == null)
            log = LogFactory.getLog(this.getClass());
        return log;
    }

    public GenomeInfoImpl getGenomeInfo(Element element) {

        /*
         * <feature name="source" location="1..4639675"> <taxon
         * scientificName="Escherichia coli str. K-12 substr. MG1655"
         * taxId="511145"> <lineage> <taxon scientificName="Bacteria"/> <taxon
         * scientificName="Proteobacteria"/> <taxon
         * scientificName="Gammaproteobacteria"/> <taxon
         * scientificName="Enterobacteriales"/> <taxon
         * scientificName="Enterobacteriaceae"/> <taxon
         * scientificName="Escherichia"/> </lineage> </taxon> <qualifier
         * name="organism"> </qualifier> <qualifier name="strain"> <value> K-12
         * </value> </qualifier> <qualifier name="sub_strain"> <value> MG1655
         * </value> </qualifier> </feature>
         */

        final Element taxonElement = getFirstChild(element, "taxon");
        if (taxonElement == null) {
            throw new EnaParsingException("Source element does not contain a taxon child");
        }
        final Map<String, List<String>> qualifiers = XmlEnaFeatureParser.getQualifiers(element);
        final String id = taxonElement.getAttributeValue("taxId");
        String proteomeName = taxonElement.getAttributeValue("scientificName");
        final int taxId = Integer.valueOf(id);
        // also get lineage
        final List<String> lineage = CollectionUtils.createArrayList();
        final Element lineageElem = taxonElement.getFirstChildElement("lineage");
        if (lineageElem == null) {
            getLog().warn("No lineage element found for taxon " + id);
        } else {
            for (final Element lin : new ElementsIterable(lineageElem.getChildElements("taxon"))) {
                lineage.add(lin.getAttributeValue("scientificName"));
            }
            lineage.add(proteomeName);
        }
        final String superregnum = CollectionUtils.getFirstElement(lineage, GenomeInfo.DEFAULT_SUPERREGNUM);
        final String strain = CollectionUtils.getFirstElement(qualifiers.get("strain"), null);
        if (!StringUtils.isEmpty(strain) && !proteomeName.contains(strain)) {
            proteomeName = proteomeName + " str. " + strain;
        }
        final GenomeInfoImpl info = new GenomeInfoImpl(id, taxId, proteomeName, superregnum.toLowerCase());

        info.getLineage().addAll(lineage);
        info.setOrganismName(OrganismNameType.FULL, info.getName());
        info.setOrganismName(OrganismNameType.SQL, getSqlName(info.getName()));

        final String serotype = CollectionUtils.getFirstElement(qualifiers.get("serotype"), null);
        if (!StringUtils.isEmpty(serotype)) {
            info.setOrganismName(OrganismNameType.SEROTYPE, serotype);
        }
        if (!StringUtils.isEmpty(strain)) {
            info.setOrganismName(OrganismNameType.STRAIN, strain);

        }
        final String substrain = CollectionUtils.getFirstElement(qualifiers.get("sub_strain"), null);
        if (!StringUtils.isEmpty(substrain)) {
            info.setOrganismName(OrganismNameType.SUBSTRAIN, substrain);
        }

        return info;
    }

    protected String getSqlName(String name) {
        String sqlName = name.toLowerCase();
        sqlName = sqlName.replaceAll("[^a-z0-9_]+", "_");
        sqlName = sqlName.replaceAll("_+", "_");
        return sqlName;
    }

}
