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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.ArrayUtils;

import uk.ac.ebi.proteome.services.sql.SqlService;

/**
 * Provides convinience methods for registering and emmiting data structures
 * to work with 
 * {@link SqlService#executeTransactionalDml(String, String[], Object[][])}.
 * It uses a {@link LinkedHashMap} to maintian insert order and therefore
 * the priority of insert staements
 * 
 * @author ayates
 * @author $Author$
 * @version $Version$
 */
public class TransactionalDmlHolder {
  
  Map<String, String> dmlHolder = new LinkedHashMap<String, String>();
  Map<String, Object[]> paramHolder = new LinkedHashMap<String, Object[]>();
  
  protected Map<String, String> getDmlHolder() {
    return dmlHolder;
  }
  
  protected Map<String, Object[]> getParamHolder() {
    return paramHolder;
  }

  /**
   * Allows you to add statements to be later retrieved by the get 
   * methods
   * 
   * @param sql The sql to run - should be dml
   * @param params The parameters to run with. If null then this is translated
   * to {@link ArrayUtils#EMPTY_OBJECT_ARRAY}
   * @return The value under which this statement and params are held
   */
  public String addStatement(String sql, Object[] params) {
    
    Object[] newParams = params;
    
    if(newParams == null) {
      newParams = ArrayUtils.EMPTY_OBJECT_ARRAY;
    }
    
    String id = getUniqueId();
    dmlHolder.put(id, sql);
    paramHolder.put(id, newParams);
    
    return id;
  }
  
  private String getUniqueId() {
    return UUID.randomUUID().toString();
  }
  
  public void reset(){
	  this.dmlHolder.clear();
	  this.paramHolder.clear();
  }
  
  /**
   * Returns the statements as in the order of insert
   */
  public String[] getStatementsArray() {
    return dmlHolder.values().toArray(new String[0]);
  }
  
  /**
   * Returns the arguments submitted with the parameters in order of insert
   */
  public Object[][] getParametersArray() {
    return paramHolder.values().toArray(new Object[0][0]);
  }
}
