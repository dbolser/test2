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
 * File: ActiveAwareGenericKeyedObjectPool.java
 * Created by: dstaines
 * Created on: Dec 11, 2006
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.commons.pool.KeyedPoolableObjectFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;

import uk.ac.ebi.proteome.util.collections.CollectionUtils;
import uk.ac.ebi.proteome.util.reflection.ReflectionUtils;

/**
 * Implementation to keep track of active connections given out by the pool
 *
 * @author dstaines
 */
public class ActiveAwareGenericKeyedObjectPool extends GenericKeyedObjectPool {

	// map is keyed by object, rather than value
	private Map<Object, Object> activeObjectMap = null;

	public Map<Object, Object> getActiveObjects() {
		if (activeObjectMap == null) {
			activeObjectMap = CollectionUtils.createHashMap();
		}
		return activeObjectMap;
	}

	protected Map<Object, Object> getActiveMap() {
		return (Map<Object, Object>) ReflectionUtils.getFieldValue(this,
				GenericKeyedObjectPool.class, "_activeMap");
	}

	protected Map<Object, Object> getPoolMap() {
		return (Map<Object, Object>) ReflectionUtils.getFieldValue(this,
				GenericKeyedObjectPool.class, "_poolMap");
	}

	public Set<Object> getKeys() {
		return getPoolMap().keySet();
	}

	public Collection<Object> getActiveObjects(Object key) {
		Set<Object> activeObjectsForKey = CollectionUtils.createHashSet();
		for (Map.Entry<Object, Object> e : getActiveObjects().entrySet()) {
			if (key == e.getValue()) {
				activeObjectsForKey.add(e.getKey());
			}
		}
		return activeObjectsForKey;
	}

	public Collection<Object> getIdleObjects(Object key) {
		Collection<Object> idle = CollectionUtils.createArrayList();
		Object o = getPoolMap().get(key);
		if (o == null) {
			return Collections.EMPTY_LIST;
		} else {
			for (Object i : (Collection<Object>) o) {
				idle.add(ReflectionUtils.getFieldValue(i, "value"));
			}
		}
		return idle;
	}

	/**
	 *
	 */
	public ActiveAwareGenericKeyedObjectPool() {
	}

	/**
	 * @param arg0
	 */
	public ActiveAwareGenericKeyedObjectPool(KeyedPoolableObjectFactory factory) {
		super(factory);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public ActiveAwareGenericKeyedObjectPool(
			KeyedPoolableObjectFactory factory, Config arg1) {
		super(factory, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public ActiveAwareGenericKeyedObjectPool(
			KeyedPoolableObjectFactory factory, int arg1) {
		super(factory, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public ActiveAwareGenericKeyedObjectPool(
			KeyedPoolableObjectFactory factory, int arg1, byte arg2, long arg3) {
		super(factory, arg1, arg2, arg3);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 */
	public ActiveAwareGenericKeyedObjectPool(
			KeyedPoolableObjectFactory factory, int arg1, byte arg2, long arg3,
			int arg4) {
		super(factory, arg1, arg2, arg3, arg4);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 */
	public ActiveAwareGenericKeyedObjectPool(
			KeyedPoolableObjectFactory factory, int arg1, byte arg2, long arg3,
			boolean arg4, boolean arg5) {
		super(factory, arg1, arg2, arg3, arg4, arg5);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 * @param arg6
	 */
	public ActiveAwareGenericKeyedObjectPool(
			KeyedPoolableObjectFactory factory, int arg1, byte arg2, long arg3,
			int arg4, boolean arg5, boolean arg6) {
		super(factory, arg1, arg2, arg3, arg4, arg5, arg6);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 * @param arg6
	 * @param arg7
	 * @param arg8
	 * @param arg9
	 * @param arg10
	 */
	public ActiveAwareGenericKeyedObjectPool(
			KeyedPoolableObjectFactory factory, int arg1, byte arg2, long arg3,
			int arg4, boolean arg5, boolean arg6, long arg7, int arg8,
			long arg9, boolean arg10) {
		super(factory, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9,
				arg10);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 * @param arg6
	 * @param arg7
	 * @param arg8
	 * @param arg9
	 * @param arg10
	 * @param arg11
	 */
	public ActiveAwareGenericKeyedObjectPool(
			KeyedPoolableObjectFactory factory, int arg1, byte arg2, long arg3,
			int arg4, int arg5, boolean arg6, boolean arg7, long arg8,
			int arg9, long arg10, boolean arg11) {
		super(factory, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9,
				arg10, arg11);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @param arg4
	 * @param arg5
	 * @param arg6
	 * @param arg7
	 * @param arg8
	 * @param arg9
	 * @param arg10
	 * @param arg11
	 * @param arg12
	 */
	public ActiveAwareGenericKeyedObjectPool(
			KeyedPoolableObjectFactory factory, int arg1, byte arg2, long arg3,
			int arg4, int arg5, int arg6, boolean arg7, boolean arg8,
			long arg9, int arg10, long arg11, boolean arg12) {
		super(factory, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9,
				arg10, arg11, arg12);
	}

	@Override
	public Object borrowObject(Object key) throws Exception {
		Object obj = super.borrowObject(key);
		getActiveObjects().put(obj, key);
		return obj;
	}

	@Override
	public void invalidateObject(Object key, Object obj) throws Exception {
		super.invalidateObject(key, obj);
		if (getActiveObjects().containsKey(obj)) {
			getActiveObjects().remove(obj);
		}
	}

	@Override
	public void returnObject(Object key, Object obj) throws Exception {
		super.returnObject(key, obj);
		getActiveObjects().remove(obj);
	}

}
