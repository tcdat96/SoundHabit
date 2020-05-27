package com.example.soundhabit.data

import android.graphics.drawable.Drawable

data class AppInfo(val packageName: String, val name: String, val icon: Drawable) {
    var enabled = false

    override fun equals(other: Any?): Boolean {
        return (other as? AppInfo)?.let {
            this.packageName == it.packageName && this.name == it.name
                    && this.enabled == it.enabled
        } ?: false
    }
}