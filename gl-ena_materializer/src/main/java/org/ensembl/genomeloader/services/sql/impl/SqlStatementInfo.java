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
 * File: SqlStatementInfo.java
 * Created by: dstaines
 * Created on: Dec 6, 2006
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.services.sql.impl;

/**
 * @author dstaines
 *
 */
public class SqlStatementInfo {
	private boolean active;
	private String sql;
	private int cursors;
	public boolean isActive() {
		return this.active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public int getCursors() {
		return this.cursors;
	}
	public void setCursors(int cursors) {
		this.cursors = cursors;
	}
	public String getSql() {
		return this.sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	/**
	 * @param active
	 * @param sql
	 * @param cursors
	 */
	public SqlStatementInfo(String sql, int cursors, boolean active) {
		super();
		this.active = active;
		this.sql = sql;
		this.cursors = cursors;
	}
	public String toString() {
		return sql+"\t"+cursors+"\t"+String.valueOf(active)+"\n";
	}
	
}
