package com.jalexy.photoroad.room

import androidx.lifecycle.LiveData
import com.jalexy.photoroad.room.Photo
import com.jalexy.photoroad.room.PhotoDao

class PhotoRepository(private val photoDao: PhotoDao) {

    val photoList: LiveData<List<Photo>> = photoDao.getAllPhotos()

    suspend fun insert(photo: Photo) {
        photoDao.insert(photo)
    }

    fun getUnsentPhotos() = photoDao.getUnsentPhotos()

    suspend fun deleteAll() {
        photoDao.deleteAll()
    }

    suspend fun deleteSentPhotos() {
        photoDao.deleteSentPhotos()
    }

    suspend fun deletePhoto(photo:Photo) {
        photoDao.deletePhoto(photo)
    }
}