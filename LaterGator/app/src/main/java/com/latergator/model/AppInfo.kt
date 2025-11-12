package com.latergator.model

import android.graphics.drawable.Drawable

/**
 * A shared data class to hold information about an application installed on the device.
 */
data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable
)
