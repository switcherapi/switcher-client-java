[![Build Status](https://app.travis-ci.com/switcherapi/switcher-client.svg?branch=master)](https://app.travis-ci.com/switcherapi/switcher-client)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=switcherapi_switcher-client&metric=alert_status)](https://sonarcloud.io/dashboard?id=switcherapi_switcher-client)
[![Known Vulnerabilities](https://snyk.io/test/github/switcherapi/switcher-client/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/switcherapi/switcher-client?targetFile=pom.xml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.switcherapi/switcher-client.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.switcherapi%22%20AND%20a:%22switcher-client%22)
[![Slack: Switcher-HQ](https://img.shields.io/badge/slack-@switcher/hq-blue.svg?logo=slack)](https://switcher-hq.slack.com/)

![Switcher API: Java Client: Cloud-based Feature Flag API](https://github.com/switcherapi/switcherapi-assets/blob/master/logo/switcherapi_java_client.png)

# About
Client Java for working with Switcher-API.
https://github.com/switcherapi/switcher-api

- Flexible and robust SDK that will keep your code clean and maintainable.
- Able to work offline using a snapshot file downloaded from your remote Switcher-API Domain.
- Silent mode is a hybrid configuration that automatically enables contingent sub-processes in case of any connectivity issue.
- Built-in mock implementation for clear and easy implementation of automated testing.
- Easy to setup. Switcher Context is responsible to manage all the configuration complexity between your application and API.

# Usage

## Install  
- Using the source code `mvn clean install`
- Adding as a dependency - Maven
```xml
<dependency>
  <groupId>com.github.switcherapi</groupId>
  <artifactId>switcher-client</artifactId>
  <version>1.3.2</version>
</dependency>
```	

## Context properties
#### Newest versions - v1.2.x
SwitcherContext implements all external configurations regarding API access and SDK behaviors.
This new approach has eliminated unnecessary boilerplates and also has added a new layer for security purposes.

Similarly as frameworks like Spring Boot, Log4j, the SDK also requires creating an external properties file that will contain all the settings.

1. Inside the resources folder, create a file called: switcherapi.properties.


```
#required
switcher.context -> Feature class that extends SwitcherContext
switcher.apikey -> Switcher-API key generated for the application/component
switcher.component -> Application/component name
switcher.domain -> Domain name

#optional
switcher.url -> Swither-API endpoint if running privately
switcher.environment -> Environment name
switcher.offline -> true/false When offline, it will only use a local snapshot file
switcher.snapshot.file -> Snapshot file path
switcher.snapshot.location -> Folder from where snapshots will be saved/read
switcher.snapshot.auto -> true/false Automated lookup for snapshot when loading the application
switcher.snapshot.skipvalidation -> true/false Skip snapshotValidation() that can be used for UT executions
switcher.silent -> true/false Contingency in case of some problem with connectivity with the API
switcher.retry -> Time given to the module to re-establish connectivity with the API - e.g. 5s (s: seconds - m: minutes - h: hours)
```

2. Defining your features

```java
public class MyAppFeatures extends SwitcherContext {
	
	@SwitcherKey
	public static final String MY_SWITCHER = "MY_SWITCHER";

}

Switcher mySwitcher = MyAppFeatures.getSwitcher(MY_SWITCHER);
mySwitcher.isItOn();
```


#### Deprecated - available only on v1.1.0
The context map properties stores all information regarding connectivity and strategy settings. These constants can be accessed using *SwitcherContextParam*.

```java
properties.put(SwitcherContextParam.URL, "https://switcher-load-balance.herokuapp.com");
properties.put(SwitcherContextParam.APIKEY, "API_KEY");
properties.put(SwitcherContextParam.DOMAIN, "MyDomain");
properties.put(SwitcherContextParam.COMPONENT, "MyApp");
properties.put(SwitcherContextParam.ENVIRONMENT, "default");
properties.put(SwitcherContextParam.SILENT_MODE, true); //require RETRY_AFTER
properties.put(SwitcherContextParam.RETRY_AFTER, "5s");
properties.put(SwitcherContextParam.SNAPSHOT_AUTO_LOAD, true); //require SNAPSHOT_LOCATION
properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, "/src/resources");

SwitcherFactory.buildContext(properties, false);
Switcher switcher = SwitcherFactory.getSwitcher("FEATURE01");
switcher.isItOn();
```

- URL: Endpoint of your Swither-API.
- APIKEY: Switcher-API key generated after creating a domain.
- DOMAIN: Domain name.
- COMPONENT: Application name.
- ENVIRONMENT: Environment name. Production environment is named as 'default'.
- SILENT_MODE: (boolean) Activate contingency in case of some problem with connectivity with the API.
- RETRY_AFTER: Time given to the module to re-establish connectivity with the API - e.g. 5s (s: seconds - m: minutes - h: hours)
- SNAPSHOT_LOCATION: Set the folder location where snapshot files will be saved.
- SNAPSHOT_AUTO_LOAD: (boolean) Set the module to automatically download the snapshot configuration.

## Executing
There are a few different ways to call the API using the java library.
Here are some examples:

1. **No parameters**
Invoking the API can be done by obtaining the switcher object and calling *isItOn*.

```java
Switcher switcher = MyAppFeatures.getSwitcher(FEATURE01);
switcher.isItOn();
```

2. **Strategy validation - preparing input**
Loading information into the switcher can be made by using *prepareEntry*, in case you want to include input from a different place of your code. Otherwise, it is also possible to include everything in the same call.

```java
List<Entry> entries = new ArrayList<>();
entries.add(new Entry(Entry.DATE, "2019-12-10"));
entries.add(new Entry(Entry.DATE, "2020-12-10"));

switcher.prepareEntry(entries);
switcher.isItOn();
//or
switcher.isItOn(entries);
```

3. **Strategy validation - chained call**
Create chained calls using 'getSwitcher' then 'prepareEntry' then 'isItOn' functions.


```java
@Deprecated
Switcher switcher = SwitcherFactory.getSwitcher(FEATURE01)
	.prepareEntry(new Entry(Entry.VALUE, "My value"))
	.prepareEntry(new Entry(Entry.NETWORK, "10.0.0.1"))
	.isItOn();
```

SwitcherFactory is deprecated on v1.2.x and this same call can be build as folowwing:

```java
MyAppFeatures.getSwitcher(FEATURE01)
	.checkValue("My value")
	.checkNetwork("10.0.0.1")
	.isItOn();
```

4. **Strategy validation - all-in-one execution**
All-in-one method is fast and include everything you need to execute a complex call to the API. Stack inputs changing the last parameter to *true* in case you need to add more values to the strategy validator.

```java
switcher.isItOn(FEATURE01, new Entry(Entry.NETWORK, "10.0.0.3"), false);
//or simply
switcher.checkNetwork("10.0.0.3").isItOn();
```

5. **Accessing the response history**
Switchers when created store the last execution result from a given switcher key. This can be useful for troubleshooting or internal logging.

```java
switcher.getHistoryExecution();
```

6. **Throttling**
Improve the overall performance by using throttle feature to skip API calls in a short time. This feature is ideal for critical or repetitive code executions that requires high performance.

```java
switcher.throttle(1000).isItOn();
```

## Offline settings
You can also force the Switcher library to work offline. In this case, the snapshot location must be set up, so the context can be re-built using the offline configuration.

```java
@Deprecated
properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, "/src/resources");
SwitcherFactory.buildContext(properties, true);

Switcher switcher = MyAppFeatures.getSwitcher(FEATURE01);
switcher.isItOn();
```

When using version 1.2.0 or greater, it also can be done as following:

```java
MyAppFeatures.getProperties().setOfflineMode(true);
MyAppFeatures.getProperties().setSnapshotLocation("/src/resources");
MyAppFeatures.initializeClient();

Switcher switcher = MyAppFeatures.getSwitcher(FEATURE01);
switcher.isItOn();
```


## Real-time snapshot updater
Let the Switcher Client manage your application local snapshot file.

In order to minimize roundtrips and unnecessary file parsing, try to use one of these features to improve the overall performance when accessing snapshots locally.

1. This feature will update the in-memory Snapshot every time a modification on the file occurs.

```java
MyAppFeatures.watchSnapshot();
MyAppFeatures.stopWatchingSnapshot();
```

2. You can tell the Switcher Client to check if the snapshot file is updated. This will ensure that your application is running the most recent version of your cloud configuration.

```java
MyAppFeatures.validateSnapshot();
```

## Built-in mock feature
Write automated tests using this built-in mock mechanism to guide your test scenario according to what you want to test.
</br>*SwitcherExecutor* implementation has 2 methods that can make mock tests easier. Use assume to force a value to a switcher and forget to reset its original state.

```java
Switcher switcher = MyAppFeatures.getSwitcher(FEATURE01);

SwitcherExecutor.assume(FEATURE01, false);
switcher.isItOn(); // 'false'

SwitcherExecutor.forget(FEATURE01);
switcher.isItOn(); // Now, it's going to return the result retrieved from the API or the Snaopshot file
```

## Smoke test
Validate Switcher Keys on your testing pipelines before deploying a change.
Switcher Keys may not be configured correctly and can cause your code to have undesired results.

This feature will validate using the context provided to check if everything is up and running.
In case something is missing, this operation will throw an exception pointing out which Switcher Keys are not configured.

```java
@Test
void testSwitchers() {
	assertDoesNotThrow(() -> MyAppFeatures.checkSwitchers());
}
```

#### SwitcherMock annotation - Requires JUnit 5 Jupiter
Predefine Switchers result outside your test methods via Parametrized Test.
</br>It encapsulates the test and makes sure that the Switcher returns to its original state after concluding the test.

```java
@ParameterizedTest
@SwitcherMock(key = MY_SWITCHER, result = true)
void testMyFeature() {
   assertTrue(instance.myFeature());
}
```

### Change Log
- 1.3.2:
	- Updated dependency org.apache.logging.log4j from 2.15.0 to 2.16.0
	- Updated managed dependency junit to 4.13.1
- 1.3.0:
	- Optimized Switcher instance creation management
	- Added Throttling and Async calls
	- Updated com.google.code.gson:gson from 2.8.6 to 2.8.9
	- Updated Jersey dependencies from 2.34 to 2.35
	- Fixed Autoload snapshot is creating null as file name
- 1.2.1: Medium Severity Security Patch: Jersey has been updated - 2.33 to 2.34
- 1.2.0:
	- Changed how SwitcherContext is implemented - added support to properties file
	- Offline mode can programmatically load snapshots
	- Added extra security layer for verifying features
	- Added @SwitcherMock feature
	- Smoke testing
	- Removed PowerMockito: tests are way simpler to read using Okhttp3
	- Updated dependecy junit to JUnit5-jupiter
- 1.1.0:
	- Improved snapshot lookup mechanism
	- Both online and offline modes can validate/update snapshot version
- 1.0.10:
	- Dependency patch: Commons Net from 3.7.1 to 3.7.2
	- Critical Fix: Downgraded jersey-media-json-jackson 3.0.0 to 2.33
- 1.0.9: Security patch
	- Updated dependency jersey-client from 2.31 to 2.32
	- Updated dependency jersey-hk2 from 2.31 to 2.32
	- Updated dependency jersey-media-json-jackson from 2.31 to 3.0.0
	- Updated dependency common-net from 3.7 to 3.7.1
- 1.0.8:
	- Fixed issues when using Silent Mode
	- Fixed error when using only access to online API
	- Improved validation when verifying whether API is accessible
	- Added validations when preparing the Switcher Context
	- Updated dependency commons-net.version from 3.6 to 3.7
- 1.0.7: Added Regex Validation
- 1.0.6: Updated depencencies & new features
	- Updated dependency jersey-hk2 from 2.28 to 2.31
	- Updated dependency commons-net from 3.3 to 3.6
	- Updated dependency commons-lang3 from 3.8.1 to 3.10
	- Updated dependency gson from 2.8.5 to 2.8.6
	- Added execution log to Switcher
	- Added bypass metrics and show detailed criteria evaluation options to Switcher objects
- 1.0.5: Security patch - Jersey has been updated - 2.28 to 2.31
- 1.0.4: Added Numeric Validation
- 1.0.3: Security patch - Log4J has been updated - 2.13.1 to 2.13.3
- 1.0.2: 
    - Improved performance when loading snapshot file.
    - Snapshot file auto load when updated.
    - Re-worked built-in mock implementation
- 1.0.1: Security patch - Log4J has been updated
- 1.0.0: Working release
