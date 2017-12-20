# gpdb-client-example
A simple spring boot application that makes use of gpdb-broker.

The app binds to the greenplum service, initializes a schema, loads some data, and exposes some REST endpoints. 

## Instructions to run the demo
1. Modify `spring.datasource.*` properties in `gpdb-client-example/src/main/resources/application-cloud.properties` to pull properties from the service the app is binding to.

eg. Replace `myGPDB` with your service instance name in `spring.datasource.url=${vcap.services.myGPDB.credentials.jdbcUrl}`

2. Modify `services` in `gpdb-client-example/manifest.yml` to point to your service.
3. run `mvn clean install`
4. Push the app:
  ```bash
  cd gpdb-client-example
  cf push
  ```
5. Get the url of the demo app:
  ```bash
  cf a
  Getting apps in org your-org / space your-space as admin...
  OK
  
  name                       requested state    instances    memory    disk    urls
  gpdb-client-example         started           1/1         512M     1G        gpdb-client-example.your.domain
  ```
6. Test out the demo in a browser. Try out some endpoints:
```
http://<url of the demo>/quote/
http://<url of the demo>/quote/MSFT
```
