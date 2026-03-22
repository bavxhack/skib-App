package com.h2Invent.skibin;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {
    private Button scanButton;
    public static Button resetBtn;
    public static Button saveButton;
    public static TextView orgName;
    public static TextView orgId;
    public static TextView orgUrl;
    public static TextView orgPartner;
    public static LinearLayout orgInfo;
    public static final String ORG_NAME = "ORG_NAME";
    public static final String ORG_Ansprechpartner = "ORG_PARTNER";
    public static final String ORG_ID = "ORG_ID";
    public static final String ORG_URL = "ORG_URL";
    public static final String USER_NAME = "USER_NAME";
    public static final String USER_ORG = "OSER_ORG";
    public static final String USER_TOKEN = "USER_TOKEN";
    public static final String USER_URL = "USER_URL";
    public static final String USER_EMAIL = "USER_EMAIL";
    public static final String IS_USER = "IS_USER";
    public static final String IS_ORG = "IS_ORG";
    public static String urlForTokenUser;
    public static String IDTokenUser;

    private EditText userEmailCode;
    private Button userEmailBtn;
    private LinearLayout userLayout;
    public static LinearLayout userEmailLayout;
    public static Button scanUser;
    private RequestQueue requestQueue;
    private JSONObject jsonRes;
    private TextView userName;
    private TextView userEmail;
    private TextView userOrg;
    private Button saveUser;
    private Button resetUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestQueue = Volley.newRequestQueue(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        scanButton = (Button) findViewById(R.id.scanSettings);
        resetBtn = (Button) findViewById(R.id.connectionResetOrg);
        saveButton = (Button) findViewById(R.id.connectionOrgSave);
        orgName = (TextView) findViewById(R.id.org_name);
        orgId = (TextView) findViewById(R.id.orgId);
        orgUrl = (TextView) findViewById(R.id.orgUrl);
        orgPartner = (TextView) findViewById(R.id.orgPartner);
        orgInfo = (LinearLayout) findViewById(R.id.orgInfo);

        userEmailLayout = (LinearLayout) findViewById(R.id.userEmailCOnfirmation);
        userEmailCode = (EditText) findViewById(R.id.edituseremailCode);
        userEmailBtn = (Button) findViewById(R.id.buttonEmailCOnfirmation);
        scanUser = (Button) findViewById(R.id.scanConnection);
        userLayout = (LinearLayout) findViewById(R.id.user_info);
        userName = (TextView) findViewById(R.id.user_name);
        userEmail = (TextView) findViewById(R.id.user_email);
        userOrg = (TextView) findViewById(R.id.user_organisation);
        saveUser = (Button) findViewById(R.id.saveUserConnection);
        resetUser = (Button) findViewById(R.id.resetUserConnection);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, ScanSettings.class);
                startActivity(intent);
            }
        });
        scanUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, ScanSettingsUser.class);
                startActivity(intent);
            }
        });
        userEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recieveToken(userEmailCode.getText().toString());
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
        resetUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset();
            }
        });
        saveUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserData();
            }
        });
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset();
            }
        });
        loadData();
    }

    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.putBoolean(IS_ORG, true);
        editor.putString(ORG_Ansprechpartner, orgPartner.getText().toString());
        editor.putString(ORG_NAME, orgName.getText().toString());
        editor.putInt(ORG_ID, Integer.parseInt(orgId.getText().toString()));
        editor.putString(ORG_URL, orgUrl.getText().toString());
        editor.apply();
        Toast.makeText(this, R.string.settingsSaved, Toast.LENGTH_SHORT).show();

        onBackPressed();
    }

    private void reset() {
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        loadData();
        Toast.makeText(this, R.string.settingsDeleted, Toast.LENGTH_SHORT).show();
        onBackPressed();
    }

    private void saveUserData() {
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        try {
            JSONObject obj = jsonRes.getJSONObject("user").getJSONObject("info");
            String userNameS = obj.getString("firstName");
            userNameS = userNameS + " ";
            userNameS = userNameS + obj.getString("lastName");
            editor.putString(USER_NAME, userNameS);
            editor.putBoolean(IS_USER, true);
            editor.putString(USER_ORG, obj.getString("organisation"));
            editor.putString(USER_TOKEN, jsonRes.getString("token"));
            editor.putString(USER_EMAIL, obj.getString("email"));
            editor.putString(USER_URL, jsonRes.getString("url"));
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, R.string.settingsSaved, Toast.LENGTH_SHORT).show();
        onBackPressed();
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
        if(sharedPreferences.getBoolean(IS_ORG, false)){
            orgId.setText(String.valueOf(sharedPreferences.getInt(ORG_ID, -1)));
            orgName.setText(sharedPreferences.getString(ORG_NAME, getApplicationContext().getResources().getString(R.string.organisationName)));
            orgPartner.setText(sharedPreferences.getString(ORG_Ansprechpartner, getApplicationContext().getResources().getString(R.string.ansprechpartner)));
            orgUrl.setText(sharedPreferences.getString(ORG_URL, ""));
            orgInfo.setVisibility(View.VISIBLE);
        }
        if(sharedPreferences.getBoolean(IS_USER, false)){
            userLayout.setVisibility(View.VISIBLE);
            userName.setText(sharedPreferences.getString(USER_NAME,""));
            userEmail.setText(sharedPreferences.getString(USER_EMAIL,""));
            userOrg.setText(sharedPreferences.getString(USER_ORG,""));

        }


    }

    private void recieveToken(final String mailToken) {
        StringRequest request = new StringRequest(Request.Method.POST, urlForTokenUser,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (obj.getBoolean("error") == false) {
                                jsonRes = obj;
                                userLayout.setVisibility(View.VISIBLE);
                                userEmailLayout.setVisibility(View.GONE);
                                String userNameS = "";
                                obj = jsonRes.getJSONObject("user").getJSONObject("info");
                                userNameS = obj.getString("firstName");
                                userNameS = userNameS + " ";
                                userNameS = userNameS + obj.getString("lastName");

                                userName.setText(userNameS);
                                userOrg.setText(obj.getString("organisation"));
                                userEmail.setText(obj.getString("email"));
                                saveUser.setEnabled(true);
                                resetUser.setEnabled(true);
                                scanUser.setEnabled(true);
                                scanUser.setText("Verbinde eine Person");
                                return;
                            }

                        } catch (Throwable t) {

                            Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                        }
                        new AlertDialog.Builder(SettingsActivity.this)
                                .setTitle("Fehler")
                                .setMessage("Sie haben einen Falschen Code eingegeben. Bitte überprüfen Sie den Bestätigungscode in der E-Mail")

                                // Specifying a listener allows you to take an action before dismissing the dialog.
                                // The dialog is automatically dismissed when a dialog button is clicked.

                                // A null listener allows the button to dismiss the dialog and take no further action.
                                .setNegativeButton(R.string.OkText, null)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
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
                params.put("requestToken", IDTokenUser);
                params.put("confirmationToken", mailToken);
                return params;
            }
        };
        requestQueue.add(request);
    }
}