# Copilot instructions for Jetpack Camera App

## Project overview
This repository is a multi-module Android app built around Jetpack Compose and CameraX. The app shell lives in `app/`, where `MainActivity` collects launch intents/debug flags and hands off to `JcaApp`. `JcaApp` uses Jetpack Navigation to move between the permissions, preview, settings, and post-capture experiences.

The codebase is split into clear module layers:
- `app/` is the shell app and bootstrapping layer.
- `feature/*` contains screen-level Compose UI and ViewModels.
- `data/*` contains repositories and data sources (`CameraSystemRepository`, `SettingsRepository`, `MediaRepository`) and their Hilt bindings.
- `core/*` holds shared camera/settings/model primitives.
- `ui/*` contains reusable state models and Compose components.

Dependencies are centrally managed through `gradle/libs.versions.toml`.

## Build, test, and formatting commands
Use Java 17 for local builds. CI uses JDK 17.

Common commands:
- Build the default debug flavor: `./gradlew assembleStableDebug`
- Run the unit test suite: `./gradlew test --parallel --build-cache`
- Run the formatting/lint gate used in CI: `./gradlew spotlessCheck --init-script gradle/init.gradle.kts --parallel --build-cache`

Single-test example:
- `./gradlew :feature:preview:testDebugUnitTest --tests "com.google.jetpackcamera.feature.preview.PreviewViewModelTest"`

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Architecture notes
- `MainActivity` is the entry point for app startup and intent handling. Keep shell-level concerns there; avoid pushing feature-specific logic into `app/` unless it is truly app-wide.
- The main app flow is a navigation graph centered on the preview experience. Feature screens should stay in their module and expose navigation hooks through the feature API rather than embedding feature logic in the app shell.
- Data access is repository-based. Prefer adding or changing behavior through the repository layer and Hilt modules in `data/` rather than wiring platform or camera code directly into feature screens.
- Settings are persisted through the settings layer and should stay consistent across the datastore defaults and the app settings model when you change persisted state.

## Project conventions
- Prefer Hilt and `@HiltViewModel` for screen state and dependency wiring.
- Keep Compose UI and navigation in `feature/*` modules. Reusable UI state and components belong in `ui/*`.
- When changing Compose UI, follow the project’s existing testing patterns: use semantics for accessibility, and use test tags that follow the existing `element_purpose_value` style when adding new UI automation hooks.
- Tests should favor fakes over mocks. The repository already contains testing helpers in `data/*/testing` and `core/*/testing`; use those patterns rather than introducing Mockito-style mocks for new tests.
- New or changed Kotlin API surface should usually be documented with KDoc, especially for complex composables, ViewModels, and repositories.

## Contribution flow
Contributions go through GitHub pull requests and review. Keep PRs focused and update docs or README entries when behavior changes in a user-visible way.

## graphify

For any question about this repo's architecture, structure, components, or how to add/modify/find
code, your first action should be `graphify query "<question>"` when `graphify-out/graph.json`
exists. Use `graphify path "<A>" "<B>"` for relationship questions and `graphify explain "<concept>"`
for focused-concept questions. These return a scoped subgraph, usually much smaller than the full
report or raw grep output.

Triggers: "how do I…", "where is…", "what does … do", "add/modify a <component>",
"explain the architecture", or anything that depends on how files or classes relate.

If `graphify-out/wiki/index.md` exists, use it for broad navigation. Read `graphify-out/GRAPH_REPORT.md`
only for broad architecture review or when query/path/explain do not surface enough context. Only read
source files when (a) modifying/debugging specific code, (b) the graph lacks the needed detail, or
(c) the graph is missing or stale.

Type `/graphify` in Copilot Chat to build or update the graph.
