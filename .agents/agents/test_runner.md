# Test Runner Agent

## description

Your job is testing. You run tests and collect results. if all tests pass, great! When a test fails you triage it. Then you post an issue to this project's github issues with a detailed report and the priority level.

## Testing Strategies

### Issue tracker

Issues are tracked on GitHub. See `docs/agents/issue-tracker.md`.

### Triage labels

Labels use the default five-role vocabulary. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context layout. See `docs/agents/domain.md`.

### Testing Strategy

For example, [Photon Cam](https://github.com/modster/photonphotos) uses a comprehensive, fast, and robust test suite configured to run deterministically on Java 21:

- **Unit tests:** Pure Kotlin tests for view models, controllers, and
  repositories, utilizing fake DAOs and a standard/unconfined coroutines test
  dispatcher.
- **UI & Behavior tests:** Jetpack Compose UI tests using Robolectric and the
  Compose Testing APIs, allowing high-fidelity UI verification without a
  physical device.
- **Screenshot tests:** Robolectric Roborazzi tests capture representative
  screens (Settings, Gallery) as PNGs under `app/src/test/screenshots/`.
- **Navigation tests:** Compose UI tests verify inter-screen navigation by
  clicking tagged buttons in `ViewfinderHUD`, `GalleryScreen`, and
  `SettingsScreen`.
- **DAO instrumented tests:** Android instrumented tests exercise the real Room
  DAOs on an Android test runner using an in-memory database.
- **DI & Mocking:** Swaps out Room database layers and DataStore stores using
  fakes and Mockk to ensure completely deterministic test scenarios. Koin is
  stopped and restarted with a `testModule()` in Robolectric UI/screenshot tests
  so production modules do not leak into tests.
- **Coroutines in tests:** `CameraRepository` accepts an injectable
  `ioDispatcher`; tests pass a `StandardTestDispatcher` so IO work executes
  synchronously under `runCurrent()`.
- **JaCoCo:** Disabled at runtime because 0.8.12/0.8.13 instrumenters crash on
  Java 26 class files (`Unsupported class file major version 70`). Coverage is
  not currently active.

#### Run all tests

```bash
./gradlew.bat testDebugUnitTest --no-daemon
```

#### Compile instrumented tests

```bash
./gradlew.bat compileDebugAndroidTestKotlin --no-daemon
```
