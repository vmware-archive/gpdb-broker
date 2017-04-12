/*
 * Copyright (C) 2016-Present Pivotal Software, Inc. All rights reserved.
 * <p>
 * This program and the accompanying materials are made available under
 * the terms of the under the Apache License, Version 2.0 (the "License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pivotal.ecosystem.dwaas.connector;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.service.ServiceInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

@Data
@AllArgsConstructor
public class DWaaSServiceInfo implements ServiceInfo {

    //keys used to store metadata for service bindings
    public static final String USERNAME = "uid";
    public static final String PASSWORD = "pw";
    public static final String URI = "uri";
    public static final String DATABASE = "db";
    public static final String HOSTNAME = "hostname";
    public static final String PORT = "port";
	
	/*
    //keys used to pull connection information out of env
    public static final String HOST_KEY = "SQL_HOST";
    public static final String PORT_KEY = "SQL_PORT";
    public static final String USER_KEY = "SQLSERVER_USERNAME";
    public static final String PW_KEY = "SQLSERVER_PASSWORD";
    public static final String JDBC_DRIVER_CLASS_NAME = "com.ddtek.jdbc.greenplum.GreenplumDriver";
	*/

	private String username;
    private String id;
    private String password;
    private String uri;
    
    private String driverClassName;

	public DWaaSServiceInfo() {
	}

	public DWaaSServiceInfo(String username, String id, String password, String uri) {
		super();
		this.username = username;
		this.id = id;
		this.password = password;
		this.uri = uri;
	}
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}
    
}
