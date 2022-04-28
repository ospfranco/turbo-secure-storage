package com.turbosecurestorage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.MasterKey
import androidx.security.crypto.EncryptedSharedPreferences

class CryptoManager(context: Context) {
    private val masterKey: MasterKey
    private val biometricMasterKey: MasterKey

    private val encryptedPrefs: SharedPreferences
    private val biometricEncryptedPrefs: SharedPreferences

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

    fun set(key: String, value: String, withBiometrics: Boolean = false) {
        return if(withBiometrics) {
            biometricEncryptedPrefs.edit().putString(key,value).apply()
        } else {
            encryptedPrefs.edit().putString(key, value).apply()
        }

    }

    fun get(key: String, withBiometrics: Boolean = false): String? {
        return if(withBiometrics) {
            biometricEncryptedPrefs.getString(key, null)
        } else {
            encryptedPrefs.getString(key, null)
        }
    }

    fun delete(key: String, withBiometrics: Boolean = false) {
        if(withBiometrics) {
            biometricEncryptedPrefs.edit().putString(key, null).apply()
        } else {
            encryptedPrefs.edit().putString(key, null).apply()
        }
    }

    companion object {
        private const val SHARED_PREFS_FILENAME = "turboSecureStorageEncryptedSharedPrefs"
        private const val BIOMETRICS_SHARED_PREFS_FILENAME = "turboSecureStorageBiometricsEncryptedSharedPrefs"
    }
}