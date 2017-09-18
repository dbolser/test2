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

package org.ensembl.genomeloader.util.collections;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ensembl.genomeloader.util.sql.SqlServiceTemplate;

/**
 * A set of useful methods you might want to use when creating collections and
 * working with Collections. The first set of methods allow you to escape from
 * generics hell i.e.
 * 
 * <code>
 * <pre>
 * Map&lt;String,List&lt;Map&lt;String,String&gt;&gt;&gt; myMap =
 *   new HashMap&lt;String,List&lt;Map&lt;String,String&gt;&gt;&gt;();
 *  / / We can write this as:
 * Map&lt;String,List&lt;Map&lt;String,String&gt;&gt;&gt; myMap = CollectionUtils.createHashMap();
 * </pre>
 * </code>
 * 
 * Decide for yourself if this is easier or not (and remember that in Java5 you
 * can do static imports so this truncates down to a call to the createHashMap
 * method).
 * 
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class CollectionUtils {

	/**
	 * @author dstaines
	 * Interface to use for determining node order when using {@link CollectionUtils#topoSort(Collection, TopologicalComparator)}
	 * @param <T> node object to compare
	 */
	public static interface TopologicalComparator<T> {
		public int countEdges(T t);
		public boolean hasEdge(T from, T to);
	}

	/**
	 * Returns a hash map typed to the generics specified in the method call
	 */
	public static <K, V> Map<K, V> createHashMap() {
		return new HashMap<K, V>();
	}

	/**
	 * Returns a hash map typed to the generics specified in the method call
	 * with the given initial capacity
	 */
	public static <K, V> Map<K, V> createHashMap(int initialCapacity) {
		return new HashMap<K, V>(initialCapacity);
	}

	/**
	 * Returns a typed array list
	 */
	public static <T> List<T> createArrayList() {
		return new ArrayList<T>();
	}

	/**
	 * Returns a typed array list with the given initial capacity
	 */
	public static <T> List<T> createArrayList(int initialCapacity) {
		return new ArrayList<T>(initialCapacity);
	}

	/**
	 * Creates a list and populates it with the contents of args
	 * 
	 * @param <T>
	 *            Generic type of list
	 * @param args
	 *            Elements to go into the list
	 * @return List of args typed accordingly
	 */
	public static <T> List<T> createArrayList(T... args) {
		List<T> list = createArrayList();
		list.addAll(asList(args));
		return list;
	}

	/**
	 * Returns a linked hash map typed to the generics specified in the method
	 * call
	 */
	public static <K, V> Map<K, V> createLinkedHashMap() {
		return new LinkedHashMap<K, V>();
	}

	/**
	 * Returns a linked hash map typed to the generics specified in the method
	 * call with the given initial capacity
	 */
	public static <K, V> Map<K, V> createLinkedHashMap(int initialCapacity) {
		return new LinkedHashMap<K, V>(initialCapacity);
	}

	/**
	 * Returns a hash set typed to the generics specified in the method call
	 */
	public static <T> Set<T> createHashSet() {
		return new HashSet<T>();
	}

	/**
	 * Returns a hash set typed to the generics specified in the method call
	 * with the given initial capacity
	 */
	public static <T> Set<T> createHashSet(int initialCapacity) {
		return new HashSet<T>(initialCapacity);
	}

	/**
	 * Returns a linked hash set typed to the generics specified in the method
	 * call
	 */
	public static <T> Set<T> createLinkedHashSet() {
		return new LinkedHashSet<T>();
	}

	/**
	 * Returns a linked hash set typed to the generics specified in the method
	 * call with the given initial capacity
	 */
	public static <T> Set<T> createLinkedHashSet(int initialCapacity) {
		return new LinkedHashSet<T>(initialCapacity);
	}

	/**
	 * Create a multi value map that uses sets with the given initial capacity
	 * 
	 * @param <K>
	 *            class for key
	 * @param <V>
	 *            class for collection element value
	 * @param initialCapacity
	 *            initial size for map
	 */
	public static <K, V> MultiSetValueMap<K, V> createMultiSetValueMap(
			int initialCapacity) {
		return new MultiSetValueMap<K, V>(initialCapacity);
	}

	/**
	 * Create a multi value map that uses sets with the given initial capacity
	 * 
	 * @param <K>
	 *            class for key
	 * @param <V>
	 *            class for collection element value
	 */
	public static <K, V> MultiSetValueMap<K, V> createMultiSetValueMap() {
		return new MultiSetValueMap<K, V>();
	}

	/**
	 * Create a multi value map with the given initial capacity that uses sorted
	 * sets
	 * 
	 * @param <K>
	 *            class for key
	 * @param <V>
	 *            class for collection element value
	 * @param initialCapacity
	 *            initial size for map
	 */
	public static <K, V> MultiSortedSetValueMap<K, V> createMultiSortedSetValueMap(
			int initialCapacity) {
		return new MultiSortedSetValueMap<K, V>(initialCapacity);
	}

	/**
	 * Create a multi value map that uses sorted sets
	 * 
	 * @param <K>
	 *            class for key
	 * @param <V>
	 *            class for collection element value
	 */
	public static <K, V> MultiSortedSetValueMap<K, V> createMultiSortedSetValueMap() {
		return new MultiSortedSetValueMap<K, V>();
	}

	/**
	 * Create a multi value map with the given initial capacity that uses lists
	 * 
	 * @param <K>
	 *            class for key
	 * @param <V>
	 *            class for collection element value
	 * @param initialCapacity
	 *            initial size for map
	 */
	public static <K, V> MultiListValueMap<K, V> createMultiListValueMap(
			int initialCapacity) {
		return new MultiListValueMap<K, V>(initialCapacity);
	}

	/**
	 * Create a multi value map that uses lists
	 * 
	 * @param <K>
	 *            class for key
	 * @param <V>
	 *            class for collection element value
	 */
	public static <K, V> MultiListValueMap<K, V> createMultiListValueMap() {
		return new MultiListValueMap<K, V>();
	}

	/**
	 * Method which will return the first element from the given collection & if
	 * one does not exist will return the given default value. Very useful if
	 * used in conjunction with {@link SqlServiceTemplate}'s set of List based
	 * methods.
	 * 
	 * @param collection
	 *            Collection to be tested. Can be null
	 * @param defaultValue
	 *            Returned if the list was null or empty
	 */
	public static <T> T getFirstElement(Collection<T> collection, T defaultValue) {
		if (collection != null && !collection.isEmpty()) {
			return collection.iterator().next();
		}
		return defaultValue;
	}

	/**
	 * Method which will return the "last" element in the given collection or a
	 * null value if not found.
	 * 
	 * @param <T>
	 *            generic collection type
	 * @param collection
	 *            collection to be checked
	 * @param defaultValue
	 *            default value if list is empty
	 * @return last element or default value
	 */
	public static <T> T getLastElement(Collection<T> collection, T defaultValue) {
		T elem = defaultValue;
		if (collection != null && !collection.isEmpty()) {
			if (List.class.isAssignableFrom(collection.getClass())) {
				elem = ((List<T>) collection).get(collection.size() - 1);
			} else {
				for (T item : collection) {
					elem = item;
				}
			}
		}
		return elem;
	}
	
	/**
	 * Generic method for sorting nodes by dependency (leaves first)
	 * @param inList list to sort
	 * @param cmp node-specific implementation of {@link TopologicalComparator}
	 * @return sorted list (leaves first)
	 */
	public static <T> List<T> topoSort(Collection<T> inList,
			TopologicalComparator<T> cmp) {
		List<T> sList = createArrayList();
		Set<T> visitList = createHashSet();
		for (T t : inList) {
			if (cmp.countEdges(t) == 0) {
				topoVisit(inList, sList, visitList, t, cmp);
			}
		}
		return sList;
	}

	private static <T> void topoVisit(Collection<T> inList,List<T> sList, Set<T> visitList, T t,
			TopologicalComparator<T> cmp) {
		if (!visitList.contains(t)) {
			visitList.add(t);
			for (T m : inList) {
				if (cmp.hasEdge(m,t)) {
					topoVisit(inList, sList, visitList, m, cmp);
				}
			}
			sList.add(t);
		}
	}

}
