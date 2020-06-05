package com.todaystudio.soha

import StorageUtil
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.chip.Chip
import com.todaystudio.soha.data.AppInfo
import com.todaystudio.soha.model.AppInfoViewModel
import com.todaystudio.soha.ui.adapter.GridSpacingItemDecoration
import com.todaystudio.soha.ui.adapter.InstalledAppAdapter
import com.todaystudio.soha.ui.adapter.InstalledAppAdapter.FilterMode
import com.todaystudio.soha.utils.AppInfoUtil
import com.todaystudio.soha.utils.DataUtil
import com.todaystudio.soha.utils.ViewUtil.dpToPx
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    private var appListRecyclerView: RecyclerView? = null
    private var appAdapter = InstalledAppAdapter()

    private val appInfoViewModel = AppInfoViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpViews()
        setUpAppData()
    }

    override fun onStart() {
        super.onStart()
        setUpAppList()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setUpAppList()
        // workaround for views being cut off issue
        appListRecyclerView?.run {
            setPadding(paddingLeft, paddingTop, paddingRight, 0)
        }
    }

    private fun setUpViews() {
        setUpToolbarTitle()
        setUpSearchBox()
        setUpFilterChips()

        setUpAppsListView()
    }

    private fun setUpAppData() {
        setUpAppList()

        appInfoViewModel.getApps().observe(this, Observer { apps ->
            showAppList(apps)
        })
    }

    private fun setUpAppList() {
        if (AppInfoUtil.needUsageStatsPermission(this)) {
            setUpRequireAccessLayout()
        } else {
            startService()
            acquireAppList()
        }
    }

    private fun setUpRequireAccessLayout() {
        val reqPermissionLayout = findViewById<LinearLayout>(R.id.ll_request_permission)
        appListRecyclerView?.visibility = View.INVISIBLE
        reqPermissionLayout.visibility = View.VISIBLE

        findViewById<Button>(R.id.btn_grant).setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            // periodically check if access is granted
            val handler = Handler()
            (object : Runnable {
                override fun run() {
                    if (!AppInfoUtil.needUsageStatsPermission(this@MainActivity)) {
                        // usage access is granted
                        reqPermissionLayout.visibility = View.GONE
                        startActivity(intent)
                        return
                    }
                    handler.postDelayed(this, 1000)
                }
            }).run()
        }
    }

    private fun acquireAppList() {
        StorageUtil.init(this)
        // retrieve installed applications
        AppInfoUtil.getInstalledApps(this)?.run {
            updateAppList(this)
        }
    }

    private fun updateAppList(apps: MutableList<AppInfo>) {
        appListRecyclerView?.visibility = View.INVISIBLE
        appInfoViewModel.saveApps(apps)
    }

    private fun showAppList(apps: List<AppInfo>) {
        appAdapter.apps = apps
        appListRecyclerView?.visibility = View.VISIBLE
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
