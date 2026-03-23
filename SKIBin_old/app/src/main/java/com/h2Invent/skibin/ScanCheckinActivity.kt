package com.h2Invent.skibin

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import org.json.JSONObject

class ScanCheckinActivity : AppCompatActivity() {
    private val requestQueue by lazy { Volley.newRequestQueue(this) }
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var barcodeView: DecoratedBarcodeView
    private var handled = false

    private val timeoutRunnable = Runnable { finish() }
    private var lastDialog: CheckinDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        barcodeView = findViewById(R.id.barcode_scanner)
        if (intent.getBooleanExtra(EXTRA_TORCH, false)) barcodeView.setTorchOn() else barcodeView.setTorchOff()
        barcodeView.decodeContinuous(callback)
        Snackbar.make(barcodeView, R.string.scanHelp, Snackbar.LENGTH_SHORT).show()
        handler.postDelayed(timeoutRunnable, SCAN_TIMEOUT_MS)
    }

    override fun onResume() {
        super.onResume()
        barcodeView.resume()
    }

    override fun onPause() {
        barcodeView.pause()
        super.onPause()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private val callback = BarcodeCallback { result ->
        val text = result.text ?: return@BarcodeCallback
        if (handled) return@BarcodeCallback
        handled = true
        handler.removeCallbacks(timeoutRunnable)
        handler.postDelayed(timeoutRunnable, SCAN_TIMEOUT_MS)
        requestData(text)
    }

    private fun requestData(url: String) {
        val orgId = intent.getIntExtra(EXTRA_ORG_ID, getSharedPreferences(SaveSettings.SHARED_PREFS, MODE_PRIVATE).getInt(SaveSettings.ORG_ID, -1))
        if (orgId <= 0) {
            Toast.makeText(this, R.string.checkinMissingOrg, Toast.LENGTH_LONG).show()
            scheduleResume()
            return
        }
        val request = object : StringRequest(Method.POST, url,
            { response -> showResult(response) },
            { error ->
                Toast.makeText(this, error.localizedMessage ?: "Request failed", Toast.LENGTH_LONG).show()
                scheduleResume()
            }) {
            override fun getParams(): MutableMap<String, String> = mutableMapOf("org_id" to orgId.toString())
        }
        requestQueue.add(request)
    }

    private fun showResult(response: String) {
        val json = JSONObject(response)
        val message = json.optString("errorText")
            .ifBlank { json.optString("message") }
            .ifBlank { if (json.optBoolean("error")) getString(R.string.checkinErrorFallback) else getString(R.string.checkinSuccessFallback) }
        val name = json.optString("name").ifBlank { json.optString("child") }
        val course = json.optString("kurs").ifBlank { json.optString("course") }

        lastDialog?.dismissAllowingStateLoss()
        lastDialog = CheckinDialog.newInstance(
            name = name,
            kurs = course,
            text = message,
            isError = json.optBoolean("error"),
        )
        lastDialog?.show(supportFragmentManager, "checkin")

        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        val effect = VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(effect)
        if (json.optBoolean("error")) {
            handler.postDelayed({ vibrator.vibrate(effect) }, 250)
        }
        scheduleResume()
    }

    private fun scheduleResume() {
        handler.postDelayed({ handled = false }, RESUME_DELAY_MS)
    }

    companion object {
        const val EXTRA_TORCH = "extra_torch"
        const val EXTRA_ORG_ID = "extra_org_id"
        private const val SCAN_TIMEOUT_MS = 120_000L
        private const val RESUME_DELAY_MS = 3_000L
    }
}
