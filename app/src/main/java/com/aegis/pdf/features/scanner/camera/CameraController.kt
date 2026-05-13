package com.aegis.pdf.features.scanner.camera

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Executor

@Singleton
class CameraController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "CameraController"
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var preview: Preview? = null
    private var imageAnalysis: ImageAnalysis? = null

    fun getProcessCameraProvider(): ListenableFuture<ProcessCameraProvider> {
        return ProcessCameraProvider.getInstance(context)
    }

    fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        previewSurfaceProvider: androidx.camera.core.Preview.SurfaceProvider,
        imageAnalyzer: ImageAnalysis.Analyzer
    ) {
        try {
            val cameraProviderFuture = getProcessCameraProvider()
            cameraProviderFuture.addListener({
                cameraProvider = cameraProviderFuture.get()

                preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewSurfaceProvider)
                }

                imageCapture = ImageCapture.Builder()
                    .setTargetRotation(android.view.Surface.ROTATION_0)
                    .build()

                imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .apply {
                        setAnalyzer(
                            ContextCompat.getMainExecutor(context),
                            imageAnalyzer
                        )
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider?.unbindAll()
                    cameraProvider?.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture,
                        imageAnalysis
                    )
                    Log.d(TAG, "Camera bound successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to bind camera", e)
                }
            }, ContextCompat.getMainExecutor(context))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get camera provider", e)
        }
    }

    fun unbindCamera() {
        cameraProvider?.unbindAll()
        Log.d(TAG, "Camera unbound")
    }

    fun captureImage(executor: Executor, callback: ImageCapture.OnImageCapturedCallback) {
        imageCapture?.takePicture(executor, callback)
    }

    fun enableTorch(enable: Boolean) {
        try {
            cameraProvider?.boundCamera(cameraProvider?.cameraInfo?.let {
                val camera = cameraProvider?.getInstance(0)
                if (enable) {
                    camera?.cameraControl?.enableTorch(true)
                } else {
                    camera?.cameraControl?.enableTorch(false)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle torch", e)
        }
    }

    fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}