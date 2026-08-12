# Graph Report - app  (2026-08-12)

## Corpus Check
- Corpus is ~27,313 words - fits in a single context window. You may not need a graph.

## Summary
- 427 nodes · 1025 edges · 25 communities (20 shown, 5 thin omitted)
- Extraction: 95% EXTRACTED · 5% INFERRED · 0% AMBIGUOUS · INFERRED: 52 edges (avg confidence: 0.84)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Capture Mode & Flash Tests
- Capture Mode Test Cases
- Navigation & Permissions Tests
- MainActivity & Navigation Host
- Device Test Suite Registry
- Test Utilities & Helpers
- Single Lens Mode Tests
- Background/Foreground Tests
- Hilt App Module DI
- Debug Overlay & External Automation Tests
- App Launcher Icons
- Flash Device Tests
- Media Metadata Utilities
- App Shell Semantic Nodes
- Settings DataStore Module
- Switch Camera Tests
- Permissions Infrastructure
- Settings Device Test
- Core DI Modules
- Application Class
- Navigation Routes
- Test Cache Param
- Media Store Test Util
- JetpackCameraApplication

## God Nodes (most connected - your core abstractions)
1. `runMainActivityScenarioTest()` - 78 edges
2. `assume()` - 23 edges
3. `runMainActivityScenarioTestForResult()` - 23 edges
4. `runMainActivityMediaStoreAutoDeleteScenarioTest()` - 22 edges
5. `CaptureModeSettingsTest` - 20 edges
6. `deleteFilesInDirAfterTimestamp()` - 18 edges
7. `ImageCaptureDeviceTest` - 17 edges
8. `waitForCaptureButton()` - 17 edges
9. `waitForNodeWithTag()` - 17 edges
10. `getSingleImageCaptureIntent()` - 16 edges

## Surprising Connections (you probably didn't know these)
- `runMainActivityMediaStoreAutoDeleteScenarioTest()` --calls--> `mediaStoreInsertedFlow()`  [INFERRED]
  src/androidTest/java/com/google/jetpackcamera/utils/UiTestUtil.kt → src/androidTest/java/com/google/jetpackcamera/utils/AppTestUtil.kt
- `BackgroundDeviceTest` --calls--> `runMainActivityScenarioTest`  [INFERRED]
  app/src/androidTest/java/com/google/jetpackcamera/BackgroundDeviceTest.kt → app/src/androidTest/java/com/google/jetpackcamera/utils/UiTestUtil.kt
- `CameraEffectDeviceTest` --calls--> `runMainActivityScenarioTest`  [INFERRED]
  app/src/androidTest/java/com/google/jetpackcamera/CameraEffectDeviceTest.kt → app/src/androidTest/java/com/google/jetpackcamera/utils/UiTestUtil.kt
- `ConcurrentCameraTest` --calls--> `runMainActivityScenarioTest`  [INFERRED]
  app/src/androidTest/java/com/google/jetpackcamera/ConcurrentCameraTest.kt → app/src/androidTest/java/com/google/jetpackcamera/utils/UiTestUtil.kt
- `DebugOverlayTest` --calls--> `runMainActivityScenarioTest`  [INFERRED]
  app/src/androidTest/java/com/google/jetpackcamera/DebugOverlayTest.kt → app/src/androidTest/java/com/google/jetpackcamera/utils/UiTestUtil.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Device Test Infrastructure (runMainActivityScenarioTest + UiTestUtil + AppTestUtil)** — app_src_androidtest_java_com_google_jetpackcamera_utils_uitestutil_runmainactivityscenariotest, app_src_androidtest_java_com_google_jetpackcamera_utils_uitestutil_runmainactivitymediastoreautodeletescenariotest, app_src_androidtest_java_com_google_jetpackcamera_utils_apptestutil_cameraxresetrule, app_src_androidtest_java_com_google_jetpackcamera_utils_apptestutil_mediastoreinsertedflow, app_src_androidtest_java_com_google_jetpackcamera_utils_composetestruleext_composetestruleext [INFERRED 0.90]
