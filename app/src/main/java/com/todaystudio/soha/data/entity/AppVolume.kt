package com.todaystudio.soha.data.entity

import android.graphics.drawable.Drawable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "app_volume")
data class AppVolume(
    @PrimaryKey @ColumnInfo(name = "pkg_name") val packageName: String
) {
    @ColumnInfo(name = "app_name")
    var name = ""

    @Ignore
    var icon: Drawable? = null

    @ColumnInfo(name = "enabled")
    var enabled = false

    @ColumnInfo(name = "speaker")
    var speakerVolume = -1

    @ColumnInfo(name = "wired")
    var wiredVolume = -1

    @ColumnInfo(name = "bluetooth")
    var bleVolume = -1
}