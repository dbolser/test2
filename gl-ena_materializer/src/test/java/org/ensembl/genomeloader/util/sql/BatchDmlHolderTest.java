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

package org.ensembl.genomeloader.util.sql;

import org.ensembl.genomeloader.util.sql.BatchDmlHolder;

import junit.framework.TestCase;

/**
 * Used for testing the batch dml holder methods
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class BatchDmlHolderTest extends TestCase {

	public void testIteration() {
		BatchDmlHolder holder = getHolder();

		int expectedNumberOfBatches = 3;
		int actualNumberOfBatches = 0;

		int currentBatch = 0;
		for(Object[][] batch: holder) {
			switch (currentBatch) {
				case 2:
					assertEquals("Batch size not as expected", 1, batch.length);
					break;
				default:
					assertEquals("Batch size not as expected", 2, batch.length);
					break;
			}

			actualNumberOfBatches++;
			currentBatch++;
		}

		assertEquals("Number of batches generated not as expected",
				expectedNumberOfBatches, actualNumberOfBatches);
	}

	public void testArrayHandout() {
		BatchDmlHolder holder = getHolder();
		int expected = 5;
		int actual = holder.getAllParams().length;
		assertEquals("Array param length not as expected", expected, actual);
	}

	private BatchDmlHolder getHolder() {
		BatchDmlHolder holder = new BatchDmlHolder(2);
		holder.addParams(new Object[]{1});
		holder.addParams(new Object[]{1});
		holder.addParams(new Object[]{1});
		holder.addParams(new Object[]{1});
		holder.addParams(new Object[]{1});
		return holder;
	}
}
