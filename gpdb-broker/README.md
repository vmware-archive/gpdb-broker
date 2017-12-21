
This project creates the service broker to a shared Greenplum remote database <br />
Follow these steps to deploy the broker <br />

1) Set the greenplum remote configs in `gpdb-broker/src/main/resources/application.properties`
```
spring.datasource.url=<greenplum remote master host>
spring.datasource.username=<greenplum username>
spring.datasource.password=<greenplum password>
```
2) Update id and name in `gpdb-broker/gpdb-broker/src/main/resources/catalog.json`

3) Run `mvn clean install` 
4) Run `cf push`
5) Run `cf create-service-broker <greenplum broker name> <greenplum remote username> <greenplum remote password> <gpdb-broker app route>`
6) Check service access on the broker with `cf service-access`. You should see something like 
```
broker: gpdb-broker
   service        plan   access   orgs
   Greenplum-PB   pws    none

```
7) Enable service access with `cf enable-service-access Greenplum-PB`
8) Re-check service access on the broker with `cf service-access`. You should see something like 
```
broker: gpdb-broker
   service        plan   access   orgs
   Greenplum-PB   pws    all
```
9) Run cf `create-service <service name> <plan> <service instance name>` to create a service
