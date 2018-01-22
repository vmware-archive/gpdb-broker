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
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

@Component
public class GreenplumDatabase {

    private static final Logger logger = LoggerFactory.getLogger(GreenplumDatabase.class);

    private JdbcTemplate SqlTemplate;
    
    private int databasePort;
    private String databaseHost;


    @Autowired
    public GreenplumDatabase(DataSource dataSource) throws SQLException {
//    	GreenplumDatabase.conn = dataSource.getConnection();
		this.SqlTemplate = new JdbcTemplate(dataSource);
        try {
            String jdbcUrl = dataSource.getConnection().getMetaData().getURL();
            // Remove "jdbc:" prefix from the connection JDBC URL to create an URI out of it.
//            String cleanJdbcUrl = jdbcUrl.replace("jdbc:pivotal", "");  // Greenplum JDBC
            String cleanJdbcUrl = jdbcUrl.replace("jdbc:", "");
            logger.debug("cleanJdbcUrl: " + cleanJdbcUrl);
            int index = cleanJdbcUrl.indexOf(";");
            if (index != -1)
            		cleanJdbcUrl = cleanJdbcUrl.substring(0, index);
            URI uri = new URI(cleanJdbcUrl);
            this.databaseHost = uri.getHost();
           	logger.debug("uri: " + uri + ", (Host:Port) = (" + uri.getHost() + ":" + uri.getPort() + ")");
            this.databasePort = uri.getPort() == -1 ? 5432 : uri.getPort();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to get DatabaseMetadata from Connection", e);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to parse JDBC URI for Database Connection", e);
        }
    }

    public  void executeUpdate(String query) throws SQLException {
    	
        logger.debug("In executeUpdate: query '" + query + "'");
		try {
			SqlTemplate.update(query);
		}
		catch(EmptyResultDataAccessException e) {
			logger.info("No rows affected");
		}
    		catch (DataAccessException e) {
    			logger.error("Error while executing SQL prepared UPDATE query '" + query + "' : ", e);
    		}
    }


    public  Map<String, String> executeSelect(String query) throws SQLException {
    		  SqlRowSet rs = SqlTemplate.queryForRowSet(query);
    		  return getResultMapFromRowSet(rs);
    }

    public  void executePreparedUpdate(String query, Object[] params, int[] types) throws SQLException {
		
        try {
        		SqlTemplate.update(query, params, types);
        } catch (DataAccessException e) {
        		logger.error("Error while executing SQL prepared UPDATE query '" + query + "'", e);
        }

    }

/*
    public  Map<String, String> executePreparedSelect(String query, Map<Integer, String> parameterMap) throws SQLException {
        if(parameterMap == null) {
            throw new IllegalStateException("parameterMap cannot be null");
        }

        PreparedStatement preparedStatement = conn.prepareStatement(query);
		  
        for(Map.Entry<Integer, String> parameter : parameterMap.entrySet()) {
            preparedStatement.setString(parameter.getKey(), parameter.getValue());
        }

        try {
            ResultSet result = preparedStatement.executeQuery();
            return getResultMapFromResultSet(result);
        } catch (SQLException e) {
            logger.error("Error while executing SQL prepared SELECT query '" + query + "'", e);
            return null;
        } finally {
            preparedStatement.close();
        }
    }
 */

    public  String getDatabaseHost() {
    		// can we dig this back out of the template ?
        return this.databaseHost;
    }

    public  int getDatabasePort() {
		// can we dig this back out of the template ?
        return this.databasePort;
    }
    
    private  Map<String, String> getResultMapFromRowSet(SqlRowSet result) throws SQLException {
        SqlRowSetMetaData resultMetaData = result.getMetaData();
        int columns = resultMetaData.getColumnCount();

        Map<String, String> resultMap = new HashMap<String, String>(columns);

        if(result.next()) {
            for(int i = 1; i <= columns; i++) {
                resultMap.put(resultMetaData.getColumnName(i), result.getString(i));
            }
        }

        return resultMap;
    }
}
