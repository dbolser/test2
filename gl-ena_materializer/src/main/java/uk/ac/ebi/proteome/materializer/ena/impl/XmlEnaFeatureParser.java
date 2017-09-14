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
 * File: XmlEnaFeatureParser.java
 * Created by: dstaines
 * Created on: Mar 25, 2010
 * CVS:  $$
 */
package uk.ac.ebi.proteome.materializer.ena.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biojavax.bio.seq.RichLocation;

import nu.xom.Element;
import uk.ac.ebi.proteome.genomebuilder.model.AnnotatedGene;
import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReference;
import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReferenceType;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.GeneName;
import uk.ac.ebi.proteome.genomebuilder.model.GeneNameType;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.model.Locatable;
import uk.ac.ebi.proteome.genomebuilder.model.impl.AnnotatedGeneImpl;
import uk.ac.ebi.proteome.genomebuilder.model.impl.DatabaseReferenceImpl;
import uk.ac.ebi.proteome.genomebuilder.model.impl.DelegatingEntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.impl.GeneNameImpl;
import uk.ac.ebi.proteome.genomebuilder.model.impl.GenomicComponentImpl;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.materializer.ena.EnaParsingException;
import uk.ac.ebi.proteome.materializer.ena.impl.XomUtils.ElementsIterable;
import uk.ac.ebi.proteome.materializer.ena.impl.XomUtils.ListMap;
import uk.ac.ebi.proteome.util.biojava.LocationUtils;
import uk.ac.ebi.proteome.util.collections.CollectionUtils;
import uk.ac.ebi.proteome.util.templating.TemplateBuilder;

/**
 * @author dstaines
 * 
 */
public abstract class XmlEnaFeatureParser {

    protected static final String EMBL_AC_RE = "[A-Z]+[0-9]+\\.[0-9]+:";
    protected static final String ID_TEMPLATE = "$ac$.$version$:$key$:$loc$";

    protected XmlEnaFeatureParser def;

    protected XmlEnaFeatureParser getDefaultParser() {
        if (def == null) {
            def = new DefaultXmlEnaFeatureParser(registry);
        }
        return def;
    }

    /**
     * Base method to get the identifier to use for a feature, using the
     * component accession, feature name and location as a fallback
     * 
     * @param component
     *            component to which the feature belongs
     * @param feature
     *            feature element
     * @return identifier
     */
    protected String getFeatureIdentifier(GenomicComponent component, Element feature) {
        return TemplateBuilder.template(ID_TEMPLATE, "ac", component.getMetaData().getAccession(), "version",
                component.getMetaData().getVersion(), "key", feature.getAttributeValue("name"), "loc",
                feature.getAttributeValue("location"));
    }

    protected DatabaseReference getFeatureIdentifierRef(GenomicComponent component, Element feature, String objType) {
        DatabaseReferenceType type = registry.getTypeForQualifiedName("ENA_FEATURE", objType);
        if (type == null) {
            throw new EnaParsingException("Could not find reference type ENA_FEATURE/" + objType);
        }
        return new DatabaseReferenceImpl(type, getFeatureIdentifier(component, feature));
    }

    protected Map<String, List<String>> parseQualifiers(Element feature) {
        Map<String, List<String>> qualifiers = new ListMap<String, String>();
        for (Element elem : new ElementsIterable(feature.getChildElements("qualifier"))) {
            String attrName = elem.getAttributeValue("name");
            String value = null;
            Element valElem = XomUtils.getFirstChild(elem, "value");
            if (valElem != null) {
                value = valElem.getValue();
            }
            qualifiers.get(attrName).add(value);
        }
        return qualifiers;
    }

    protected DelegatingEntityLocation parseLocation(Element element) {
        String locStr = element.getAttributeValue("location");
        RichLocation loc = LocationUtils.parseEmblLocation(cleanupLoc(locStr));
        // // fix to deal with locations in which order is
        // incorrect/inconsistent - not sure if this is sensible or not!
        // if(locStr.contains("order")) {
        // loc = LocationUtils.construct(LocationUtils.sortLocation(loc));
        // }
        return new DelegatingEntityLocation(loc);
    }

    /**
     * Method to remove EMBL ACs from location strings to make them cleaner to
     * parse
     * 
     * @param locStr
     *            string
     * @return cleaned location string
     */
    protected String cleanupLoc(String locStr) {
        // return locStr;
        return locStr.replaceAll(EMBL_AC_RE, StringUtils.EMPTY);
    }

    protected static Map<String, List<String>> getQualifiers(Element element) {
        return XomUtils.hashValuesByAttribute(element.getChildElements("qualifier"), "name");
    }

