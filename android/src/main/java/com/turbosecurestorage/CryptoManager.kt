package com.turbosecurestorage

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.MasterKey
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

class CryptoManager(context: Context) {
    private val masterKey: MasterKey
    private val encryptedPrefs: SharedPreferences
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    init {
        this.masterKey = generateMasterKey(context, false)
        this.encryptedPrefs = EncryptedSharedPreferences.create(
            context,
            SHARED_PREFS_FILENAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        executor = ContextCompat.getMainExecutor(context)

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Input fingerprint")
            .setSubtitle("Authenticate via fingerprint to get the password")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BIOMETRIC_STRONG)
            .build()
    }

//    TODO The prompt is being shown, but the master key needs to be created with the flag
//    So the module initialization code needs to change, so that it takes the parameter BEFORE
//    creating the master key
    private fun showBiometricPrompt(act: AppCompatActivity, onSuccess: () -> Unit) {
        biometricPrompt = BiometricPrompt(act, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }

    fun save(key: String, value: String) {
        this.encryptedPrefs.edit().putString(key, value).apply()
    }

    fun saveWithAuthentication(act: AppCompatActivity, key: String, value: String) {
        showBiometricPrompt(act) { save(key, value) }
    }

    fun get(key: String): String? {
        return this.encryptedPrefs.getString(key, null)
    }

    fun getWithAuthentication(act: AppCompatActivity, key: String) {
        showBiometricPrompt(act) { get(key) }
    }

    fun delete(key: String) {
        this.encryptedPrefs.edit().putString(key, null).apply()
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
    }
}