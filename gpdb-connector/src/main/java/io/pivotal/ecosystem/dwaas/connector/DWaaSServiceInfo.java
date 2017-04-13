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

import org.springframework.cloud.service.ServiceInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

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


}
