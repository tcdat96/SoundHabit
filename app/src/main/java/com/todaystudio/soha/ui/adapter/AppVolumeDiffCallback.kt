package com.todaystudio.soha.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.todaystudio.soha.data.AppVolume

class AppVolumeDiffCallback(private val oldList: List<AppVolume>, private val newList: List<AppVolume>)
    : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].packageName == newList[newItemPosition].packageName
    }

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}