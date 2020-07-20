# gpdb-test-client
A simple spring boot application that makes use of greenplum-broker.

The app binds to a greenplum service, initializes a schema, loads some data, and exposes some REST endpoints. 

## Instructions to run the test client
1. Create a service.
   - `cf create-service Greenplum Free <your service name>`
2. Modify `services` entry in `./manifest.yml` to use your service.
3. Run `mvn clean install`
4. Push the app from the gpdb-test-client directory:
   - `cf push`
5. Get the route of the demo app:
  ```
  $ cf app <your app name>
  Showing health and status for app gpdb-test-client in org pivotal / space Data Engineering as user@pivotal.io...

  name:              gpdb-test-client
  requested state:   started
  routes:            gpdb-test-client.cfapps.io
  last uploaded:     Mon 15 Jul 21:03:53 EDT 2019
  stack:             cflinuxfs3
  buildpacks:        python

  type:           web
  instances:      1/1
  memory usage:   128M
       state     since                  cpu    memory          disk           details
  #0   running   2020-07-16T22:49:28Z   1.5%   44.1M of 128M   137.6M of 1Gk
  ```
6. Test out the client.
From a browser:
```
http://<route of the demo>/quote/
http://<route of the demo>/quote/AMZN
```
From command line:
```
curl http://<url of the demo>/quote/
curl http://<url of the demo>/quote/AMZN
```
7. Clean up
   - `cf delete <app name>`
   - `cf delete-service <service name>`
