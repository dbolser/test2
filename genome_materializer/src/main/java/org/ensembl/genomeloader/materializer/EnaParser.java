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
 * File: XmlEnaComponentParser.java
 * Created by: dstaines
 * Created on: Mar 23, 2010
 * CVS:  $$
 */
package org.ensembl.genomeloader.materializer;

import static org.ensembl.genomeloader.materializer.impl.XomUtils.getFirstChild;
import static org.ensembl.genomeloader.materializer.impl.XomUtils.hashByAttribute;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executor;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.materializer.impl.CdsFeatureParser;
import org.ensembl.genomeloader.materializer.impl.DefaultXmlEnaFeatureParser;
import org.ensembl.genomeloader.materializer.impl.GeneFeatureParser;
import org.ensembl.genomeloader.materializer.impl.MrnaFeatureParser;
import org.ensembl.genomeloader.materializer.impl.PeptideFeatureParser;
import org.ensembl.genomeloader.materializer.impl.RepeatFeatureParser;
import org.ensembl.genomeloader.materializer.impl.RnaFeatureParser;
import org.ensembl.genomeloader.materializer.impl.SourceFeatureParser;
import org.ensembl.genomeloader.materializer.impl.UtrFeatureParser;
import org.ensembl.genomeloader.materializer.impl.XmlEnaFeatureParser;
import org.ensembl.genomeloader.materializer.impl.XomUtils.ElementsIterable;
import org.ensembl.genomeloader.metadata.GenomicComponentMetaData;
import org.ensembl.genomeloader.model.DatabaseReference;
import org.ensembl.genomeloader.model.DatabaseReferenceType;
import org.ensembl.genomeloader.model.GenomicComponent;
import org.ensembl.genomeloader.model.impl.AssemblySequenceImpl;
import org.ensembl.genomeloader.model.impl.DatabaseReferenceImpl;
import org.ensembl.genomeloader.model.impl.GenomicComponentImpl;
import org.ensembl.genomeloader.model.sequence.Sequence;
import org.ensembl.genomeloader.model.sequence.SequenceInformation;
import org.ensembl.genomeloader.util.InputOutputUtils;
import org.ensembl.genomeloader.util.biojava.LocationUtils;
import org.ensembl.genomeloader.util.collections.CollectionUtils;
import org.ensembl.genomeloader.util.reflection.ReflectionUtils;
import org.ensembl.genomeloader.xrefregistry.DatabaseReferenceTypeRegistry;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

/**
 * @author dstaines
 * 
 */
public class EnaParser {

