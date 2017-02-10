#ms-sql-server-broker
This is a Cloud Foundry service broker for [Microsoft SqlServer](https://www.microsoft.com/en-us/sql-server/sql-server-2016). It currently supports multiple database instances hosted on a single, external-to-cloudfoundry installation of SqlServer

This version should be considered a beta product, and has been tested against PCF Enterprise Runtime v1.9

##Where to get the tile
The tile will be available on [pivnet](https://network.pivotal.io/) in the near future. In the meanwhile, if you are interested in using the broker you can build and deploy it manually following the directions below.

##Prerequisites
This is a service broker implementation and requires an existing SqlServer install.

##The Modules
This project includes the following modules. See their respective READMEs for more information.

###[sqlserver-broker](https://github.com/cf-platform-eng/ms-sql-server-broker/tree/master/sqlserver-broker)
* This module contains the broker code.

###[sqlserver-connector](https://github.com/cf-platform-eng/ms-sql-server-broker/tree/master/sqlserver-connector)
* This module contains spring-cloud-connector code that can optionally be used by consumers of the brokered service.

###[sqlserver-client](https://github.com/cf-platform-eng/ms-sql-server-broker/tree/master/sqlserver-client)
* A sample project that can be used to demo usage of the broker and the connector.
 
###[sqlserver-util](https://github.com/cf-platform-eng/ms-sql-server-broker/tree/master/sqlserver-util)
* Shared utilities for interacting with the SqlServer backend.

##Instructions to run the demo
1. check out and build the project

  ```bash
  git clone git@github.com:cf-platform-eng/ms-sql-server-broker.git
  cd ms-sql-server-broker
  mvn clean install  
  ```
2. Follow the instructions in the [sqlserver-broker](https://github.com/cf-platform-eng/ms-sql-server-broker/tree/master/sqlserver-broker) to push and register the broker.
3. Create the cf service instance, making sure the name of the service matches the names in your broker's manifest.yml file. 

  ```bash  
  cf create-service SqlServer sharedVM sqlserver-service  
  ```  
4. Follow the instructions in the [sqlserver-client](https://github.com/cf-platform-eng/ms-sql-server-broker/tree/master/sqlserver-client) to push the sample app.