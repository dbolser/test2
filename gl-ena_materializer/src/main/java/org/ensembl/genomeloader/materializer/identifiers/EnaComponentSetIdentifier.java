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

package org.ensembl.genomeloader.materializer.identifiers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.ensembl.genomeloader.materializer.EnaGenomeConfig;
import org.ensembl.genomeloader.materializer.EnaParser;
import org.ensembl.genomeloader.materializer.impl.IdentificationUncheckedException;
import org.ensembl.genomeloader.materializer.impl.XmlEnaGenomeInfoParser;
import org.ensembl.genomeloader.metadata.GenomeMetaData;
import org.ensembl.genomeloader.metadata.GenomicComponentMetaData;
import org.ensembl.genomeloader.model.GenomeInfo;
import org.ensembl.genomeloader.model.GenomeInfo.OrganismNameType;
import org.ensembl.genomeloader.util.collections.CollectionUtils;
import org.ensembl.genomeloader.util.templating.TemplateBuilder;

public class EnaComponentSetIdentifier {

    private final EnaGenomeConfig econfig;
    private final EnaParser<GenomeInfo> parser;

    public EnaComponentSetIdentifier(EnaGenomeConfig econfig) {
        this.econfig = econfig;
        this.parser = new XmlEnaGenomeInfoParser();
    }


    public GenomeMetaData getMetaDataForIdentifier(String id, Object... params) {

        Collection<String> components = (Collection<String>) params[0];
        String masterAc = CollectionUtils.getFirstElement(components, null);

        // identifier is a name to use for this one e.g. ustilago_maydis
        // params[0] is a Set of component accessions

        // use the first accession as a master to get the source information
        GenomeInfo info = parser.parse(getUrl(masterAc));
        GenomeMetaData md = new GenomeMetaData(info);
        md.setOrganismName(OrganismNameType.SQL, id);
        md.setIdentifier(id);

        for (String component : components) {
            GenomicComponentMetaData cMd = new GenomicComponentMetaData(GenomicComponentMetaData.DNA_SOURCE_TYPE,
                    component, component, md);
            md.getComponentMetaData().add(cMd);

        }
        return md;
    }

    protected URL getUrl(String accession) {
        String url = TemplateBuilder.template(this.econfig.getEnaXmlUrl(), "ac", accession);
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new IdentificationUncheckedException("Could not parse URL " + url, e);
        }
    }

}
