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

import android.content.Context
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "HudCameraController"
private const val HISTOGRAM_BINS = 32

@Composable
fun rememberHudCameraController(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    onLuminance: (average: Float, histogram: List<Float>) -> Unit,
    onExposure: (ExposureMetadata) -> Unit,
    onError: () -> Unit
): HudCameraController {
    val latestOnLuminance by rememberUpdatedState(onLuminance)
    val latestOnExposure by rememberUpdatedState(onExposure)
    val latestOnError by rememberUpdatedState(onError)
    return remember(context, lifecycleOwner) {
        HudCameraController(
            context = context,
            lifecycleOwner = lifecycleOwner,
            onLuminance = { average, histogram -> latestOnLuminance(average, histogram) },
            onExposure = { latestOnExposure(it) },
            onError = { latestOnError() }
        )
    }
}

/**
 * Owns the forked HUD camera session: preview + image analysis + exposure metadata callbacks.
 */
@OptIn(ExperimentalCamera2Interop::class)
@Stable
class HudCameraController(
    context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val onLuminance: (average: Float, histogram: List<Float>) -> Unit,
    private val onExposure: (ExposureMetadata) -> Unit,
    private val onError: () -> Unit
) {
    private val appContext = context.applicationContext
    private val mainExecutor = ContextCompat.getMainExecutor(appContext)
    private val analysisExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    var surfaceRequest: SurfaceRequest? by mutableStateOf(null)
        private set

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var isBackCamera: Boolean = true
    private var isFlashOn: Boolean = false

    private val preview = Preview.Builder().build().apply {
        setSurfaceProvider { request -> surfaceRequest = request }
    }

    private val imageAnalysis: ImageAnalysis = buildImageAnalysis()

    private fun buildImageAnalysis(): ImageAnalysis {
        val builder = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        Camera2Interop.Extender(builder).setSessionCaptureCallback(
            object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    val iso = result.get(CaptureResult.SENSOR_SENSITIVITY)
                    val exposureTimeNs = result.get(CaptureResult.SENSOR_EXPOSURE_TIME)
                    val compensation = result.get(CaptureResult.CONTROL_AE_EXPOSURE_COMPENSATION)
                    onExposure(
                        ExposureMetadata(
                            iso = iso,
                            exposureTimeNs = exposureTimeNs,
                            exposureCompensation = compensation
                        )
                    )
                }
            }
        )
        return builder.build().apply {
            setAnalyzer(analysisExecutor, ::analyze)
        }
    }

    private fun analyze(imageProxy: ImageProxy) {
        try {
            val buffer = imageProxy.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            if (bytes.isEmpty()) {
                onLuminance(0f, List(HISTOGRAM_BINS) { 0f })
                return
            }

            var sum = 0L
            val bins = IntArray(HISTOGRAM_BINS)
            for (b in bytes) {
                val value = b.toInt() and 0xFF
                sum += value
                val bin = (value * HISTOGRAM_BINS) / 256
                bins[bin.coerceIn(0, HISTOGRAM_BINS - 1)]++
            }
            val average = (sum.toFloat() / bytes.size) / 255f
            val maxCount = bins.maxOrNull()?.coerceAtLeast(1) ?: 1
            val histogram = bins.map { it.toFloat() / maxCount }
            onLuminance(average.coerceIn(0f, 1f), histogram)
        } catch (error: Exception) {
            Log.e(TAG, "Luminosity analysis failed", error)
        } finally {
            imageProxy.close()
        }
    }

    fun openCamera(useBackCamera: Boolean = isBackCamera, flashOn: Boolean = isFlashOn) {
        isBackCamera = useBackCamera
        isFlashOn = flashOn
        val existingProvider = cameraProvider
        if (existingProvider != null) {
            bindCamera(existingProvider, useBackCamera, flashOn)
            return
        }
        val providerFuture = ProcessCameraProvider.getInstance(appContext)
        providerFuture.addListener(
            {
                try {
                    val provider = providerFuture.get().also { cameraProvider = it }
                    bindCamera(provider, useBackCamera, flashOn)
                } catch (error: Exception) {
                    Log.e(TAG, "HUD camera provider failed", error)
                    onError()
                }
            },
            mainExecutor
        )
    }

    private fun bindCamera(
        provider: ProcessCameraProvider,
        useBackCamera: Boolean,
        flashOn: Boolean
    ) {
        try {
            provider.unbindAll()
            val selector = if (useBackCamera) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                selector,
                preview,
                imageAnalysis
            )
            applyFlash(flashOn)
        } catch (error: Exception) {
            Log.e(TAG, "HUD camera bind failed", error)
            onError()
        }
    }

    fun setLensFacing(useBackCamera: Boolean) {
        if (useBackCamera == isBackCamera) return
        openCamera(useBackCamera = useBackCamera, flashOn = isFlashOn)
    }

    fun setFlash(enabled: Boolean) {
        isFlashOn = enabled
        applyFlash(enabled)
    }

    private fun applyFlash(enabled: Boolean) {
        val cam = camera ?: return
        if (cam.cameraInfo.hasFlashUnit()) {
            // Fire-and-forget torch request; result is not required for HUD chrome.
            @Suppress("UnusedEquals")
            cam.cameraControl.enableTorch(enabled)
        }
    }

    fun closeCamera() {
        cameraProvider?.unbindAll()
        camera = null
    }

    fun release() {
        closeCamera()
        analysisExecutor.shutdown()
    }
}
