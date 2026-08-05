package com.h2Invent.skibin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class ScanSettingsActivity : AppCompatActivity() {
    private val requestQueue by lazy { Volley.newRequestQueue(this) }
    private lateinit var barcodeView: DecoratedBarcodeView
    private var handled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        barcodeView = findViewById(R.id.barcode_scanner)
        Snackbar.make(barcodeView, R.string.settingsHelp, Snackbar.LENGTH_SHORT).show()
        barcodeView.decodeContinuous(callback)
    }

    override fun onResume() {
        super.onResume()
        handled = false
        barcodeView.resume()
    }

    override fun onPause() {
        barcodeView.pause()
        super.onPause()
    }

    private val callback = BarcodeCallback { result ->
        val text = result.text ?: return@BarcodeCallback
        if (handled) return@BarcodeCallback
        handled = true
        requestData(text)
    }

    private fun requestData(url: String) {
        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                setResult(RESULT_OK, intent.putExtra(EXTRA_RESULT, response.toString()))
                finish()
            },
            { error ->
                Toast.makeText(this, error.localizedMessage ?: getString(R.string.settingsFail), Toast.LENGTH_LONG).show()
                setResult(RESULT_CANCELED)
                finish()
            },
        )
        requestQueue.add(request)
    }

    companion object {
        const val EXTRA_RESULT = "result"
    }
}
