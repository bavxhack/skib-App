package com.h2Invent.skibin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

public class OrgMainActivity extends AppCompatActivity {
    private static final String LOG = OrgMainActivity.class.getSimpleName();
    public static Button btn;
    public static TextView orgNameMain;
    public static int orgId;
    public static String orgName;
    public static String orgPartner;
    public static String orgUrl;
    public static String imageUrl;
    private TextView amountKinder;
    private TextView ansprechpartner;
    private TextView telefon;
    private RequestQueue requestQueue;
    public static CheckBox torch;
    private String checkinUrl;
    private String kinderlisteUrl;
    public static String userToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_org_main);
        Fresco.initialize(this);
        requestQueue = Volley.newRequestQueue(this);
        logTester();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        ansprechpartner = (TextView) findViewById(R.id.ansprechpartner);
        telefon = (TextView) findViewById(R.id.telefon);
        amountKinder = (TextView) findViewById(R.id.amount_kinder);
        orgNameMain = (TextView) findViewById(R.id.orgNameMain);
        btn = (Button) findViewById(R.id.scan_button);
        torch = (CheckBox) findViewById(R.id.torchSwitch);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrgMainActivity.this, scanActivity.class);
                startActivity(intent);
            }
        });
        torch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = "";
                if (torch.isChecked()) {
                    text = getApplicationContext().getResources().getString(R.string.torchOn);
                } else {
                    text = getApplicationContext().getResources().getString(R.string.torchOff);
                }
                Snackbar.make(view, text, Snackbar.LENGTH_SHORT).show();
            }
        });
        initApp();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        initApp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_get_data:
                initApp();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(OrgMainActivity.this, TabSettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                Log.d(LOG, "Debug       - Press something.");
                return super.onOptionsItemSelected(item);
        }
    }

    private void logTester() {
        Log.v(LOG, "Verbose     - Meldung.");
        Log.d(LOG, "Debug       - Meldung.");
        Log.i(LOG, "Information - Meldung.");
        Log.w(LOG, "Warning     - Meldung.");
        Log.e(LOG, "Error       - Meldung.");
    }

    private void refesh() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, orgUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            String tetxt = getApplicationContext().getResources().getString(R.string.anwesendeKinder) + " " + String.valueOf(response.getInt("anwesend"));
                            amountKinder.setText(tetxt);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            ansprechpartner.setText(response.getString("partner"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            telefon.setText(response.getString("tel"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            String url = response.getString("image");
                            imageUrl = url;
                            Uri uri = Uri.parse(imageUrl);
                            SimpleDraweeView draweeView = (SimpleDraweeView) findViewById(R.id.my_image_view);
                            draweeView.setImageURI(uri);
                            draweeView.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


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
    public  void initApp() {

        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
        if (sharedPreferences.getBoolean(SettingsActivity.IS_ORG, false)) {
            orgId = sharedPreferences.getInt(SettingsActivity.ORG_ID, -1);
            orgName = sharedPreferences.getString(SettingsActivity.ORG_NAME, "Organisation Name");
            orgPartner = sharedPreferences.getString(SettingsActivity.ORG_Ansprechpartner, "Ansprechpartner");
            orgUrl = sharedPreferences.getString(SettingsActivity.ORG_URL, "URL der Organisation");
            orgNameMain.setText(orgName);
            ansprechpartner.setText(orgPartner);
            btn.setEnabled(true);

            telefon.setText(getApplicationContext().getResources().getString(R.string.telefon));
            amountKinder.setText(getApplicationContext().getResources().getString(R.string.anwesendeKinder));
            refesh();
        }

    }
}