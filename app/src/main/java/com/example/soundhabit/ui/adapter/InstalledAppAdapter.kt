package com.example.soundhabit.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.soundhabit.R
import com.example.soundhabit.data.AppInfo

class InstalledAppAdapter(private val apps: List<AppInfo>) :
    RecyclerView.Adapter<InstalledAppAdapter.AppItemViewHolder>() {

    private val shownApps = ArrayList(apps)

    private val onItemClickListener = object :
        OnItemClickListener {
        override fun onClick(view: View, position: Int) {
            shownApps[position].enabled = !shownApps[position].enabled
            notifyItemChanged(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppItemViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.install_app_item, parent, false)
        return AppItemViewHolder(view, onItemClickListener)
    }

    override fun getItemCount() = shownApps.size

    override fun onBindViewHolder(holder: AppItemViewHolder, position: Int) {
        val app = shownApps[position]
        holder.bind(app)
    }

    fun filter(query: String) {
        val filteredList = apps.filter { it.name.contains(query, true) }
        filter(filteredList)
    }

    private fun filter(newList: List<AppInfo>) {
        val diffCallback = AppDiffCallback(shownApps, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        shownApps.clear()
        shownApps.addAll(newList)

        diffResult.dispatchUpdatesTo(this)
    }

    enum class FilterMode { SHOW_ALL, SHOW_ENABLED, SHOW_DISABLED}

    interface OnItemClickListener {
        fun onClick(view: View, position: Int)
    }

    inner class AppItemViewHolder(itemView: View, private val listener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private var nameTextView: TextView = itemView.findViewById(R.id.tv_package_name)
        private var iconImageView: ImageView = itemView.findViewById(R.id.iv_app_icon)
        private var container = nameTextView.parent as? ConstraintLayout

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(appInfo: AppInfo) {
            nameTextView.text = appInfo.name
            iconImageView.setImageDrawable(appInfo.icon)

            container?.run {
                val backgroundColor = ContextCompat.getColor(itemView.context,
                    if (appInfo.enabled) R.color.appEnabledBackground else R.color.appDisabledBackground
                )
                setBackgroundColor(backgroundColor)
            }
        }

        override fun onClick(v: View?) {
            if (v != null && adapterPosition != RecyclerView.NO_POSITION) {
                listener.onClick(v, adapterPosition)
            }
        }
    }
}