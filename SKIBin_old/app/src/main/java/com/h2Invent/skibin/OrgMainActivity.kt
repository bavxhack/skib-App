package com.h2Invent.skibin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar

class OrgMainActivity : AppCompatActivity() {
    private val requestQueue by lazy { Volley.newRequestQueue(this) }

    private lateinit var orgNameMain: TextView
    private lateinit var amountKinder: TextView
    private lateinit var ansprechpartner: TextView
    private lateinit var telefon: TextView
    private lateinit var scanButton: MaterialButton
    private lateinit var torchSwitch: CheckBox

    private var orgId: Int = -1
    private var orgUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fresco.initialize(this)
        setContentView(R.layout.activity_org_main)
        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar_main))

        orgNameMain = findViewById(R.id.orgNameMain)
        amountKinder = findViewById(R.id.amount_kinder)
        ansprechpartner = findViewById(R.id.ansprechpartner)
        telefon = findViewById(R.id.telefon)
        scanButton = findViewById(R.id.scan_button)
        torchSwitch = findViewById(R.id.torchSwitch)

        scanButton.setOnClickListener {
            startActivity(
                Intent(this, ScanCheckinActivity::class.java)
                    .putExtra(ScanCheckinActivity.EXTRA_TORCH, torchSwitch.isChecked)
                    .putExtra(ScanCheckinActivity.EXTRA_ORG_ID, orgId),
            )
        }
        torchSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            val text = if (isChecked) getString(R.string.torchOn) else getString(R.string.torchOff)
            Snackbar.make(buttonView, text, Snackbar.LENGTH_SHORT).show()
        }

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

    private fun initApp() {
        val sharedPreferences = getSharedPreferences(SaveSettings.SHARED_PREFS, MODE_PRIVATE)
        if (!sharedPreferences.getBoolean(SaveSettings.IS_ORG, false)) return

        orgId = sharedPreferences.getInt(SaveSettings.ORG_ID, -1)
        orgUrl = sharedPreferences.getString(SaveSettings.ORG_URL, "") ?: ""
        orgNameMain.text = sharedPreferences.getString(SaveSettings.ORG_NAME, getString(R.string.organisationName))
        ansprechpartner.text = sharedPreferences.getString(SaveSettings.ORG_ANSPRECHPARTNER, getString(R.string.ansprechpartner))
        telefon.text = getString(R.string.telefon)
        amountKinder.text = getString(R.string.anwesendeKinder)
        scanButton.isEnabled = orgId > 0

        refreshOrganisation()
    }

    private fun refreshOrganisation() {
        if (orgUrl.isBlank()) return
        val request = JsonObjectRequest(Request.Method.GET, orgUrl, null,
            { response ->
                amountKinder.text = getString(R.string.anwesendeKinder) + " ${response.optInt("anwesend")}" 
                ansprechpartner.text = response.optString("partner", ansprechpartner.text.toString())
                telefon.text = response.optString("tel", telefon.text.toString())
                val imageUrl = response.optString("image")
                if (imageUrl.isNotBlank()) {
                    findViewById<SimpleDraweeView>(R.id.my_image_view).apply {
                        setImageURI(Uri.parse(imageUrl))
                        hierarchy.actualImageScaleType = ScalingUtils.ScaleType.FIT_CENTER
                    }
                }
            },
            { error -> Snackbar.make(scanButton, error.localizedMessage ?: "Request failed", Snackbar.LENGTH_LONG).show() },
        )
        requestQueue.add(request)
    }

    companion object {
        const val EXTRA_ORG_ID = "extra_org_id"
    }
}
