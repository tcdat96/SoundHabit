package com.example.soundhabit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.soundhabit.data.AppInfo

class InstalledAppAdapter(val apps: List<AppInfo>) : RecyclerView.Adapter<InstalledAppAdapter.AppItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.install_app_item, parent, false)
        return AppItemViewHolder(view)
    }

    override fun getItemCount() = apps.size

    override fun onBindViewHolder(holder: AppItemViewHolder, position: Int) {
        val app = apps[position]
        holder.bind(app)
    }

    inner class AppItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var nameTextView : TextView? = null
        var iconImageView: ImageView? = null

        init {
            nameTextView = itemView.findViewById(R.id.tv_package_name)
            iconImageView = itemView.findViewById(R.id.iv_app_icon)
        }

        fun bind(appInfo: AppInfo) {
            nameTextView?.text = appInfo.name
            iconImageView?.setImageDrawable(appInfo.icon)
        }
    }
}