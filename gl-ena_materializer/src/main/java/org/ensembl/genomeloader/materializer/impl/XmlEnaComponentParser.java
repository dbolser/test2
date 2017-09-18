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
package org.ensembl.genomeloader.materializer.impl;

import static org.ensembl.genomeloader.materializer.impl.XomUtils.getFirstChild;
import static org.ensembl.genomeloader.materializer.impl.XomUtils.hashByAttribute;

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
import org.ensembl.genomeloader.genomebuilder.metadata.GenomicComponentMetaData;
import org.ensembl.genomeloader.genomebuilder.model.DatabaseReference;
import org.ensembl.genomeloader.genomebuilder.model.DatabaseReferenceType;
import org.ensembl.genomeloader.genomebuilder.model.impl.AssemblySequenceImpl;
import org.ensembl.genomeloader.genomebuilder.model.impl.DatabaseReferenceImpl;
import org.ensembl.genomeloader.genomebuilder.model.impl.GenomicComponentImpl;
import org.ensembl.genomeloader.genomebuilder.model.sequence.Sequence;
import org.ensembl.genomeloader.genomebuilder.model.sequence.SequenceInformation;
import org.ensembl.genomeloader.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import org.ensembl.genomeloader.materializer.EnaGenomeMaterializer;
import org.ensembl.genomeloader.materializer.EnaParser;
import org.ensembl.genomeloader.materializer.EnaParsingException;
import org.ensembl.genomeloader.materializer.impl.XomUtils.ElementsIterable;
import org.ensembl.genomeloader.util.biojava.LocationUtils;
import org.ensembl.genomeloader.util.collections.CollectionUtils;
import org.ensembl.genomeloader.util.reflection.ReflectionUtils;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

/**
 * @author dstaines
 * 
 */
