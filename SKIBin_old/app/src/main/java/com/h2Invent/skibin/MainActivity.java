package com.h2Invent.skibin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;

public class MainActivity extends AppCompatActivity {
    private static final String LOG = MainActivity.class.getSimpleName();
    public static TextView urlresult;
    public static final String SHARED_PREFS = "shared_prefs";
    private Button buttonNOsetting;
    public static String userToken;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int PHONE_PERMISSION_CODE = 101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        logTester();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        buttonNOsetting = (Button) findViewById(R.id.buttonNoSettingOrganisation);

        buttonNOsetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TabSettingsActivity.class);
                startActivity(intent);
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
                Intent intent = new Intent(MainActivity.this, TabSettingsActivity.class);
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

    public  void initApp() {

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        checkPermission(Manifest.permission.CAMERA,
                CAMERA_PERMISSION_CODE);
        checkPermission(Manifest.permission.CALL_PHONE,
                PHONE_PERMISSION_CODE);
        if (sharedPreferences.getBoolean(SettingsActivity.IS_ORG, false)) {
            startActivity(new Intent(MainActivity.this, OrgMainActivity.class));
        } else if (sharedPreferences.getBoolean(SettingsActivity.IS_USER, false)) {
            startActivity(new Intent(MainActivity.this, ChildListMainActivity.class));
        } else {
            buttonNOsetting.setVisibility(View.VISIBLE);
        }

    }

    // Function to check and request permission.
    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] { permission },
                    requestCode);
        }

    }



}