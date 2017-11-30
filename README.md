# gpdb-broker
This is a Cloud Foundry service broker for [Greenplum Database](https://www.microsoft.com/en-us/sql-server/sql-server-2016). It currently supports multiple database instances within a sharedVM via a [bosh release](https://github.com/Pivotal-Field-Engineering/greenplum-release), or GPDB instances running external to PCF.

This version should be considered a alpha product, and has been tested against PCF Enterprise Runtime v1.9

## Prerequisites
The service broker requires an existing GPDB install.

## The Modules
### [gpdb-broker](https://github.com/parthobardhan/gpdb-broker/tree/master/gpdb-broker)
This module contains the broker code. Its [readme](https://github.com/parthobardhan/gpdb-broker/blob/master/gpdb-broker/README.md) contains information on how to build, configure, and deploy the broker.

### [gpdb-connector](https://github.com/parthobardhan/gpdb-broker/tree/master/gpdb-connector)
This module contains spring-cloud-connector code that can optionally be used by consumers of a brokered service.

### [gpdb-client-example](https://github.com/parthobardhan/gpdb-broker/tree/master/gpdb-client-example)
A sample project that demos usage of the broker and the connector. See its [readme](https://github.com/parthobardhan/gpdb-broker/blob/master/gpdb-client-example/README.md) for more details.
 
### [gpdb-util](https://github.com/parthobardhan/gpdb-broker/tree/master/gpdb-util)
Shared utilities for interacting with the GPDB backend.
