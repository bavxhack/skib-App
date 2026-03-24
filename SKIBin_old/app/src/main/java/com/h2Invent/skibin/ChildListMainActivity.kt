package com.h2Invent.skibin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.android.material.tabs.TabLayout

class ChildListMainActivity : AppCompatActivity(),
    FragmentChildListAfterList.OnItemSelectedListener,
    FragmentChildListCheckin.OnItemSelectedListener {

    private lateinit var childListContainer: LinearLayout
    private lateinit var childListAdapter: TabChildListAdapter
    private lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fresco.initialize(this)
        setContentView(R.layout.activity_child_list_main)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar_main))

        childListContainer = findViewById(R.id.childListContainer)
        viewPager = findViewById(R.id.pager)
        childListAdapter = TabChildListAdapter(supportFragmentManager)
        viewPager.adapter = childListAdapter
        findViewById<TabLayout>(R.id.tabChildList).setupWithViewPager(viewPager)
    }

    override fun onResume() {
        super.onResume()
        initApp()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_get_data -> {
            initApp()
            true
        }
        R.id.action_settings -> {
            startActivity(Intent(this, TabSettingsActivity::class.java))
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    fun initApp() {
        val sharedPreferences = getSharedPreferences(SaveSettings.SHARED_PREFS, MODE_PRIVATE)
        childListContainer.visibility = View.VISIBLE

        val childListUrl = sharedPreferences.getString(SaveSettings.USER_KINDERLISTE_URL, "").orEmpty()
        val checkinUrl = sharedPreferences.getString(SaveSettings.USER_CHECKIN_URL, "").orEmpty()
        val userToken = sharedPreferences.getString(SaveSettings.USER_TOKEN, "").orEmpty()

        childListAdapter.checkinFragment.setConfig(checkinUrl, userToken)
        childListAdapter.afterListFragment.setConfig(childListUrl, userToken)
    }

    companion object {
        const val EXTRA_USER_TOKEN = "extra_user_token"
    }
}