    /**
     * @author dstaines
     * 
     */
    public static final class XmlEnaFeatureParserComparator implements Comparator<XmlEnaFeatureParser> {
        public int compare(XmlEnaFeatureParser o1, XmlEnaFeatureParser o2) {
            if (o1.dependsOn().contains(o2.getClass())) {
                return 1;
            } else if (o2.dependsOn().contains(o1.getClass())) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    private static final int MAX_TRIES = 3;

    /**
     * Lookup for parsers for each class
     */
    private static Map<String, Class<? extends XmlEnaFeatureParser>> parsersByName = new HashMap<String, Class<? extends XmlEnaFeatureParser>>() {
        {
            put("CDS", CdsFeatureParser.class);
            put("gene", GeneFeatureParser.class);
            put("tRNA", RnaFeatureParser.class);
            put("rRNA", RnaFeatureParser.class);
            put("ncRNA", RnaFeatureParser.class);
            put("tmRNA", RnaFeatureParser.class);
            put("mRNA", MrnaFeatureParser.class);
            put("5'UTR", UtrFeatureParser.class);
            put("3'UTR", UtrFeatureParser.class);
            put("repeat_region", RepeatFeatureParser.class);
            put("mat_peptide", PeptideFeatureParser.class);
            put("sig_peptide", PeptideFeatureParser.class);
            put("transit_peptide", PeptideFeatureParser.class);
        }
    };

    private static final long SLEEP_TIME = 30000;
    private final Executor executor;
    private Log log;

    private final DatabaseReferenceType pubmedType;
    private final DatabaseReferenceTypeRegistry registry;

    public EnaParser(Executor executor, DatabaseReferenceTypeRegistry registry) {
        this.executor = executor;
        this.registry = registry;
        pubmedType = registry.getTypeForName("PUBMED");
    }

    protected Log getLog() {
        if (log == null) {
            log = LogFactory.getLog(this.getClass());
        }
        return log;
    }

    /**
     * Get a parser for the supplied feature
     * 
     * @param key
     *            name of feature
     * @return parser instance or null if none supported
     */
    protected XmlEnaFeatureParser getParserForFeature(String key) {
        Class<? extends XmlEnaFeatureParser> clazz = parsersByName.get(key);
        if (clazz != null) {
            return (XmlEnaFeatureParser) ReflectionUtils.newInstance(clazz,
                    new Class[] { DatabaseReferenceTypeRegistry.class }, new Object[] { registry });
        } else {
            getLog().warn("No parser found for feature " + key + ": using "
                    + DefaultXmlEnaFeatureParser.class.getSimpleName());
            return new DefaultXmlEnaFeatureParser(registry);
        }
    }

    protected Map<String, String> getProperties(Element entryElem) {
        Map<String, String> properties = CollectionUtils.createHashMap();
        if (entryElem.getAttribute("version") == null) {
            throw new EnaParsingException("version not set in entry element");
        }
        properties.put(SequenceInformation.PROPERTY_VERSION, entryElem.getAttribute("version").getValue());
        if (entryElem.getAttribute("lastUpdated") == null) {
            throw new EnaParsingException("lastUpdated not set in entry element");
        }
        properties.put(SequenceInformation.PROPERTY_DATE, entryElem.getAttribute("lastUpdated").getValue());
        return properties;
    }

    public GenomicComponentImpl parse(GenomicComponentMetaData md, Document doc) {

        GenomicComponentImpl component = new GenomicComponentImpl(md);
        Element entryElem = getFirstChild(doc.getRootElement(), "entry");
        if (entryElem == null) {
            throw new EnaParsingException("entry element not found - record not available");
        }
        // metadata needs parsing first to pick up version of record which is
        // used by other parsers
        parseMetaData(md, entryElem);
        Sequence seq = parseSequence(entryElem);
        parseFeatures(component, entryElem);
        component.setAccession(component.getMetaData().getAccession());
        component.setLength(component.getMetaData().getLength());
        if (component.getLength() != seq.getSequence().length()) {
            throw new EnaParsingException(component.getMetaData().getAccession() + " metadata has length "
                    + component.getMetaData().getLength() + " but parsed sequence contains "
                    + seq.getSequence().length() + " characters");
        }
        seq.setDescription(component.getMetaData().getDescription());
        seq.setIdentifier(component.getAccession());
        component.setSequence(seq);
        component.getDatabaseReferences().addAll(parseReferences(entryElem));
        parseContig(component, entryElem);
        return component;
    }

    public GenomicComponent parse(GenomicComponentMetaData md, File file) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            return parse(md, is);
        } catch (FileNotFoundException e) {
            throw new EnaParsingException("Could not parse ENA record from file " + file.getPath(), e);
        } finally {
            InputOutputUtils.closeQuietly(is);
        }
    }

    public GenomicComponent parse(GenomicComponentMetaData md, InputStream record) {
        return parse(md, parseDocument(record));
    }

    public GenomicComponent parse(GenomicComponentMetaData md, final URL url) {
        try {
            final File f = File.createTempFile("ENA", ".xml");
            executor.execute(new Runnable() {
                public void run() {
                    int tries = 0;
                    InputStream is = null;
                    while (tries < MAX_TRIES) {
                        try {
                            URLConnection uc = url.openConnection();
                            is = uc.getInputStream();
                            InputOutputUtils.copyInputStreamToFileSystem(is, f);
                            break;
                        } catch (IOException e) {
                            if (tries++ < MAX_TRIES) {
                                getLog().warn("Could not parse ENA record from URL " + url + ": retrying", e);
                                try {
                                    Thread.sleep(SLEEP_TIME);
                                } catch (InterruptedException e1) {
                                    getLog().warn("Woke up from sleep", e1);
                                }
                            } else {
                                throw new EnaParsingException("Could not parse ENA record from URL " + url, e);
                            }
                        } finally {
                            InputOutputUtils.closeQuietly(is);
                        }
                    }
                }
            });
            if (f == null) {
                throw new EnaParsingException("Could not parse ENA record from URL " + url);
            }
            try {
                return parse(md, f);
            } finally {
                f.delete();
            }
        } catch (IOException e) {
            throw new EnaParsingException(e);
        }

    }

