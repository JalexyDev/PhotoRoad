package com.jalexy.photoroad.controllers

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaCodec
import android.util.Log
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import com.jalexy.photoroad.PhotoCallback
import com.jalexy.photoroad.room.Photo
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.util.*
import java.util.concurrent.LinkedTransferQueue
import kotlin.collections.HashSet

class UploadController(val context: Context) {

    private val queue: Queue<Photo> by lazy { LinkedTransferQueue() }

    //эта дичь не дает повторно отправлять одни и те же фотки
    private val sessionSentPhotoSet: HashSet<String> by lazy { HashSet() }
    private var busy: Boolean = false
    private var callback: PhotoCallback? = null

    private val client: OkHttpClient by lazy { OkHttpClient() }
    private val headers: Headers by lazy {
        Headers.Builder()
            .add("Content-Type", "multipart/form-data")
            .add("User-Agent", "androidApp")
            .add("X-Device-Guid", getGuid())
            .build()
    }

    private val sharedPreferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(
            context
        )
    }

    // добавляем фотки в очередь и если загрузчик свободен, стартуем выгрузку первой фотки.
    // после чего колбкек вызовет выгрузку следующей и так, пока очередь не опустеет.
    fun sendNewPhotoList(photoList: List<Photo>, callback: PhotoCallback) {

        for (photo in photoList) {
            if (photo.sent) {
                continue
            }

            if (!queue.contains(photo)) {
                queue.add(photo)
            }
        }

        if (!busy) {
            queue.poll()?.let {
                uploadPhoto(it, callback)
            }
        }
    }

    // добавляем фотку в очередь. Если там только одна фотка и загрузчик свободен, сразу грузим
    // ее на сервер. В противном случае ждем пока колбек вызовет sendNextPhotoInQueue
    fun sendPhoto(photo: Photo, callback: PhotoCallback) {
        if (!queue.contains(photo)) {
            queue.add(photo)
        }

        if (queue.size == 1 && !busy) {
            queue.poll()?.let {
                if (!photo.sent) {
                    uploadPhoto(it, callback)
                }
            }
        }
    }

    fun sendNextPhotoInQueue() {
        busy = false

        val nextPhoto = queue.poll()

        if (nextPhoto == null) {
            sessionSentPhotoSet.clear()
        } else {
            nextPhoto?.let {
                if (!it.sent && !busy) {
                    uploadPhoto(it, callback!!)
                }
            }
        }
    }

    fun notifyConnectionLost() {
        busy = false
    }

    private fun uploadPhoto(photo: Photo, callback: PhotoCallback) {
        busy = true

        this.callback = callback

        try {
            val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart(
                    "image",
                    photo.photoUri.substringAfterLast("/"),
                    photo.photoUri.toUri().toFile().asRequestBody(MEDIA_TYPE_JPG)
                )
                .addFormDataPart("latitude", photo.latitude.toString())
                .addFormDataPart("longitude", photo.longitude.toString())
                .build()

            val request = Request.Builder()
                .headers(headers)
                .url(BASE_URL)
                .post(requestBody)
                .build()

            callback.photo = photo

            if (sessionSentPhotoSet.contains(photo.photoUri)) {
                busy = false
                return
            }

            sessionSentPhotoSet.add(photo.photoUri)
            client.newCall(request).enqueue(callback)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun getGuid(): String {
        var guid = sharedPreferences.getString(GUID, "") ?: ""

        if (guid.isBlank()) {
            guid = getRandomString(48)
            sharedPreferences.edit().putString(GUID, guid).apply()
        }

        return guid
    }

    private fun getRandomString(length: Int): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..length)
            .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString { "" }
    }

    companion object {
        private const val BASE_URL = "http://46.101.238.176/api/upload"
        private val MEDIA_TYPE_JPG = "image/jpg".toMediaType()

        private const val GUID = "guid"
    }
}