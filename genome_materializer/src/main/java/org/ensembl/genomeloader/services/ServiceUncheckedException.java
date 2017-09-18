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

package org.ensembl.genomeloader.services;

/**
 * Used to represent very severe errors which are not meant to be caught and
 * are intended to force program termination at the first available 
 * oppertunity. This confirms to a 'fail fast' policy
 * 
 * @author ayates
 * @author $Author$
 * @version $Version$
 */
public class ServiceUncheckedException extends RuntimeException {

  private static final long serialVersionUID = -3753672584276350970L;

  public ServiceUncheckedException() {
    super();
  }

  public ServiceUncheckedException(String message) {
    super(message);
  }

  public ServiceUncheckedException(Throwable cause) {
    super(cause);
  }

  public ServiceUncheckedException(String message, Throwable cause) {
    super(message, cause);
  }

}
