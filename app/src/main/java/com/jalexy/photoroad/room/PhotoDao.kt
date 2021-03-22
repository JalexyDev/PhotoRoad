package com.jalexy.photoroad.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photo: Photo)

    @Query("UPDATE photos SET sent = :sent WHERE uri = :photoUri")
    suspend fun updateSent(photoUri: String, sent: Int)

    //тк метод возвращает LiveData<T> он будет запущен в асинхронном потоке
    @Query("SELECT * FROM photos ORDER BY uri DESC")
    fun getAllPhotos(): LiveData<List<Photo>>

    @Query("DELETE FROM photos")
    suspend fun deleteAll()

    @Query("DELETE FROM photos WHERE sent = 1")
    suspend fun deleteSentPhotos()
}