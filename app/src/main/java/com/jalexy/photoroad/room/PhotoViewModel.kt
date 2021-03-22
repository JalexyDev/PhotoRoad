package com.jalexy.photoroad.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PhotoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PhotoRepository
    val photoList: LiveData<List<Photo>>

    init {
        val photoDao = PhotoRoomDatabase.getDatabase(application, viewModelScope).photoDao()
        repository = PhotoRepository(photoDao)
        photoList = repository.photoList
    }

    fun insert(photo: Photo) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(photo)
    }

    fun updateSent(photo: Photo) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateSent(photo)
    }

    fun deleteAllPhotos() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAll()
    }

    fun deleteSentPhotos() = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteSentPhotos()
    }
}