package com.turbosecurestorage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.MasterKey
import androidx.security.crypto.EncryptedSharedPreferences

class CryptoManager(context: Context) {
    private val masterKey: MasterKey
    private val encryptedPrefs: SharedPreferences
    init {
        this.masterKey = generateMasterKey(context, false)
        this.encryptedPrefs = EncryptedSharedPreferences.create(
            context,
            SHARED_PREFS_FILENAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun save(key: String, value: String) {
        this.encryptedPrefs.edit().putString(key, value).apply()
    }

    fun get(key: String): String? {
        return this.encryptedPrefs.getString(key, null)
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
            .setUserAuthenticationRequired(requireUserAuthentication ?: false)
            .build()
    }

    companion object {
        private const val SHARED_PREFS_FILENAME = "turboSecureStorageEncryptedSharedPrefs"
    }
}