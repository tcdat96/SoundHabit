package com.example.soundhabit

import StorageUtil
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Adapter
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.soundhabit.ui.adapter.GridSpacingItemDecoration
import com.example.soundhabit.ui.adapter.InstalledAppAdapter
import com.example.soundhabit.utils.AppInfoUtil
import com.example.soundhabit.utils.ViewUtil.dpToPx
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    private var appListRecyclerView: RecyclerView? = null
    private var appAdapter: InstalledAppAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpToolbarTitle()
        setUpSearchBox()

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

    private fun setUpToolbarTitle() {
        val collapsingToolbarLayout = findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)
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
                appAdapter?.filter(query)
                state?.run { layoutManager.onRestoreInstanceState(state) }
            }
        }
    }

    private fun initInstalledAppsList() {
        findViewById<RecyclerView>(R.id.rv_installed_apps).apply {
            val spanCount = 2
            layoutManager = GridLayoutManager(context, spanCount)
            itemAnimator = DefaultItemAnimator()
            adapter = appAdapter
            addItemDecoration(GridSpacingItemDecoration(spanCount, dpToPx(context, 16f)))
            setHasFixedSize(true)
        }
    }

    private fun startService() {
        Intent(this@MainActivity, ScanForegroundService::class.java).also { intent ->
//            ContextCompat.startForegroundService(this@MainActivity, intent)
        }
    }
}
