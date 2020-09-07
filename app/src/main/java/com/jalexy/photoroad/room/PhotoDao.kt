package com.jalexy.photoroad.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: Photo)

    //тк метод возвращает LiveData<T> он будет запущен в асинхронном потоке
    @Query("SELECT * FROM photos ORDER BY uri ASC")
    fun getAllPhotos(): LiveData<List<Photo>>

    //todo убедиться, что DESC - это нужная сортировка (иначе ASC)
    @Query("SELECT * FROM photos WHERE sent = 0 ORDER BY uri DESC")
    fun getUnsentPhotos(): LiveData<List<Photo>>

    @Query("DELETE FROM photos")
    suspend fun deleteAll()

    @Query("DELETE FROM photos WHERE sent = 1")
    suspend fun deleteSentPhotos()

    @Delete
    suspend fun deletePhoto(photo: Photo)
}