- **Test Media File Lifecycle (observe, capture, verify, delete)** — app_src_androidtest_java_com_google_jetpackcamera_utils_apptestutil_mediastoreinsertedflow, app_src_androidtest_java_com_google_jetpackcamera_utils_uitestutil_runmainactivitymediastoreautodeletescenariotest, app_src_androidtest_java_com_google_jetpackcamera_utils_uitestutil_deletefilesindiraftertimestamp, app_src_androidtest_java_com_google_jetpackcamera_utils_uitestutil_mediastoreentryexistsaftertimestamp [INFERRED 0.85]
- **Permission Handling Utilities for Tests** — app_src_androidtest_java_com_google_jetpackcamera_utils_apptestutil_app_required_permissions, app_src_androidtest_java_com_google_jetpackcamera_utils_apptestutil_test_required_permissions, app_src_androidtest_java_com_google_jetpackcamera_utils_uitestutil_individualtestgrantpermissionrule, app_src_androidtest_java_com_google_jetpackcamera_utils_uitestutil_uideviceextensions [INFERRED 0.85]
- **Hilt DI App-Level Module Configuration** — app_src_main_java_com_google_jetpackcamera_appmodule_appmodule, app_src_main_java_com_google_jetpackcamera_appsettingsmodule_appsettingsmodule, app_src_main_java_com_google_jetpackcamera_jetpackcameraapplication_jetpackcameraapplication [EXTRACTED 0.95]
- **Material3 Theme System** — app_src_main_java_com_google_jetpackcamera_ui_theme_color_colorpalette, app_src_main_java_com_google_jetpackcamera_ui_theme_theme_jetpackcameratheme, app_src_main_java_com_google_jetpackcamera_ui_theme_type_apptypography [EXTRACTED 0.95]
- **App Startup Flow** — app_src_main_java_com_google_jetpackcamera_mainactivity_mainactivity, app_src_main_java_com_google_jetpackcamera_mainactivityviewmodel_mainactivityviewmodel, app_src_main_java_com_google_jetpackcamera_ui_jcaapp_jcaapp [EXTRACTED 0.95]
- **All Square Launcher Icon Density Variants** — app_src_main_res_mipmap_hdpi_ic_launcher_hdpi_square, app_src_main_res_mipmap_mdpi_ic_launcher_mdpi_square, app_src_main_res_mipmap_xhdpi_ic_launcher_xhdpi_square, app_src_main_res_mipmap_xxhdpi_ic_launcher_xxhdpi_square, app_src_main_res_mipmap_xxxhdpi_ic_launcher_xxxhdpi_square [INFERRED 0.90]
- **All Round Launcher Icon Density Variants** — ic_launcher_hdpi_round, ic_launcher_mdpi_round, ic_launcher_xhdpi_round, ic_launcher_xxhdpi_round, ic_launcher_xxxhdpi_round [INFERRED 0.90]
- **Adaptive Icon Square and Round Concept Pair** — app_src_main_res_mipmap_xxxhdpi_ic_launcher_square_concept, app_src_main_res_mipmap_xxxhdpi_ic_launcher_round_concept [INFERRED 0.90]

## Communities (25 total, 5 thin omitted)

### Community 0 - "Capture Mode & Flash Tests"
Cohesion: 0.08
Nodes (55): ConcurrentCameraMode, FlashMode, R, SemanticsMatcher, SemanticsNodeInteraction, SemanticsPropertyKey, GrantPermissionRule, ConcurrentCameraTest (+47 more)

### Community 1 - "Capture Mode Test Cases"
Cohesion: 0.09
Nodes (21): CoroutineContext, CaptureModeSettingsTest, CaptureMode, ImageCaptureDeviceTest, GrantPermissionRule, CacheParam, NO_CACHE, WITH_CACHE (+13 more)

### Community 2 - "Navigation & Permissions Tests"
Cohesion: 0.08
Nodes (12): GrantPermissionRule, NavigationTest, PermissionsTest, GrantPermissionRule, PostCaptureTest, GrantPermissionRule, LensFacing, SettingsDeviceTest (+4 more)

### Community 3 - "MainActivity & Navigation Host"
Cohesion: 0.10
Nodes (30): ComponentActivity, Modifier, NavHostController, isInDarkMode(), Bundle, DebugSettings, ExternalCaptureMode, Intent (+22 more)

### Community 4 - "Device Test Suite Registry"
Cohesion: 0.08
Nodes (29): BackgroundDeviceTest, CameraEffectDeviceTest, CaptureModeSettingsTest, ConcurrentCameraTest, DebugOverlayTest, FlashDeviceTest, FocusMeteringTest, ImageCaptureDeviceTest (+21 more)

### Community 5 - "Test Utilities & Helpers"
Cohesion: 0.19
Nodes (19): Statement, ensureTagNotAppears(), askEveryTimeDialog(), denyPermissionDialog(), expectedNumFiles(), findObjectById(), grantPermissionDialog(), IndividualTestGrantPermissionRule (+11 more)

### Community 6 - "Single Lens Mode Tests"
Cohesion: 0.16
Nodes (11): android, Flow, GrantPermissionRule, SingleLensModeTest, CameraXResetRule, Description, Instrumentation, TestRule (+3 more)

