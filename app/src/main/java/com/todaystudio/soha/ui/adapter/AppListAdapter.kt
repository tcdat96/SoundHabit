package com.todaystudio.soha.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.todaystudio.soha.R
import com.todaystudio.soha.data.entity.AppVolume

class AppListAdapter :
    RecyclerView.Adapter<AppListAdapter.AppItemViewHolder>() {

    var apps: List<AppVolume> = listOf()
        set(value) {
            field = value
            filter()
        }

    private val shownApps = ArrayList(apps)
    private var currFilterMode = FilterMode.SHOW_ALL
    private var currQuery = ""

    var onItemChangeListener: OnItemChangeListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.install_app_item, parent, false)
        return AppItemViewHolder(view)
    }

    override fun getItemCount() = shownApps.size

    override fun onBindViewHolder(holder: AppItemViewHolder, position: Int) {
        val app = shownApps[position]
        holder.bind(app)
    }

    fun onItemClick(position: Int) {
        val app = shownApps[position]
        app.enabled = !app.enabled
        notifyItemChanged(position)
        onItemChangeListener?.onChange(shownApps[position])
    }

    fun filter(query: String = currQuery, filterMode: FilterMode = currFilterMode) {
        currFilterMode = filterMode
        currQuery = query

        when (filterMode) {
            FilterMode.SHOW_ALL -> apps
            FilterMode.SHOW_ENABLED -> apps.filter { it.enabled }
            FilterMode.SHOW_DISABLED -> apps.filter { !it.enabled }
        }
            .filter { it.name.contains(query, true) }
            .run { filter(this) }
    }

    private fun filter(newList: List<AppVolume>) {
        val diffCallback = AppVolumeDiffCallback(shownApps, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        shownApps.clear()
        shownApps.addAll(newList)

        diffResult.dispatchUpdatesTo(this)
    }

    enum class FilterMode { SHOW_ALL, SHOW_ENABLED, SHOW_DISABLED }

    interface OnItemChangeListener {
        fun onChange(item: AppVolume)
    }

    inner class AppItemViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val nameTextView: TextView = itemView.findViewById(R.id.tv_package_name)
        private val iconImageView: ImageView = itemView.findViewById(R.id.iv_app_icon)
        private val speakerTextView: TextView = itemView.findViewById(R.id.tv_speaker_volume)
        private val wiredTextView: TextView = itemView.findViewById(R.id.tv_wired_volume)
        private val bluetoothTextView: TextView = itemView.findViewById(R.id.tv_bluetooth_volume)
        private var container = nameTextView.parent as? ConstraintLayout

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(app: AppVolume) {
            nameTextView.text = app.name

            app.icon?.run { iconImageView.setImageDrawable(this) }

            container?.run {
                val bgrColor = ContextCompat.getColor(itemView.context,
                    if (app.enabled) R.color.appEnabledBackground else R.color.appDisabledBackground
                )
                setBackgroundColor(bgrColor)
            }

            speakerTextView.text = if (app.speakerVolume >= 0) app.speakerVolume.toString() else "-"
            wiredTextView.text = if (app.wiredVolume >= 0) app.wiredVolume.toString() else "-"
            bluetoothTextView.text = if (app.bleVolume >= 0) app.bleVolume.toString() else "-"
        }

        override fun onClick(v: View?) {
            if (v != null && adapterPosition != RecyclerView.NO_POSITION) {
                onItemClick(adapterPosition)
            }
        }
    }
}