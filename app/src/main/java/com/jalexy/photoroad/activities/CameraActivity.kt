package com.jalexy.photoroad.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jalexy.photoroad.R
import com.jalexy.photoroad.controllers.CameraController
import com.jalexy.photoroad.controllers.GpsController
import com.jalexy.photoroad.utilites.OrientationManager
import com.jalexy.photoroad.utilites.OrientationManager.*
import kotlinx.android.synthetic.main.activity_camera.*

class CameraActivity : AppCompatActivity(), OrientationManager.OrientationListener {

    private lateinit var intervalText: AppCompatEditText
    private lateinit var photoBtn: ImageButton
    private lateinit var galleryBtn: ImageButton
    private lateinit var mapBtn: ImageButton
    private lateinit var photoCountLayout: CardView
    private lateinit var photoCountText: TextView

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var orientationManager: OrientationManager
    private lateinit var cameraController: CameraController
    private lateinit var gpsController: GpsController

    private var sessionPhotoCount = 0

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                cameraController.startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Пользователь не дал нужного разрешения",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        supportActionBar?.hide()

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        sharedPreferences = getPreferences(Context.MODE_PRIVATE)

        gpsController = GpsController(this, object : GpsController.OnTrack {
            override fun onUpdate() {
                cameraController.takePhotoInLocation(gpsController.lastLocation)
                sessionPhotoCount++
                photoCountText.text = sessionPhotoCount.toString()
            }

            override fun onRefresh() {
                sessionPhotoCount = 0
                photoCountText.text = sessionPhotoCount.toString()
            }
        })
        gpsController.callbackInterval = sharedPreferences.getInt(PHOTO_INTERVAL, 100)

        orientationManager = OrientationManager(this, SensorManager.SENSOR_DELAY_GAME, this)
        orientationManager.enable()

        cameraController = CameraController(this, view_finder)

        if (allPermissionsGranted()) {
            cameraController.startCamera()

        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        intervalText = interval_value
        intervalText.setText(gpsController.callbackInterval.toString())

        intervalText.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN &&
                keyCode == KeyEvent.KEYCODE_ENTER
            ) {
                val meters = intervalText.text

                if (meters.isNullOrEmpty() || meters.contains("[^0-9]") || meters.toString()
                        .toInt() < 0
                ) {
                    intervalText.setText("0")
                    intervalText.selectAll()
                    saveInterval(0)
                } else {
                    gpsController.callbackInterval = meters.toString().toInt()
                    saveInterval(gpsController.callbackInterval)
                }

                Handler(Looper.getMainLooper())
                    .postDelayed({ intervalText.clearFocus() }, 100)

                return@OnKeyListener true
            }
            false
        })

        decrease_btn.setOnClickListener {
            gpsController.callbackInterval--

            if (gpsController.callbackInterval < 0) {
                gpsController.callbackInterval = 0
            }

            intervalText.setText(gpsController.callbackInterval.toString())
            saveInterval(gpsController.callbackInterval)
        }

        increase_btn.setOnClickListener {
            gpsController.callbackInterval++
            intervalText.setText(gpsController.callbackInterval.toString())
            saveInterval(gpsController.callbackInterval)
        }

        photoBtn = camera_capture_btn
        photoBtn.setOnClickListener {
            if (gpsController.locationUpdateState) {
                gpsController.locationUpdateState = false
                gpsController.removeLocationUpdates()

            } else {
                gpsController.startLocationUpdates()
            }

            setPhotoMode(gpsController.locationUpdateState)
        }

        galleryBtn = gallery_btn
        galleryBtn.setOnClickListener {
            val intent = Intent(baseContext, PhotoListActivity::class.java)
            startActivity(intent)
        }

        mapBtn = map_btn
        mapBtn.setOnClickListener {
            val intent = Intent(baseContext, MapsActivity::class.java)
            startActivity(intent)
        }

        photoCountLayout = photo_count_layout
        photoCountText = photo_count

        gpsController.initController()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                if (gpsController.locationUpdateState) {
                    gpsController.startLocationUpdates()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()

        cameraController.unregisterReceiver()
        gpsController.removeLocationUpdates()
    }

    override fun onResume() {
        super.onResume()

        cameraController.registerReceiver()

        setPhotoMode(gpsController.locationUpdateState)

        if (gpsController.locationUpdateState) {
            gpsController.startLocationUpdates()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        orientationManager.disable()

        gpsController.locationUpdateState = false
        cameraController.shutDown()
    }

    override fun onOrientationChange(screenOrientation: ScreenOrientation) {
        setOrientationMode(screenOrientation)
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun setPhotoMode(isPhotoMode: Boolean) {
        if (isPhotoMode) {
            mapBtn.visibility = View.GONE
            galleryBtn.visibility = View.GONE
            photoBtn.setImageResource(R.drawable.selector_stop)

            photoCountLayout.visibility = View.VISIBLE
        } else {
            mapBtn.visibility = View.VISIBLE
            galleryBtn.visibility = View.VISIBLE
            photoBtn.setImageResource(R.drawable.selector_start)

            photoCountLayout.visibility = View.GONE
        }
    }

    private fun saveInterval(interval: Int) {
        with(sharedPreferences.edit()) {
            putInt(PHOTO_INTERVAL, interval)
            commit()
        }
    }

    private fun setOrientationMode(screenOrientation: ScreenOrientation) {
        val rotation = when(screenOrientation) {
            ScreenOrientation.PORTRAIT, ScreenOrientation.REVERSED_PORTRAIT  ->  -90
            ScreenOrientation.LANDSCAPE -> 0
            ScreenOrientation.REVERSED_LANDSCAPE -> 180
        }

        mapBtn.rotation = rotation.toFloat()
        galleryBtn.rotation = rotation.toFloat()
    }

    companion object {
        const val PHOTO_INTERVAL = "PHOTO_INTERVAL"
        const val REQUEST_CHECK_SETTINGS = 2

        private const val REQUEST_CODE_PERMISSIONS = 10

        //todo добавить перпишины, если надо
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}