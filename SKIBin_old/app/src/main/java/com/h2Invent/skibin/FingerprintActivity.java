package com.h2Invent.skibin;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.concurrent.Executor;

public class FingerprintActivity extends AppCompatActivity {
    private static String SECURITY_SAVE = "SECURITY_SAVE";
    private Button authenticateBtn;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private TextView authticationStatus;
    private static final int BIOMETRIC_PERMISSION_CODE = 102;
    private String pinHash;
    private String pinSalt;
    private TextView pinInput;
    private TextView pinVerify;
    private TextView pinNewInput;
    private Button pinLogin;
    private Button pinSet;
    public static final String DATA = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz|!£$%&/=@#";
    public Random RANDOM = new Random();
    private TextView pinSetNewBtn;
    private TextView resetAll;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint);
        authenticateBtn = (Button) findViewById(R.id.authenticate);
        authticationStatus = (TextView) findViewById(R.id.authenticationStatus);
        pinInput = (TextView) findViewById(R.id.pinNumber);
        pinVerify = (TextView) findViewById(R.id.pinNumberVerify);
        pinLogin = (Button) findViewById(R.id.pinAuthenticateBtn);
        pinSet = (Button) findViewById(R.id.pinAuthenticateBtnNew);
        pinSetNewBtn = (TextView) findViewById(R.id.pinsetNewBtn);
        pinNewInput = (TextView) findViewById(R.id.pinNewNumber);
        resetAll = (TextView) findViewById(R.id.pinresetAll);
        if (readPassword()) {
            startActivity(new Intent(FingerprintActivity.this, MainActivity.class));
        }
        ;

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(FingerprintActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                String text = (String) errString;

            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                grantAccess();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                String text = "Fehler in Authentifizierung";
                notifyUser(text);
            }
        });
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getResources().getString(R.string.fingerprintAuthenticateTitle))
                .setSubtitle(getResources().getString(R.string.fingerprintAuthenticateSubtitle))
                .setNegativeButtonText(getResources().getString(R.string.fingerprintAuthenticateUsePin))
                .build();

        authenticateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                biometricPrompt.authenticate(promptInfo);
            }
        });
        pinLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (verifyPassword(pinInput.getText().toString())) {
                    grantAccess();
                }
            }
        });
        pinSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePassword();
            }
        });
        pinSetNewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pinVerify.setVisibility(View.VISIBLE);
                pinSet.setVisibility(View.VISIBLE);
                pinSetNewBtn.setVisibility(View.GONE);
                pinLogin.setVisibility(View.GONE);
                pinNewInput.setVisibility(View.VISIBLE);
            }
        });
        resetAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(FingerprintActivity.this)
                        .setTitle(R.string.resetTitle)
                        .setMessage(R.string.resetSubtitle)

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (resetAll()) {
                                    Toast.makeText(FingerprintActivity.this, R.string.resetApp, Toast.LENGTH_SHORT).show();

                                }
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();


            }
        });
        if (!pinSalt.equals("")) {
            biometricPrompt.authenticate(promptInfo);
        }

        checkPermission(Manifest.permission.USE_BIOMETRIC,
                BIOMETRIC_PERMISSION_CODE);
    }

    private void grantAccess() {
        String text = getResources().getString(R.string.accessGranted);
        notifyUser(text);
        pinInput.setText("");
        startActivity(new Intent(FingerprintActivity.this, MainActivity.class));
    }

    private void notifyUser(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(FingerprintActivity.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(FingerprintActivity.this,
                    new String[]{permission},
                    requestCode);
        }

    }

    public boolean readPassword() {
        SharedPreferences sharedPreferences = getSharedPreferences(SECURITY_SAVE, MODE_PRIVATE);
        pinHash = sharedPreferences.getString(SaveSettings.USER_PIN_HASH, "");
        pinSalt = sharedPreferences.getString(SaveSettings.USER_PIN_SALT, "");
        if (pinSalt != null && pinSalt.equals("")) {
            pinNewInput.setVisibility(View.VISIBLE);
            pinSet.setVisibility(View.VISIBLE);
            pinVerify.setVisibility(View.VISIBLE);
            pinSetNewBtn.setVisibility(View.GONE);
            pinInput.setVisibility(View.GONE);
            pinLogin.setVisibility(View.GONE);
            authenticateBtn.setVisibility(View.GONE);
        }
        sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
        return sharedPreferences.getBoolean(SaveSettings.IS_ORG, false);
    }

    private boolean verifyPassword(String passwd) {
        String hashAct = digest(pinSalt + passwd, "MD5");
        if (hashAct.equals(pinHash)) {
            return true;
        } else {
            Toast.makeText(this, R.string.pinWrong, Toast.LENGTH_SHORT).show();
        }
        return false;

    }

    public void savePassword() {
        SharedPreferences sharedPreferences = getSharedPreferences(SECURITY_SAVE, MODE_PRIVATE);
        String pin0 = pinInput.getText().toString();
        String pin1 = pinNewInput.getText().toString();
        String pin2 = pinVerify.getText().toString();

        if (!pinSalt.equals("") && !pinHash.equals(digest(pinSalt + pin0, "MD5"))) {
            Toast.makeText(this, R.string.pinWrong, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!pin1.equals(pin2)) {
            Toast.makeText(this, R.string.pinWrong, Toast.LENGTH_SHORT).show();
            return;
        }
        if (pin1.length() < 4) {
            Toast.makeText(this, R.string.pinToShort, Toast.LENGTH_SHORT).show();
            return;
        }
        pinSalt = randomString(24);
        pinHash = digest(pinSalt + (String) pin1, "MD5");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SaveSettings.USER_PIN_HASH, pinHash);
        editor.putString(SaveSettings.USER_PIN_SALT, pinSalt);
        editor.apply();
        pinVerify.setVisibility(View.GONE);
        pinNewInput.setVisibility(View.GONE);
        pinSet.setVisibility(View.GONE);
        pinInput.setVisibility(View.VISIBLE);
        pinLogin.setVisibility(View.VISIBLE);
        pinSetNewBtn.setVisibility(View.VISIBLE);
        authenticateBtn.setVisibility(View.VISIBLE);
        Toast.makeText(this, R.string.pinSaved, Toast.LENGTH_SHORT).show();
    }


    public static String digest(String s, String algorithm) {
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return s;
        }

        m.update(s.getBytes(), 0, s.length());
        return byteArrayToHex(m.digest());
    }

    private static char[] hextable = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String byteArrayToHex(byte[] array) {
        String s = "";
        for (int i = 0; i < array.length; ++i) {
            int di = (array[i] + 256) & 0xFF; // Make it unsigned
            s = s + hextable[(di >> 4) & 0xF] + hextable[di & 0xF];
        }
        return s;
    }

    public String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);

        for (int i = 0; i < len; i++) {
            sb.append(DATA.charAt(RANDOM.nextInt(DATA.length())));
        }

        return sb.toString();
    }

    public boolean resetAll() {
        SharedPreferences sharedPreferences = getSharedPreferences(SECURITY_SAVE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        sharedPreferences = getSharedPreferences(MainActivity.SHARED_PREFS, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        finish();
        startActivity(getIntent());
        return true;
    }
}