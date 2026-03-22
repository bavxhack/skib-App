package com.h2Invent.skibin;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.Result;

import org.json.JSONObject;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanSettingsUser extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;
    private RequestQueue requestQueue;
    private OnItemSelectedListener listener;

    public interface OnItemSelectedListener {
        public void recieveUserToken(JSONObject response);
    }
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        requestQueue = Volley.newRequestQueue(this);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
        Snackbar.make(mScannerView, R.string.settingsHelp, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        MainActivity.urlresult.setText(rawResult.getText());
        requestData(rawResult.getText());
      //  onBackPressed();
    }

    public void requestData(String url) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Intent returnIntent = getIntent();
                        returnIntent.putExtra("result",response.toString());
                        setResult(RESULT_OK,returnIntent);
                        finish();
                        //TabSettingsActivity.recieveUserToken(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        requestQueue.add(request);

    }

}