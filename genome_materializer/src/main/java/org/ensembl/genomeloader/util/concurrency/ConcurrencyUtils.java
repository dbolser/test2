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
 * File: ConcurrencyUtils.java
 * Created by: dstaines
 * Created on: Jul 9, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.util.concurrency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Set of utility methods associated with concurrency and multi-threading
 *
 * @author dstaines
 *
 */
public class ConcurrencyUtils {

	/**
	 * Method to execute one of more {@link Callable} instances with a set
	 * timeout. Suitable for use with potentially blocking service calls.
	 *
	 * @param <T>
	 *            class of object associated with {@link Callable} instace
	 * @param timeout
	 *            time to wait in milliseconds before abandoning a thread
	 * @param calls
	 *            list of callable objects to execute with timeout
	 * @returns list of output objects from callable (these may include
	 *          exceptions)
	 * @throws Exception
	 *             if one or more callable tasks could not be executed
	 * @throws CancellationException
	 *             if one or more callable tasks timed out
	 */
	public static <T extends Object> List<T> executeWithTimeout(long timeout,
			Callable<T>... calls) throws CancellationException, Exception {
		List<Callable<T>> callList = Arrays.asList(calls);
		List<T> outputList = new ArrayList<T>(callList.size());

		int size = callList.size();

		ExecutorService executor = new ThreadPoolExecutor(
				size, size, 1000, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>(size), getDaemonThreadFactory());

		try {
			List<Future<T>> futures = executor.invokeAll(callList, timeout,
					TimeUnit.MILLISECONDS);
			for (Future<T> future : futures) {
				try {
					outputList.add(future.get());
				} catch (ExecutionException e) {
					System.out.println(e.getCause().getClass());
					System.out.println(Exception.class.isAssignableFrom(e
							.getCause().getClass()));
					if (e.getCause() != null
							&& Exception.class.isAssignableFrom(e.getCause()
									.getClass())) {
						throw (Exception) e.getCause();
					} else {
						throw e;
					}
				}
			}
		} finally {
			executor.shutdownNow();
		}
		return outputList;
	}

	/**
	 * See {@link DaemonThreadFactory} for more details.
	 */
	public static ThreadFactory getDaemonThreadFactory() {
		return new DaemonThreadFactory();
	}

	/**
	 * Returns a thread factory which alters the returned thread by
	 * {@link Executors#defaultThreadFactory()} so that it is a
	 * daemon and has the lowest priority
	 *
	 * @author ayates
	 * @author $Author$
	 * @version $Revision$
	 */
	protected static class DaemonThreadFactory implements ThreadFactory {

		ThreadFactory defaultFactory = null;

		public DaemonThreadFactory() {
			defaultFactory = Executors.defaultThreadFactory();
		}

		public Thread newThread(Runnable r) {
			Thread thread = defaultFactory.newThread(r);
			thread.setDaemon(true);
			thread.setPriority(Thread.MIN_PRIORITY);
			return thread;
		}
	}
}
