package com.jalexy.photoroad.room

import android.util.Log
import androidx.lifecycle.LiveData

class PhotoRepository(private val photoDao: PhotoDao) {

    val photoList: LiveData<List<Photo>> = photoDao.getAllPhotos()

    suspend fun insert(photo: Photo) = photoDao.insert(photo)

    suspend fun updateSent(photo: Photo) {
        val sent = if (photo.sent) 1 else 0
        photoDao.updateSent(photo.photoUri, sent)

        Log.i("Test", "обновляю ${photo.photoUri} sent = $sent")
    }

    suspend fun deleteAll() = photoDao.deleteAll()

    suspend fun deleteSentPhotos() = photoDao.deleteSentPhotos()
}