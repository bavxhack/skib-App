package com.h2Invent.skibin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import java.security.MessageDigest
import java.security.SecureRandom

class FingerprintActivity : AppCompatActivity() {
    private lateinit var authenticateButton: MaterialButton
    private lateinit var pinInput: EditText
    private lateinit var pinVerify: EditText
    private lateinit var pinNewInput: EditText
    private lateinit var pinLogin: MaterialButton
    private lateinit var pinSet: MaterialButton
    private lateinit var statusView: TextView
    private lateinit var resetHelp: TextView
    private lateinit var loginMode: View
    private lateinit var resetMode: View
    private lateinit var secondaryActions: View
    private var pinHash = ""
    private var pinSalt = ""
    private var biometricAvailable = false
    private var pendingPinReset = false
    private var uiState: LoginUiState = LoginUiState.BiometricReady
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fingerprint)
        if (!hasSavedConnection()) {
            startActivity(Intent(this, MainActivity::class.java)); finish(); return
        }
        bindViews()
        readPassword()
        setupBiometric()
        bindActions()
        setState(if (hasStoredPin()) LoginUiState.BiometricReady else LoginUiState.PinReset)
    }

    private fun bindViews() {
        authenticateButton = findViewById(R.id.authenticate)
        pinInput = findViewById(R.id.pinNumber)
        pinVerify = findViewById(R.id.pinNumberVerify)
        pinNewInput = findViewById(R.id.pinNewNumber)
        pinLogin = findViewById(R.id.pinAuthenticateBtn)
        pinSet = findViewById(R.id.pinAuthenticateBtnNew)
        statusView = findViewById(R.id.authenticationStatus)
        resetHelp = findViewById(R.id.pinResetHelp)
        loginMode = findViewById(R.id.loginMode)
        resetMode = findViewById(R.id.pinResetMode)
        secondaryActions = findViewById(R.id.loginSecondaryActions)
    }

    private fun bindActions() {
        authenticateButton.setOnClickListener { startBiometric(false) }
        pinLogin.setOnClickListener {
            if (uiState == LoginUiState.BiometricRunning) return@setOnClickListener
            val pin = pinInput.text?.toString().orEmpty()
            if (pin.length !in PIN_LENGTH) setState(LoginUiState.Error(getString(R.string.loginPinInvalid)))
            else if (verifyPassword(pin)) grantAccess()
            else setState(LoginUiState.Error(getString(R.string.pinWrong)))
        }
        pinSet.setOnClickListener { savePassword() }
        findViewById<View>(R.id.pinsetNewBtn).setOnClickListener {
            if (biometricAvailable) startBiometric(true)
            else setState(LoginUiState.Error(getString(R.string.loginResetNeedsAuthentication)))
        }
        findViewById<View>(R.id.cancelPinReset).setOnClickListener {
            if (hasStoredPin()) setState(LoginUiState.BiometricReady)
        }
        findViewById<View>(R.id.pinresetAll).setOnClickListener { confirmResetAll() }
    }

    private fun setupBiometric() {
        biometricAvailable = BiometricManager.from(this).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
            BiometricManager.BIOMETRIC_SUCCESS
        biometricPrompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this), object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                if (pendingPinReset) {
                    pendingPinReset = false
                    setState(LoginUiState.PinReset)
                } else grantAccess()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                pendingPinReset = false
                setState(LoginUiState.Error(errString.toString()))
            }
            override fun onAuthenticationFailed() {
                // The platform prompt remains active; keep all submit actions locked.
                statusView.text = getString(R.string.fingerprintAuthenticateSubtitle)
            }
        })
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.fingerprintAuthenticateTitle))
            .setSubtitle(getString(R.string.fingerprintAuthenticateSubtitle))
            .setNegativeButtonText(getString(R.string.fingerprintAuthenticateUsePin))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
    }

    private fun startBiometric(forPinReset: Boolean) {
        if (!biometricAvailable) {
            setState(LoginUiState.PinEntry)
            return
        }
        pendingPinReset = forPinReset
        setState(LoginUiState.BiometricRunning)
        biometricPrompt.authenticate(promptInfo)
    }

    private fun setState(state: LoginUiState) {
        uiState = state
        val resetVisible = state == LoginUiState.PinReset
        loginMode.visibility = if (resetVisible) View.GONE else View.VISIBLE
        resetMode.visibility = if (resetVisible) View.VISIBLE else View.GONE
        secondaryActions.visibility = if (resetVisible) View.INVISIBLE else View.VISIBLE
        authenticateButton.visibility = if (biometricAvailable) View.VISIBLE else View.GONE
        val busy = state == LoginUiState.BiometricRunning || state == LoginUiState.Authenticated
        authenticateButton.isEnabled = !busy
        pinLogin.isEnabled = !busy
        pinSet.isEnabled = !busy
        statusView.text = when (state) {
            LoginUiState.BiometricRunning -> getString(R.string.loginBiometricRunning)
            is LoginUiState.Error -> state.message
            LoginUiState.PinEntry -> getString(R.string.loginBiometricUnavailable)
            else -> ""
        }
        if (!biometricAvailable && !resetVisible) {
            findViewById<TextView>(R.id.loginDescription).text = getString(R.string.loginPinPrimaryDescription)
        }
        if (resetVisible) pinNewInput.requestFocus()
    }

    private fun savePassword() {
        val newPin = pinNewInput.text?.toString().orEmpty()
        val verifyPin = pinVerify.text?.toString().orEmpty()
        val error = when {
            !newPin.matches(Regex("^\\d{4,8}$")) -> getString(R.string.loginPinInvalid)
            newPin != verifyPin -> getString(R.string.loginPinMismatch)
            else -> null
        }
        if (error != null) {
            resetHelp.apply { text = error; setTextColor(ContextCompat.getColor(context, R.color.buttonDanger)) }
            return
        }
        pinSalt = randomSalt()
        pinHash = digest(pinSalt + newPin, HASH_ALGORITHM)
        getSharedPreferences(SaveSettings.SECURITY_PREFS, MODE_PRIVATE).edit()
            .putString(SaveSettings.USER_PIN_HASH, pinHash)
            .putString(SaveSettings.USER_PIN_SALT, pinSalt)
            .apply()
        pinNewInput.setText(""); pinVerify.setText("")
        resetHelp.apply { text = getString(R.string.pinSaved); setTextColor(ContextCompat.getColor(context, R.color.phoneGreen)) }
        Toast.makeText(this, R.string.pinSaved, Toast.LENGTH_SHORT).show()
        setState(LoginUiState.BiometricReady)
    }

    private fun verifyPassword(password: String): Boolean {
        val secureHash = digest(pinSalt + password, HASH_ALGORITHM)
        if (MessageDigest.isEqual(secureHash.toByteArray(), pinHash.toByteArray())) return true
        // One-time compatibility with PINs stored by older app versions.
        if (digest(pinSalt + password, LEGACY_HASH_ALGORITHM) == pinHash) {
            pinHash = secureHash
            getSharedPreferences(SaveSettings.SECURITY_PREFS, MODE_PRIVATE).edit()
                .putString(SaveSettings.USER_PIN_HASH, secureHash).apply()
            return true
        }
        return false
    }

    private fun grantAccess() {
        setState(LoginUiState.Authenticated)
        pinInput.setText("")
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun confirmResetAll() {
        AlertDialog.Builder(this).setTitle(R.string.resetTitle).setMessage(R.string.loginResetExplanation)
            .setPositiveButton(R.string.loginResetConfirm) { _, _ -> resetEverything() }
            .setNegativeButton(R.string.detailCancel, null).show()
    }

    private fun resetEverything() {
        getSharedPreferences(SaveSettings.SECURITY_PREFS, MODE_PRIVATE).edit().clear().apply()
        getSharedPreferences(SaveSettings.SHARED_PREFS, MODE_PRIVATE).edit().clear().apply()
        startActivity(Intent(this, MainActivity::class.java)); finish()
    }

    private fun hasSavedConnection(): Boolean = getSharedPreferences(SaveSettings.SHARED_PREFS, MODE_PRIVATE).run {
        getBoolean(SaveSettings.IS_ORG, false) || getBoolean(SaveSettings.IS_USER, false)
    }
    private fun readPassword() = getSharedPreferences(SaveSettings.SECURITY_PREFS, MODE_PRIVATE).run {
        pinHash = getString(SaveSettings.USER_PIN_HASH, "").orEmpty(); pinSalt = getString(SaveSettings.USER_PIN_SALT, "").orEmpty()
    }
    private fun hasStoredPin() = pinHash.isNotBlank() && pinSalt.isNotBlank()
    private fun digest(input: String, algorithm: String): String = MessageDigest.getInstance(algorithm)
        .digest(input.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    private fun randomSalt(): String = ByteArray(24).also { SecureRandom().nextBytes(it) }.joinToString("") { "%02x".format(it) }

    private sealed interface LoginUiState {
        data object BiometricReady : LoginUiState
        data object BiometricRunning : LoginUiState
        data object PinEntry : LoginUiState
        data object PinReset : LoginUiState
        data class Error(val message: String) : LoginUiState
        data object Authenticated : LoginUiState
    }

    companion object {
        private val PIN_LENGTH = 4..8
        private const val HASH_ALGORITHM = "SHA-256"
        private const val LEGACY_HASH_ALGORITHM = "MD5"
    }
}
