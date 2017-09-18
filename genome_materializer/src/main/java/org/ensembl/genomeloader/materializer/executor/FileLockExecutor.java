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
 * FileLockExecutor
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package org.ensembl.genomeloader.materializer.executor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author dstaines
 * 
 */
public class FileLockExecutor implements Executor {

	private final String lockDir;
	private final int slotN;
	private final Log log;

	/**
	 * @param slotN
	 * @param lockDir
	 */
	public FileLockExecutor(String lockDir, int slotN) {
		this.lockDir = lockDir;
		this.slotN = slotN;
		log = LogFactory.getLog(this.getClass());
	}

	public synchronized void execute(Runnable command) {
		boolean success = false;
		while (!success) {
			for (int n = 0; n < slotN; n++) {
				FileChannel channel = null;
				try {
					channel = new RandomAccessFile(new File(lockDir + "/lock"
							+ n), "rw").getChannel();
					FileLock lock = null;
					try {
						log.debug("Acquiring lock " + n);
						lock = channel.tryLock();
					} catch (OverlappingFileLockException e) {
						lock = null;
					}
					if (lock != null) {
						try {
							log.debug("Acquired lock " + n);
							command.run();
							success = true;
							break;
						} finally {
							log.debug("Releasing lock " + n);
							lock.release();
						}
					} else {
						log.debug("Failed to acquire lock " + n);
					}
				} catch (FileNotFoundException e) {
					throw new RuntimeException("Problem locking", e);
				} catch (IOException e) {
					throw new RuntimeException("Problem locking", e);
				} finally {
					try {
						if (channel != null) {
							channel.close();
						}
					} catch (IOException e1) {
						// swallow
					}
				}
			}
		}
	}
}
