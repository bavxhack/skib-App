package com.h2Invent.skibin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class ChildDetailActivity : AppCompatActivity() {
    private val requestQueue by lazy { Volley.newRequestQueue(this) }

    private lateinit var childName: TextView
    private lateinit var infoContainer: LinearLayout
    private lateinit var booleanContainer: LinearLayout

    private var phoneNumber: String = ""
    private var parentName: String = ""

    private val callPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) performCall() else Toast.makeText(this, R.string.noCallPermission, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_detail)

        childName = findViewById(R.id.childDetailName)
        infoContainer = findViewById(R.id.childDettailInfo)
        booleanContainer = findViewById(R.id.childDetailBoolean)

        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.callParents)
            .setOnClickListener { showCallDialog() }

        val detailUrl = intent.getStringExtra(EXTRA_URL).orEmpty()
        val userToken = intent.getStringExtra(ChildListMainActivity.EXTRA_USER_TOKEN).orEmpty()
        if (detailUrl.isNotBlank() && userToken.isNotBlank()) loadDetails(detailUrl, userToken)
    }

    private fun loadDetails(detailUrl: String, userToken: String) {
        val request = JsonObjectRequest(Request.Method.GET, "$detailUrl?communicationToken=$userToken", null,
            { response ->
                if (response.optBoolean("error")) {
                    Toast.makeText(this, response.optString("errorText"), Toast.LENGTH_LONG).show()
                } else {
                    childName.text = listOf(response.optString("vorname"), response.optString("name"))
                        .filter { it.isNotBlank() }
                        .joinToString(" ")
                    phoneNumber = response.optString("phone")
                    parentName = response.optString("parentsName")
                    populateInfo(response)
                }
            },
            { error -> Toast.makeText(this, error.localizedMessage ?: "Request failed", Toast.LENGTH_LONG).show() },
        )
        requestQueue.add(request)
    }

    private fun populateInfo(response: org.json.JSONObject) {
        val infoJson = response.optJSONArray("info")
        for (index in 0 until (infoJson?.length() ?: 0)) {
            val info = infoJson?.optJSONObject(index) ?: continue
            val valueView = TextView(this).apply {
                text = info.optString("value").takeUnless { it.isBlank() || it == "null" } ?: "Keine Angabe"
                textSize = 20f
                setPadding(0, 16, 20, 0)
                setTextColor(ContextCompat.getColor(this@ChildDetailActivity, R.color.colorPrimaryDark))
            }
            val labelView = TextView(this).apply {
                text = info.optString("name")
                textSize = 12f
                setPadding(0, 0, 20, 0)
                setTextColor(ContextCompat.getColor(this@ChildDetailActivity, R.color.grey))
            }
            infoContainer.addView(valueView)
            infoContainer.addView(labelView)
        }

        val booleanJson = response.optJSONArray("boolean")
        for (index in 0 until (booleanJson?.length() ?: 0)) {
            val info = booleanJson?.optJSONObject(index) ?: continue
            val checkBox = CheckBox(this).apply {
                text = info.optString("name")
                isEnabled = false
                isChecked = info.optBoolean("value")
                setTextColor(ContextCompat.getColor(this@ChildDetailActivity, R.color.black))
            }
            booleanContainer.addView(checkBox)
        }
    }

    private fun showCallDialog() {
        if (phoneNumber.isBlank()) {
            Toast.makeText(this, R.string.noCallPermission, Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle(parentName)
            .setMessage(R.string.callTitle)
            .setPositiveButton(android.R.string.yes) { _, _ -> requestCallPermissionOrCall() }
            .setNegativeButton(android.R.string.no, null)
            .show()
    }

    private fun requestCallPermissionOrCall() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            performCall()
        } else {
            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }

    private fun performCall() {
        startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber")))
    }

    companion object {
        const val EXTRA_URL = "detailUrl"
    }
}
