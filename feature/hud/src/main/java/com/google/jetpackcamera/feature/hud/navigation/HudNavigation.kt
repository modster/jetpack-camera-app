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
package com.google.jetpackcamera.feature.hud.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import com.google.jetpackcamera.feature.hud.HudScreen

object HudRoute {
    const val ROUTE: String = "hud"
}

fun NavController.navigateToHud(
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    navigate(HudRoute.ROUTE, builder)
}

fun NavGraphBuilder.hudScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    composable(route = HudRoute.ROUTE) {
        HudScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToSettings = onNavigateToSettings
        )
    }
}
