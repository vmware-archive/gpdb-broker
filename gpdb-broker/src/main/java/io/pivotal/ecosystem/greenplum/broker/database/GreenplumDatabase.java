/*
 * Copyright 2014 the original author or authors.
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

package io.pivotal.ecosystem.greenplum.broker.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

@Component
public class GreenplumDatabase {

	private static final Logger logger = LoggerFactory.getLogger(GreenplumDatabase.class);

	private JdbcTemplate sqlTemplate;
	private int databasePort;
	private String databaseHost;

	@Autowired
	private Environment env;

	@Autowired
	public GreenplumDatabase(DataSource dataSource) throws SQLException {

		this.sqlTemplate = new JdbcTemplate(dataSource);
		try {
			String jdbcUrl = dataSource.getConnection().getMetaData().getURL();
			String cleanJdbcUrl = jdbcUrl.replace("jdbc:", "");
			logger.info("cleanJdbcUrl: " + cleanJdbcUrl);
			int index = cleanJdbcUrl.indexOf(";");
			if (index != -1) {
				cleanJdbcUrl = cleanJdbcUrl.substring(0, index);
			}
			URI uri = new URI(cleanJdbcUrl);
			this.databaseHost = uri.getHost();
			logger.info("uri: " + uri + ", (Host:Port) = (" + uri.getHost() + ":" + uri.getPort() + ")");
			this.databasePort = uri.getPort() == -1 ? 5432 : uri.getPort();
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to get DatabaseMetadata from Connection", e);
		} catch (URISyntaxException e) {
			throw new IllegalStateException("Unable to parse JDBC URI for Database Connection", e);
		}
	}

	/*
	 * This method is for when we run a "DROP OWNED BY <role>", which has to be run
	 * within the service instance DB associated with the given role.
	 */
	public void executeUpdateForDb(String db, String query) throws SQLException {
		String jdbcUrl = sqlTemplate.getDataSource().getConnection().getMetaData().getURL();
		// FIXME: it would be really nice if there was a better way to get these values.
		String dbUser = env.getProperty("spring.datasource.username");
		String dbPass = env.getProperty("spring.datasource.password");
		String dbDriverClassName = env.getProperty("spring.datasource.driver-class-name");
		assert (dbUser != null && dbPass != null);
		assert (dbDriverClassName != null);
		jdbcUrl += "&user=" + dbUser;
		jdbcUrl += "&password=" + dbPass;
		jdbcUrl += "&prepareThreshold=0";
		logger.info("Original JDBC URL:  " + jdbcUrl);
		jdbcUrl = jdbcUrl.replaceAll("^(jdbc:.+?//(?:\\d+\\.){3}\\d+:\\d+)/[^?]+\\?(.+)$", "$1/" + db + "?$2");
		logger.info("Rewritten JDBC URL: " + jdbcUrl);

		/*
		 * For the connection to the instance DB, we don't want connection pooling, which is why we
		 * use a SimpleDriverDataSource here.
		 */
		DataSource nonPoolingDataSource;
		try {
			Class<?> clazz = Class.forName(dbDriverClassName);
			Driver driver = (Driver) clazz.newInstance();
			nonPoolingDataSource = new SimpleDriverDataSource(driver, jdbcUrl);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		Connection con;
		try {
			con = nonPoolingDataSource.getConnection();
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
		con.setAutoCommit(false);
		try {
			Statement st = con.createStatement();
			st.execute(query);
			st.close();
			con.commit();
		} catch (SQLException e) {
			con.rollback();
			throw new SQLException(e);
		} finally {
			con.close();
		}
	}

	public void executeUpdate(String query) throws SQLException {

		logger.info("In executeUpdate: query '" + query + "'");
		try {
			sqlTemplate.update(query);
		} catch (EmptyResultDataAccessException e) {
			logger.info("No rows affected");
		} catch (DataAccessException e) {
			logger.error("Error while executing SQL prepared UPDATE query '" + query + "' : ", e);
		}
	}
	
	/**
	 * 
	 * @param query
	 * @return a list of string containing the value of the single column projected in the query
	 */
	public List<String> getListFromSelect (String query) {
		List<String> rv = sqlTemplate.queryForList(query, String.class);
		return rv;
	}

	public Integer getIntFromSelect (String query) {
		Integer rv = sqlTemplate.queryForObject(query, Integer.class);
		return rv;
	}

	public Map<String, String> executeSelect(String query) throws SQLException {
		logger.info("In executeSelect: (" + query + ")");
		SqlRowSet rs = sqlTemplate.queryForRowSet(query);
		return getResultMapFromRowSet(rs);
	}

	public void executePreparedUpdate(String query, Object[] params, int[] types) throws SQLException {
		logger.info("In executePrepareUpdate: (" + query + ")");
		try {
			sqlTemplate.update(query, params, types);
		} catch (DataAccessException e) {
			logger.error("Error while executing SQL prepared UPDATE query (" + query + ")", e);
		}
	}

	public String getDatabaseHost() {
		// can we dig this back out of the template ?
		return this.databaseHost;
	}

	public int getDatabasePort() {
		// can we dig this back out of the template ?
		return this.databasePort;
	}

	private Map<String, String> getResultMapFromRowSet(SqlRowSet result) throws SQLException {
		logger.info("in getResultMapFromRowSet");

		SqlRowSetMetaData resultMetaData = result.getMetaData();
		int columns = resultMetaData.getColumnCount();

		Map<String, String> resultMap = new HashMap<String, String>(columns);

		if (result.next()) {
			for (int i = 1; i <= columns; i++) {
				resultMap.put(resultMetaData.getColumnName(i), result.getString(i));
			}
		}
		return resultMap;
	}
}
