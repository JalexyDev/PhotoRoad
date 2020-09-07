package com.jalexy.photoroad.activities

import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jalexy.photoroad.R
import com.jalexy.photoroad.adapters.PhotoListAdapter
import com.jalexy.photoroad.room.PhotoViewModel
import kotlinx.android.synthetic.main.activity_photo_list.*

class PhotoListActivity : AppCompatActivity() {
    private lateinit var photoViewModel: PhotoViewModel
    private lateinit var photoListRecycler: RecyclerView
    private lateinit var adapter: PhotoListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_list)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        photoListRecycler = photo_list
        adapter = PhotoListAdapter(this)
        photoListRecycler.adapter = adapter
        photoListRecycler.layoutManager = LinearLayoutManager(this)

        photoViewModel = ViewModelProvider(this, ).get(PhotoViewModel::class.java)
        photoViewModel.photoList.observe(this, Observer { photos ->
            photos?.let { adapter.setPhotos(it)}
        })
    }
}