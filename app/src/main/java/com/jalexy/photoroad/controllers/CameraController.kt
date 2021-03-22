package com.jalexy.photoroad.controllers

import android.location.Location
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

class CameraController(val activity: AppCompatActivity, private val viewFinder: PreviewView) {

    private var photoFileController = PhotoFileController(activity)
    private var cameraExecutor = Executors.newSingleThreadExecutor()

    private var imageCapture: ImageCapture? = null

    fun takePhotoInLocation(location: Location?) {
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = photoFileController.createPhotoFile()

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(activity),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val saveUri = Uri.fromFile(photoFile)

                    photoFileController.savePhoto(saveUri, location)

                    val msg = "Сделана фотография: ${saveUri.path}"
                    Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Не удалось сделать фото: ${exception.message}", exception)
                }
            }
        )
    }

    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.createSurfaceProvider())
                }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    activity, cameraSelector, preview, imageCapture
                )

            } catch (ex: Exception) {
                Log.e(TAG, "Не удалось прибиндить камеру", ex)
            }
        }, ContextCompat.getMainExecutor(activity))
    }

    fun registerReceiver() {
        photoFileController.registerReceiver()
    }

    fun unregisterReceiver() {
        photoFileController.unregister()
    }

    fun shutDown() {
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraController"
    }
}