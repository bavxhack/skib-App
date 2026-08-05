package com.h2Invent.skibin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    private lateinit var buttonNoSetting: MaterialButton

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fresco.initialize(this)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar_main))

        buttonNoSetting = findViewById(R.id.buttonNoSettingOrganisation)
        buttonNoSetting.setOnClickListener {
            startActivity(Intent(this, TabSettingsActivity::class.java))
        }

        requestRuntimePermissions()
        initApp()
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
        when {
            sharedPreferences.getBoolean(SaveSettings.IS_ORG, false) -> {
                buttonNoSetting.visibility = View.GONE
                startActivity(Intent(this, OrgMainActivity::class.java))
            }
            sharedPreferences.getBoolean(SaveSettings.IS_USER, false) -> {
                buttonNoSetting.visibility = View.GONE
                startActivity(Intent(this, ChildListMainActivity::class.java))
            }
            else -> buttonNoSetting.visibility = View.VISIBLE
        }
    }

    private fun requestRuntimePermissions() {
        val permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.CALL_PHONE,
        ).filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissions.isNotEmpty()) {
            permissionsLauncher.launch(permissions.toTypedArray())
        }
    }
}
