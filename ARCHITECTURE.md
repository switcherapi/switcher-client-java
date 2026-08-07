# Switcher Client SDK — Architecture

This document describes the architecture of the Switcher Client SDK, from the API surface exposed to
consumers down to the internal building blocks and design patterns used to implement it. It complements
[README.md](README.md) (usage-oriented) with an engineering-oriented view of *how* the SDK is built.

## Table of Contents

- [1. Design Goals](#1-design-goals)
- [2. API-First View](#2-api-first-view)
- [3. High-Level Architecture](#3-high-level-architecture)
- [4. Package Structure](#4-package-structure)
- [5. Core Domain Model](#5-core-domain-model)
- [6. Execution Pipeline (Criteria Evaluation)](#6-execution-pipeline-criteria-evaluation)
- [7. Operating Modes](#7-operating-modes)
- [8. Configuration Architecture](#8-configuration-architecture)
- [9. Remote Communication Layer](#9-remote-communication-layer)
- [10. Local Snapshot Layer](#10-local-snapshot-layer)
- [11. Strategy Validators](#11-strategy-validators)
- [12. Concurrency & Background Workers](#12-concurrency--background-workers)
- [13. Resilience: Silent/Circuit-Breaker Mode](#13-resilience-silentcircuit-breaker-mode)
- [14. Testing Support](#14-testing-support)
- [15. Error Handling](#15-error-handling)
- [16. Design Patterns Summary](#16-design-patterns-summary)
- [17. Native Image / GraalVM Considerations](#17-native-image--graalvm-considerations)

---

## 1. Design Goals

The SDK is built around a set of goals that shape most design decisions:

- **API-first ergonomics** — feature flags are declared as typed constants and consumed through a
  fluent, chainable API (`getSwitcher(KEY).checkValue(...).isItOn()`).
- **Location transparency** — the same `Switcher` API works whether the criteria is resolved
  **remotely** (Switcher API), **locally** (snapshot file/in-memory), or a **hybrid** of both, without
  the caller needing to change code.
- **Resilience by default** — network failures must not necessarily break feature evaluation; the SDK
  offers default results, silent/circuit-breaker mode, and local snapshot fallback.
- **Low overhead** — thread pools, throttling, and caching prevent the SDK from becoming a bottleneck
  in hot code paths.
- **Testability** — first-class support for bypassing/mocking switcher results in unit tests
  (`SwitcherBypass`, `@SwitcherTest`) without needing a live API or snapshot.
- **Zero reflection at runtime where possible** — designed to be compatible with GraalVM Native Image.

---

## 2. API-First View

From a consumer's perspective, the SDK exposes three API surfaces, layered in order of typical usage:

### 2.1 Feature Declaration API
Applications declare feature flags as `public static final String` constants annotated with
`@SwitcherKey` inside a class that extends `SwitcherContext` (properties-based) or
`SwitcherContextBase` (programmatic, e.g. Spring Boot). This class acts as a **static registry/facade**
for all switchers in the application.

### 2.2 Fluent Evaluation API (`Switcher` / `SwitcherBuilder` / `SwitcherRequest`)
```java
getSwitcher(FEATURE_PREMIUM_ACCESS)
    .checkValue("premium_user")
    .checkNetwork("192.168.1.0/24")
    .checkDate("2024-01-01")
    .throttle(1000)
    .isItOn();
```
Every `check*` call appends a `StrategyValidator` input (`Entry`) to the request. Terminal operations
(`isItOn()`, `submit()`) trigger evaluation and return either a `boolean` or a rich `SwitcherResult`
(reason, metadata, execution history).

### 2.3 Management/Operational API (static methods on `SwitcherContextBase`)
Lifecycle and operational concerns are exposed as static methods: `initializeClient()`,
`validateSnapshot()`, `watchSnapshot()`, `checkSwitchers()`, `scheduleSnapshotAutoUpdate(...)`,
`configure(ContextBuilder)`. These map directly to the **Key Features** advertised in the README
(real-time snapshot updates, smoke testing, performance tuning).

### 2.4 Testing API
`SwitcherBypass` (programmatic mocking) and the JUnit Jupiter extension `@SwitcherTest` /
`@SwitcherTestValue` / `@SwitcherTestWhen` provide a declarative way to control switcher outcomes in
tests, entirely bypassing the executor pipeline.

This layering means the **public contract is the `Switcher` interface and the static context class**;
everything below (`SwitcherExecutor`, `ClientRemote`, `ClientLocal`, validators) is an internal
implementation detail that can evolve independently.

---

## 3. High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         Application Code                                │
│  MyAppFeatures extends SwitcherContext / SwitcherContextBase            │
│  @SwitcherKey public static final String FEATURE_X = "FEATURE_X";       │
└───────────────────────────────┬───────────────────────────────────────-─┘
                                │ getSwitcher(KEY)
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  SwitcherContext / SwitcherContextBase  (SwitcherConfig)                │
│  • Static registry of SwitcherRequest instances (cache)                 │
│  • Lifecycle: initializeClient(), configure(ContextBuilder)             │
│  • Scheduling: snapshot auto-update, snapshot watcher, token refresh    │
└───────────────────────────────┬─────────────────────────────────────────┘
                                │ delegates evaluation to
                                ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  SwitcherRequest (Switcher / SwitcherBuilder)                           │
│  • Fluent builder for Entry (strategy inputs)                           │
│  • Bypass check (SwitcherBypass) → Async/Throttle (AsyncSwitcher) →     │
│    SwitcherExecutor.executeCriteria(this)                               │
└───────────────────────────────┬─────────────────────────────────────────┘
                                │
                 ┌──────────────┴─────────────────┐
                 ▼                                ▼
   ┌────────────────────────────┐     ┌────────────────────────────────┐
   │ SwitcherRemoteService      │     │ SwitcherLocalService           │
   │ (Remote Mode / Hybrid)     │◄───►│ (Local Mode / Hybrid fallback) │
   │ implements SwitcherExecutor│     │ implements SwitcherExecutor    │
   └──────────────┬─────────────┘     └────────────────┬───────────────┘
                  │                                    │
                  ▼                                    ▼
      ┌────────────────────────┐          ┌────────────────────────────┐
      │ ClientRemote           │          │ ClientLocal                │
      │ (ClientRemoteService)  │          │ (ClientLocalService)       │
      │ • Auth/token lifecycle │          │ • Criteria evaluation      │
      │ • Silent mode          │          │   against in-memory Domain │
      └──────────┬─────────---─┘          │ • SwitcherValidator        │
                 ▼                        │   (Strategy dispatch)      │
      ┌────────────────────────┐          └──────────────┬─────────────┘
      │ ClientWS (HTTP)        │                         ▼
      │ ClientWSImpl (java.net │           ┌───────────────────────────┐
      │  .http.HttpClient)     │           │ SnapshotLoader / Watcher  │
      └───────────────────────-┘           │ (criteria/Domain model)   │
                                           └───────────────────────────┘
```

---

## 4. Package Structure

| Package | Responsibility |
|---|---|
| `com.switcherapi.client` | Public entry points: `SwitcherContext`, `SwitcherContextBase`, `SwitcherConfig`, `ContextBuilder`, `SwitcherExecutor`(+Impl), `SwitcherProperties`(+Impl) |
| `com.switcherapi.client.model` | Domain/API model: `Switcher`, `SwitcherBuilder`, `SwitcherRequest`, `SwitcherResult`, `Entry`, `StrategyValidator`, `AsyncSwitcher`, `ContextKey` |
| `com.switcherapi.client.model.criteria` | Snapshot data model mirroring the Switcher API domain: `Snapshot`, `Domain`, `Group`, `Config`, `StrategyConfig`, `Relay`, `SwitcherElement` |
| `com.switcherapi.client.service` | Service-level contracts: `SwitcherFactory` (result builder), `SwitcherValidator`, `ValidatorService`, `WorkerName` |
| `com.switcherapi.client.service.local` | Local execution: `ClientLocal`(+Service), `SwitcherLocalService` |
| `com.switcherapi.client.service.remote` | Remote execution: `ClientRemote`(+Service), `SwitcherRemoteService` |
| `com.switcherapi.client.service.validators` | Strategy pattern implementations: `ValueValidator`, `NetworkValidator`, `DateValidator`, `TimeValidator`, `DateTimeValidator`, `NumericValidator`, `RegexValidator`, `PayloadValidator` |
| `com.switcherapi.client.remote` | HTTP transport: `ClientWS`(+Impl), `ClientWSBuilder`, `Constants` |
| `com.switcherapi.client.remote.dto` | Wire DTOs: `AuthRequest/Response`, `CriteriaRequest/Response`, `SnapshotDataResponse`, `SnapshotVersionResponse`, `SwitchersCheck` |
| `com.switcherapi.client.utils` | Cross-cutting utilities: `SnapshotLoader`, `SnapshotSerializer`, `SnapshotWatcher`, `SnapshotEventHandler`, `Mapper`, `SwitcherUtils` |
| `com.switcherapi.client.exception` | Exception hierarchy rooted at `SwitcherException` |
| `com.switcherapi.client.test` | Testing utilities: `SwitcherBypass`, `@SwitcherTest`, `SwitcherTestExtension`, `SwitcherTestTemplate` |

---

## 5. Core Domain Model

- **`Switcher`** — the public interface describing the operations a caller can perform on a feature
  flag (`isItOn`, `submit`, `prepareEntry`, `flushExecutions`, `getLastExecutionResult`).
- **`SwitcherBuilder`** (abstract) — implements the fluent, chainable configuration surface
  (`checkValue`, `checkDate`, `throttle`, `remote`, `bypassMetrics`, `restrictRelay`, `defaultResult`,
  `keepExecutions`). This is the **Builder pattern** applied to per-call configuration.
- **`SwitcherRequest`** (final) — concrete `Switcher`/`SwitcherBuilder` implementation and the object
  returned by `getSwitcher(KEY)`. It owns: the switcher key, a reference to the resolved
  `SwitcherExecutor`, an `executionsMap` (execution history keyed by input list, `ConcurrentHashMap` for
  thread safety), and lazy `AsyncSwitcher` creation for throttled calls.
- **`Entry`** — an immutable strategy input pair (`StrategyValidator` type + value), the atomic unit of
  criteria input (e.g., `Entry.of(StrategyValidator.DATE, "2024-01-01")`).
- **`SwitcherResult`** — the evaluation outcome: `result` (boolean), `reason`, `metadata` (arbitrary
  JSON-deserializable payload via `getMetadata(Class)`), and switcher key/input echo for debugging.
- **`model.criteria` package** — a typed mirror of the Switcher API's snapshot JSON contract
  (`Snapshot` → `Domain` → `Group[]` → `Config[]` → `StrategyConfig[]`), used identically by both the
  remote criteria cache and the local snapshot evaluator. `SwitcherElement` is the common base
  (`description`, `activated`) shared by `Domain`, `Group`, `Config`.

Switchers registered via `@SwitcherKey` are instantiated **once** and cached in the static
`switchers` map inside `SwitcherContextBase`; `getSwitcher(key)` looks them up rather than
allocating a new object per call, keeping the hot path allocation-light.

---

## 6. Execution Pipeline (Criteria Evaluation)

Calling `switcher.submit()` (or `isItOn()`, which delegates to it) triggers this pipeline, in order:

1. **Test bypass check** — `SwitcherBypass.getBypass()` is consulted first; if the key is mocked, the
   pipeline short-circuits and returns the mocked `SwitcherResult` immediately. This guarantees test
   mocks always take precedence over real evaluation, with no network/IO cost.
2. **Throttle/async check** — if `throttle(ms)` was configured and prior executions exist,
   `AsyncSwitcher` decides whether enough time has elapsed to trigger a new async evaluation; otherwise
   it returns the last cached result from `executionsMap` without blocking the caller thread. This
   follows a **Stale-While-Revalidate (SWR)** strategy: the caller always receives immediately available
   data (the last cached `SwitcherResult`), while a fresh evaluation — when the throttle window has
   elapsed — is kicked off asynchronously in the background (`AsyncSwitcher#run`) and simply replaces the
   cached entry in `executionsMap` for the *next* call to pick up, rather than blocking the current one.
3. **Criteria execution** — `SwitcherExecutor.executeCriteria(SwitcherRequest)` is invoked. The
   concrete executor (`SwitcherRemoteService` or `SwitcherLocalService`) was selected once at
   `initializeClient()` time based on `switcher.local` configuration.
4. **Execution bookkeeping** — if `keepExecutions()` was set, the result is stored in
   `executionsMap` keyed by the exact `List<Entry>` used, enabling `getLastExecutionResult()`.

### Result construction
`SwitcherFactory` centralizes `SwitcherResult` construction (`buildResultEnabled`,
`buildResultDisabled`, `buildFromDefault`) so that reason strings and metadata propagation stay
consistent across remote and local paths.

---

## 7. Operating Modes

The mode is decided **once**, at `initializeClient()`, by `SwitcherContextBase#buildInstance()`:

```java
if (contextBol(ContextKey.LOCAL_MODE)) {
    return new SwitcherLocalService(clientRemote, clientLocal, switcherProperties);
}
return new SwitcherRemoteService(clientRemote, new SwitcherLocalService(clientRemote, clientLocal, switcherProperties));
```

- **Remote Mode** (default) — `SwitcherRemoteService` is the active `SwitcherExecutor`. It always
  attempts the HTTP call first (`ClientRemote.executeCriteria`); a `SwitcherLocalService` instance is
  still constructed internally to support silent-mode fallback and `defaultResult`.
- **Local Mode** — `SwitcherLocalService` is the active executor. It loads a `Domain` snapshot from a
  file (`SnapshotLoader.loadSnapshot`) or from the remote API at startup (`snapshotAutoLoad`), then
  evaluates entirely in-memory via `ClientLocal`.
- **Hybrid Mode** — Local mode is active, but per-call `switcher.forceRemote()`
  (`SwitcherBuilder#remote(true)`) routes that specific evaluation through
  `ClientLocalService#executeCriteria`, which detects `switcherRequest.isRemote()` and delegates to
  `ClientRemote.executeCriteria` instead of the in-memory `Domain`. Snapshot auto-update
  (`snapshotAutoUpdateInterval`) keeps the in-memory snapshot fresh in the background regardless.

In both Remote and Local modes, `SwitcherExecutor` is the common abstraction the rest of the SDK
depends on (**Strategy pattern** at the executor level) — callers never know which concrete
implementation is active.

---

## 8. Configuration Architecture

Two parallel but convergent configuration entry points exist, unified by `SwitcherProperties`:

- **`SwitcherContext`** — properties-file driven (`switcherapi.properties`), parsed by
  `SwitcherPropertiesImpl`, which supports `${ENV_VAR:default}` substitution.
- **`SwitcherContextBase`** — programmatic, via the `ContextBuilder` fluent builder
  (singleton-per-JVM `context` reference), or Spring-style `@ConfigurationProperties` beans whose
  setters populate protected fields consumed by `configureClient()`.

Both paths converge on a single `SwitcherProperties` instance (backed by a `ContextKey → value` map),
which is the single source of truth read by executors, validators, and the remote client.
`SwitcherContextValidator` enforces required properties (`url`, `apikey`, `domain`, `component`, etc.)
before `initializeClient()` proceeds, failing fast with `SwitcherContextException`.

`ContextKey` is an enum acting as a **typed key registry** for all configuration parameters,
avoiding stringly-typed property access throughout the codebase.

---

## 9. Remote Communication Layer

- **`ClientWS` / `ClientWSImpl`** — thin HTTP transport built on Java's built-in
  `java.net.http.HttpClient` (no external HTTP dependency). Handles the four Switcher API endpoints:
  auth, criteria execution, snapshot resolution, and snapshot version check/switchers check.
  `ClientWSBuilder` configures the client (executor thread pool, timeout, optional custom truststore
  for TLS).
- **`ClientRemote` / `ClientRemoteService`** — sits above `ClientWS` and owns **authentication
  lifecycle**: token acquisition, expiration tracking (`AuthResponse.isExpired()`), optional
  auto-refresh (`switcher.auth.autorefresh`) via a dedicated scheduled executor, and **silent-mode
  token forgery** (a fake `AuthResponse` with `SILENT_MODE` as the token sentinel) used to short-circuit
  repeated remote calls during an outage window.
- Thread pools are **partitioned by concern**: `SWITCHER_REMOTE_WORKER` (HTTP call execution pool,
  sized via `switcher.poolsize`), `SWITCHER_TOKEN_WORKER` (auth refresh), `SNAPSHOT_UPDATE_WORKER`
  (scheduled snapshot polling), `SNAPSHOT_WATCH_WORKER` (filesystem watch), `SWITCHER_ASYNC_WORKER`
  (per-`SwitcherRequest` throttle execution). All are daemon threads named via `WorkerName`, so they
  never block JVM shutdown.

---

## 10. Local Snapshot Layer

- **`SnapshotLoader`** — reads/writes the JSON snapshot file (`{environment}.json`) using Gson,
  deserializing into the `criteria.Snapshot` → `Domain` graph. `SnapshotSerializer` handles custom
  (de)serialization concerns for the criteria model.
- **`SnapshotWatcher`** — a `Runnable` using `java.nio.file.WatchService` to detect file system
  modifications to the snapshot file and trigger `SwitcherLocalService.notifyChange(...)`, which
  reloads the `Domain` in place and invokes a `SnapshotEventHandler` (`onSuccess`/`onError` callback
  interface).
- **`ClientLocal` / `ClientLocalService`** — the in-memory criteria evaluator. Walks
  `Domain → Group[] → Config[] → StrategyConfig[]`, short-circuiting on the first disabled level
  (domain/group/config/relay-restricted), then delegates strategy validation to `SwitcherValidator`.

---

## 11. Strategy Validators

Each `StrategyValidator` enum value (`VALUE`, `NUMERIC`, `NETWORK`, `REGEX`, `TIME`, `DATE`, `PAYLOAD`,
...) maps to a `Validator` subclass in `service.validators` (`ValueValidator`, `NumericValidator`,
`NetworkValidator`, `RegexValidator`, `TimeValidator`, `DateValidator`/`DateTimeValidator`,
`PayloadValidator`). This is a textbook **Strategy pattern**:

- `Validator` (abstract) defines the template: `execute(...)` logs and delegates to the abstract
  `process(StrategyConfig, Entry)`.
- **`ValidatorService`** (implements `SwitcherValidator`) is the dispatcher: given a
  `StrategyConfig.getStrategy()` name, it resolves and invokes the matching `Validator` instance.
- Regex evaluation uses a bounded "Timed Match Worker" (`switcher.regextimeout`, v1 only) as a
  **ReDoS protection mechanism** for user-supplied patterns.

Adding a new strategy type means adding one `Validator` implementation and registering it — existing
callers (`SwitcherBuilder.check*`, `ClientLocalService.processOperation`) require no changes.

---

## 12. Concurrency & Background Workers

The SDK relies on cooperative background workers rather than blocking the caller thread. Summary of
executor services and their trigger points:

| Executor Service | Created in | Purpose |
|---|---|---|
| `scheduledTokenExecutorService` | `initTokenExecutorService()` | Auto-refresh remote auth token before expiry |
| Remote pool (`ClientWS`) | `initRemotePoolExecutorService()` | Executes HTTP calls (`switcher.poolsize` threads) |
| `scheduledSnapshotExecutorService` | `scheduleSnapshotAutoUpdate(...)` | Periodic `validateSnapshot()` + local snapshot refresh |
| `watcherExecutorService` | `watchSnapshot(...)` | Runs `SnapshotWatcher` to observe file system changes |
| `AsyncSwitcher`'s internal `ExecutorService` | Lazily, per `SwitcherRequest`, on first throttle | Executes criteria off the caller thread when throttling |

All thread factories create **daemon threads** with descriptive names (`WorkerName` enum), ensuring
they don't prevent JVM shutdown and are identifiable in thread dumps.

---

## 13. Resilience: Silent/Circuit-Breaker Mode

`switcher.silent` (`ContextKey.SILENT_MODE`) configures a duration (e.g. `"5m"`) during which, after a
remote failure, the SDK stops attempting remote calls and instead evaluates against the local snapshot
(`SwitcherRemoteService.tryExecuteLocalCriteria`). Mechanically:

1. A `SwitcherRemoteException` from `ClientRemote` triggers `setSilentModeExpiration()` in
   `ClientRemoteService`, which installs a synthetic `AuthResponse` whose token equals the
   `SILENT_MODE` sentinel and whose expiry is `now + silent duration`.
2. Subsequent calls see `isTokenValid() == SILENT` and immediately throw `SwitcherRemoteException`
   without hitting the network, which `SwitcherRemoteService` catches and routes to the internal
   `SwitcherLocalService` (`switcherLocal.executeCriteria`).
3. Once the silent window expires, the next call naturally falls back to `INVALID` token state and
   attempts a real re-authentication, "closing" the circuit.

If silent mode is not configured, a `defaultResult(boolean)` set on the `SwitcherBuilder` acts as the
last-resort fallback (`SwitcherFactory.buildFromDefault`); otherwise the original exception propagates.

---

## 14. Testing Support

- **`SwitcherBypass`** — a static in-memory map (`key → SwitcherResult`) checked first in
  `SwitcherRequest.submit()`. `assume(key, result[, metadata])` / `forget(key)` let tests force
  outcomes without touching the executor pipeline at all.
- **`@SwitcherTest` + `SwitcherTestExtension`** (JUnit Jupiter `TestTemplateInvocationContextProvider` +
  `Before/AfterTestExecutionCallback`) — a declarative wrapper around `SwitcherBypass`:
  installs the mock(s) before the test, removes them after, and (for `abTest = true`) runs the test
  **twice**, inverting the result each time via `SwitcherTestTemplate`, so both branches of a feature
  toggle are exercised in one test method.
- **Smoke testing** (`checkSwitchers()`) — delegates to `SwitcherExecutor.checkSwitchers(Set<String>)`,
  which validates (remotely or against the local snapshot) that every declared `@SwitcherKey` maps to
  a real Switcher API key, throwing `SwitchersValidationException` listing anything missing. Can run
  on demand or automatically at startup (`switcher.check=true`).

---

## 15. Error Handling

All SDK-specific exceptions extend `SwitcherException` (unchecked), giving callers a single type to
catch if desired, while specific subclasses allow fine-grained handling:

- `SwitcherContextException` — invalid/missing configuration.
- `SwitcherKeyNotFoundException` — key not registered or not found in domain/snapshot.
- `SwitcherRemoteException` — HTTP/network failure.
- `SwitcherSnapshotLoadException` / `SwitcherSnapshotWriteException` — local snapshot I/O issues.
- `SwitchersValidationException` — one or more keys failed `checkSwitchers()`.
- `SwitcherInvalid*Exception` (`DateTimeArgument`, `NumericFormat`, `Operation`, `OperationInput`,
  `Strategy`, `Validator`, `TimeFormat`) — malformed criteria configuration or input during strategy
  evaluation.

This hierarchy lets the executor layer decide, per exception type, whether to fall back
(`defaultResult`, silent mode) or propagate to the caller.

---

## 16. Design Patterns Summary

| Pattern | Where |
|---|---|
| **Facade / Registry** | `SwitcherContext`/`SwitcherContextBase` static methods hide executor wiring, scheduling, and property loading behind a small static API |
| **Builder** | `ContextBuilder` (client configuration), `SwitcherBuilder` (per-call criteria configuration) |
| **Strategy** | `SwitcherExecutor` (Local vs Remote), `Validator` implementations per `StrategyValidator` |
| **Decorator/Delegation** | `SwitcherRemoteService` wraps a `SwitcherLocalService` for fallback without inheriting from it |
| **Template Method** | `Validator#execute()` delegates to abstract `process(...)`; `SwitcherExecutorImpl` shares snapshot init/version-check logic used differently by Local/Remote executors |
| **Factory** | `SwitcherFactory` builds consistent `SwitcherResult` instances (`buildResultEnabled/Disabled/FromDefault`) |
| **Observer/Callback** | `SnapshotCallback` (auto-update notifications), `SnapshotEventHandler` (file watcher events) |
| **Singleton (per-context)** | `ContextBuilder` holds one static builder instance per JVM context class |
| **Proxy/Bypass** | `SwitcherBypass` intercepts evaluation before it reaches the executor pipeline, used by `@SwitcherTest` |

---

## 17. Native Image / GraalVM Considerations

The SDK is designed to compile under GraalVM Native Image:

- `@SwitcherKey` field discovery uses reflection (`Class#getFields()`), so native image consumers must
  either rely on the SDK's reflection configuration or explicitly call
  `registerSwitcherKeys(...)`/override `configureClient()` to avoid relying on reflective classpath
  scanning (see the `MyNativeAppFeatures` example in the README).
- HTTP transport uses the JDK's built-in `java.net.http.HttpClient` rather than a third-party client,
  reducing the reflection/proxy surface that would otherwise need native-image configuration.
- Gson is used for JSON (de)serialization of DTOs and snapshot models; DTOs are plain POJOs with public
  getters/setters to keep Gson's reflective (de)serialization native-image-friendly.
