package com.todaystudio.soha.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.todaystudio.soha.data.AppInfo

class AppDiffCallback(private val oldList: List<AppInfo>, private val newList: List<AppInfo>)
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