package com.h2Invent.skibin;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class scanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;
    private RequestQueue requestQueue;
    private MediaPlayer mediaPlayer;
    private CheckinDialog checkinDialog;
    private Runnable runnerStop;
    private Handler handler;
    int finishTime = 120; //10 secs
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        requestQueue = Volley.newRequestQueue(this);
        checkinDialog = new CheckinDialog();
        Log.d("TAG", "Start here the camera");
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        mScannerView.setFlash(OrgMainActivity.torch.isChecked());
        setContentView(mScannerView);                // Set the scanner view as the content view
        Snackbar.make(mScannerView, R.string.scanHelp, Snackbar.LENGTH_SHORT).show();

         handler = new Handler();
        runnerStop =new Runnable() {
            public void run() {
                scanActivity.this.finish();
            }
        };

        handler.postDelayed(runnerStop, finishTime * 1000);
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
        // Do something with the result here
        // Log.v("tag", rawResult.getText()); // Prints scan results
        // Log.v("tag", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        //MainActivity.urlresult.setText(rawResult.getText());
        Log.v("Result from Scan", rawResult.getText());
        requestData(rawResult.getText());
        Log.v("recieved data","yes");
        //onBackPressed();
        // If you would like to resume scanning, call this method below:
        handler.removeCallbacks(runnerStop);
        handler.postDelayed(runnerStop, finishTime * 1000);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // starts the scanning back up again
                        mScannerView.resumeCameraPreview(scanActivity.this);
                    }
                });
            }
        }).start();

    }

    public void requestData(String url) {

        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {

                            JSONObject obj = new JSONObject(response);
                            if (checkinDialog.isAdded()){
                                checkinDialog.dismiss();
                            }

                            checkinDialog.setName(obj.getString("name"));
                            checkinDialog.setKurs(obj.getString("kurs"));
                            checkinDialog.setText(obj.getString("errorText"));
                            checkinDialog.setBackground(obj.getBoolean("error"));
                            checkinDialog.show(getSupportFragmentManager(), "checkin");
                            Vibrator vibrator = (Vibrator) getSystemService(getApplicationContext().VIBRATOR_SERVICE);
                            if (Build.VERSION.SDK_INT >= 26) {
                                if (obj.getBoolean("error")) {
                                    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                                    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                                } else {
                                    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                                }
                            }
                        } catch (Throwable t) {

                            Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("org_id", String.valueOf(OrgMainActivity.orgId));
                return params;
            }
        };
        requestQueue.add(request);
    }


}