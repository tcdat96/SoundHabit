package com.example.soundhabit

import StorageUtil
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.provider.Settings
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.soundhabit.ui.adapter.GridSpacingItemDecoration
import com.example.soundhabit.ui.adapter.InstalledAppAdapter
import com.example.soundhabit.utils.AppInfoUtil
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    private var appAdapter: InstalledAppAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupToolbarTitle()

        // retrieve installed applications
        AppInfoUtil.getInstalledApps(this)?.run {
            forEach { StorageUtil.savePackage(it.name) }
            appAdapter =
                InstalledAppAdapter(this)
        }

        initInstalledAppsList()
    }

    override fun onStart() {
        super.onStart()
        if (AppInfoUtil.needUsageStatsPermission(this)) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        } else {
            startService()
        }
    }

    private fun setupToolbarTitle() {
        val collapsingToolbarLayout = findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)
        val searchArea = collapsingToolbarLayout.children.iterator().next()

        var isShown = true
        var scrollRange = -1
        appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (scrollRange == -1) {
                scrollRange = appBarLayout.totalScrollRange!!
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

    private fun initInstalledAppsList() {
        val appsRv = findViewById<RecyclerView>(R.id.rv_installed_apps).apply {
            layoutManager = GridLayoutManager(context, 2)
            itemAnimator = DefaultItemAnimator()
            adapter = appAdapter
            addItemDecoration(GridSpacingItemDecoration(2, dpToPx(16f)))
            setHasFixedSize(true)
        }
    }

    private fun startService() {
        Intent(this@MainActivity, ScanForegroundService::class.java).also { intent ->
//            ContextCompat.startForegroundService(this@MainActivity, intent)
        }
    }

    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
            .roundToInt()
    }
}
