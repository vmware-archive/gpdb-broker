# gpdb-broker

This is a Cloud Foundry service broker for the [Greenplum Analytic
Warehouse](https://pivotal.io/pivotal-greenplum). It currently supports
multiple database instances within a shared Greenplum cluster running external
to PCF.

This version should be considered a beta product, and has been tested against
Pivotal Application Service (PAS) v. 2.7.0.

## Prerequisites
The service broker requires an existing Greenplum instance.

## The Modules

### [gpdb-broker](https://github.com/Pivotal-Field-Engineering/gpdb-broker/tree/master/gpdb-broker)
This module contains the broker code. Its
[README](https://github.com/Pivotal-Field-Engineering/gpdb-broker/tree/master/gpdb-broker/README.md)
contains information on how to build, configure, and deploy the broker.

### [gpdb-connector](https://github.com/Pivotal-Field-Engineering/gpdb-broker/tree/master/gpdb-connector)
This module contains spring-cloud-connector code that can optionally be used by
consumers of a brokered service.

### [gpdb-test-client](https://github.com/Pivotal-Field-Engineering/gpdb-broker/tree/master/gpdb-test-client)
A test project that demos usage of the broker and can be used to verify a bound
service. See its
[readme](https://github.com/parthobardhan/gpdb-broker/blob/master/gpdb-test-client/README.md)
for more details.

### [gpdb-util](https://github.com/Pivotal-Field-Engineering/gpdb-broker/tree/master/gpdb-util)
Shared utilities for interacting with the Greenplum backend.

