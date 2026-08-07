package com.h2Invent.skibin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
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
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import org.json.JSONObject
import java.util.Locale

class ChildDetailActivity : AppCompatActivity() {
    private val requestQueue by lazy { Volley.newRequestQueue(this) }
    private lateinit var childName: TextView
    private lateinit var infoContainer: LinearLayout
    private lateinit var consentContainer: LinearLayout
    private lateinit var contactContainer: LinearLayout
    private lateinit var checkinButton: MaterialButton
    private var phoneNumber = ""
    private var emergencyNumber = ""
    private var parentName = ""
    private var childDisplayName = ""
    private var checkinUrl = ""
    private var userToken = ""
    private var callTarget = ""

    private val callPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) performCall() else Toast.makeText(this, R.string.noCallPermission, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_child_detail)
        childName = findViewById(R.id.childDetailName)
        infoContainer = findViewById(R.id.childDettailInfo)
        consentContainer = findViewById(R.id.childDetailBoolean)
        contactContainer = findViewById(R.id.detailContacts)
        checkinButton = findViewById(R.id.detailCheckin)
        checkinUrl = intent.getStringExtra(EXTRA_CHECKIN_URL).orEmpty()
        userToken = intent.getStringExtra(ChildListMainActivity.EXTRA_USER_TOKEN).orEmpty()

        findViewById<View>(R.id.detailBack).setOnClickListener { finish() }
        findViewById<View>(R.id.callParents).setOnClickListener { confirmCall(phoneNumber, parentName) }
        findViewById<View>(R.id.callEmergency).setOnClickListener { confirmCall(emergencyNumber, getString(R.string.detailEmergencyContact)) }
        checkinButton.setOnClickListener { confirmCheckin() }

        val detailUrl = intent.getStringExtra(EXTRA_URL).orEmpty()
        if (detailUrl.isNotBlank() && userToken.isNotBlank()) loadDetails(detailUrl) else showError()
    }

    private fun loadDetails(detailUrl: String) {
        findViewById<View>(R.id.detailLoading).visibility = View.VISIBLE
        requestQueue.add(JsonObjectRequest(Request.Method.GET, "$detailUrl?communicationToken=$userToken", null,
            { response ->
                findViewById<View>(R.id.detailLoading).visibility = View.GONE
                if (response.optBoolean("error")) showError(response.optString("errorText")) else bindDetails(response)
            },
            { showError() },
        ))
    }

    private fun bindDetails(response: JSONObject) {
        childDisplayName = listOf(response.optString("vorname"), response.optString("name")).filter { valid(it) }.joinToString(" ")
            .ifBlank { getString(R.string.detailMissing) }
        childName.text = childDisplayName + if (response.optBoolean("hasBirthday")) "  🎈" else ""
        findViewById<TextView>(R.id.detailInitials).text = childDisplayName.split(' ').take(2).joinToString("") { it.take(1).uppercase(Locale.GERMAN) }
        val school = response.optString("schule").takeIf(::valid)
        val grade = response.optString("klasse").takeIf(::valid)?.let { "Klasse $it" }
        findViewById<TextView>(R.id.detailSubtitle).text = listOfNotNull(grade, school).joinToString(" · ").ifBlank { getString(R.string.detailMissing) }
        phoneNumber = response.optString("phone").takeIf(::valid).orEmpty()
        emergencyNumber = response.optString("emergencyPhone").takeIf(::valid).orEmpty()
        parentName = response.optString("parentsName").takeIf(::valid) ?: getString(R.string.detailParents)
        checkinUrl = response.optString("checkinUrl").takeIf(::valid) ?: checkinUrl
        checkinButton.visibility = if (checkinUrl.isBlank() || response.optBoolean("checkin")) View.GONE else View.VISIBLE
        if (response.optBoolean("checkin")) {
            findViewById<TextView>(R.id.detailStatus).apply { text = "✓ Anwesend"; setTextColor(ContextCompat.getColor(context, R.color.phoneGreen)) }
        }
        contactContainer.removeAllViews()
        addInfoRow(contactContainer, getString(R.string.detailGuardians), parentName)
        addInfoRow(contactContainer, getString(R.string.telefon), phoneNumber.ifBlank { getString(R.string.detailMissing) })
        addInfoRow(contactContainer, getString(R.string.detailEmergencyContact), emergencyNumber.ifBlank { getString(R.string.detailMissing) })
        populateInfo(response)
    }

    private fun populateInfo(response: JSONObject) {
        infoContainer.removeAllViews()
        consentContainer.removeAllViews()
        val info = response.optJSONArray("info")
        var medical: String? = null
        for (index in 0 until (info?.length() ?: 0)) {
            val entry = info?.optJSONObject(index) ?: continue
            val label = entry.optString("name").takeIf(::valid) ?: getString(R.string.detailInformation)
            val value = entry.optString("value").takeIf(::valid) ?: getString(R.string.detailMissing)
            if (label.contains("allerg", true) || label.contains("medizin", true) || label.contains("notfallset", true)) {
                medical = listOfNotNull(medical, "$label: $value").joinToString("\n")
            } else addInfoRow(infoContainer, label, value)
        }
        if (infoContainer.childCount == 0) addInfoRow(infoContainer, getString(R.string.detailInformation), getString(R.string.detailMissing))
        findViewById<MaterialCardView>(R.id.medicalCard).visibility = if (medical == null) View.GONE else View.VISIBLE
        findViewById<TextView>(R.id.medicalText).text = medical

        val booleans = response.optJSONArray("boolean")
        for (index in 0 until (booleans?.length() ?: 0)) {
            val entry = booleans?.optJSONObject(index) ?: continue
            addConsent(entry.optString("name").takeIf(::valid) ?: getString(R.string.detailInformation), entry.optBoolean("value"))
        }
        if (consentContainer.childCount == 0) addInfoRow(consentContainer, getString(R.string.detailConsents), getString(R.string.detailMissing))
    }

    private fun addInfoRow(container: LinearLayout, label: String, value: String) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, dp(12), 0, dp(12)) }
        row.addView(TextView(this).apply { text = label; textSize = 13f; setTextColor(ContextCompat.getColor(context, R.color.grey)); layoutParams = LinearLayout.LayoutParams(0, -2, 1f) })
        row.addView(TextView(this).apply { text = value; textSize = 13f; gravity = android.view.Gravity.END; setTextColor(ContextCompat.getColor(context, R.color.black)); setTypeface(typeface, android.graphics.Typeface.BOLD); layoutParams = LinearLayout.LayoutParams(0, -2, 1f) })
        container.addView(row)
    }

    private fun addConsent(label: String, allowed: Boolean) {
        consentContainer.addView(TextView(this).apply {
            text = if (allowed) "✓ $label" else "✕ $label"
            textSize = 12f; setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, if (allowed) R.color.phoneGreen else R.color.statusPurple))
            setPadding(dp(10), dp(9), dp(10), dp(9))
        })
    }

    private fun confirmCheckin() {
        if (checkinUrl.isBlank() || checkinButton.isEnabled.not()) return
        AlertDialog.Builder(this).setTitle(getString(R.string.detailCheckinTitle, childDisplayName))
            .setMessage(R.string.detailCheckinMessage).setNegativeButton(R.string.detailCancel, null)
            .setPositiveButton(R.string.checkinButton) { _, _ -> executeCheckin() }.show()
    }

    private fun executeCheckin() {
        checkinButton.isEnabled = false
        checkinButton.text = getString(R.string.detailCheckingIn)
        requestQueue.add(JsonObjectRequest(Request.Method.GET, "$checkinUrl?communicationToken=$userToken", null,
            { response ->
                if (response.optBoolean("error")) restoreCheckin(response.optString("errorText")) else {
                    checkinButton.visibility = View.GONE
                    findViewById<TextView>(R.id.detailStatus).apply { text = "✓ Anwesend"; setTextColor(ContextCompat.getColor(context, R.color.phoneGreen)) }
                    Toast.makeText(this, getString(R.string.detailCheckedIn, childDisplayName), Toast.LENGTH_LONG).show()
                }
            }, { restoreCheckin() }))
    }

    private fun restoreCheckin(message: String = getString(R.string.checkinErrorFallback)) {
        checkinButton.isEnabled = true; checkinButton.text = getString(R.string.detailCheckinNow)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun confirmCall(number: String, label: String) {
        if (number.isBlank()) { Toast.makeText(this, R.string.detailPhoneMissing, Toast.LENGTH_SHORT).show(); return }
        callTarget = number
        AlertDialog.Builder(this).setTitle(label).setMessage(getString(R.string.detailCallMessage, number))
            .setPositiveButton(R.string.detailCall) { _, _ -> requestCallPermissionOrCall() }.setNegativeButton(R.string.detailCancel, null).show()
    }

    private fun requestCallPermissionOrCall() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) performCall()
        else callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
    }

    private fun performCall() = startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$callTarget")))
    private fun showError(message: String = getString(R.string.detailLoadError)) { findViewById<View>(R.id.detailLoading).visibility = View.GONE; Toast.makeText(this, message, Toast.LENGTH_LONG).show() }
    private fun valid(value: String) = value.isNotBlank() && value != "null"
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    companion object { const val EXTRA_URL = "detailUrl"; const val EXTRA_CHECKIN_URL = "checkinUrl" }
}
