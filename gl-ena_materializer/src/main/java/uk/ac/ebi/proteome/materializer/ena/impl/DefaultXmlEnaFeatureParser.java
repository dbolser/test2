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

package uk.ac.ebi.proteome.materializer.ena.impl;

import java.util.Collections;
import java.util.List;

import nu.xom.Element;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReference;
import uk.ac.ebi.proteome.genomebuilder.model.SimpleFeature;
import uk.ac.ebi.proteome.genomebuilder.model.impl.GenomicComponentImpl;
import uk.ac.ebi.proteome.genomebuilder.model.impl.SimpleFeatureImpl;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;

/**
 * Parser to use for any feature for which there is no more specific parser
 * available. This adds the feature as a {@link SimpleFeature} to the component.
 * 
 * @author dstaines
 * 
 */
public class DefaultXmlEnaFeatureParser extends XmlEnaFeatureParser {

	public DefaultXmlEnaFeatureParser(DatabaseReferenceTypeRegistry registry) {
		super(registry);
	}

	@Override
	public void parseFeature(GenomicComponentImpl component, Element element) {
		SimpleFeatureImpl feature = new SimpleFeatureImpl();
		DatabaseReference idReg = getFeatureIdentifierRef(component, element, "FEATURE");
		feature.addDatabaseReference(idReg);
		feature.getDatabaseReferences().addAll(parseXrefs(element));
		feature.setLocation(parseLocation(element));
		feature.setFeatureType(element.getAttributeValue("name"));
		feature.getQualifiers().putAll(getQualifiers(element));
		if (feature.getQualifiers().containsKey("note")) {
			feature.setDisplayLabel(StringUtils.join(
					feature.getQualifiers().get("note").iterator(), "; ")
					.replaceAll("\\s+", " "));
		} else {
			feature.setDisplayLabel(idReg.getPrimaryIdentifier());
		}
		if(feature.getQualifiers().containsKey("locus_tag")) {
			feature.setDisplayLabel(StringUtils.join(
					feature.getQualifiers().get("locus_tag").iterator(), "; ")+"; "+feature.getDisplayLabel());
		}
		feature.setIdentifyingId(idReg.getPrimaryIdentifier());
		component.getFeatures().add(feature);
	}

	@Override
	public List<Class<? extends XmlEnaFeatureParser>> dependsOn() {
		return Collections.EMPTY_LIST;
	}

}