    protected void parseContig(GenomicComponentImpl component, Element entryElem) {
        /*
         * <contig> <range primaryBegin="1" primaryEnd="14671" begin="1"
         * end="14671" accession="ACNK01000001" version="1" complement="true"/>
         * <gap begin="14672" end="14771" length="100" unknownLength="true"/>
         * <range primaryBegin="1" primaryEnd="48149" begin="14772" end="62920"
         * accession="ACNK01000002" version="1" complement="true"/> <gap
         * begin="62921" end="63020" length="100" unknownLength="true"/>
         */

        Elements contigElements = entryElem.getChildElements("contig");
        if (contigElements.size() == 1) {
            Elements childElements = contigElements.get(0).getChildElements();
            for (int i = 0; i < childElements.size(); i++) {
                Element childElement = childElements.get(i);
                String name = childElement.getLocalName();
                if (name.equals("range")) {

                    component
                            .getAssemblyElements().add(
                                    new AssemblySequenceImpl(
                                            LocationUtils.buildLocation(
                                                    Integer.parseInt(childElement.getAttributeValue("primaryBegin")),
                                                    Integer.parseInt(childElement.getAttributeValue("primaryEnd")), 0,
                                                    "true".equals(childElement.getAttributeValue("complement")), null),
                                            childElement.getAttributeValue("accession"),
                                            Integer.parseInt(childElement.getAttributeValue("version")),
                                            Integer.parseInt(childElement.getAttributeValue("begin")),
                                            Integer.parseInt(childElement.getAttributeValue("end"))));

                } else if (name.equals("gap")) {

                    // component
                    // .getAssemblyElements()
                    // .add(new AssemblyGapImpl(
                    // LocationUtils.buildLocation(
                    // Integer.parseInt(childElement
                    // .getAttributeValue("begin")),
                    // Integer.parseInt(childElement
                    // .getAttributeValue("end")),
                    // component.getLength(),
                    // "true".equals(childElement
                    // .getAttributeValue("complement")),
                    // null),
                    // Integer.parseInt(childElement
                    // .getAttributeValue("length")),
                    // "true".equals(childElement
                    // .getAttributeValue("unknownLength"))));

                } else {
                    getLog().warn("Don't know how to process contig element " + name);
                }
            }
        }
    }

    public Document parseDocument(InputStream record) {
        try {
            Document doc = new Builder().build(new BufferedReader(new InputStreamReader(record)));
            return doc;
        } catch (ValidityException e) {
            throw new EnaParsingException("Could not parse ENA record", e);
        } catch (ParsingException e) {
            throw new EnaParsingException("Could not parse ENA record", e);
        } catch (IOException e) {
            throw new EnaParsingException("Could not parse ENA record", e);
        } finally {
        }
    }

    /**
     * @param component
     * @param entryElem
     */
    protected void parseFeatures(GenomicComponentImpl component, Element entryElem) {

        Elements childElements = entryElem.getChildElements("feature");

        // 1. hash features by name
        Map<String, List<Element>> features = hashByAttribute(childElements, "name");
        Map<XmlEnaFeatureParser, List<Element>> parsers = CollectionUtils.createHashMap();
        for (Entry<String, List<Element>> e : features.entrySet()) {
            getLog().debug("Handling " + e.getValue().size() + " features of type " + e.getKey());
            try {
                XmlEnaFeatureParser parserForFeature = getParserForFeature(e.getKey());
                getLog().debug("Found parser " + parserForFeature.getClass().getName());
                parsers.put(parserForFeature, e.getValue());
            } catch (EnaParsingException ex) {
                getLog().warn(ex.getMessage());
            } finally {
            }
        }

        // 2. process features in stages using individual parsers
        // 2.1 parse CDSs into gene-protein-transcripts or pseudogenes
        // accordingly
        // 2.2 parse mRNA
        // 2.3 parse ncRNA
        // 2.4 parse gene -> overlay onto proteins or turn into pseudogenes
        // 2.5 parse repeats
        // 2.6 parse features
        // 2.7 parse products
        getLog().debug("Found " + parsers.keySet().size() + " parser-map pairs to deal with:" + parsers.keySet());
        List<XmlEnaFeatureParser> sortParsers = sortParsers(parsers.keySet());
        for (XmlEnaFeatureParser parser : sortParsers) {
            // parse each feature in turn
            List<Element> featuresForParser = parsers.get(parser);
            getLog().info(
                    "Parsing " + featuresForParser.size() + " features with " + parser.getClass().getSimpleName());
            for (Element feature : featuresForParser) {
                parser.parseFeature(component, feature);
            }
        }

        component.setTopLevel(true);

    }

