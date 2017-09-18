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

package uk.ac.ebi.proteome.util;

/**
 * Used to throw util specific runtime exceptions
 * 
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class UtilUncheckedException extends RuntimeException {

  private static final long serialVersionUID = 876200398127276474L;

  public UtilUncheckedException() {
    super();
  }
  
  public UtilUncheckedException(String msg) {
    super(msg);
  }
  
  public UtilUncheckedException(Throwable t) {
    super(t);
  }
  
  public UtilUncheckedException(String msg, Throwable t) {
    super(msg, t);
  }
}
