# gpdb-client-example
A simple spring boot application that makes use of gpdb-broker.

The app binds to the greenplum service, initializes a schema, loads some data, and exposes some REST endpoints. 

## Instructions to run the demo
1. Modify `spring.datasource.*` properties in gpdb-broker/gpdb-client-example/src/main/resources/application-cloud.properties to pull properties from the service the app is binding to.
eg. Replace `myGPDB` with your service instance name in `spring.datasource.url=${vcap.services.myGPDB.credentials.jdbcUrl}`
2. run `mvn clean install`
2. Push the demo:
  ```bash
  cd gpdb-client-example
  cf push
  ```
4. Get the url of the demo app:
  ```bash
  cf a
  Getting apps in org your-org / space your-space as admin...
  OK
  
  name                       requested state    instances    memory    disk    urls
  gpdb-client-example         started           1/1         512M     1G        gpdb-client-example.your.domain
  ```
5. Test out the demo in a browser. Try out some endpoints:
```
http://<url of the demo>/quote/
http://<url of the demo>/quote/MSFT
```
