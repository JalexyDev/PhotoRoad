package com.jalexy.photoroad.utilites

import android.content.Context
import android.view.OrientationEventListener


class OrientationManager(context: Context, rate: Int, private val listener: OrientationListener) : OrientationEventListener(context, rate) {

    private var screenOrientation: ScreenOrientation? = null

    override fun onOrientationChanged(orientationValue: Int) {

        if (orientationValue == -1) {
            return
        }

        val newOrientation = when (orientationValue) {
            in 60..140 -> {
                ScreenOrientation.REVERSED_LANDSCAPE
            }
            in 140..220 -> {
                ScreenOrientation.REVERSED_PORTRAIT
            }
            in 220..300 -> {
                ScreenOrientation.LANDSCAPE
            }
            else -> {
                ScreenOrientation.PORTRAIT
            }
        }

        if (newOrientation != screenOrientation) {
            screenOrientation = newOrientation
            listener.onOrientationChange(screenOrientation ?: ScreenOrientation.PORTRAIT)
        }
    }

    enum class ScreenOrientation {
        REVERSED_LANDSCAPE, LANDSCAPE, PORTRAIT, REVERSED_PORTRAIT
    }

    interface OrientationListener {
        fun onOrientationChange(screenOrientation: ScreenOrientation)
    }
}