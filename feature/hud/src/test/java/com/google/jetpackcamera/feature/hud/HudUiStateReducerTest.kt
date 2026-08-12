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

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class HudUiStateReducerTest {

    @Test
    fun toggleGraph_flipsVisibility() {
        val initial = HudUiState.Ready(isGraphVisible = true)
        val toggled = HudUiStateReducer.toggleGraph(initial)
        assertThat(toggled.isGraphVisible).isFalse()
        assertThat(HudUiStateReducer.toggleGraph(toggled).isGraphVisible).isTrue()
    }

    @Test
    fun toggleFlash_and_lensFacing_updateFlags() {
        val initial = HudUiState.Ready(isFlashOn = false, isBackCamera = true)
        val flashed = HudUiStateReducer.toggleFlash(initial)
        val flipped = HudUiStateReducer.setLensFacing(flashed, isBackCamera = false)

        assertThat(flashed.isFlashOn).isTrue()
        assertThat(flipped.isBackCamera).isFalse()
    }

    @Test
    fun updateLuminance_clampsAverageAndStoresHistogram() {
        val initial = HudUiState.Ready()
        val updated = HudUiStateReducer.updateLuminance(
            state = initial,
            averageLuminance = 1.4f,
            histogram = listOf(0.2f, 0.8f, 0.5f)
        )

        assertThat(updated.averageLuminance).isEqualTo(1f)
        assertThat(updated.luminanceHistogram).containsExactly(0.2f, 0.8f, 0.5f).inOrder()
    }

    @Test
    fun updateExposure_replacesMetadata() {
        val exposure = ExposureMetadata(iso = 200, exposureTimeNs = 8_000_000L, exposureCompensation = 1)
        val updated = HudUiStateReducer.updateExposure(HudUiState.Ready(), exposure)
        assertThat(updated.exposure).isEqualTo(exposure)
    }
}