### Community 7 - "Background/Foreground Tests"
Cohesion: 0.21
Nodes (5): BackgroundDeviceTest, GrantPermissionRule, CameraEffectDeviceTest, GrantPermissionRule, visitSettingDialog()

### Community 8 - "Hilt App Module DI"
Cohesion: 0.21
Nodes (6): SaveMode, AppModule, CaptureMode, FilePathGenerator, JcaFilePathGenerator, FilePathGenerator

### Community 9 - "Debug Overlay & External Automation Tests"
Cohesion: 0.16
Nodes (5): DebugOverlayTest, GrantPermissionRule, ExternalAutomationCompatibilityTest, GrantPermissionRule, UiDevice

### Community 10 - "App Launcher Icons"
Cohesion: 0.21
Nodes (14): Play Store Launcher Icon (1024px PNG), Launcher Icon hdpi Square, Launcher Icon mdpi Square, Launcher Icon xhdpi Square, Launcher Icon xxhdpi Square, Round Launcher Icon Variant, Square (Adaptive) Launcher Icon Variant, Launcher Icon xxxhdpi Square (+6 more)

### Community 11 - "Flash Device Tests"
Cohesion: 0.18
Nodes (3): FlashDeviceTest, GrantPermissionRule, LensFacing

### Community 12 - "Media Metadata Utilities"
Cohesion: 0.26
Nodes (7): Rational, Size, getAspectRatio(), getHeight(), getResolution(), getWidth(), useAndRelease()

### Community 13 - "App Shell Semantic Nodes"
Cohesion: 0.22
Nodes (10): MainActivity, MainActivityUiState, MainActivityViewModel, JcaApp Composable, JetpackCameraNavHost Composable, Routes, Color Palette, ExtendedColorScheme (+2 more)

### Community 14 - "Settings DataStore Module"
Cohesion: 0.42
Nodes (6): Context, DataStore, Preferences, SettingsDataSource, AppSettingsModule, CaptureMode

### Community 15 - "Switch Camera Tests"
Cohesion: 0.43
Nodes (4): ComposeTestRule, GrantPermissionRule, runFlipCameraTest(), SwitchCameraTest

### Community 16 - "Permissions Infrastructure"
Cohesion: 0.40
Nodes (5): PermissionsTest, APP_REQUIRED_PERMISSIONS, TEST_REQUIRED_PERMISSIONS, IndividualTestGrantPermissionRule, UiDevice Permission Dialog Extensions

### Community 17 - "Settings Device Test"
Cohesion: 0.67
Nodes (3): SettingsDeviceTest, ComposeTestRuleExt, stateDescriptionMatches

### Community 18 - "Core DI Modules"
Cohesion: 0.67
Nodes (3): AppModule, AppSettingsModule, JcaFilePathGenerator

## Knowledge Gaps
- **38 isolated node(s):** `NO_CACHE`, `WITH_CACHE`, `Loading`, `Routes`, `ExtendedColorScheme` (+33 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **5 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `runMainActivityScenarioTest()` connect `Navigation & Permissions Tests` to `Capture Mode & Flash Tests`, `Capture Mode Test Cases`, `Test Utilities & Helpers`, `Single Lens Mode Tests`, `Background/Foreground Tests`, `Debug Overlay & External Automation Tests`, `Flash Device Tests`, `Switch Camera Tests`?**
  _High betweenness centrality (0.266) - this node is a cross-community bridge._
- **Why does `MainActivity` connect `MainActivity & Navigation Host` to `Test Utilities & Helpers`?**
  _High betweenness centrality (0.095) - this node is a cross-community bridge._
- **Why does `runMainActivityMediaStoreAutoDeleteScenarioTest()` connect `Capture Mode Test Cases` to `Capture Mode & Flash Tests`, `Navigation & Permissions Tests`, `Test Utilities & Helpers`, `Single Lens Mode Tests`, `Flash Device Tests`?**
  _High betweenness centrality (0.072) - this node is a cross-community bridge._
- **Are the 8 inferred relationships involving `assume()` (e.g. with `.assumeSupportsSingleStream()` and `.assumeSupportsSingleStream()`) actually correct?**
  _`assume()` has 8 INFERRED edges - model-reasoned connections that need verification._
- **What connects `NO_CACHE`, `WITH_CACHE`, `Loading` to the rest of the system?**
  _38 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Capture Mode & Flash Tests` be split into smaller, more focused modules?**
  _Cohesion score 0.08432432432432432 - nodes in this community are weakly interconnected._
- **Should `Capture Mode Test Cases` be split into smaller, more focused modules?**
  _Cohesion score 0.0929281122150789 - nodes in this community are weakly interconnected._