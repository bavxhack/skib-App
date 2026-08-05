package com.h2Invent.skibin

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.VolleyError
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
            { error -> handleCheckinError(error) }) {
            override fun getParams(): MutableMap<String, String> = mutableMapOf("org_id" to orgId.toString())
        }
        requestQueue.add(request)
    }

    private fun showResult(response: String) {
        val json = JSONObject(response)
        val checkinText = json.optString("checkinText")
        val errorText = json.optString("errorText")
        val message = listOf(checkinText, errorText)
            .filter { it.isNotBlank() }
            .joinToString("\n")
            .ifBlank { if (json.optBoolean("error")) getString(R.string.checkinErrorFallback) else getString(R.string.checkinSuccessFallback) }
        val name = json.optString("name").ifBlank { json.optString("child") }
        val course = json.optString("kurs").ifBlank { json.optString("course") }

        showDialog(
            name = name,
            course = course,
            message = message,
            isError = json.optBoolean("error"),
        )

        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        val effect = VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(effect)
        if (json.optBoolean("error")) {
            handler.postDelayed({ vibrator.vibrate(effect) }, 250)
        }
        scheduleResume()
    }


    private fun handleCheckinError(error: VolleyError) {
        val responseBody = error.networkResponse?.data?.toString(Charsets.UTF_8).orEmpty()
        if (responseBody.isNotBlank()) {
            try {
                val json = JSONObject(responseBody)
                val message = listOf(json.optString("checkinText"), json.optString("errorText"))
                    .filter { it.isNotBlank() }
                    .joinToString("\n")
                    .ifBlank { getString(R.string.checkinErrorFallback) }
                showDialog(
                    name = json.optString("name"),
                    course = json.optString("kurs"),
                    message = message,
                    isError = true,
                )
                scheduleResume()
                return
            } catch (_: Exception) {
                // fall through to generic fallback
            }
        }

        val fallbackMessage = when (error.networkResponse?.statusCode) {
            500 -> getString(R.string.checkinServerNotFound)
            else -> error.localizedMessage ?: getString(R.string.checkinErrorFallback)
        }
        showDialog(
            name = "",
            course = "",
            message = fallbackMessage,
            isError = true,
        )
        scheduleResume()
    }

    private fun showDialog(name: String, course: String, message: String, isError: Boolean) {
        lastDialog?.dismissAllowingStateLoss()
        lastDialog = CheckinDialog.newInstance(
            name = name,
            kurs = course,
            text = message,
            isError = isError,
        )
        lastDialog?.show(supportFragmentManager, "checkin")
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
