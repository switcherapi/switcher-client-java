[![Build Status](https://travis-ci.com/petruki/switcher-client.svg?branch=master)](https://travis-ci.com/petruki/switcher-client)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=switcher-client-java&metric=alert_status)](https://sonarcloud.io/dashboard?id=switcher-client-java)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.petruki/switcher-client.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.petruki%22%20AND%20a:%22switcher-client%22)

![Switcher API: Java Client: Cloud-based Feature Flag API](https://github.com/petruki/switcher-client/blob/master/images/switcherclient.png)

# Install  
- Using the source code
`mvn clean install`

- Adding as a dependency - Maven
```xml
<dependency>
  <groupId>com.github.petruki</groupId>
  <artifactId>switcher-client</artifactId>
  <version>1.0.0</version>
</dependency>
```	

# About  
Client Java for working with Switcher-API.
https://github.com/petruki/switcher-api

Switcher Client is a friendly lib to interact with the Switcher API by:
- Simplifying validations throughout your remote Switcher configuration.
- Able to work offline using a snapshot claimed from your remote Switcher-API.
- Able to run in silent mode that will prevent your application to not be 100% dependent on the online API.
- Being flexible in order to remove the complexity of multi-staging (add as many environments as you want).
- Being friendly by making possible to manipulate switchers without changing your online switchers. (useful for automated tests - see below some examples about bypassing switchers).
- Being secure by using OAuth 2 flow. Requests are made using tokens that will validate your domain, component, environment and API key.
Tokens have an expiration time and are not stored. The Switcher Client is responsible to renew it using your settings.

# Example
## Client configuration
```java
properties.put(SwitcherContextParam.URL, "http://localhost:3000/criteria");
properties.put(SwitcherContextParam.APIKEY, "API_KEY");
properties.put(SwitcherContextParam.DOMAIN, "MyDomain");
properties.put(SwitcherContextParam.COMPONENT, "MyApp");
properties.put(SwitcherContextParam.ENVIRONMENT, "default");
properties.put(SwitcherContextParam.SILENT_MODE, true);
properties.put(SwitcherContextParam.RETRY_AFTER, "5s");
properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "default.json");

SwitcherFactory.buildContext(properties, false);
Switcher switcher = SwitcherFactory.getSwitcher("FF2FOR2020");
switcher.isItOn();
```
- **APIKEY**: Obtained after creating your domain using the Switcher-API project.
- **ENVIRONMENT**: You can run multiple environments. Production environment is 'default' which is created automatically after creating the domain.
- **DOMAIN**: This is your business name identification.
- **COMPONENT**: This is the name of the application that will be using this API.
- **URL**: Endpoint of your Swither-API.
- **SILENT_MODE**: Enable the client work in silent mode if some problem network issues happen. It's necessary to save the snapshot localy.
- **RETRY_AFTER**: Represents the time a retry to reach the API should take.

## Offline settings
```java
properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "default.json");
SwitcherFactory.buildContext(properties, true);

Switcher switcher = SwitcherFactory.getSwitcher("FF2FOR2020");
switcher.isItOn();
```

## Bypass example
```java
Switcher switcher = SwitcherFactory.getSwitcher("FF2FOR2020");
switcher.isItOn(); // Pretending your API or Snapshot return 'true'

switcher.assume("FF2FOR2020", false);
switcher.isItOn(); // Now, it's going to return 'false'

switcher.forget("FF2FOR2020");
switcher.isItOn(); // Now, it's going to return 'true'
```