# gpdb-test-client
A simple spring boot application that makes use of greenplum-broker.

The app binds to a greenplum service, initializes a schema, loads some data, and exposes some REST endpoints. 

## Instructions to run the test client
1. Create a service.
2. Modify `services` in `gpdb-test-client/manifest.yml` to point to your service.
3. run `mvn clean install`
4. Push the app:
  ```bash
  cd gpdb-test-client
  cf push
  ```
5. Get the url of the demo app:
  ```
  $ cf a
  Getting apps in org your-org / space your-space as user...
  OK
  
  name                       requested state    instances    memory    disk    urls
  gpdb-test-client           started            1/1          512M      1G      gpdb-test-client.your.domain
  ```
6. Test out the client.
From a browser:
```
http://<url of the demo>/quote/
http://<url of the demo>/quote/AMZN
```
From command line:
```
curl http://<url of the demo>/quote/
curl http://<url of the demo>/quote/AMZN
```
