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

/**
 * UI state for the forked HUD camera screen.
 */
sealed interface HudUiState {
    data object Loading : HudUiState

    data object Error : HudUiState

    data class Ready(
        val isBackCamera: Boolean = true,
        val isFlashOn: Boolean = false,
        val isGraphVisible: Boolean = true,
        val isSidebarOpen: Boolean = true,
        /** Average scene luminance in the range `[0f, 1f]`. */
        val averageLuminance: Float = 0f,
        /** Histogram bins normalized to `[0f, 1f]`. */
        val luminanceHistogram: List<Float> = emptyList(),
        /** Device roll in degrees for the spirit level. */
        val levelRollDegrees: Float = 0f,
        /** Device pitch in degrees for the spirit level. */
        val levelPitchDegrees: Float = 0f,
        val exposure: ExposureMetadata = ExposureMetadata()
    ) : HudUiState
}

/**
 * Exposure metadata chips shown on the HUD.
 *
 * @param iso Sensor sensitivity, or null when unavailable.
 * @param exposureTimeNs Exposure time in nanoseconds, or null when unavailable.
 * @param exposureCompensation Ev compensation index, or null when unavailable.
 */
data class ExposureMetadata(
    val iso: Int? = null,
    val exposureTimeNs: Long? = null,
    val exposureCompensation: Int? = null
)

/**
 * Pure helpers for HUD UI state transitions.
 */
object HudUiStateReducer {
    fun toggleGraph(state: HudUiState.Ready): HudUiState.Ready =
        state.copy(isGraphVisible = !state.isGraphVisible)

    fun toggleSidebar(state: HudUiState.Ready): HudUiState.Ready =
        state.copy(isSidebarOpen = !state.isSidebarOpen)

    fun toggleFlash(state: HudUiState.Ready): HudUiState.Ready =
        state.copy(isFlashOn = !state.isFlashOn)

    fun setLensFacing(state: HudUiState.Ready, isBackCamera: Boolean): HudUiState.Ready =
        state.copy(isBackCamera = isBackCamera)

    fun updateLuminance(
        state: HudUiState.Ready,
        averageLuminance: Float,
        histogram: List<Float>
    ): HudUiState.Ready = state.copy(
        averageLuminance = averageLuminance.coerceIn(0f, 1f),
        luminanceHistogram = histogram
    )

    fun updateLevel(
        state: HudUiState.Ready,
        rollDegrees: Float,
        pitchDegrees: Float
    ): HudUiState.Ready = state.copy(
        levelRollDegrees = rollDegrees,
        levelPitchDegrees = pitchDegrees
    )

    fun updateExposure(
        state: HudUiState.Ready,
        exposure: ExposureMetadata
    ): HudUiState.Ready = state.copy(exposure = exposure)
}
