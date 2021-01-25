package com.todaystudio.soha.data

import android.graphics.drawable.Drawable

data class AppVolume(val packageName: String, val name: String, val icon: Drawable) {
    var enabled = false
    var values = arrayListOf<Int>()
}