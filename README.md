# my-postgres-broker
A demonstration cloud foundry service broker that can be used to integrate cf applications with postgres databases.

## Prerequisites
This broker requires redis. The build requires maven and JDK 8.

## Building the broker
1. clone the repo
  ```bash
  git clone git@github.com:cf-platform-eng/my-postgres-broker.git
  cd my-postgres-broker
  ```
2. Build the broker
  ```bash
  mvn clean install
  ```

## Using the broker

There are two ways to install the broker: via a tile, or by pushing it as an app and registering it via the cf cli.

### As a tile
1. See the information [here](http://docs.pivotal.io/tiledev/tile-generator.html) about creating a tile from source.
4. Install and configure the tile in the usual fashion using Operations Manager. 

### As a pushed application
1. edit the [manifest](https://github.com/cf-platform-eng/my-postgres-broker/blob/master/manifest.yml) file to match your postgres environment.

2. The broker requires a redis datastore. To set this up:
  
  ```bash
  cf create-service p-redis shared-vm redis-for-postgres
  ```
3. The broker makes use of spring-security to protect itself against unauthorized meddling. To set its username and password edit the [manifest.yml](https://github.com/cf-platform-eng/my-postgres-broker/blob/master/manifest.yml)) file as needed for your CF install (you probably don't want to check this in!).
4. Push the broker to cf:
  
  ```bash
  cf push
  ```
5. Register the broker:
  ```bash
  cf create-service-broker my-postgres-broker the_broker_user_from_the_manifest the_broker_password_from_the_manifest https://uri.of.your.broker.app
  ```
6. See the broker:
  
  ```bash
  cf service-brokers
  Getting service brokers as admin...
  
  name                          url
  ...
  my-postgres-broker            https://your-broker-url
  ...
  
  cf service-access
  Getting service access as admin...
  ...  
  broker: my-postgres-broker
     service    plan        access   orgs
     postgres   SharedVMs

  ...
  
  cf enable-service-access postgres
  Enabling access to all plans of service postgres for all orgs as admin...

  cf marketplace
  Getting services from marketplace in org your-org / space your-space as you...
  OK
  
  service          plans                                      description
  postgres         SharedVMs                                  Hi I'm Mr.Postgres! I share VMs
  ...
  ```
7. Create an instance:
  
  ```bash
  cf create-service postgres SharedVMs test-postgres-service
  cf s
  
  Getting services in org your-org / space your-space as admin...
  OK
  
  name                   service   plan            bound apps         last operation
  test-postgres-service  postgres  SharedVMs                          create succeeded
  ```
