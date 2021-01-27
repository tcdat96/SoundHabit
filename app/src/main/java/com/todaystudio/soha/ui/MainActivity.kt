package com.todaystudio.soha.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.todaystudio.soha.R
import com.todaystudio.soha.services.ScanForegroundService
import com.todaystudio.soha.data.AppVolumeRepository
import com.todaystudio.soha.data.db.AppVolumeDatabase
import com.todaystudio.soha.data.entity.AppVolume
import com.todaystudio.soha.data.util.AppVolumeServiceImpl
import com.todaystudio.soha.viewmodels.AppVolumeViewModel
import com.todaystudio.soha.ui.adapter.AppListAdapter
import com.todaystudio.soha.ui.adapter.AppListAdapter.FilterMode
import com.todaystudio.soha.ui.adapter.GridSpacingItemDecoration
import com.todaystudio.soha.utils.DataUtil
import com.todaystudio.soha.utils.ViewUtil.dpToPx
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    companion object {
        @Suppress("unused")
        const val TAG = "MainActivity"
    }

    private var appAdapter = AppListAdapter()

    private var appListRecyclerView: RecyclerView? = null
    private var usageAccessPermLayout: LinearLayout? = null

    private lateinit var appVolumeService: AppVolumeServiceImpl
    private lateinit var appVolumeRepository: AppVolumeRepository
    private lateinit var appVolumeViewModel: AppVolumeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpViews()

        val database = AppVolumeDatabase.getInstance(applicationContext)
        appVolumeService = AppVolumeServiceImpl(applicationContext)
        appVolumeRepository = AppVolumeRepository.getInstance(database.appVolumeDao).apply {
            init(Executors.newFixedThreadPool(4), appVolumeService)
        }

        appVolumeViewModel = AppVolumeViewModel(appVolumeRepository).apply {
            apps.observe(this@MainActivity, Observer { apps ->
                // update current app list
                appAdapter.apps = apps
            })
            hasUsageAccess.observe(this@MainActivity, Observer { enabled ->
                appListRecyclerView?.visibility = if (enabled) View.VISIBLE else View.GONE
                usageAccessPermLayout?.visibility = if (enabled) View.GONE else View.VISIBLE
            })
        }

        setUpAppList()
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

    private fun setUpAppList() {
        if (appVolumeService.needUsageStatsPermission(this)) {
            appVolumeViewModel.hasUsageAccess.postValue(false)
            setUpRequireAccessLayout()
        } else {
            appVolumeViewModel.hasUsageAccess.postValue(true)
            startService()
        }
    }

    private fun setUpRequireAccessLayout() {
        usageAccessPermLayout = findViewById<LinearLayout>(R.id.ll_request_permission).also {
            findViewById<Button>(R.id.btn_grant).setOnClickListener {
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                // periodically check if access is granted
                val handler = Handler(Looper.getMainLooper())
                (object : Runnable {
                    override fun run() {
                        if (!appVolumeService.needUsageStatsPermission(this@MainActivity)) {
                            // usage access is granted
                            startActivity(intent)
                            appVolumeViewModel.hasUsageAccess.postValue(true)
                            return
                        }
                        handler.postDelayed(this, 1000)
                    }
                }).run()
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
        appAdapter.onItemChangeListener = object : AppListAdapter.OnItemChangeListener {
            override fun onChange(item: AppVolume) {
                appVolumeViewModel.saveVolume(item.packageName, item.enabled)
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
