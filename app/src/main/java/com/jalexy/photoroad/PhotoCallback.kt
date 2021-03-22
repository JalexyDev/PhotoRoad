package com.jalexy.photoroad

import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jalexy.photoroad.room.Photo
import okhttp3.Call
import okhttp3.Callback
import java.io.IOException

abstract class PhotoCallback(private val activity: AppCompatActivity) : Callback {

    var photo: Photo? = null

    override fun onFailure(call: Call, e: IOException) {
        Log.e(TAG, e.message ?: "Не получилось отправить фоточку: ${photo?.photoUri}")

        activity.runOnUiThread {
            Toast.makeText(
                activity,
                "Не удалось отправить: ${e.message}", Toast.LENGTH_SHORT
            ).show()
        }
    }

    companion object{
        private const val TAG = "PhotoCallback"
    }
}