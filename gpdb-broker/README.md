# gpdb-broker
A cloud foundry service broker for Greenplum.

## Download the GPDB JDBC driver:
1. Go to https://network.pivotal.io/products/pivotal-gpdb
2. Download the `Greenplum Database 4.x - DataDirect JDBC Driver 32 & 64 bit`
3. Run the following command from the directory where the JDBC jar is downloaded:

  ```bash
  mvn install:install-file -Dfile=greenplum.jar -DgroupId=com.pivotal \ 
  -DartifactId=greenplum -Dversion=5.1.4 -Dpackaging=jar -DgeneratePom=true
  ```
## Create lib under gpdb-broker/gpdb-broker
  ```bash
  mkdir lib
  ```

## Download the vSphere JARs insd=ide lib:

  ```bash
  cd lib
  wget -O vapi-authentication-2.7.0.jar 'https://github.com/vmware/vsphere-automation-sdk-java/blob/master/lib/vapi-authentication-2.7.0.jar?raw=true'
  wget -O vapi-runtime-2.7.0.jar 'https://github.com/vmware/vsphere-automation-sdk-java/blob/master/lib/vapi-runtime-2.7.0.jar?raw=true'
  wget -O vapi-samltoken-2.7.0.jar 'https://github.com/vmware/vsphere-automation-sdk-java/blob/master/lib/vapi-samltoken-2.7.0.jar?raw=true'
  wget -O vsphereautomation-client-sdk-6.6.1.jar 'https://github.com/vmware/vsphere-automation-sdk-java/blob/master/lib/vsphereautomation-client-sdk-6.6.1.jar?raw=true'
  wget -O vsphereautomation-lookupservice-6.6.1.jar 'https://github.com/vmware/vsphere-automation-sdk-java/blob/master/lib/vsphereautomation-lookupservice-6.6.1.jar?raw=true'
  ```

## Install jars to local maven

  ```bash
  mvn install:install-file -Dfile=vapi-runtime-2.7.0.jar -DgroupId=com.vmware.vapi -DartifactId=vapi-runtime -Dversion=2.7.0 -Dpackaging=jar

  mvn install:install-file -Dfile=vapi-authentication-2.7.0.jar -DgroupId=com.vmware.vapi -DartifactId=vapi-authentication -Dversion=2.7.0 -Dpackaging=jar

  mvn install:install-file -Dfile=vapi-samltoken-2.7.0.jar -DgroupId=com.vmware.vapi -DartifactId=vapi-samltoken -Dversion=2.7.0 -Dpackaging=jar

  mvn install:install-file -Dfile=vsphereautomation-client-sdk-6.6.1.jar -DgroupId=com.vmware -DartifactId=vsphereautomation-client-sdk -Dversion=6.6.1 -Dpackaging=jar

  mvn install:install-file -Dfile=vsphereautomation-lookupservice-6.6.1.jar -DgroupId=com.vmware.vsphereautomation.lookup -DartifactId=vsphereautomation-lookupservice -Dversion=6.6.1 -Dpackaging=jar 
  ```

## Using gpdb-broker
1. gpdb-broker requires a redis datastore. To set this up (depending on the environment it can be p-redis or rediscloud. Make sure to check the cf marketplace for available redis offerings)
  ```bash
  cf create-service p-redis shared-vm redis-for-gpdb
  ```
2. Edit the [manifest.yml](https://github.com/kdunn-pivotal/gpdb-broker/blob/master/gpdb-broker/manifest.yml) file as needed for your installation.
1. check out and build the project
  ```bash
  git clone git@github.com:parthobardhan/gpdb-broker.git
  cd gpdb-broker
  mvn clean install  
  ```
4. Push the broker to cf:
  ```bash
  cf push
  ```
5. Register the broker. The broker makes use of spring-security to protect itself against unauthorized meddling. For more information, please see [here](https://github.com/cloudfoundry-community/spring-boot-cf-service-broker#security).
  ```bash
  cf create-service-broker <gpdbUser> <gpdb password> https://uri.of.your.broker.app
  ```
6. See the broker:
  ```bash
  cf service-brokers
  Getting service brokers as admin...
  
  name                          url
  ...
  gpdb-broker              https://your-broker-url
  ...
  ```
7. Enable access to the broker:
  ```bash
  cf service-access
  Getting service access as admin...
  ...
  broker: gpdb-broker
   service     plan      access   orgs
   Cognition   aws       none
   Cognition   azure     none
   Cognition   vsphere   none
  ...
  
  
  cf enable-service-access Cognition
  Enabling access to all plans of service GPDB for all orgs as admin...


  cf marketplace
  Getting services from marketplace in org your-org / space your-space as you...
  OK
  
  service          plans                     description
  Cognition        aws, azure, vsphere       Cognition for Pivotal Cloud Foundry
  ...
  ```
  
## Managing the broker
Please refer to [this documentation](https://docs.cloudfoundry.org/services/managing-service-brokers.html) for general information on how to manage service brokers.

### Creating a service instance
Using the broker to create a service instance results in the creation of a new role with a random name and password.
  ```bash
  cf create-service Cognition aws myCognition
  ```
Optionally, users can provide an alphanumeric name for the database as follows:
  ```bash
  cf create-service Cognition aws myCognition -c '{"db" : "aDatabaseName"}'
  ```
### Deleting a service instance
Deleting a service instance results in the immediate deletion of the corresponding database.
  ```bash
  cf delete-service myCognition
  ```
### Binding to a service
Once a service instance (contained database) has been created, users can bind application to it in the usual fashion. The binding process includes the creation of random database-level credentials that are tied to the binding.
  ```bash
  cf bind-service anApplication myCognition
  ```
Optionally, users can provide an alphanumeric user names and passwords for the binding as follows:
  ```bash
  cf bind-service anApplication myCognition -c '{"uid" : "aUserId", "pw" : "aValidGpdbPassword"}'
  ```
