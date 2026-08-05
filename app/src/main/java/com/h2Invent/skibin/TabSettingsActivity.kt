package com.h2Invent.skibin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.tabs.TabLayout
import org.json.JSONObject

class TabSettingsActivity : AppCompatActivity(),
    FragmentConnection.OnItemSelectedListener,
    FragmentSettings.OnItemSelectedListener {

    private lateinit var viewPager: ViewPager
    private lateinit var viewPagerAdapter: ViewPageAdapter

    private var scannedJson: JSONObject? = null
    private var requestToken: String = ""
    private var confirmationUrl: String = ""
    private var saveUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tab_settings)
        viewPager = findViewById(R.id.pager)
        viewPagerAdapter = ViewPageAdapter(supportFragmentManager)
        viewPager.adapter = viewPagerAdapter
        findViewById<TabLayout>(R.id.tab).setupWithViewPager(viewPager)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_CODE_SCAN) return

        if (resultCode == RESULT_OK) {
            val result = data?.getStringExtra(ScanSettingsActivity.EXTRA_RESULT) ?: return
            val json = JSONObject(result)
            scannedJson = json
            when (json.optString("type")) {
                "ORGANISATION" -> handleOrganisationScan(json)
                "USER" -> handleUserScan(json)
            }
        } else {
            Toast.makeText(this, R.string.settingsFail, Toast.LENGTH_SHORT).show()
        }
    }

    override fun scanClicked() {
        startActivityForResult(Intent(this, ScanSettingsActivity::class.java), REQUEST_CODE_SCAN)
    }

    override fun userSaveClicked() {
        saveUserData(scannedJson ?: return)
    }

    override fun orgSaveClicked() {
        saveOrganisationData(scannedJson ?: return)
    }

    override fun resetClicked() {
        getSharedPreferences(SaveSettings.SHARED_PREFS, MODE_PRIVATE).edit().clear().apply()
        Toast.makeText(this, R.string.settingsDeleted, Toast.LENGTH_SHORT).show()
        loadData()
        finish()
    }

    override fun userEmailConfirmClicked(token: String) {
        receiveToken(token)
    }

    override fun init() {
        loadData()
    }

    private fun handleOrganisationScan(json: JSONObject) {
        viewPagerAdapter.connectionFragment.apply {
            setOrgText(json.optString("name"), json.optString("partner"))
            enableOrgSetting(true)
            showOrgInfo(true)
            showUserInfo(false)
            activateEmail(false)
            setScanText(getString(R.string.noSetting))
        }
    }

    private fun handleUserScan(json: JSONObject) {
        requestToken = json.optString("token")
        confirmationUrl = json.optString("url")
        viewPagerAdapter.connectionFragment.apply {
            activateEmail(true)
            enableScanButton(false)
            enableUserSetting(false)
            showUserInfo(false)
            showOrgInfo(false)
        }
    }

    private fun saveOrganisationData(response: JSONObject) {
        getSharedPreferences(SaveSettings.SHARED_PREFS, MODE_PRIVATE).edit().apply {
            clear()
            putBoolean(SaveSettings.IS_ORG, true)
            putString(SaveSettings.ORG_ANSPRECHPARTNER, response.optString("partner"))
            putString(SaveSettings.ORG_NAME, response.optString("name"))
            putInt(SaveSettings.ORG_ID, response.optInt("id"))
            putString(SaveSettings.ORG_URL, response.optString("url"))
            apply()
        }
        Toast.makeText(this, R.string.settingsSaved, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun saveUserData(response: JSONObject) {
        val info = response.optJSONObject("user")?.optJSONObject("info") ?: return
        val fullName = listOf(info.optString("firstName"), info.optString("lastName"))
            .filter { it.isNotBlank() }
            .joinToString(" ")

        getSharedPreferences(SaveSettings.SHARED_PREFS, MODE_PRIVATE).edit().apply {
            clear()
            putBoolean(SaveSettings.IS_USER, true)
            putString(SaveSettings.USER_NAME, fullName)
            putString(SaveSettings.USER_ORG, info.optString("organisation"))
            putString(SaveSettings.USER_TOKEN, response.optString("token"))
            putString(SaveSettings.USER_EMAIL, info.optString("email"))
            putString(SaveSettings.USER_CHECKIN_URL, info.optString("urlCheckinKids"))
            putString(SaveSettings.USER_KINDERLISTE_URL, info.optString("urlKinderListeHeute"))
            apply()
        }

        if (saveUrl.isBlank()) {
            Toast.makeText(this, R.string.settingsSaved, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val request = object : StringRequest(Method.POST, saveUrl,
            {
                Toast.makeText(this, R.string.settingsSaved, Toast.LENGTH_SHORT).show()
                finish()
            },
            { error -> Toast.makeText(this, error.localizedMessage ?: getString(R.string.settingsFail), Toast.LENGTH_LONG).show() }) {
            override fun getParams(): MutableMap<String, String> = mutableMapOf(
                "token" to getSharedPreferences(SaveSettings.SHARED_PREFS, MODE_PRIVATE)
                    .getString(SaveSettings.USER_TOKEN, "")
                    .orEmpty(),
            )
        }
        Volley.newRequestQueue(this).add(request)
    }

    private fun loadData() {
        val sharedPreferences = getSharedPreferences(SaveSettings.SHARED_PREFS, MODE_PRIVATE)
        val fragment = viewPagerAdapter.connectionFragment

        if (sharedPreferences.getBoolean(SaveSettings.IS_ORG, false)) {
            fragment.setOrgText(
                sharedPreferences.getString(SaveSettings.ORG_NAME, "").orEmpty(),
                sharedPreferences.getString(SaveSettings.ORG_ANSPRECHPARTNER, "").orEmpty(),
            )
            fragment.enableOrgSetting(true)
            fragment.showOrgInfo(true)
            fragment.showUserInfo(false)
        } else {
            fragment.showOrgInfo(false)
            fragment.enableOrgSetting(false)
        }

        if (sharedPreferences.getBoolean(SaveSettings.IS_USER, false)) {
            fragment.setUserText(
                sharedPreferences.getString(SaveSettings.USER_NAME, "").orEmpty(),
                sharedPreferences.getString(SaveSettings.USER_ORG, "").orEmpty(),
                sharedPreferences.getString(SaveSettings.USER_EMAIL, "").orEmpty(),
            )
            fragment.enableUserSetting(true)
            fragment.showUserInfo(true)
            fragment.showOrgInfo(false)
            fragment.activateEmail(false)
            fragment.enableScanButton(true)
            fragment.setScanText(getString(R.string.noSetting))
        } else {
            fragment.showUserInfo(false)
            fragment.enableUserSetting(false)
            fragment.activateEmail(false)
            fragment.enableScanButton(true)
            fragment.setScanText(getString(R.string.noSetting))
        }
    }

    private fun receiveToken(mailToken: String) {
        if (confirmationUrl.isBlank() || requestToken.isBlank()) return
        val request = object : StringRequest(Method.POST, confirmationUrl,
            { response ->
                var success = false
                try {
                    val json = JSONObject(response)
                    if (!json.optBoolean("error")) {
                        scannedJson = json
                        saveUrl = json.optString("urlSave")
                        val info = json.optJSONObject("user")?.optJSONObject("info")
                        if (info != null) {
                            val fullName = listOf(info.optString("firstName"), info.optString("lastName"))
                                .filter { it.isNotBlank() }
                                .joinToString(" ")

                            viewPagerAdapter.connectionFragment.apply {
                                activateEmail(false)
                                setUserText(fullName, info.optString("organisation"), info.optString("email"))
                                enableUserSetting(true)
                                enableScanButton(true)
                                setScanText(getString(R.string.noSetting))
                            }
                            success = true
                        }
                    }
                } catch (exception: Exception) {
                    Log.e(LOG_TAG, "Could not parse JSON", exception)
                }
                if (!success) {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.errorTitle)
                        .setMessage(R.string.emailTokenError)
                        .setNegativeButton(R.string.OkText, null)
                        .show()
                }
            },
            { error -> Toast.makeText(this, error.localizedMessage ?: getString(R.string.settingsFail), Toast.LENGTH_LONG).show() }) {
            @SuppressLint("HardwareIds")
            override fun getParams(): MutableMap<String, String> = mutableMapOf(
                "requestToken" to requestToken,
                "confirmationToken" to mailToken,
                "device" to "${Build.MANUFACTURER} ${Build.PRODUCT}",
                "os" to getAndroidVersion(),
                "imei" to Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID),
            )
        }
        Volley.newRequestQueue(this).add(request)
    }

    private fun getAndroidVersion(): String = "Android SDK: ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})"

    companion object {
        private const val REQUEST_CODE_SCAN = 1001
        private const val LOG_TAG = "TabSettingsActivity"
    }
}
