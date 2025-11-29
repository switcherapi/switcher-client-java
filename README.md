***

<div align="center">
<b>Switcher Client SDK</b><br>
A Java SDK for Switcher API
</div>

<div align="center">

[![Master CI](https://github.com/switcherapi/switcher-client-java/actions/workflows/master-2.yml/badge.svg)](https://github.com/switcherapi/switcher-client-java/actions/workflows/master-2.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=switcherapi_switcher-client&branch=master-2&&metric=alert_status)](https://sonarcloud.io/summary/overall?id=switcherapi_switcher-client&branch=master-2)
[![Known Vulnerabilities](https://snyk.io/test/github/switcherapi/switcher-client-java/badge.svg?targetFile=pom.xml)](https://snyk.io/test/github/switcherapi/switcher-client-java?targetFile=pom.xml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Maven Central](https://img.shields.io/maven-central/v/com.switcherapi/switcher-client.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/com.switcherapi/switcher-client)
[![Slack: Switcher-HQ](https://img.shields.io/badge/slack-@switcher/hq-blue.svg?logo=slack)](https://switcher-hq.slack.com/)

</div>

***

![Switcher API: Java Client: Cloud-based Feature Flag API](https://github.com/switcherapi/switcherapi-assets/blob/master/logo/switcherapi_java_client.png)

# Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
  - [Using SwitcherContext (Properties-based)](#using-switchercontext-properties-based)
  - [Using SwitcherContextBase (Programmatic)](#using-switchercontextbase-programmatic)
  - [Defining Feature Flags](#defining-feature-flags)
- [Usage Patterns](#usage-patterns)
- [Operating Modes](#operating-modes)
  - [Remote Mode](#remote-mode)
  - [Local Mode](#local-mode)
  - [Hybrid Mode](#hybrid-mode)
- [Advanced Features](#advanced-features)
  - [Real-time Snapshot Management](#real-time-snapshot-management)
  - [Performance Optimization](#performance-optimization)
- [Testing](#testing)
- [Native Image Support](#native-image-support)

# Overview

The Switcher Client SDK is a comprehensive Java library for integrating with [Switcher API](https://github.com/switcherapi/switcher-api), a cloud-based feature flag management system. This SDK enables you to control feature toggles, A/B testing, and configuration management in your Java applications.

# Key Features

- **🔧 Flexible Configuration**: Multiple configuration approaches to fit different application architectures
- **📁 Local & Remote Operation**: Works with remote API or local snapshot files for offline capability
- **🔄 Real-time Updates**: Hot-swapping support with automatic snapshot synchronization
- **🧪 Testing Support**: Built-in testing utilities and annotations for automated testing
- **⚡ Performance Optimized**: Includes throttling, caching, and async execution capabilities
- **🛡️ Resilient**: Silent mode with automatic fallback mechanisms for high availability
- **🔍 Easy Debugging**: Comprehensive execution history and metadata tracking

# Installation

## Maven Dependency

Add the following dependency to your `pom.xml`:

```xml
<dependency>
  <groupId>com.switcherapi</groupId>
  <artifactId>switcher-client</artifactId>
  <version>${switcher-client.version}</version>
</dependency>
```

## Build from Source

```bash
mvn clean install
```

## Version Compatibility

| SDK Version | Java Version | Jakarta EE | Description |
|-------------|--------------|------------|-------------|
| v1.x | Java 8+ | No | For traditional Java EE applications |
| v2.x | Java 17+ | Yes | For Jakarta EE 9+ applications |

# Quick Start

Here's a minimal example to get you started:

```java
// 1. Define your feature flags
public class MyAppFeatures extends SwitcherContext {
    @SwitcherKey
    public static final String FEATURE_NEW_UI = "FEATURE_NEW_UI";
    
    @SwitcherKey
    public static final String FEATURE_PREMIUM = "FEATURE_PREMIUM";
}

// 2. Use in your application
public class MyService {
    public void processRequest() {
        if (MyAppFeatures.getSwitcher(FEATURE_NEW_UI).isItOn()) {
            // Use new UI logic
        } else {
            // Use legacy UI logic
        }
    }
}
```

# Configuration

## Using SwitcherContext (Properties-based)

This approach automatically loads configuration from a properties file, ideal for applications with externalized configuration.

### Step 1: Create Configuration File

Create `src/main/resources/switcherapi.properties`:

```properties
# Required Configuration
switcher.context=com.example.MyAppFeatures
switcher.url=https://api.switcherapi.com
switcher.apikey=YOUR_API_KEY
switcher.component=my-application
switcher.domain=MY_DOMAIN

# Optional Configuration
switcher.environment=default
switcher.timeout=3000
switcher.poolsize=2
```

### Configuration Properties Reference

| Property                            | Required | Default | Description                                                                          |
|-------------------------------------|----------|---------|--------------------------------------------------------------------------------------|
| `switcher.context`                  | ✅ | -       | Fully qualified class name extending SwitcherContext                                 |
| `switcher.url`                      | ✅ | -       | Switcher API endpoint URL                                                            |
| `switcher.apikey`                   | ✅ | -       | API key for authentication                                                           |
| `switcher.component`                | ✅ | -       | Your application/component identifier                                                |
| `switcher.domain`                   | ✅ | -       | Domain name in Switcher API                                                          |
| `switcher.environment`              | ❌ | default | Environment name (dev, staging, default)                                             |
| `switcher.local`                    | ❌ | false   | Enable local-only mode                                                               |
| `switcher.check`                    | ❌ | false   | Validate switcher keys on startup                                                    |
| `switcher.relay.restrict`           | ❌ | true    | Defines if client will trigger local snapshot relay verification                     |
| `switcher.snapshot.location`        | ❌ | -       | Directory for snapshot files                                                         |
| `switcher.snapshot.auto`            | ❌ | false   | Auto-load snapshots on startup                                                       |
| `switcher.snapshot.skipvalidation`  | ❌ | false   | Skip snapshot validation on load                                                     |
| `switcher.snapshot.updateinterval`  | ❌ | -       | Interval for automatic snapshot updates (e.g., "5s", "2m")                           |
| `switcher.snapshot.watcher`         | ❌ | false   | Monitor snapshot files for changes                                                   |
| `switcher.silent`                   | ❌ | -       | Enable silent mode (e.g., "5s", "2m")                                                |
| `switcher.timeout`                  | ❌ | 3000    | API timeout in milliseconds                                                          |
| `switcher.poolsize`                 | ❌ | 2       | Thread pool size for API calls                                                       |
| `switcher.regextimeout` (v1-only)   | ❌ | 3000    | Time in ms given to Timed Match Worker used for local Regex (ReDoS safety mechanism) |
| `switcher.truststore.path`          | ❌ | -       | Path to custom truststore file                                                       |
| `switcher.truststore.password`      | ❌ | -       | Password for custom truststore                                                       |

> 💡 **Environment Variables**: Use `${ENV_VAR:default_value}` syntax for environment variable substitution.

### Step 2: Define Feature Class

```java
public class MyAppFeatures extends SwitcherContext {
    @SwitcherKey
    public static final String FEATURE_NEW_UI = "FEATURE_NEW_UI";
    
    @SwitcherKey
    public static final String FEATURE_PREMIUM = "FEATURE_PREMIUM";
}
```

## Using SwitcherContextBase (Programmatic)

This approach provides more flexibility and is ideal for applications requiring dynamic configuration.

### Basic Programmatic Configuration

```java
public class MyAppFeatures extends SwitcherContextBase {
    @SwitcherKey
    public static final String FEATURE_NEW_UI = "FEATURE_NEW_UI";
    
    // Configure programmatically
    static {
        configure(ContextBuilder.builder()
            .context(MyAppFeatures.class.getName())
            .apiKey("YOUR_API_KEY")
            .url("https://api.switcherapi.com")
            .domain("MY_DOMAIN")
            .component("my-application")
            .environment("default"));
        
        initializeClient();
    }
}
```

### Spring Boot Integration

```java
@ConfigurationProperties(prefix = "switcher")
public class MySwitcherConfig extends SwitcherContextBase {
    
    @SwitcherKey
    public static final String FEATURE_NEW_UI = "FEATURE_NEW_UI";
    
    @Override
    @PostConstruct
    public void configureClient() {
        // Add any pre-configuration logic here
        super.configureClient();
        // Add any post-configuration logic here
    }
}
```

### Custom Properties File

```java
// Load from custom properties file
MyAppFeatures.loadProperties("switcherapi-test");
```

## Defining Feature Flags

Feature flags must follow specific conventions for proper functionality:

```java
public class MyAppFeatures extends SwitcherContext {
    
    // ✅ Correct: public static final String
    @SwitcherKey
    public static final String FEATURE_NEW_UI = "FEATURE_NEW_UI";
    
    @SwitcherKey
    public static final String FEATURE_PREMIUM_ACCESS = "FEATURE_PREMIUM_ACCESS";
    
    // ❌ Incorrect examples:
    // private static final String WRONG = "WRONG";        // Not public
    // public static String WRONG2 = "WRONG2";             // Not final
    // public final String WRONG3 = "WRONG3";              // Not static
}
```

**Why these conventions matter:**
- **`public`**: Accessible from other parts of your application
- **`static`**: No need to instantiate the class to access the constant
- **`final`**: Prevents accidental modification during runtime

You can also name your feature flag attributes differently, but ensure the values match those defined in Switcher API.

# Usage Patterns

## 1. Basic Flag Checking

```java
// Simple boolean check
Switcher switcher = MyAppFeatures.getSwitcher(FEATURE_NEW_UI);
if (switcher.isItOn()) {
    // Feature is enabled
}
```

## 2. Detailed Result Information

```java
Switcher switcher = MyAppFeatures.getSwitcher(FEATURE_NEW_UI);
SwitcherResult result = switcher.submit();

if (result.isItOn()) {
    System.out.println("Feature enabled: " + result.getReason());
    // Access additional metadata if needed
    MyMetadata metadata = result.getMetadata(MyMetadata.class);
}
```

## 3. Strategy Validation with Input Parameters

### Preparing Input Separately

```java
List<Entry> entries = new ArrayList<>();
entries.add(Entry.of(StrategyValidator.DATE, "2024-01-01"));
entries.add(Entry.of(StrategyValidator.TIME, "14:00"));

switcher.prepareEntry(entries);
boolean isEnabled = switcher.isItOn();
```

### Fluent API Style (Recommended)

```java
import static com.example.MyAppFeatures.*;

boolean isEnabled = getSwitcher(FEATURE_PREMIUM_ACCESS)
    .checkValue("premium_user")
    .checkNetwork("192.168.1.0/24")
    .checkDate("2024-01-01")
    .isItOn();
```

## 4. Execution History Tracking

```java
Switcher switcher = getSwitcher(FEATURE_NEW_UI)
    .keepExecutions()  // Enable execution tracking
    .checkValue("user_type");

switcher.isItOn();

// Access the last execution result
SwitcherResult lastResult = switcher.getLastExecutionResult();
System.out.println("Last execution reason: " + lastResult.getReason());

// Clear history when needed
switcher.flushExecutions();
```

## 5. Performance Optimization with Throttling

```java
// Execute asynchronously with 1-second throttle
// Returns cached result if called within throttle period
boolean isEnabled = switcher.throttle(1000).isItOn();
```

# Operating Modes

## Remote Mode

Default mode that communicates directly with Switcher API.

```java
MyAppFeatures.configure(ContextBuilder.builder()
    .url("https://api.switcherapi.com")
    .apiKey("YOUR_API_KEY")
    .domain("MY_DOMAIN")
    .component("my-app"));

MyAppFeatures.initializeClient();
```

**Use Cases:**
- Real-time feature flag updates
- A/B testing with immediate changes
- Centralized configuration management

## Local Mode

Uses local snapshot files without API communication.

```java
MyAppFeatures.configure(ContextBuilder.builder()
    .local(true)
    .snapshotLocation("./src/main/resources/snapshots"));

MyAppFeatures.initializeClient();
```

**Use Cases:**
- Offline environments
- High-performance scenarios where API latency is critical
- Development and testing environments

## Hybrid Mode

Combines remote and local capabilities for optimal flexibility.

### Force Remote Resolution

```java
// Force specific switcher to resolve remotely even in local mode
switcher.forceRemote().isItOn();
```

### In-Memory Snapshots with Auto-Update

```java
MyAppFeatures.configure(ContextBuilder.builder()
    .url("https://api.switcherapi.com")
    .apiKey("YOUR_API_KEY")
    .domain("MY_DOMAIN")
    .local(true)
    .snapshotAutoLoad(true)
    .snapshotAutoUpdateInterval("30s")  // Check for updates every 30 seconds
    .component("my-app"));

MyAppFeatures.initializeClient();

// Optional: Schedule with callback for monitoring
MyAppFeatures.scheduleSnapshotAutoUpdate("30s", new SnapshotCallback() {
    @Override
    public void onSnapshotUpdate(long version) {
        logger.info("Snapshot updated to version: {}", version);
    }

    @Override
    public void onSnapshotUpdateError(Exception e) {
        logger.error("Failed to update snapshot: {}", e.getMessage());
    }
});
```

# Advanced Features

## Real-time Snapshot Management

### File System Watcher

Monitor snapshot files for external changes:

```java
// Start watching for file changes
MyAppFeatures.watchSnapshot();

// Stop watching when no longer needed
MyAppFeatures.stopWatchingSnapshot();
```

Or enable during initialization:

```java
MyAppFeatures.configure(ContextBuilder.builder()
    .snapshotWatcher(true)
    .snapshotLocation("./src/main/resources/snapshots"));
```

### Manual Snapshot Validation

```java
// Check if remote snapshot is newer than local
boolean hasUpdates = MyAppFeatures.validateSnapshot();
if (hasUpdates) {
    logger.info("New snapshot version available");
}
```

### Automated Background Updates

```java
MyAppFeatures.configure(ContextBuilder.builder()
    .snapshotAutoUpdateInterval("5m")  // Check every 5 minutes
    .snapshotLocation("./src/main/resources/snapshots"));
```

## Performance Optimization

### Silent Mode (Resilience)

Automatically fall back to cached results when API is unavailable:

```java
MyAppFeatures.configure(ContextBuilder.builder()
    .silentMode("30s")  // Retry API calls every 30 seconds when failing
    .url("https://api.switcherapi.com")
    // ... other config
);
```

**Time formats supported:**
- `5s` - 5 seconds
- `2m` - 2 minutes  
- `1h` - 1 hour

### Connection Pooling

```java
MyAppFeatures.configure(ContextBuilder.builder()
    .timeoutMs(5000)        // 5 second timeout
    .poolConnectionSize(5)  // 5 concurrent connections
    // ... other config
);
```

# Testing

## Built-in Test Utilities

### SwitcherBypass for Unit Tests

```java
@Test
void testFeatureEnabled() {
    // Force switcher to return specific value
    SwitcherBypass.assume(FEATURE_NEW_UI, true);
    
    assertTrue(myService.usesNewUI());
    
    // Reset to original behavior
    SwitcherBypass.forget(FEATURE_NEW_UI);
}

@Test
void testWithConditions() {
    Switcher switcher = MyAppFeatures.getSwitcher(FEATURE_PREMIUM_ACCESS)
        .checkValue("user_type");
    
    // Assume true only when specific condition is met
    SwitcherBypass.assume(FEATURE_PREMIUM_ACCESS, true)
        .when(StrategyValidator.VALUE, "premium");
    
    assertTrue(switcher.isItOn());
}
```

### JUnit 5 Integration

#### Single Switcher Test

```java
@SwitcherTest(key = FEATURE_NEW_UI, result = true)
void testNewUIFeature() {
    // FEATURE_NEW_UI will return true during this test
    assertTrue(myService.usesNewUI());
    // Automatically resets after test completion
}
```

#### Multiple Switchers

```java
@SwitcherTest(switchers = {
    @SwitcherTestValue(key = FEATURE_NEW_UI, result = true),
    @SwitcherTestValue(key = FEATURE_PREMIUM_ACCESS, result = false)
})
void testMultipleFeatures() {
    assertTrue(myService.usesNewUI());
    assertFalse(myService.hasPremiumAccess());
}
```

#### A/B Testing

```java
@SwitcherTest(key = FEATURE_NEW_UI, abTest = true)
void testFeatureABTesting() {
    // Test passes regardless of switcher result
    // Useful for testing both code paths
    myService.handleUILogic();
}
```

#### Conditional Testing

```java
@SwitcherTest(
    key = FEATURE_PREMIUM_ACCESS, 
    result = true,
    when = @SwitcherTestWhen(value = "premium_user")
)
void testPremiumFeature() {
    // Test with specific input conditions
    assertTrue(myService.checkPremiumAccess("premium_user"));
}
```

## Smoke Testing

Validate all switcher keys are properly configured:

```java
@Test
void validateSwitcherConfiguration() {
    // Throws exception if any switcher key is not found
    assertDoesNotThrow(() -> MyAppFeatures.checkSwitchers());
}
```

Enable automatic validation during startup:

```java
MyAppFeatures.configure(ContextBuilder.builder()
    .checkSwitchers(true)  // Validate on initialization
    // ... other config
);
```

# Native Image Support

Switcher Client fully supports GraalVM Native Image compilation:

```java
@ConfigurationProperties
public class MyNativeAppFeatures extends SwitcherContextBase {
	
    public static final String FEATURE_NEW_UI = "FEATURE_NEW_UI";
    public static final String FEATURE_PREMIUM = "FEATURE_PREMIUM";

    @Override 
    @PostConstruct 
    protected void configureClient() {
        super.registerSwitcherKeys(FEATURE_NEW_UI, FEATURE_PREMIUM);
        super.configureClient();
    }
}
```

---

## Additional Resources

- 📚 [Switcher Tutorials](https://github.com/switcherapi/switcherapi-tutorials) - Complete code examples and tutorials
- 💬 [Join our Slack](https://switcher-hq.slack.com/) - Community support and discussions
- 🐛 [Report Issues](https://github.com/switcherapi/switcher-client-java/issues) - Bug reports and feature requests

---