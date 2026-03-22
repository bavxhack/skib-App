package com.h2Invent.skibin;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TabSettingsActivity extends AppCompatActivity implements FragmentConnection.OnItemSelectedListener, FragmentSettings.OnItemSelectedListener {
    private ViewPager mViewPager;
    private ViewPageAdapter mViewPagerAdapter;
    private TabLayout mTabLayout;
    public static JSONObject jsonRes;
    private RequestQueue requestQueue;
    private String requestToken;
    private  String idToken;
    private  String urlToken;
    private String emailToken;
    private String ComToken;
    private static int SCAN_ACTIVITY = 1;
    private static int ORG_SCAN_ACTIVITY = 2;
    public static int prechooseTab = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_settings);
        setViewPager();
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                String result = data.getStringExtra("result");
                try {

                    ViewPageAdapter adapter = ((ViewPageAdapter) mViewPager.getAdapter());
                    final FragmentConnection fragmentConnection = (FragmentConnection) mViewPagerAdapter.getItem(0);
                    jsonRes = new JSONObject(result);
                    if(jsonRes.getString("type").equals("ORGANISATION")){
                        //this is the stuff for organisation
                        if (fragmentConnection != null) {
                            fragmentConnection.setOrgText(jsonRes.getString("name"),jsonRes.getString("partner"));
                            fragmentConnection.enableOrgSetting(true);
                            fragmentConnection.showUserInfo(false);
                            fragmentConnection.showOrgInfo(true);
                        }
                    }else if (jsonRes.getString("type").equals("USER")){
                        //this is the stuff for a user with email verfication
                        idToken = jsonRes.getString("token");
                        urlToken = jsonRes.getString("url");
                        if (fragmentConnection != null) {
                            fragmentConnection.activateEmail(true);
                            fragmentConnection.enableScanBtn(true);
                            fragmentConnection.enableOrgSetting(false);
                            fragmentConnection.showUserInfo(true);
                            fragmentConnection.showOrgInfo(false);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
                Toast.makeText(getApplicationContext(), R.string.settingsFail, Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void setViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPagerAdapter = new ViewPageAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);
        mTabLayout = (TabLayout) findViewById(R.id.tab);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void scanClicked() {
        Intent intent = new Intent(TabSettingsActivity.this, ScanSettings.class);
        startActivityForResult(intent, SCAN_ACTIVITY);
    }

    @Override
    public void userSaveClicked() {
        saveUserData(jsonRes);
    }

    @Override
    public void resetClicked() {
        resetSettings();
    }

    @Override
    public void userEmailConfirmClicked(String token) {
        recieveToken(token, urlToken, idToken);
    }


    @Override
    public void orgSaveClicked() {
        saveData(jsonRes);
    }

    @Override
    public void init() {
        loadData();
    }

    private void saveData(JSONObject response) {
        SharedPreferences sharedPreferences = getSharedPreferences(SaveSettings.SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        try {
            editor.putBoolean(SaveSettings.IS_ORG, true);
            editor.putString(SaveSettings.ORG_Ansprechpartner, response.getString("partner"));
            editor.putString(SaveSettings.ORG_NAME, response.getString("name"));
            editor.putInt(SaveSettings.ORG_ID, response.getInt("id"));
            editor.putString(SaveSettings.ORG_URL, response.getString("url"));
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Toast.makeText(this, R.string.settingsSaved, Toast.LENGTH_SHORT).show();

        onBackPressed();
    }

    private void resetSettings() {
        SharedPreferences sharedPreferences = getSharedPreferences(SaveSettings.SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        loadData();
        Toast.makeText(this, R.string.settingsDeleted, Toast.LENGTH_SHORT).show();
        onBackPressed();
    }

    private void saveUserData(JSONObject response) {
        final SharedPreferences sharedPreferences = getSharedPreferences(SaveSettings.SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        try {
            JSONObject obj = jsonRes.getJSONObject("user").getJSONObject("info");
            String userNameS = obj.getString("firstName");
            userNameS = userNameS + " ";
            userNameS = userNameS + obj.getString("lastName");
            editor.putString(SaveSettings.USER_NAME, userNameS);
            editor.putBoolean(SaveSettings.IS_USER, true);
            editor.putString(SaveSettings.USER_ORG, obj.getString("organisation"));
            editor.putString(SaveSettings.USER_TOKEN, jsonRes.getString("token"));
            editor.putString(SaveSettings.USER_EMAIL, obj.getString("email"));
            editor.putString(SaveSettings.USER_CHECKIN_URL, obj.getString("urlCheckinKids"));
            editor.putString(SaveSettings.USER_KINDERLISTE_URL, obj.getString("urlKinderListeHeute"));
            editor.apply();

            requestQueue = Volley.newRequestQueue(this);
            StringRequest request = new StringRequest(Request.Method.POST, urlToken,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject obj = new JSONObject(response);
                                if (!obj.getBoolean("error")) {
                                    Toast.makeText(getApplicationContext(), R.string.settingsSaved, Toast.LENGTH_SHORT).show();
                                    return;
                                }else {
                                    final SharedPreferences sharedPreferences = getSharedPreferences(SaveSettings.SHARED_PREFS, MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.clear();
                                    Toast.makeText(getApplicationContext(), R.string.settingsFail, Toast.LENGTH_SHORT).show();
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
                @SuppressLint("HardwareIds")
                @Override
                protected Map<String, String> getParams() {
                    SharedPreferences sharedPreferences = getSharedPreferences(SaveSettings.SHARED_PREFS, MODE_PRIVATE);
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("token", sharedPreferences.getString(SaveSettings.USER_TOKEN,""));
                    return params;
                }
            };
            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        onBackPressed();
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
        if (sharedPreferences.getBoolean(SaveSettings.IS_ORG, false)) {
            TabLayout tabLayout = (TabLayout) findViewById(R.id.tab);
            TabLayout.Tab tab = tabLayout.getTabAt(0);
            if (tab != null) {
                tab.select();
            }
            final FragmentConnection fragmentConnection = (FragmentConnection) mViewPagerAdapter.getItem(0);
            fragmentConnection.setOrgText(
                    sharedPreferences.getString(SaveSettings.ORG_NAME, ""),
                    sharedPreferences.getString(SaveSettings.ORG_Ansprechpartner, "")
            );
            FragmentConnection.setIsOrg(true);

        }else {
            ViewPageAdapter adapter = ((ViewPageAdapter) mViewPager.getAdapter());
            final FragmentConnection fragmentConnection = (FragmentConnection) mViewPagerAdapter.getItem(0);
            fragmentConnection.setIsOrg(false);
        }
        if (sharedPreferences.getBoolean(SaveSettings.IS_USER, false)) {
            TabLayout tabLayout = (TabLayout) findViewById(R.id.tab);
            TabLayout.Tab tab = tabLayout.getTabAt(0);
            if (tab != null) {
                tab.select();
            }

            ViewPageAdapter adapter = ((ViewPageAdapter) mViewPager.getAdapter());

            final FragmentConnection fragmentConnection = (FragmentConnection) mViewPagerAdapter.getItem(0);
            fragmentConnection.setUserText(
                    sharedPreferences.getString(SaveSettings.USER_ORG, ""),
                    sharedPreferences.getString(SaveSettings.USER_NAME, ""),
                    sharedPreferences.getString(SaveSettings.USER_EMAIL, "")
            );
            fragmentConnection.setUser(true);

        }else {
            ViewPageAdapter adapter = ((ViewPageAdapter) mViewPager.getAdapter());
            final FragmentConnection fragmentConnection = (FragmentConnection) mViewPagerAdapter.getItem(0);
            fragmentConnection.setUser(false);
        }
    }

    private void recieveToken(final String mailToken, final String urlForTokenUser, final String IDTokenUser) {
        requestQueue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, urlForTokenUser,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                jsonRes = obj;
                                urlToken = obj.getString("urlSave");
                                int index = mViewPager.getCurrentItem();
                                ViewPageAdapter adapter = ((ViewPageAdapter) mViewPager.getAdapter());
                                final FragmentConnection fragmentConnection = (FragmentConnection) mViewPagerAdapter.getItem(index);
                                fragmentConnection.activateEmail(false);
                                String userNameS = "";
                                obj = jsonRes.getJSONObject("user").getJSONObject("info");
                                userNameS = obj.getString("firstName");
                                userNameS = userNameS + " ";
                                userNameS = userNameS + obj.getString("lastName");

                                fragmentConnection.setUserText(
                                        userNameS,
                                        obj.getString("organisation"),
                                        obj.getString("email")
                                );
                                fragmentConnection.enableUSerSetting(true);
                                fragmentConnection.enableScanBtn(true);
                                fragmentConnection.setScanText(getApplicationContext().getResources().getString(R.string.noSetting));

                                return;
                            }

                        } catch (Throwable t) {

                            Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                        }
                        new AlertDialog.Builder(TabSettingsActivity.this)
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
            @SuppressLint("HardwareIds")
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<String, String>();
                params.put("requestToken", IDTokenUser);
                params.put("confirmationToken", mailToken);
                params.put("device", android.os.Build.MANUFACTURER +" "+ android.os.Build.PRODUCT);
                params.put("os",getAndroidVersion());
                params.put("imei", Settings.Secure.getString(getApplicationContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID));
                return params;
            }
        };
        requestQueue.add(request);
    }
    public String getAndroidVersion() {
        String release = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;
        return "Android SDK: " + sdkVersion + " (" + release +")";
    }
}