# gpdb-client-example
A simple spring boot application that makes use of greenplum-broker.

The app binds to a greenplum service, initializes a schema, loads some data, and exposes some REST endpoints. 

## Instructions to run the demo
1. Create a service.
2. Modify `services` in `gpdb-client-example/manifest.yml` to point to your service.
3. run `mvn clean install`
4. Push the app:
  ```bash
  cd gpdb-client-example
  cf push
  ```
5. Get the url of the demo app:
  ```
  $ cf a
  Getting apps in org your-org / space your-space as user...
  OK
  
  name                       requested state    instances    memory    disk    urls
  gpdb-client-example         started           1/1         512M     1G        gpdb-client-example.your.domain
  ```
6. Test out the demo.
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
