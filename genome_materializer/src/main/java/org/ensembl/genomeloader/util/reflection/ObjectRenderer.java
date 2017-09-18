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
 * File: NestedObjectToStringHelper.java
 * Created by: dstaines
 * Created on: Apr 25, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.util.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Utility class which uses reflection to render a nested object as a string,
 * with indentation to allow the structure to be clearly seen
 *
 * @author dstaines
 *
 */
public class ObjectRenderer {

	/**
	 * Utility exception to be raised when an ObjectRenderer tries to indirectly
	 * use another ObjectRenderer method
	 *
	 * @author dstaines
	 *
	 */
	private static class ObjectRendererRecursionException extends
			RuntimeException {

		private static final long serialVersionUID = -4177574429992748617L;
	}

	private static ThreadLocal<Boolean> recursive = new ThreadLocal<Boolean>();

	private static boolean getRecursive() {
		if (recursive.get() == null) {
			setRecursive(false);
		}
		return recursive.get();
	}

	private static void setRecursive(boolean val) {
		recursive.set(val);
	}

	private static final String PREFIX = " ";

	/**
	 *
	 * @param obj
	 * @return
	 */
	public static String objectToString(Object obj) {

		if (getRecursive()) {
			throw new ObjectRendererRecursionException();
		}

		StringBuilder s = new StringBuilder();
		s.append(renderObject(obj, 0, new HashSet()));
		return s.toString();
	}

	private static void addPrefix(StringBuilder s, int level) {
		for (int i = 0; i < level; i++) {
			s.append(PREFIX);
		}
	}

	private static String renderObject(Object obj, int level, Set objs) {
		try {
			StringBuilder s = new StringBuilder();
			if (obj == null) {
				s.append(obj);
			} else if (!objs.contains(obj)) {
				Class<?> clazz = obj.getClass();
				Package pack = clazz.getPackage();
				boolean basic = (pack != null && pack.equals(Package
						.getPackage("java.lang")));
				boolean hasToString = false;
				if (!basic) {
					try {
						Method toMethod = clazz.getMethod("toString",
								new Class[] {});
						if (toMethod.getDeclaringClass() != Object.class) {
							// Only use toString if recursive is not set.
							// Recursive is set in the thread when toString is
							// invoked from within this method
							// to prevent a stack overflow from this method
							// calling
							// toString which will then call ObjectRender
							if (!getRecursive()) {
								// set a thread local variable to indicate that
								// we
								// shouldn't be recursing here
								setRecursive(true);
								try {
									s.append(obj.toString());
									hasToString = true;
								} catch (ObjectRendererRecursionException e) {
									hasToString = false;
								}
								setRecursive(false);
							}
						}
					} catch (NoSuchMethodException e) {
						hasToString = false;
					}
				}
				if (basic) {
					s.append(obj.toString());
				} else if (!hasToString) {
					objs.add(obj);
					if (Map.class.isAssignableFrom(obj.getClass())) {
						s.append(renderMap(obj, level, objs));
					} else if (Iterable.class.isAssignableFrom(clazz)) {
						s.append(renderCollection(obj, level, objs));
					} else if (Object[].class.isAssignableFrom(clazz)) {
						s.append(renderArray(obj, level, objs));
					} else {
						s.append(clazz.getSimpleName());
						s.append(":[\n");
						level++;
						s.append(renderFields(obj, level, clazz, objs));
						level--;
						addPrefix(s, level);
						s.append(']');
					}
				}
			} else {
				s.append('[' + obj.getClass().getSimpleName() + ']');
			}
			return s.toString();
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param s
	 * @param obj
	 * @param level
	 * @param clazz
	 * @throws IllegalAccessException
	 */
	private static String renderFields(Object obj, int level, Class<?> clazz,
			Set objs) throws IllegalAccessException {
		StringBuilder s = new StringBuilder();
		List<Field> fields = new ArrayList<Field>();
		while (true) {
			for (Field field : clazz.getDeclaredFields()) {
				field.setAccessible(true);
				if(!Modifier.isStatic(field.getModifiers())) {
					fields.add(field);
				}
			}
			clazz = clazz.getSuperclass();
			if (clazz == null) {
				break;
			}
		}
		for (Field field : fields) {
			Object fobj = field.get(obj);
			if (fobj != null) {
				addPrefix(s, level);
				s.append(field.getName());
				s.append(':');
				s.append(renderObject(fobj, level + 1, objs));
				s.append('\n');
			}
		}
		return s.toString();
	}

	/**
	 * @param s
	 * @param obj
	 * @param level
	 */
	private static String renderArray(Object obj, int level, Set objs) {
		StringBuilder s = new StringBuilder();
		s.append("{\n");
		for (Object obj2 : (Object[]) (obj)) {
			addPrefix(s, level + 1);
			s.append(renderObject(obj2, level + 1, objs));
			s.append('\n');
		}
		addPrefix(s, level);
		s.append("}");
		return s.toString();
	}

	/**
	 * @param s
	 * @param obj
	 * @param level
	 */
	private static String renderCollection(Object obj, int level, Set objs) {
		StringBuilder s = new StringBuilder();
		s.append("{\n");
		for (Object obj2 : (Iterable) (obj)) {
			if (obj2 != null) {
				addPrefix(s, level + 1);
				s.append(renderObject(obj2, level + 1, objs));
				s.append('\n');
			}
		}
		addPrefix(s, level);
		s.append("}");
		return s.toString();
	}

	/**
	 * @param s
	 * @param obj
	 * @param level
	 */
	private static String renderMap(Object obj, int level, Set objs) {
		StringBuilder s = new StringBuilder();
		s.append("{\n");
		for (Object o : ((Map) obj).entrySet()) {
			Entry e = (Entry) o;
			addPrefix(s, level + 1);
			s.append(e.getKey());
			s.append(':');
			s.append(renderObject(e.getValue(), level + 1, objs));
			s.append('\n');
		}
		addPrefix(s, level);
		s.append("}");
		return s.toString();
	}

}
