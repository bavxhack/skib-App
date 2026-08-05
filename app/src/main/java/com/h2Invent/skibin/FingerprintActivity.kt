package com.h2Invent.skibin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Random

class FingerprintActivity : AppCompatActivity() {
    private lateinit var authenticateButton: Button
    private lateinit var pinInput: EditText
    private lateinit var pinVerify: EditText
    private lateinit var pinNewInput: EditText
    private lateinit var pinLogin: Button
    private lateinit var pinSet: Button
    private lateinit var pinSetNewButton: TextView
    private lateinit var resetAll: TextView

    private var pinHash: String = ""
    private var pinSalt: String = ""

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fingerprint)

        if (!hasSavedConnection()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        authenticateButton = findViewById(R.id.authenticate)
        pinInput = findViewById(R.id.pinNumber)
        pinVerify = findViewById(R.id.pinNumberVerify)
        pinNewInput = findViewById(R.id.pinNewNumber)
        pinLogin = findViewById(R.id.pinAuthenticateBtn)
        pinSet = findViewById(R.id.pinAuthenticateBtnNew)
        pinSetNewButton = findViewById(R.id.pinsetNewBtn)
        resetAll = findViewById(R.id.pinresetAll)

        readPassword()
        setupBiometric()
        updatePinUi(hasStoredPin())

        authenticateButton.setOnClickListener { biometricPrompt.authenticate(promptInfo) }
        pinLogin.setOnClickListener {
            if (verifyPassword(pinInput.text?.toString().orEmpty())) grantAccess()
        }
        pinSet.setOnClickListener { savePassword() }
        pinSetNewButton.setOnClickListener { updatePinUi(false) }
        resetAll.setOnClickListener { confirmResetAll() }

        if (hasStoredPin()) biometricPrompt.authenticate(promptInfo)
    }

    private fun setupBiometric() {
        val executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                grantAccess()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(this@FingerprintActivity, R.string.pinWrong, Toast.LENGTH_SHORT).show()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.fingerprintAuthenticateTitle))
            .setSubtitle(getString(R.string.fingerprintAuthenticateSubtitle))
            .setNegativeButtonText(getString(R.string.fingerprintAuthenticateUsePin))
            .build()
    }

    private fun grantAccess() {
        pinInput.setText("")
        Toast.makeText(this, R.string.accessGranted, Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun confirmResetAll() {
        AlertDialog.Builder(this)
            .setTitle(R.string.resetTitle)
            .setMessage(R.string.resetSubtitle)
            .setPositiveButton(android.R.string.yes) { _, _ ->
                if (resetEverything()) Toast.makeText(this, R.string.resetApp, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(android.R.string.no, null)
            .show()
    }

    private fun hasSavedConnection(): Boolean {
        val appPrefs = getSharedPreferences(SaveSettings.SHARED_PREFS, MODE_PRIVATE)
        return appPrefs.getBoolean(SaveSettings.IS_ORG, false) || appPrefs.getBoolean(SaveSettings.IS_USER, false)
    }

    private fun readPassword() {
        val sharedPreferences = getSharedPreferences(SaveSettings.SECURITY_PREFS, MODE_PRIVATE)
        pinHash = sharedPreferences.getString(SaveSettings.USER_PIN_HASH, "").orEmpty()
        pinSalt = sharedPreferences.getString(SaveSettings.USER_PIN_SALT, "").orEmpty()
    }

    private fun hasStoredPin(): Boolean = pinHash.isNotBlank() && pinSalt.isNotBlank()

    private fun verifyPassword(password: String): Boolean {
        if (digest(pinSalt + password, "MD5") == pinHash) return true
        Toast.makeText(this, R.string.pinWrong, Toast.LENGTH_SHORT).show()
        return false
    }

    private fun savePassword() {
        val currentPin = pinInput.text?.toString().orEmpty()
        val newPin = pinNewInput.text?.toString().orEmpty()
        val verifyPin = pinVerify.text?.toString().orEmpty()

        if (hasStoredPin() && digest(pinSalt + currentPin, "MD5") != pinHash) {
            Toast.makeText(this, R.string.pinWrong, Toast.LENGTH_SHORT).show()
            return
        }
        if (newPin != verifyPin) {
            Toast.makeText(this, R.string.pinWrong, Toast.LENGTH_SHORT).show()
            return
        }
        if (newPin.length < 4) {
            Toast.makeText(this, R.string.pinToShort, Toast.LENGTH_SHORT).show()
            return
        }

        pinSalt = randomString(24)
        pinHash = digest(pinSalt + newPin, "MD5")
        getSharedPreferences(SaveSettings.SECURITY_PREFS, MODE_PRIVATE).edit().apply {
            putString(SaveSettings.USER_PIN_HASH, pinHash)
            putString(SaveSettings.USER_PIN_SALT, pinSalt)
            apply()
        }
        Toast.makeText(this, R.string.pinSaved, Toast.LENGTH_SHORT).show()
        updatePinUi(true)
    }

    private fun updatePinUi(hasPin: Boolean) {
        pinInput.visibility = android.view.View.VISIBLE
        pinLogin.visibility = android.view.View.VISIBLE
        authenticateButton.visibility = if (hasPin) android.view.View.VISIBLE else android.view.View.GONE
        pinNewInput.visibility = if (hasPin) android.view.View.GONE else android.view.View.VISIBLE
        pinVerify.visibility = if (hasPin) android.view.View.GONE else android.view.View.VISIBLE
        pinSet.visibility = if (hasPin) android.view.View.GONE else android.view.View.VISIBLE
        pinSetNewButton.visibility = if (hasPin) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun resetEverything(): Boolean {
        getSharedPreferences(SaveSettings.SECURITY_PREFS, MODE_PRIVATE).edit().clear().apply()
        getSharedPreferences(SaveSettings.SHARED_PREFS, MODE_PRIVATE).edit().clear().apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
        return true
    }

    private fun digest(input: String, algorithm: String): String {
        return try {
            val messageDigest = MessageDigest.getInstance(algorithm)
            messageDigest.update(input.toByteArray(), 0, input.length)
            byteArrayToHex(messageDigest.digest())
        } catch (_: NoSuchAlgorithmException) {
            input
        }
    }

    private fun byteArrayToHex(array: ByteArray): String = buildString {
        array.forEach { append(HEX_TABLE[(it.toInt() shr 4) and 0x0F]).append(HEX_TABLE[it.toInt() and 0x0F]) }
    }

    private fun randomString(length: Int): String = buildString {
        repeat(length) { append(DATA[random.nextInt(DATA.length)]) }
    }

    companion object {
        private const val DATA = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz|!£$%&/=@#"
        private val HEX_TABLE = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
        private val random = Random()
    }
}
