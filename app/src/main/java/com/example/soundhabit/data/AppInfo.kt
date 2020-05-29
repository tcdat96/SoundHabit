package com.example.soundhabit.data

import android.graphics.drawable.Drawable

data class AppInfo(val packageName: String, val name: String, val icon: Drawable) {
    var enabled = false
    var volumes = arrayListOf<Int>()
}