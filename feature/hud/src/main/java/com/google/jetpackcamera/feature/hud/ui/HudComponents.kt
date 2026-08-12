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
package com.google.jetpackcamera.feature.hud.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.google.jetpackcamera.feature.hud.ExposureMetadata
import com.google.jetpackcamera.feature.hud.R
import kotlin.math.abs
import kotlin.math.roundToInt

const val HUD_RETICLE_TAG = "hud_reticle_level"
const val HUD_GRAPH_TAG = "hud_graph_luminosity"
const val HUD_EXPOSURE_CHIPS_TAG = "hud_chips_exposure"
const val HUD_SIDEBAR_TAG = "hud_sidebar_quick_settings"
const val BTN_HUD_TOGGLE_GRAPH_TAG = "btn_hud_toggle_graph"
const val BTN_HUD_TOGGLE_FLASH_TAG = "btn_hud_toggle_flash"
const val BTN_HUD_FLIP_CAMERA_TAG = "btn_hud_flip_camera"
const val BTN_HUD_TOGGLE_SIDEBAR_TAG = "btn_hud_toggle_sidebar"

@Composable
fun CircularReticleWithLevel(
    rollDegrees: Float,
    pitchDegrees: Float,
    modifier: Modifier = Modifier
) {
    val leveled = abs(rollDegrees) < 1.5f && abs(pitchDegrees) < 1.5f
    val ringColor = if (leveled) Color(0xFF69F0AE) else Color.White.copy(alpha = 0.9f)
    Canvas(
        modifier = modifier
            .size(96.dp)
            .testTag(HUD_RETICLE_TAG)
    ) {
        val stroke = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        val radius = size.minDimension / 2f - stroke.width
        drawCircle(color = ringColor, radius = radius, style = stroke)
        drawCircle(color = ringColor, radius = 4.dp.toPx())

        val rollOffset = (rollDegrees / 45f).coerceIn(-1f, 1f) * radius * 0.55f
        val pitchOffset = (pitchDegrees / 45f).coerceIn(-1f, 1f) * radius * 0.55f
        val bubbleCenter = center + Offset(rollOffset, pitchOffset)
        drawCircle(
            color = if (leveled) Color(0xFF69F0AE) else Color(0xFFFFF176),
            radius = 8.dp.toPx(),
            center = bubbleCenter
        )
        drawLine(
            color = ringColor.copy(alpha = 0.7f),
            start = Offset(center.x - radius, center.y),
            end = Offset(center.x + radius, center.y),
            strokeWidth = 1.5.dp.toPx()
        )
        drawLine(
            color = ringColor.copy(alpha = 0.7f),
            start = Offset(center.x, center.y - radius),
            end = Offset(center.x, center.y + radius),
            strokeWidth = 1.5.dp.toPx()
        )
    }
}

@Composable
fun LuminosityGraph(
    histogram: List<Float>,
    averageLuminance: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .testTag(HUD_GRAPH_TAG)
            .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = stringResource(
                R.string.hud_graph_title,
                (averageLuminance * 100f).roundToInt()
            ),
            color = Color.White,
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
        ) {
            if (histogram.isEmpty()) return@Canvas
            val barWidth = size.width / histogram.size
            histogram.forEachIndexed { index, value ->
                val barHeight = size.height * value.coerceIn(0f, 1f)
                drawRect(
                    color = Color(0xFF80D8FF),
                    topLeft = Offset(index * barWidth, size.height - barHeight),
                    size = Size(barWidth * 0.8f, barHeight)
                )
            }
        }
    }
}

@Composable
fun ExposureMetadataChips(
    exposure: ExposureMetadata,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .testTag(HUD_EXPOSURE_CHIPS_TAG)
            .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetadataChip(
            label = stringResource(R.string.hud_chip_iso),
            value = exposure.iso?.toString() ?: stringResource(R.string.hud_value_unavailable)
        )
        MetadataChip(
            label = stringResource(R.string.hud_chip_shutter),
            value = formatShutter(exposure.exposureTimeNs)
                ?: stringResource(R.string.hud_value_unavailable)
        )
        MetadataChip(
            label = stringResource(R.string.hud_chip_ev),
            value = exposure.exposureCompensation?.let { formatEv(it) }
                ?: stringResource(R.string.hud_value_unavailable)
        )
    }
}

@Composable
private fun MetadataChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
        Text(text = value, color = Color.White, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun HudQuickSettingsSidebar(
    isBackCamera: Boolean,
    isFlashOn: Boolean,
    isGraphVisible: Boolean,
    onToggleFlash: () -> Unit,
    onFlipCamera: () -> Unit,
    onToggleGraph: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .testTag(HUD_SIDEBAR_TAG)
            .width(132.dp)
            .fillMaxHeight()
            .padding(8.dp),
        color = Color.Black.copy(alpha = 0.55f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.hud_sidebar_title),
                color = Color.White,
                style = MaterialTheme.typography.titleSmall
            )
            SidebarButton(
                text = stringResource(
                    if (isFlashOn) R.string.hud_action_flash_on else R.string.hud_action_flash_off
                ),
                onClick = onToggleFlash,
                testTag = BTN_HUD_TOGGLE_FLASH_TAG
            )
            SidebarButton(
                text = stringResource(
                    if (isBackCamera) {
                        R.string.hud_action_flip_to_front
                    } else {
                        R.string.hud_action_flip_to_back
                    }
                ),
                onClick = onFlipCamera,
                testTag = BTN_HUD_FLIP_CAMERA_TAG
            )
            SidebarButton(
                text = stringResource(
                    if (isGraphVisible) {
                        R.string.hud_action_hide_graph
                    } else {
                        R.string.hud_action_show_graph
                    }
                ),
                onClick = onToggleGraph,
                testTag = BTN_HUD_TOGGLE_GRAPH_TAG
            )
            TextButton(onClick = onOpenSettings) {
                Text(text = stringResource(R.string.hud_action_open_settings), color = Color.White)
            }
        }
    }
}

@Composable
private fun SidebarButton(
    text: String,
    onClick: () -> Unit,
    testTag: String
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { role = Role.Button }
            .testTag(testTag)
    ) {
        Text(text = text)
    }
}

private fun formatShutter(exposureTimeNs: Long?): String? {
    if (exposureTimeNs == null || exposureTimeNs <= 0L) return null
    val seconds = exposureTimeNs / 1_000_000_000.0
    return if (seconds >= 1.0) {
        String.format("%.1fs", seconds)
    } else {
        val denominator = (1.0 / seconds).roundToInt().coerceAtLeast(1)
        "1/$denominator"
    }
}

private fun formatEv(compensation: Int): String =
    if (compensation > 0) "+$compensation" else compensation.toString()
