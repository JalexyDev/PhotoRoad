package com.jalexy.photoroad.controllers

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import com.jalexy.photoroad.room.Photo
import com.jalexy.photoroad.room.PhotoViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class PhotoFileController(private val activity: AppCompatActivity) {

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    private val photoViewModel: PhotoViewModel by lazy {
        ViewModelProvider(activity).get(PhotoViewModel::class.java) }

    private var outputDirectory = getOutputDirectory()

    fun createPhotoFile() = File(
        outputDirectory,
        SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis()) + ".jpg"
    )

    fun savePhoto(uri: Uri) {
        savePhotoInBase(uri)

        if (canSend()) {
            sendPhotoToServer(uri)
        }
    }

    fun sendNewPhotoListToServer() {
        val newPhotos = getNewPhotoList()
        //todo логика отправки списка фоток на сервер
    }

    private fun sendPhotoToServer(photoUri: Uri) {
        //todo отправка одного файла с фоткой на сервер

        val inputStream = activity.contentResolver.openInputStream(photoUri)
        inputStream?.use{input ->

            //todo тут вместо FileOutputStream использовать запись на сервер, напр через сокеты???
            //todo а еще лучше см в еде отсылка фоток к отзывам
            val out = FileOutputStream(File(photoUri.path))
            val buf = ByteArray(1024)
            var len: Int
            while (input.read(buf).also { len = it } > 0) {
                out.write(buf, 0, len)
            }

            //todo после удачной отправки вставить изменить создать фотку с таким же uri
            // и статусом sent = true вставить в базу (чтобы заменить)
            out.close()
        }
    }

    private fun savePhotoInBase(uri: Uri) {
        val photo = Photo(uri.toString(), false)
        photoViewModel.insert(photo)
    }

    private fun canSend(): Boolean {
        //todo если инет позволяет, шлем на сервер
        return false
    }

    private fun getNewPhotoList(): LiveData<List<Uri>> {
        return photoViewModel.getUnsentPhotoList().map {
            it.map { photo ->
                photo.photoUri.toUri()
            }
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = activity.externalMediaDirs.firstOrNull()?.let {
            File(it, "photo").apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir
        else
            activity.filesDir
    }

}