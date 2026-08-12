/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.jetpackcamera.feature.hud

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.Manifest
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.SurfaceRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.jetpackcamera.feature.hud.ui.BTN_HUD_TOGGLE_SIDEBAR_TAG
import com.google.jetpackcamera.feature.hud.ui.CircularReticleWithLevel
import com.google.jetpackcamera.feature.hud.ui.ExposureMetadataChips
import com.google.jetpackcamera.feature.hud.ui.HudQuickSettingsSidebar
import com.google.jetpackcamera.feature.hud.ui.LuminosityGraph
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

const val HUD_SCREEN_TAG = "hud_screen_root"

/**
 * Fully forked HUD camera screen with its own preview session and HUD chrome.
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HudScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    var uiState by remember { mutableStateOf<HudUiState>(HudUiState.Loading) }
    val readyStateFlow = remember { MutableStateFlow(HudUiState.Ready()) }

    val controller = rememberHudCameraController(
        context = context,
        lifecycleOwner = lifecycleOwner,
        onLuminance = { average, histogram ->
            readyStateFlow.update {
                HudUiStateReducer.updateLuminance(it, average, histogram)
            }
        },
        onExposure = { exposure ->
            readyStateFlow.update { HudUiStateReducer.updateExposure(it, exposure) }
        },
        onError = { uiState = HudUiState.Error }
    )

    LaunchedEffect(Unit) {
        readyStateFlow.collect { ready ->
            if (uiState !is HudUiState.Error) {
                uiState = ready
            }
        }
    }

    DisposableEffect(controller) {
        onDispose { controller.release() }
    }

    LaunchedEffect(cameraPermission.status.isGranted) {
        if (cameraPermission.status.isGranted) {
            val ready = readyStateFlow.value
            controller.openCamera(useBackCamera = ready.isBackCamera, flashOn = ready.isFlashOn)
            uiState = ready
        }
    }

    LevelSensorEffect { roll, pitch ->
        readyStateFlow.update { HudUiStateReducer.updateLevel(it, roll, pitch) }
    }

    when (val state = uiState) {
        HudUiState.Loading -> {
            if (!cameraPermission.status.isGranted) {
                PermissionPrompt(
                    onRequest = { cameraPermission.launchPermissionRequest() },
                    onNavigateBack = onNavigateBack,
                    modifier = modifier
                )
            } else {
                LoadingHud(onNavigateBack = onNavigateBack, modifier = modifier)
            }
        }

        HudUiState.Error -> {
            ErrorHud(onNavigateBack = onNavigateBack, modifier = modifier)
        }

        is HudUiState.Ready -> {
            HudReadyContent(
                state = state,
                surfaceRequest = controller.surfaceRequest,
                onNavigateBack = onNavigateBack,
                onNavigateToSettings = onNavigateToSettings,
                onToggleGraph = {
                    readyStateFlow.update(HudUiStateReducer::toggleGraph)
                },
                onToggleSidebar = {
                    readyStateFlow.update(HudUiStateReducer::toggleSidebar)
                },
                onToggleFlash = {
                    readyStateFlow.update { current ->
                        val next = HudUiStateReducer.toggleFlash(current)
                        controller.setFlash(next.isFlashOn)
                        next
                    }
                },
                onFlipCamera = {
                    readyStateFlow.update { current ->
                        val next = HudUiStateReducer.setLensFacing(current, !current.isBackCamera)
                        controller.setLensFacing(next.isBackCamera)
                        next
                    }
                },
                modifier = modifier
            )
        }
    }
}

@Composable
private fun HudReadyContent(
    state: HudUiState.Ready,
    surfaceRequest: SurfaceRequest?,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onToggleGraph: () -> Unit,
    onToggleSidebar: () -> Unit,
    onToggleFlash: () -> Unit,
    onFlipCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag(HUD_SCREEN_TAG)
    ) {
        if (surfaceRequest != null) {
            CameraXViewfinder(
                surfaceRequest = surfaceRequest,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            TopBar(
                onNavigateBack = onNavigateBack,
                onToggleSidebar = onToggleSidebar,
                isSidebarOpen = state.isSidebarOpen
            )

            CircularReticleWithLevel(
                rollDegrees = state.levelRollDegrees,
                pitchDegrees = state.levelPitchDegrees,
                modifier = Modifier.align(Alignment.Center)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(if (state.isSidebarOpen) 0.72f else 1f)
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                ExposureMetadataChips(exposure = state.exposure)
                if (state.isGraphVisible) {
                    LuminosityGraph(
                        histogram = state.luminanceHistogram,
                        averageLuminance = state.averageLuminance,
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth()
                    )
                }
            }

            if (state.isSidebarOpen) {
                HudQuickSettingsSidebar(
                    isBackCamera = state.isBackCamera,
                    isFlashOn = state.isFlashOn,
                    isGraphVisible = state.isGraphVisible,
                    onToggleFlash = onToggleFlash,
                    onFlipCamera = onFlipCamera,
                    onToggleGraph = onToggleGraph,
                    onOpenSettings = onNavigateToSettings,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    onNavigateBack: () -> Unit,
    onToggleSidebar: () -> Unit,
    isSidebarOpen: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Text(
                text = "←",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        }
        Text(
            text = stringResource(R.string.hud_screen_title),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        TextButton(
            onClick = onToggleSidebar,
            modifier = Modifier.testTag(BTN_HUD_TOGGLE_SIDEBAR_TAG)
        ) {
            Text(
                text = stringResource(
                    if (isSidebarOpen) {
                        R.string.hud_action_hide_sidebar
                    } else {
                        R.string.hud_action_show_sidebar
                    }
                ),
                color = Color.White
            )
        }
    }
}

@Composable
private fun LoadingHud(onNavigateBack: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        TopBar(onNavigateBack = onNavigateBack, onToggleSidebar = {}, isSidebarOpen = false)
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
private fun ErrorHud(onNavigateBack: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
    ) {
        TopBar(onNavigateBack = onNavigateBack, onToggleSidebar = {}, isSidebarOpen = false)
        Text(
            text = stringResource(R.string.hud_error_message),
            color = Color.White,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun PermissionPrompt(
    onRequest: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp)
    ) {
        TopBar(onNavigateBack = onNavigateBack, onToggleSidebar = {}, isSidebarOpen = false)
        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringResource(R.string.hud_permission_message), color = Color.White)
            TextButton(onClick = onRequest) {
                Text(text = stringResource(R.string.hud_permission_action))
            }
        }
    }
}

@Composable
private fun LevelSensorEffect(onLevel: (rollDegrees: Float, pitchDegrees: Float) -> Unit) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService<SensorManager>()
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (sensorManager == null || accelerometer == null) {
            onDispose { }
        } else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    val roll = Math.toDegrees(atan2(x.toDouble(), z.toDouble())).toFloat()
                    val pitch = Math.toDegrees(atan2(y.toDouble(), z.toDouble())).toFloat()
                    // Conflate tiny jitter before UI update.
                    onLevel(
                        (roll * 10f).roundToInt() / 10f,
                        (pitch * 10f).roundToInt() / 10f
                    )
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            sensorManager.registerListener(
                listener,
                accelerometer,
                SensorManager.SENSOR_DELAY_UI
            )
            onDispose { sensorManager.unregisterListener(listener) }
        }
    }
}
