package com.jalexy.photoroad.controllers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import com.jalexy.photoroad.PhotoCallback
import com.jalexy.photoroad.room.Photo
import com.jalexy.photoroad.room.PhotoViewModel
import okhttp3.Call
import okhttp3.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.stream.Collectors


class PhotoFileController(private val activity: AppCompatActivity) {

    companion object {
        private const val TAG = "PhotoFileController"
        private const val SUCCESS_CODE = "200"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

        private const val ACTION_INTERNET_CONNECTION_STATE_CHANGED =
            "android.net.conn.CONNECTIVITY_CHANGE"
    }

    enum class Filter { ALL, SENT }

    private val photoViewModel: PhotoViewModel by lazy {
        ViewModelProvider(activity).get(PhotoViewModel::class.java)
    }

    private var outputDirectory = getOutputDirectory()
    private val uploadController: UploadController by lazy { UploadController(activity) }

    private val networkChangeReceiver: BroadcastReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {

                val action: String? = p1?.action
                if (ACTION_INTERNET_CONNECTION_STATE_CHANGED == action) {
                    Log.i(
                        "Test",
                        "О, что-то случилось с интернетом! connection = ${hasConnection()}"
                    )

                    if (hasConnection()) {

                        sendNewPhotoListToServer()
                    } else {

                        uploadController.notifyConnectionLost()
                    }
                }
            }
        }
    }

    private val photoCallback: PhotoCallback by lazy {
        object : PhotoCallback(activity) {

            override fun onResponse(call: Call, response: Response) {
                if (response.code.toString() == SUCCESS_CODE) {

                    val notNullPhoto = photo
                    notNullPhoto?.let {
                        notNullPhoto.sent = true
                        photoViewModel.updateSent(notNullPhoto)
                        uploadController.sendNextPhotoInQueue()

                        Log.i(TAG, "Фото отправлено: ${notNullPhoto.photoUri}")
                    }
                }
            }
        }
    }

    fun registerReceiver() {
        val filter = IntentFilter()
        filter.addAction(ACTION_INTERNET_CONNECTION_STATE_CHANGED)
        activity.registerReceiver(networkChangeReceiver, filter)
    }

    fun unregister() {
        activity.unregisterReceiver(networkChangeReceiver)
    }

    fun createPhotoFile() = File(
        outputDirectory,
        SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis()) + ".jpg"
    )

    fun savePhoto(uri: Uri, location: Location?) {

        val photo: Photo = if (location != null) {
            Photo(uri.toString(), false, location.latitude, location.longitude)
        } else {
            Photo(uri.toString(), false, 0.0, 0.0)
        }

        savePhotoInBase(photo)

        if (hasConnection()) {
            sendPhotoToServer(photo)
        }
    }

    fun getLiveDataPhotoList() = photoViewModel.photoList

    fun sendNewPhotoListToServer() {
        photoViewModel.photoList.observe(activity, { photos ->
            photos?.let {
                val unsentPhotos = it.stream()
                    .filter { photo -> !photo.sent }
                    .collect(Collectors.toList())

                if (unsentPhotos.isNotEmpty()) {
                    uploadController.sendNewPhotoList(unsentPhotos, photoCallback)
                }
            }
        })
    }

    fun deletePhotos(filter: Filter) {
        val photoList = photoViewModel.photoList.value

        when (filter) {
            Filter.ALL -> photoViewModel.deleteAllPhotos()
            Filter.SENT -> photoViewModel.deleteSentPhotos()
        }

        if (photoList != null) {
            for (photo in photoList) {

                when (filter) {
                    Filter.ALL -> deleteFile(photo.photoUri.toUri().toFile())
                    Filter.SENT -> if (photo.sent) deleteFile(photo.photoUri.toUri().toFile())
                }
            }
        }
    }

    private fun deleteFile(file: File) {
        file.delete()
        if (file.exists()) {
            file.canonicalFile.delete()
            if (file.exists()) {
                activity.applicationContext.deleteFile(file.name)
            }
        }
    }

    private fun sendPhotoToServer(photo: Photo) {
        uploadController.sendPhoto(photo, photoCallback)
    }

    private fun savePhotoInBase(photo: Photo) {
        photoViewModel.insert(photo)
    }

    private fun hasConnection(): Boolean {
        val connectivityManager =
            activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        connectivityManager?.let {

            val capabilities =
                it.getNetworkCapabilities(it.activeNetwork)

            capabilities?.let { cap ->
                return when {
                    cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            cap.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            }
        }

        return false
    }

    private fun getOutputDirectory(): File {
        val mediaDir = activity.externalMediaDirs.firstOrNull()?.let {
            File(it, "photo").apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else activity.filesDir
    }
}