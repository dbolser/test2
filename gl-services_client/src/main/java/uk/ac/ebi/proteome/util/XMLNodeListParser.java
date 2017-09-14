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
 * File: XMLNodeFactory.java
 * Created by: dstaines
 * Created on: Feb 8, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import uk.ac.ebi.proteome.services.ServiceUncheckedException;

/**
 * Simple class for extracting nodes from a file as a list or by name
 * 
 * @author dstaines
 * 
 */
public class XMLNodeListParser {

	private InputSource inputSource;

	private String nodePath;

	private String nodeIdentifier;

	private Node rootNode = null;

	private XPath xpath;

	/**
	 * Construct a parser for the specified nodes read from the supplied input
	 * source
	 * 
	 * @param inputSource
	 * @param nodePath
	 *            XPath query for nodes to retrieve
	 * @param nodeIdentifier
	 *            attribute uniquely identifying each node for retrieval
	 */
	public XMLNodeListParser(InputSource inputSource, String nodePath,
			String nodeIdentifier) {
		this.inputSource = inputSource;
		this.nodePath = nodePath;
		this.nodeIdentifier = nodeIdentifier;
		this.xpath = XPathFactory.newInstance().newXPath();
	}

	/**
	 * Construct a parser for the specified nodes read from the supplied reader
	 * 
	 * @param reader
	 * @param nodePath
	 *            XPath query for nodes to retrieve
	 * @param nodeIdentifier
	 *            attribute uniquely identifying each node for retrieval
	 */
	public XMLNodeListParser(Reader reader, String nodePath,
			String nodeIdentifier) {
		this.inputSource = new InputSource(reader);
		this.nodePath = nodePath;
		this.nodeIdentifier = nodeIdentifier;
		this.xpath = XPathFactory.newInstance().newXPath();
	}

	/**
	 * Construct a parser for the specified nodes read from the supplied file
	 * 
	 * @param file
	 * @param nodePath
	 *            XPath query for nodes to retrieve
	 * @param nodeIdentifier
	 *            attribute uniquely identifying each node for retrieval
	 * @throws FileNotFoundException
	 */
	public XMLNodeListParser(File file, String nodePath, String nodeIdentifier)
			throws FileNotFoundException {
		this.inputSource = new InputSource(new FileReader(file));
		this.nodePath = nodePath;
		this.nodeIdentifier = nodeIdentifier;
		this.xpath = XPathFactory.newInstance().newXPath();
	}

	/**
	 * Construct a parser for the specified nodes read from the supplied
	 * location
	 * 
	 * @param location
	 * @param nodePath
	 *            XPath query for nodes to retrieve
	 * @param nodeIdentifier
	 *            attribute uniquely identifying each node for retrieval
	 */
	public XMLNodeListParser(String location, String nodePath,
			String nodeIdentifier) {
		this.inputSource = new InputSource(location);
		this.nodePath = nodePath;
		this.nodeIdentifier = nodeIdentifier;
		this.xpath = XPathFactory.newInstance().newXPath();
	}

	/**
	 * Construct a parser for the specified nodes read from the supplied
	 * root node
	 * @param rootNode
	 * @param nodePath
	 *            XPath query for nodes to retrieve
	 * @param nodeIdentifier
	 *            attribute uniquely identifying each node for retrieval
	 */
	public XMLNodeListParser(Node rootNode, String nodePath,
			String nodeIdentifier) {
		this.rootNode = rootNode;
		this.nodePath = nodePath;
		this.nodeIdentifier = nodeIdentifier;
		this.xpath = XPathFactory.newInstance().newXPath();
	}

	private static final Object LOCK = new Object();

	/**
	 * Retrieve the root node for the source
	 * 
	 * @return
	 */
	public Node getRootNode() {
		synchronized (LOCK) {
			if (rootNode == null) {
				try {
					DocumentBuilder builder = DocumentBuilderFactory
							.newInstance().newDocumentBuilder();
					rootNode = builder.parse(inputSource);
				} catch (ParserConfigurationException e) {
					throw new ServiceUncheckedException(
							"Could not get XML parser configuration", e);
				} catch (SAXException e) {
					throw new ServiceUncheckedException("Could not parse XML",
							e);
				} catch (IOException e) {
					throw new ServiceUncheckedException(
							"Could not read in XML", e);
				}
			}
		}
		return rootNode;
	}

	private String getNodeListQuery() {
		return nodePath;
	}

	private String namedNodeQuery = null;

	private String getNamedNodeQuery() {
		if (namedNodeQuery == null) {
			namedNodeQuery = getNodeListQuery() + "[@" + nodeIdentifier
					+ "=''{0}'']";
		}
		return namedNodeQuery;
	}

	/**
	 * Retrieve all the nodes as a Java list
	 * 
	 * @return
	 */
	public List<Node> getNodesAsList() {
		NodeList nodeList = getNodeList();
		List<Node> nodes = new ArrayList<Node>(nodeList.getLength());
		for (int i = 0; i < nodeList.getLength(); i++) {
			nodes.add(nodeList.item(i));
		}
		return nodes;
	}

	/**
	 * Retrieve all the nodes as a DOM NodeList
	 * 
	 * @return
	 */
	public NodeList getNodeList() {
		NodeList nodeList = null;
		try {
			nodeList = (NodeList) xpath.evaluate(getNodeListQuery(),
					getRootNode(), XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			throw new ServiceUncheckedException(
					"Found XPATH problem whilst parsing input XML file", e);
		}
		return nodeList;
	}

	/**
	 * Retrieve the node with the specified name
	 * 
	 * @param name
	 * @return
	 */
	public Node getNodeForName(String name) {
		Node targetNode = null;
		String query = MessageFormat.format(getNamedNodeQuery(), name);
		try {
			targetNode = (Node) xpath.evaluate(query, getRootNode(),
					XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			throw new ServiceUncheckedException("Could not parse XPath query "
					+ query, e);
		}
		return targetNode;
	}

}