public class XmlEnaComponentParser extends
		AbstractXmlEnaParser<GenomicComponentImpl> implements
		EnaParser<GenomicComponentImpl> {

	/**
	 * @author dstaines
	 * 
	 */
	public static final class XmlEnaFeatureParserComparator implements
			Comparator<XmlEnaFeatureParser> {
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

	/**
	 * Lookup for parsers for each class
	 */
	private static Map<String, Class<? extends XmlEnaFeatureParser>> parsersByName = new HashMap<String, Class<? extends XmlEnaFeatureParser>>() {
		{
			put("CDS", CdsFeatureParser.class);
			put("gene", GeneFeatureParser.class);
			put("source", SourceFeatureParser.class);
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
					new Class[] { DatabaseReferenceTypeRegistry.class },
					new Object[] { registry });
		} else {
			getLog().warn(
					"No parser found for feature " + key + ": using "
							+ DefaultXmlEnaFeatureParser.class.getSimpleName());
			return new DefaultXmlEnaFeatureParser(registry);
		}
	}

	private final DatabaseReferenceTypeRegistry registry;
	private final DatabaseReferenceType pubmedType;

	public XmlEnaComponentParser(Executor executor,
			DatabaseReferenceTypeRegistry registry) {
		super(executor);
		this.registry = registry;
		pubmedType = registry.getTypeForName("PUBMED");
	}

	public GenomicComponentImpl parse(Document doc) {

		GenomicComponentImpl component = new GenomicComponentImpl();
		Element entryElem = getFirstChild(doc.getRootElement(), "entry");
		if (entryElem == null) {
			throw new EnaParsingException(
					"entry element not found - record not available");
		}
		component.setMetaData(parseMetaData(entryElem));
		Sequence seq = parseSequence(entryElem);
		parseFeatures(component, entryElem);
		component.setAccession(component.getMetaData().getAccession());
		component.setLength(component.getMetaData().getLength());
		if (component.getLength() != seq.getSequence().length()) {
			throw new EnaParsingException(component.getMetaData()
					.getAccession()
					+ " metadata has length "
					+ component.getMetaData().getLength()
					+ " but parsed sequence contains "
					+ seq.getSequence().length() + " characters");
		}
		seq.setDescription(component.getMetaData().getDescription());
		seq.setIdentifier(component.getAccession());
		component.setSequence(seq);
		component.getDatabaseReferences().addAll(parseReferences(entryElem));
		parseContig(component, entryElem);
		return component;
	}

	protected Set<DatabaseReference> parseReferences(Element entryElem) {
		Set<DatabaseReference> refs = CollectionUtils.createHashSet();
		for (Element refElem : new ElementsIterable(
				entryElem.getChildElements("reference"))) {
			String pmid = null;
			String doi = null;
			for (Element xrefElem : new ElementsIterable(
					refElem.getChildElements("xref"))) {
				// <xref db="DOI" id="10.1126/science.277.5331.1453"/>
				// <xref db="PUBMED" id="9278503"/>
				if (xrefElem.getAttributeValue("db").equals("DOI")) {
					doi = xrefElem.getAttributeValue("id");
				} else if (xrefElem.getAttributeValue("db").equals("PUBMED")) {
					pmid = xrefElem.getAttributeValue("id");
				}
			}
			if (!StringUtils.isEmpty(pmid)) {
				DatabaseReferenceImpl xref = new DatabaseReferenceImpl(
						pubmedType, pmid);
				if (!StringUtils.isEmpty(doi)) {
					xref.setSecondaryIdentifier(doi);
				}
				refs.add(xref);
			}
		}
		return refs;
	}

	protected Map<String, String> getProperties(Element entryElem) {
		Map<String, String> properties = CollectionUtils.createHashMap();
		if (entryElem.getAttribute("version") == null) {
			throw new EnaParsingException("version not set in entry element");
		}
		properties.put(SequenceInformation.PROPERTY_VERSION, entryElem
				.getAttribute("version").getValue());
		if (entryElem.getAttribute("lastUpdated") == null) {
			throw new EnaParsingException(
					"lastUpdated not set in entry element");
		}
		properties.put(SequenceInformation.PROPERTY_DATE, entryElem
				.getAttribute("lastUpdated").getValue());
		return properties;
	}

	/**
	 * @param component
	 * @param entryElem
	 */
	protected void parseFeatures(GenomicComponentImpl component,
			Element entryElem) {

		Elements childElements = entryElem.getChildElements("feature");

		// 1. hash features by name
		Map<String, List<Element>> features = hashByAttribute(childElements,
				"name");
		Map<XmlEnaFeatureParser, List<Element>> parsers = CollectionUtils
				.createHashMap();
		for (Entry<String, List<Element>> e : features.entrySet()) {
			getLog().debug(
					"Handling " + e.getValue().size() + " features of type "
							+ e.getKey());
			try {
				XmlEnaFeatureParser parserForFeature = getParserForFeature(e
						.getKey());
				getLog().debug(
						"Found parser " + parserForFeature.getClass().getName());
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
		getLog().debug(
				"Found " + parsers.keySet().size()
						+ " parser-map pairs to deal with:" + parsers.keySet());
		List<XmlEnaFeatureParser> sortParsers = sortParsers(parsers.keySet());
		for (XmlEnaFeatureParser parser : sortParsers) {
			// parse each feature in turn
			List<Element> featuresForParser = parsers.get(parser);
			getLog().info(
					"Parsing " + featuresForParser.size() + " features with "
							+ parser.getClass().getSimpleName());
			for (Element feature : featuresForParser) {
				parser.parseFeature(component, feature);
			}
		}

		component.setTopLevel(true);

	}

	protected GenomicComponentMetaData parseMetaData(Element entryElem) {
		// <entry accession="AP001918" version="1" entryVersion="12"
		// dataClass="STD"
		// taxonomicDivision="UNC" moleculeType="genomic DNA"
		// sequenceLength="99159"
		// topology="circular" firstPublic="2000-07-06" firstPublicRelease="64"
		// lastUpdated="2007-11-28" lastUpdatedRelease="93">
		// <description>Plasmid F genomic DNA, complete sequence.</description>
		GenomicComponentMetaData md = new GenomicComponentMetaData();
		Attribute acc = entryElem.getAttribute("accession");
		if (acc == null) {
			String msg = "Accession attribute not found";
			getLog().debug(msg + entryElem.toXML());
			throw new EnaParsingException(msg);
		}
		md.setCon("CON".equals(entryElem.getAttributeValue("dataClass")));
		md.setAccession(acc.getValue());
		md.setVersion(entryElem.getAttributeValue("version"));
		md.setCreationDate(EnaGenomeMaterializer.parseEnaDate(entryElem
				.getAttributeValue("firstPublic")));
		md.setUpdateDate(EnaGenomeMaterializer.parseEnaDate(entryElem
				.getAttributeValue("lastUpdated")));
		md.setLength(Integer.parseInt(entryElem.getAttribute("sequenceLength")
				.getValue()));
		md.setDescription(getFirstChild(entryElem, "description").getValue());
		md.parseComponentDescription();
		Attribute topoElem = entryElem.getAttribute("topology");
		if (topoElem == null) {
			getLog().warn("Topology not set for " + acc.getValue());
		} else {
			md.setCircular("circular".equals(topoElem.getValue()));
		}
		md.setGeneticCode(GenomicComponentMetaData.NULL_GENETIC_CODE);
		// set master accession if WGS and shares same root (to deal with
		// multiple secondaries)
		Elements childElements = entryElem
				.getChildElements("secondaryAccession");
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
		return md;
	}

	protected Sequence parseSequence(Element entryElem) {
		Sequence seq = new Sequence(entryElem.getChildElements("sequence")
				.get(0).getValue().replaceAll("\\s+", ""));
		seq.setProperties(getProperties(entryElem));
		return seq;
	}

	private List<XmlEnaFeatureParser> sortParsers(
			Set<XmlEnaFeatureParser> keySet) {
		List<XmlEnaFeatureParser> parsers = new ArrayList<XmlEnaFeatureParser>();
		parsers.addAll(keySet);
		parsers = CollectionUtils
				.topoSort(
						parsers,
						new CollectionUtils.TopologicalComparator<XmlEnaFeatureParser>() {
							public int countEdges(XmlEnaFeatureParser t) {
								return t.dependsOn().size();
							}

							public boolean hasEdge(XmlEnaFeatureParser from,
									XmlEnaFeatureParser to) {
								return from.dependsOn().contains(to.getClass());
							}
						});
		Collections.reverse(parsers);
		return parsers;
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
							.getAssemblyElements()
							.add(new AssemblySequenceImpl(
									LocationUtils.buildLocation(
											Integer.parseInt(childElement
													.getAttributeValue("primaryBegin")),
											Integer.parseInt(childElement
													.getAttributeValue("primaryEnd")),
											0,
											"true".equals(childElement
													.getAttributeValue("complement")),
											null), childElement
											.getAttributeValue("accession"),
									Integer.parseInt(childElement
											.getAttributeValue("version")),
									Integer.parseInt(childElement
											.getAttributeValue("begin")),
									Integer.parseInt(childElement
											.getAttributeValue("end"))));

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
					getLog().warn(
							"Don't know how to process contig element " + name);
				}
			}
		}
	}

}
