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

package uk.ac.ebi.proteome.util.sql;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import uk.ac.ebi.proteome.services.ServiceUncheckedException;

/**
 * Used to hold the parameters used during batch statements. This class
 * is used at both sides of the implementation providing batching at the client
 * and server level.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class BatchDmlHolder implements Iterable<Object[][]>{

	private static final Object[][] TWO_D_ARRAY = new Object[0][0];
	private static final int DEFAULT_BATCH_SIZE = 1000;
	private final int batchSize;
	private boolean allowAddition = true;
	private List<Object[]> allParams = new ArrayList<Object[]>();

	public BatchDmlHolder() {
		this(DEFAULT_BATCH_SIZE);
	}

	public BatchDmlHolder(int batchSize) {
		this.batchSize = batchSize;
	}

	/**
	 * Used to add a single line of parameters
	 */
	public void addParams(Object[] params) {
		assertAdditionAllowed();
		allParams.add(params);
	}

	/**
	 * Used to add a single parameter to a batch command
	 */
	public void addParam(Object param) {
		assertAdditionAllowed();
		allParams.add(new Object[]{param});
	}

	/**
	 * Used to add a multiple parameters at the same time
	 */
	public void addParams(Object[][] params) {
		assertAdditionAllowed();
		for(Object[] currentParamSet: params) {
			addParams(currentParamSet);
		}
	}

	/**
	 * Returns the currently registered set of parameters in this object
	 */
	public Object[][] getAllParams() {
		return allParams.toArray(TWO_D_ARRAY);
	}

	/**
	 * Returns the current size of the intended batch
	 */
	public int getBatchSize() {
		return batchSize;
	}

	private void assertAdditionAllowed() {
		if(!allowAddition) {
			throw new ServiceUncheckedException("Cannot add to a batch holder " +
					"which is currently not allowing any more parameters");
		}
	}

	public Iterator<Object[][]> iterator() {
		return new BatchIterator();
	}

	/**
	 * Used to provide iteration over the current instance of
	 * {@link BatchDmlHolder}. This iterator will throw a
	 * {@link ConcurrentModificationException} if the holder is added to once
	 * running and does not support the remove operation.
	 *
	 * @author ayates
	 * @author $Author$
	 * @version $Revision$
	 */
	protected class BatchIterator implements Iterator<Object[][]> {

		private final int sizeOfParamsWhenCreated;
		private int currentPosition = 0;

		protected BatchIterator() {
			this.sizeOfParamsWhenCreated = allParams.size();
		}

		/**
		 * Returns true if we are currently less than the inital size of the
		 * backing batch dml holder
		 */
		public boolean hasNext() throws ConcurrentModificationException {
			assertOkayToRun();
			boolean hasNext = (currentPosition < sizeOfParamsWhenCreated);
			return hasNext;
		}

		/**
		 * Returns the next batch according to the batch size as specified when
		 * creating this dml holder
		 */
		public Object[][] next() throws ConcurrentModificationException {
			assertOkayToRun();
			Object[][] paramsArray = null;
			int valuesLeft = sizeOfParamsWhenCreated - currentPosition;
			int nextArraySize = -1;
			if(valuesLeft < batchSize) {
				nextArraySize = valuesLeft;
			}
			else {
				nextArraySize = batchSize;
			}

			paramsArray = new Object[nextArraySize][];

			for(int i=0; i<nextArraySize; i++) {
				paramsArray[i] = allParams.get(currentPosition);
				currentPosition++;
			}

			return paramsArray;
		}

		/**
		 * Unsupported
		 */
		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException("Not supporting remove operation");
		}

		private void assertOkayToRun() {
			if(sizeOfParamsWhenCreated != allParams.size()) {
				throw new ConcurrentModificationException("Batch holder was modified " +
						"whilst this iterator was running.");
			}
		}
	}
}
