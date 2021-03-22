package com.jalexy.photoroad.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jalexy.photoroad.R
import com.jalexy.photoroad.adapters.PhotoListAdapter
import com.jalexy.photoroad.controllers.PhotoFileController
import kotlinx.android.synthetic.main.activity_photo_list.*

class PhotoListActivity : AppCompatActivity() {
    private lateinit var photoListRecycler: RecyclerView
    private lateinit var adapter: PhotoListAdapter

    private val photoFileController: PhotoFileController by lazy {
        PhotoFileController(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_list)

        title = "Фотографии"

        photoListRecycler = photo_list
        adapter = PhotoListAdapter(this)
        photoListRecycler.adapter = adapter
        photoListRecycler.layoutManager = LinearLayoutManager(this)

        photoFileController.getLiveDataPhotoList().observe(this, Observer { photos ->
            photos?.let { adapter.setPhotos(it) }
        })
    }

    override fun onStart() {
        super.onStart()
        photoFileController.registerReceiver()
    }

    override fun onStop() {
        super.onStop()
        photoFileController.unregister()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.photo_list_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.send_new -> sendAllNewPhotos()

            R.id.delete_sent -> photoFileController.deletePhotos(PhotoFileController.Filter.SENT)

            R.id.delete_all -> photoFileController.deletePhotos(PhotoFileController.Filter.ALL)
        }

        return true
    }

    private fun sendAllNewPhotos() {
        photoFileController.sendNewPhotoListToServer()
    }
}