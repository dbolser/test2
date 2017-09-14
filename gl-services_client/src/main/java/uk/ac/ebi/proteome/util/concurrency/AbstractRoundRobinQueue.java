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
 * AbstractRoundRobinQueue
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package uk.ac.ebi.proteome.util.concurrency;

import java.util.AbstractQueue;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import uk.ac.ebi.proteome.util.collections.CollectionUtils;


/**
 * Queue that inserts new elements in an interleaved manner. Not threadsafe.
 * 
 * @author dstaines
 */
public abstract class AbstractRoundRobinQueue<T> extends AbstractQueue<T> {

	private Log log = null;
	
	private final List<T> queueList;

	public AbstractRoundRobinQueue() {
		queueList = new LinkedList<T>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Queue#offer(java.lang.Object)
	 */
	public synchronized boolean offer(T e) {
		String newKey = itemToKey(e);
		log.debug("Offering "+newKey+" to queue of size "+queueList.size());

		// find the position of the last key of this kind
		int pos = 0;
		for (pos = queueList.size(); pos >= 0; pos--) {
			if (newKey.equals(itemToKey(queueList.get(pos - 1)))) {
				break;
			}
		}
		log.debug("Next position is "+pos);

		// work forward until I find the same thing twice
		Set<String> keys = new HashSet<String>();
		for (; pos < queueList.size(); pos++) {
			String key = itemToKey(queueList.get(pos));
			if (keys.contains(key)) {
				break;
			}
			keys.add(key);
		}
		log.debug("Adding at "+pos);
		queueList.add(pos, e);

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Queue#poll()
	 */
	public synchronized T poll() {
		if (queueList.isEmpty()) {
			return null;
		} else {
			return queueList.remove(0);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Queue#peek()
	 */
	public synchronized T peek() {
		return CollectionUtils.getFirstElement(queueList, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractCollection#iterator()
	 */
	@Override
	public synchronized Iterator<T> iterator() {
		return queueList.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public synchronized int size() {
		return queueList.size();
	}

	protected abstract String itemToKey(T e);

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public synchronized String toString() {
		return "AbstractRoundRobinQueue [queueList=" + queueList + "]";
	}

}
