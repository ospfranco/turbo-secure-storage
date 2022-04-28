package com.turbosecurestorage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.MasterKey
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor
import java.util.concurrent.Semaphore

class CryptoManager(context: Context) {
    private val masterKey: MasterKey
    private val biometricMasterKey: MasterKey

    private val encryptedPrefs: SharedPreferences
    private val biometricEncryptedPrefs: SharedPreferences

    private var executor: Executor
//    private lateinit var biometricPrompt: BiometricPrompt
    private var promptInfo: BiometricPrompt.PromptInfo

    init {
        // Create a regular master key and a biometrics secured master key
        masterKey = generateMasterKey(context, false)
        biometricMasterKey = generateMasterKey(context, true)

        // Create a regular encryptedSharedPreferences and a biometrics one
        encryptedPrefs = EncryptedSharedPreferences.create(
            context,
            SHARED_PREFS_FILENAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        biometricEncryptedPrefs = EncryptedSharedPreferences.create(
            context,
            BIOMETRICS_SHARED_PREFS_FILENAME,
            biometricMasterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        executor = ContextCompat.getMainExecutor(context)

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Please authenticate")
            .setSubtitle("Biometric authentication is required to safely read/write data")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .build()
    }

    private fun showBiometricPrompt(act: AppCompatActivity, onSuccess: () -> Unit) {
        var biometricPrompt = BiometricPrompt(act, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }

    fun set(key: String, value: String, withBiometrics: Boolean = false) {
        return if(withBiometrics) {
            biometricEncryptedPrefs.edit().putString(key,value).apply()
        } else {
            encryptedPrefs.edit().putString(key, value).apply()
        }

    }

    fun setWithAuthentication(act: AppCompatActivity, key: String, value: String) {
        Log.w(Constants.TAG, "Setting with authentication")
        var mutex = Semaphore(0)
        var authenticationCallback = TSSAuthenticationCallback(mutex)
        var biometricPrompt = BiometricPrompt(act, executor, authenticationCallback)
        Log.w(Constants.TAG, "User should be prompted")
        biometricPrompt.authenticate(promptInfo)
        Log.w(Constants.TAG, "After prompt")

        try {
            mutex.acquire()
        } catch (e: Exception) {
            Log.e("BLAH", "Interrupted mutex exception");
        }

        if(authenticationCallback.isAuthenticated) {
            set(key, value, true)
        }
    }

    fun get(key: String, withBiometrics: Boolean = false): String? {
        return if(withBiometrics) {
            biometricEncryptedPrefs.getString(key, null)
        } else {
            encryptedPrefs.getString(key, null)
        }
    }

    fun getWithAuthentication(act: AppCompatActivity, key: String): String? {
        var mutex = Semaphore(0)
        var authenticationCallback = TSSAuthenticationCallback(mutex)
        var biometricPrompt = BiometricPrompt(act, executor, authenticationCallback)
        biometricPrompt.authenticate(promptInfo)

        try {
            mutex.acquire()
        } catch (e: Exception) {
            Log.e("BLAH", "Interrupted mutex exception");
        }

        if(authenticationCallback.isAuthenticated) {
            return get(key, true)
        }

        return null
    }

    fun delete(key: String, withBiometrics: Boolean = false) {
        if(withBiometrics) {
            biometricEncryptedPrefs.edit().putString(key, null).apply()
        } else {
            encryptedPrefs.edit().putString(key, null).apply()
        }
    }

    fun deleteWithBiometrics(act: AppCompatActivity, key: String) {
        showBiometricPrompt(act) { delete(key, true)}
    }

//    By Choosing AES256_GCM the default settings for the key are
//    KeyGenParameterSpec.Builder(
//            mKeyAlias,
//            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
//            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
//            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
//            .setKeySize(DEFAULT_AES_GCM_MASTER_KEY_SIZE)
    private fun generateMasterKey(context: Context, requireUserAuthentication: Boolean?): MasterKey {

        return MasterKey
            .Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .setUserAuthenticationRequired(requireUserAuthentication ?: false, 10)
            .build()
    }

    companion object {
        private const val SHARED_PREFS_FILENAME = "turboSecureStorageEncryptedSharedPrefs"
        private const val BIOMETRICS_SHARED_PREFS_FILENAME = "turboSecureStorageBiometricsEncryptedSharedPrefs"
    }
}