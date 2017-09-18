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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.proteome.services.sql.ROResultSet;

/**
 * A convenience class which contains a number of methods which are useful when
 * dealing with databases and database objects. All methods are null safe
 *
 * @author ayates
 * @author $Author$
 * @version $Version$
 */
public class DbUtils {

	private static Log getLog() {
		return LogFactory.getLog(DbUtils.class);
	}

	/**
	 * Closes any object which is derived from a statement and closes it. Errors
	 * get reported at WARN level to this classes' appender
	 */
	public static void closeDbObject(Statement st) {
		if (st != null) {
			try {
				st.close();
			} catch (SQLException e) {
				getLog().warn("Could not close Statement", e);
			}
		}
	}

	/**
	 * Closes any object which is derived from a ResultSet and closes it. Errors
	 * get reported at WARN level to this classes' appender
	 */
	public static void closeDbObject(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				getLog().warn("Could not close ResultSet", e);
			}
		}
	}

	/**
	 * Closes any object which is derived from a ROResultSet and closes it.
	 * Errors get reported at WARN level to this classes' appender
	 */
	public static void closeDbObject(ROResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				getLog().warn("Could not close ROResultSet", e);
			}
		}
	}

	/**
	 * Wrapper around the other two closeDbObject methods but this ensures that
	 * result sets are closed before statements. This is very important when
	 * dealing with some JDBC drivers (like Oracle) which are very poor at
	 * clearing up after themselves.
	 *
	 * <p>
	 * e.g. In MySQL JDBC drivers closing a higher object will close all objects
	 * that were created from it. In Oracle since it operates using handles to
	 * the database, these are cleared up once a database connection has been
	 * destroyed. If it is not destroyed i.e. in a pooling system then they will
	 * always remain open
	 */
	public static void closeDbObject(Statement st, ResultSet rs) {
		closeDbObject(rs);
		closeDbObject(st);
	}

	/**
	 * This is used to close down a normal JDBC implementing database
	 * {@link Connection}. As with the other methods this method is null safe.
	 */
	public static void closeDbObject(Connection conn) {
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			getLog().warn("Could not close given connection", e);
		}
	}

	/**
	 * Issues a rollback on the given connection and logs any exceptions to WARN
	 * level from this class' log. This method will not rollback a connection
	 * which will auto-commit as this causes an inconsistent state issue
	 */
	public static void rollback(Connection conn) {
		if (conn != null) {
			try {
				if (!conn.getAutoCommit()) {
					conn.rollback();
				}
			} catch (SQLException e) {
				getLog().warn("Could not rollback given connection", e);
			}
		}
	}

	/**
	 * Turns off a connection's autocommit ability and logs any errors to WARN
	 */
	public static void turnOffAutoCommit(Connection conn) {
		if (conn != null) {
			try {
				if (conn.getAutoCommit()) {
					conn.setAutoCommit(false);
				} else {
					getLog().debug(
							"Not turning autocommit off, as its already off");
				}
			} catch (SQLException e) {
				getLog().warn("Could not turn the connection's autocommit off",
						e);
			}
		}
	}

	/**
	 * Turns on a connection's autocommit ability and logs any errors to WARN
	 */
	public static void turnOnAutoCommit(Connection conn) {
		if (conn != null) {
			try {
				if (!conn.getAutoCommit()) {
					conn.setAutoCommit(true);
				} else {
					getLog().debug(
							"Not turning autocommit on, as its already on");
				}
			} catch (SQLException e) {
				getLog().warn("Could not turn the connection's autocommit on",
						e);
			}
		}
	}
}
