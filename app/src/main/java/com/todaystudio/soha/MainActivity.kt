package com.todaystudio.soha

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.todaystudio.soha.data.AppInfo
import com.todaystudio.soha.ui.adapter.GridSpacingItemDecoration
import com.todaystudio.soha.ui.adapter.InstalledAppAdapter
import com.todaystudio.soha.ui.adapter.InstalledAppAdapter.FilterMode
import com.todaystudio.soha.utils.AppInfoUtil
import com.todaystudio.soha.utils.DataUtil
import com.todaystudio.soha.utils.ViewUtil.dpToPx
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.serialization.UnstableDefault


class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    private var appListRecyclerView: RecyclerView? = null
    private var appAdapter = InstalledAppAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpToolbarTitle()
        setUpSearchBox()
        setUpFilterChips()

        setUpAppsListView()
    }

    @UnstableDefault
    override fun onStart() {
        super.onStart()
        if (AppInfoUtil.needUsageStatsPermission(this)) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        } else {
            startService()
            StorageUtil.init(this)
            // retrieve installed applications
            AppInfoUtil.getInstalledApps(this)?.run {
                // save new package or get existing sound profile
                val iterator = listIterator()
                while (iterator.hasNext()) {
                    val app = iterator.next()
                    StorageUtil.savePackage(app.packageName)?.run {
                        app.enabled = enabled
                        app.volumes.addAll(volumes)
                        iterator.set(app)
                    }
                }
                // update the adapter
                appAdapter.apps = this
            }
        }
    }

    private fun setUpToolbarTitle() {
        findViewById<TextView>(R.id.tv_greeting).text = DataUtil.generateGreeting()

        val collapsingToolbarLayout = findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)
            .apply { setCollapsedTitleTextColor(Color.BLACK) }
        val searchArea = collapsingToolbarLayout.children.iterator().next()

        var isShown = true
        var scrollRange = -1
        appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (scrollRange == -1) {
                scrollRange = appBarLayout.totalScrollRange
            }
            if (scrollRange + verticalOffset == 0) {
                collapsingToolbarLayout.title = getString(R.string.app_name)
                toolbar.animation
                searchArea.visibility = View.INVISIBLE
                isShown = true
            } else if (isShown) {
                collapsingToolbarLayout.title = " "
                searchArea.visibility = View.VISIBLE
                isShown = false
            }
        })
    }

    private fun setUpSearchBox() {
        findViewById<EditText>(R.id.et_search_box).apply {
            doOnTextChanged { text, _, _, _ ->
                // preserve user's viewpoint
                val layoutManager = appListRecyclerView?.layoutManager
                val state = layoutManager?.onSaveInstanceState()
                // filter query
                val query = text.toString().trim()
                appAdapter.filter(query)
                state?.run { layoutManager.onRestoreInstanceState(state) }
            }

            // workaround for views being cut off issue
            setOnFocusChangeListener { _, _ ->
                appListRecyclerView?.run {
                    setPadding(paddingLeft, paddingTop, paddingRight, 0)
                }
                onFocusChangeListener = null
            }
        }
    }

    private fun setUpFilterChips() {
        val listener = View.OnClickListener {
            when (it.id) {
                R.id.chip_all_apps -> FilterMode.SHOW_ALL
                R.id.chip_enabled_apps -> FilterMode.SHOW_ENABLED
                R.id.chip_disabled_apps -> FilterMode.SHOW_DISABLED
                else -> FilterMode.SHOW_ALL
            }.let { mode ->
                appAdapter.filter(filterMode = mode)
            }
        }

        findViewById<Chip>(R.id.chip_all_apps).setOnClickListener(listener)
        findViewById<Chip>(R.id.chip_enabled_apps).setOnClickListener(listener)
        findViewById<Chip>(R.id.chip_disabled_apps).setOnClickListener(listener)
    }

    private fun setUpAppsListView() {
        appAdapter.onItemChangeListener = object : InstalledAppAdapter.OnItemChangeListener {
            override fun onChange(item: AppInfo) {
                StorageUtil.setPackageEnable(item.packageName, item.enabled)
            }
        }

        appListRecyclerView = findViewById<RecyclerView>(R.id.rv_installed_apps).apply {
            val spanCount = 2
            layoutManager = GridLayoutManager(context, spanCount)
            adapter = appAdapter
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(GridSpacingItemDecoration(spanCount, dpToPx(context, 16f)))
            setHasFixedSize(true)
        }
    }

    private fun startService() {
        Intent(this@MainActivity, ScanForegroundService::class.java).also { intent ->
            ContextCompat.startForegroundService(this@MainActivity, intent)
        }
    }
}