    protected String getGeneId(AnnotatedGene gene) {
        List<GeneName> names = gene.getNameMap().get(GeneNameType.ORDEREDLOCUSNAMES);
        if (names != null && names.size() > 0) {
            return names.get(0).getName();
        } else {
            return null;
        }
    }

    protected List<DatabaseReference> parseXrefs(Element feature) {
        List<DatabaseReference> refs = CollectionUtils.createArrayList();
        for (Element elem : new ElementsIterable(feature.getChildElements("xref"))) {
            String db = elem.getAttributeValue("db");
            if (db.equals("EnsemblGenomes")) {
                // skip references to ourselves
                continue;
            }
            String id = elem.getAttributeValue("id");
            DatabaseReferenceType type = getXrefRegistry().getTypeForOtherName(db);
            if (type == null) {
                getLog().debug("Unknown database type " + db);
            } else {
                refs.add(new DatabaseReferenceImpl(type, id));
            }
        }
        return refs;
    }

    protected static String getDescription(Map<String, List<String>> qualifiers) {
        String des = CollectionUtils.getFirstElement(qualifiers.get("product"), null);
        if (!StringUtils.isEmpty(des)) {
            des = des.replaceAll("[ \\n\\t\\r]+", " ");
        }
        return des;
    }

    private Log log;

    protected Log getLog() {
        if (log == null) {
            log = LogFactory.getLog(this.getClass());
        }
        return log;
    }

    private final DatabaseReferenceTypeRegistry registry;

    protected DatabaseReferenceTypeRegistry getXrefRegistry() {
        return registry;
    }

    public XmlEnaFeatureParser(DatabaseReferenceTypeRegistry registry) {
        this.registry = registry;
    }

    /**
     * Parse the XML element and attach it to the component
     * 
     * @param component
     *            target component
     * @param element
     *            feature element from ENA XML file
     */
    public abstract void parseFeature(GenomicComponentImpl component, Element element);

    /**
     * @return optional list of feature parsers that need to be run first
     */
    public abstract List<Class<? extends XmlEnaFeatureParser>> dependsOn();

    /**
     * Utility method to extract features that are enclosed by a specified
     * location
     * 
     * @param <T>
     * @param features
     * @param loc
     * @return
     */
    protected static <T extends Locatable> List<T> findEnclosedFeatures(Collection<T> features, EntityLocation loc) {
        List<T> enclosed = CollectionUtils.createArrayList();
        for (T feature : features) {
            if (LocationUtils.encloses(loc, feature.getLocation())) {
                enclosed.add(feature);
            }
        }
        return enclosed;
    }

    /**
     * Utility method to extract features who share the same gene name
     * 
     * @param <T>
     * @param features
     *            list of features to check
     * @param genes
     *            gene name set to check
     * @return
     */
    protected static <T extends AnnotatedGene> List<T> findGenesByName(Collection<T> features,
            Collection<GeneName> genes) {
        List<T> enclosed = CollectionUtils.createArrayList();
        for (T feature : features) {
            for (GeneName name : genes) {
                for (GeneName locusT : feature.getNameMap().get(name.getType())) {
                    if (name.equals(locusT)) {
                        enclosed.add(feature);
                        break;
                    }
                }
            }
        }
        return enclosed;
    }

    protected static boolean locationsOverlap(RichLocation loc1, RichLocation loc2) {
        return (loc1.getStrand().equals(loc2.getStrand()) && loc1.overlaps(loc2));
    }

    protected AnnotatedGene getGeneName(Map<String, List<String>> qualifiers) {
        AnnotatedGeneImpl gene = new AnnotatedGeneImpl();
        for (String geneName : qualifiers.get("gene")) {
            if (StringUtils.isEmpty(gene.getIdentifyingId())) {
                gene.setIdentifyingId(geneName);
            }
            gene.addGeneName(new GeneNameImpl(geneName, GeneNameType.NAME));
        }
        for (String geneName : qualifiers.get("locus_tag")) {
            if (StringUtils.isEmpty(gene.getIdentifyingId())) {
                gene.setIdentifyingId(geneName);
            }
            gene.addGeneName(new GeneNameImpl(geneName, GeneNameType.ORDEREDLOCUSNAMES));
        }
        for (String geneName : qualifiers.get("gene_synonym")) {
            gene.addGeneName(new GeneNameImpl(geneName, GeneNameType.SYNONYMS));
        }
        for (String geneName : qualifiers.get("old_locus_tag")) {
            gene.addGeneName(new GeneNameImpl(geneName, GeneNameType.SYNONYMS));
        }
        return gene;
    }

    protected boolean hasNote(Map<String, List<String>> qualifiers, String string) {
        boolean matches = false;
        List<String> vals = qualifiers.get("note");
        if (vals != null) {
            for (String s : vals) {
                if (s.matches(string)) {
                    matches = true;
                    break;
                }
            }
        }
        return matches;
    }

}
