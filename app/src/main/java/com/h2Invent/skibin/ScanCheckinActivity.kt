package com.h2Invent.skibin

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import org.json.JSONObject

class ScanCheckinActivity : AppCompatActivity() {
    private val requestQueue by lazy { Volley.newRequestQueue(this) }
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var torchButton: MaterialButton
    private var handled = false
    private var torchEnabled = false
    private var lastCode = ""
    private var lastCodeAt = 0L

    private val timeoutRunnable = Runnable { finish() }
    private var lastDialog: CheckinDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        barcodeView = findViewById(R.id.barcode_scanner)
        torchButton = findViewById(R.id.torchSwitch)
        torchButton.setOnClickListener { toggleTorch() }
        barcodeView.decodeContinuous(callback)
        Snackbar.make(barcodeView, R.string.scanHelp, Snackbar.LENGTH_SHORT).show()
        handler.postDelayed(timeoutRunnable, SCAN_TIMEOUT_MS)
    }

    override fun onResume() {
        super.onResume()
        if (!handled) barcodeView.resume()
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
        val now = System.currentTimeMillis()
        if (text == lastCode && now - lastCodeAt < DUPLICATE_THROTTLE_MS) return@BarcodeCallback
        lastCode = text
        lastCodeAt = now
        handled = true
        barcodeView.pause()
        handler.removeCallbacks(timeoutRunnable)
        handler.postDelayed(timeoutRunnable, SCAN_TIMEOUT_MS)
        requestData(text)
    }

    private fun requestData(url: String) {
        val orgId = intent.getIntExtra(EXTRA_ORG_ID, getSharedPreferences(SaveSettings.SHARED_PREFS, MODE_PRIVATE).getInt(SaveSettings.ORG_ID, -1))
        if (orgId <= 0) {
            Toast.makeText(this, R.string.checkinMissingOrg, Toast.LENGTH_LONG).show()
            handler.postDelayed(::resumeScanning, RETRY_DELAY_MS)
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
        playFeedback(json.optBoolean("error"))
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
                playFeedback(true)
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
        playFeedback(true)
    }

    private fun showDialog(name: String, course: String, message: String, isError: Boolean) {
        lastDialog?.dismissAllowingStateLoss()
        lastDialog = CheckinDialog.newInstance(
            name = name,
            kurs = course,
            text = message,
            isError = isError,
        ).apply { onConfirmed = ::resumeScanning }
        lastDialog?.show(supportFragmentManager, "checkin")
    }

    private fun resumeScanning() {
        handled = false
        barcodeView.resume()
    }

    private fun toggleTorch() {
        torchEnabled = !torchEnabled
        if (torchEnabled) barcodeView.setTorchOn() else barcodeView.setTorchOff()
        torchButton.isSelected = torchEnabled
        torchButton.setText(if (torchEnabled) R.string.scannerTorchOn else R.string.scannerTorchOff)
    }

    private fun playFeedback(isError: Boolean) {
        if (Settings.System.getInt(contentResolver, Settings.System.SOUND_EFFECTS_ENABLED, 1) == 1) {
            val sound = if (isError) R.raw.fail else R.raw.success
            MediaPlayer.create(this, sound)?.apply {
                setOnCompletionListener { it.release() }
                start()
            }
        }
        if (Settings.System.getInt(contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) == 1) {
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            if (isError) {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 120, 100, 220), -1))
            } else {
                vibrator.vibrate(VibrationEffect.createOneShot(180, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }
    }

    companion object {
        const val EXTRA_ORG_ID = "extra_org_id"
        private const val SCAN_TIMEOUT_MS = 120_000L
        private const val DUPLICATE_THROTTLE_MS = 5_000L
        private const val RETRY_DELAY_MS = 3_000L
    }
}