    protected void parseMetaData(GenomicComponentMetaData md, Element entryElem) {

        // parse out source first so we can set it into the genome metadata if required
        for (Element elem : new ElementsIterable(entryElem.getChildElements("feature"))) {
            if ("source".equals(elem.getAttributeValue("name"))) {
               new SourceFeatureParser().parseFeature(md, elem);
            }
        }

        // <entry accession="AP001918" version="1" entryVersion="12"
        // dataClass="STD"
        // taxonomicDivision="UNC" moleculeType="genomic DNA"
        // sequenceLength="99159"
        // topology="circular" firstPublic="2000-07-06" firstPublicRelease="64"
        // lastUpdated="2007-11-28" lastUpdatedRelease="93">
        // <description>Plasmid F genomic DNA, complete sequence.</description>
        Attribute acc = entryElem.getAttribute("accession");
        if (acc == null) {
            String msg = "Accession attribute not found";
            getLog().debug(msg + entryElem.toXML());
            throw new EnaParsingException(msg);
        }
        md.setCon("CON".equals(entryElem.getAttributeValue("dataClass")));
        md.setAccession(acc.getValue());
        md.setVersion(entryElem.getAttributeValue("version"));
        md.setCreationDate(EnaGenomeMaterializer.parseEnaDate(entryElem.getAttributeValue("firstPublic")));
        md.setUpdateDate(EnaGenomeMaterializer.parseEnaDate(entryElem.getAttributeValue("lastUpdated")));
        md.setLength(Integer.parseInt(entryElem.getAttribute("sequenceLength").getValue()));
        md.setDescription(getFirstChild(entryElem, "description").getValue());
        md.getDescriptionHandler().parseComponentDescription(md);
        Attribute topoElem = entryElem.getAttribute("topology");
        if (topoElem == null) {
            getLog().warn("Topology not set for " + acc.getValue());
        } else {
            md.setCircular("circular".equals(topoElem.getValue()));
        }
        md.setGeneticCode(GenomicComponentMetaData.NULL_GENETIC_CODE);
        // set master accession if WGS and shares same root (to deal with
        // multiple secondaries)
        Elements childElements = entryElem.getChildElements("secondaryAccession");
        if ("WGS".equals(entryElem.getAttributeValue("dataClass"))) {
            if (childElements.size() == 1) {
                md.setMasterAccession(childElements.get(0).getValue());
            } else {
                for (Element elem : new ElementsIterable(childElements)) {
                    String masterAc = elem.getValue();
                    String root = masterAc.replaceAll("0+$", "");
                    if (masterAc.startsWith(root)) {
                        md.setMasterAccession(masterAc);
                        break;
                    }
                }
            }
        }

    }

    protected Set<DatabaseReference> parseReferences(Element entryElem) {
        Set<DatabaseReference> refs = CollectionUtils.createHashSet();
        for (Element refElem : new ElementsIterable(entryElem.getChildElements("reference"))) {
            String pmid = null;
            String doi = null;
            for (Element xrefElem : new ElementsIterable(refElem.getChildElements("xref"))) {
                // <xref db="DOI" id="10.1126/science.277.5331.1453"/>
                // <xref db="PUBMED" id="9278503"/>
                if (xrefElem.getAttributeValue("db").equals("DOI")) {
                    doi = xrefElem.getAttributeValue("id");
                } else if (xrefElem.getAttributeValue("db").equals("PUBMED")) {
                    pmid = xrefElem.getAttributeValue("id");
                }
            }
            if (!StringUtils.isEmpty(pmid)) {
                DatabaseReferenceImpl xref = new DatabaseReferenceImpl(pubmedType, pmid);
                if (!StringUtils.isEmpty(doi)) {
                    xref.setSecondaryIdentifier(doi);
                }
                refs.add(xref);
            }
        }
        return refs;
    }

    protected Sequence parseSequence(Element entryElem) {
        Sequence seq = new Sequence(entryElem.getChildElements("sequence").get(0).getValue().replaceAll("\\s+", ""));
        seq.setProperties(getProperties(entryElem));
        return seq;
    }

    private List<XmlEnaFeatureParser> sortParsers(Set<XmlEnaFeatureParser> keySet) {
        List<XmlEnaFeatureParser> parsers = new ArrayList<XmlEnaFeatureParser>();
        parsers.addAll(keySet);
        parsers = CollectionUtils.topoSort(parsers, new CollectionUtils.TopologicalComparator<XmlEnaFeatureParser>() {
            public int countEdges(XmlEnaFeatureParser t) {
                return t.dependsOn().size();
            }

            public boolean hasEdge(XmlEnaFeatureParser from, XmlEnaFeatureParser to) {
                return from.dependsOn().contains(to.getClass());
            }
        });
        Collections.reverse(parsers);
        return parsers;
    }

}
