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
 * File: XomUtils.java
 * Created by: dstaines
 * Created on: Mar 24, 2010
 * CVS:  $$
 */
package org.ensembl.genomeloader.materializer.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;

import org.apache.commons.lang.StringUtils;
import org.ensembl.genomeloader.util.collections.CollectionUtils;
import org.ensembl.genomeloader.util.collections.FactoryMap;
import org.ensembl.genomeloader.util.collections.ObjectFactory;

/**
 * @author dstaines
 * 
 */
public class XomUtils {

	public static class ListMap<K, V> extends FactoryMap<K, List<V>> {
		public ListMap() {
			super(new ObjectFactory<List<V>>() {
				public List<V> get() {
					return CollectionUtils.createArrayList();
				}
			});
		}
	};

	/**
	 * Class for allowing {@link Elements} to be used in a for loop
	 * 
	 * @author dstaines
	 */
	public static class ElementsIterable implements Iterable<Element> {

		private final Elements elems;

		public ElementsIterable(Elements elems) {
			this.elems = elems;
		}

		public Iterator<Element> iterator() {
			return new Iterator<Element>() {
				private int i = 0;

				public boolean hasNext() {
					return i<elems.size();
				}

				public Element next() {
					return elems.get(i++);
				}

				public void remove() {
					throw new UnsupportedOperationException();
				}

			};
		}
	};

	/**
	 * General runtime exception wrapper for problems manipulating XOM
	 * 
	 * @author dstaines
	 * 
	 */
	public static class XomUtilsException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public XomUtilsException() {
			super();
		}

		public XomUtilsException(String message, Throwable cause) {
			super(message, cause);
		}

		public XomUtilsException(String message) {
			super(message);
		}

		public XomUtilsException(Throwable cause) {
			super(cause);
		}

	}

	/**
	 * Divide up child elements by a key attribute
	 * 
	 * @param elem
	 *            parent element of elements to hash
	 * @param attr
	 *            name of attribute to use as key
	 * @return hash of elements keyed by attribute value
	 */
	public static Map<String, List<Element>> hashByAttribute(Element elem,
			String attr) {
		return hashByAttribute(elem.getChildElements(), attr);
	}

	/**
	 * Divide up a set of elements by a key attribute
	 * 
	 * @param elems
	 *            set of elements to hash
	 * @param attr
	 *            name of attribute to use as key
	 * @return hash of elements keyed by attribute value
	 */
	public static Map<String, List<Element>> hashByAttribute(Elements elems,
			String attr) {
		Map<String, List<Element>> fs = new FactoryMap<String, List<Element>>(
				new ObjectFactory<List<Element>>() {
					public List<Element> get() {
						return CollectionUtils.createArrayList();
					}
				});
		for (Element elem : new ElementsIterable(elems)) {
			fs.get(elem.getAttributeValue(attr)).add(elem);
		}
		return fs;
	}

	/**
	 * @param elem
	 * @return first child belonging to the specified element
	 */
	public static Element getFirstChild(Element elem) {
		int count = elem.getChildCount();
		if (count == 0) {
			throw new XomUtilsException("No children found for element " + elem);
		} else {
			Node e = elem.getChild(0);
			if (Element.class.isAssignableFrom(e.getClass())) {
				return (Element) e;
			} else {
				throw new XomUtilsException("Child of " + elem + " of type "
						+ e.getClass().getName() + " cannot be cast as "
						+ Element.class.getName());
			}
		}
	}

	/**
	 * @param elem
	 * @param name
	 * @return first child of the named type belonging to the specified element
	 */
	public static Element getFirstChild(Element elem, String name) {
		Elements elems = elem.getChildElements(name);
		if (elems.size() == 0) {
			return null;
		} else {
			return elems.get(0);
		}
	}

	/**
	 * Build a hash of the values of the supplied elements keyed by the named
	 * attribute
	 * 
	 * @param elems
	 *            elements to use
	 * @param attribute
	 *            name of attribute to use for key
	 * @return hash of values keyed by named attribute
	 */
	public static Map<String, List<String>> hashValuesByAttribute(
			Elements elems, String attribute) {
		Map<String, List<String>> map = new ListMap<String, String>();
		for (Element elem : new ElementsIterable(elems)) {
			String attrVal = elem.getAttributeValue(attribute);
			if (!StringUtils.isEmpty(attrVal)) {
				map.get(attrVal).add(elem.getValue().trim());
			}
		}
		return map;
	}

}
