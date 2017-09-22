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
 * File: XmlBeanUtils.java
 * Created by: dstaines
 * Created on: Dec 8, 2006
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.util.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.locale.converters.DateLocaleConverter;
import org.ensembl.genomeloader.services.config.ConfigException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author dstaines
 * 
 */
public class XmlBeanUtils {

	static {
		ConvertUtils.register(new DateLocaleConverter(Locale.getDefault(),
				"d/M/yyyy"), java.util.Date.class);

	}

	public static void xmlToBean(Object bean, String rootElement, File file)
			throws IOException, ConfigException {
		xmlToBean(bean, rootElement, new FileInputStream(file));
	}

	public static void xmlToBean(Object bean, String rootElement, InputStream is)
			throws IOException, ConfigException {
		try {
			xmlToBean(bean, rootElement, DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().parse(is));
		} catch (SAXException e) {
			throw new ConfigException("Could not parse XML input stream", e);
		} catch (ParserConfigurationException e) {
			throw new ConfigException("Could not parse XML input stream", e);
		} finally {
		}
	}

	public static void xmlToBean(Object bean, String rootElement, Document doc)
			throws IOException, ConfigException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		try {
			xmlToBean(bean, (NodeList) (xpath.evaluate(
					"/" + rootElement + "/*", doc, XPathConstants.NODESET)));
		} catch (XPathExpressionException e) {
			throw new ConfigException("Could not parse XML document", e);
		}
	}

	public static void xmlToBean(Object bean, NodeList set)
			throws ConfigException {
		try {
			Node node = null;
			HashMap<String, Object> map = new HashMap<String, Object>();
			Map objDes = BeanUtils.describe(bean);
			for (int i = 0; i < set.getLength(); i++) {
				node = set.item(i);
				if (node != null) {
					if (objDes.containsKey(node.getNodeName())) {
						map.put(node.getNodeName(), node.getFirstChild().getNodeValue());
					} else {
						throw new ConfigException("Unknown element "
								+ node.getNodeName() + " in configuration file");
					}
				}
			}
			BeanUtils.populate(bean, map);
		} catch (IllegalAccessException e) {
			throw new ConfigException("Could not access bean", e);
		} catch (InvocationTargetException e) {
			throw new ConfigException("Could not access bean", e);
		} catch (NoSuchMethodException e) {
			throw new ConfigException("Could not access bean", e);
		} finally {
		}
	}
}
