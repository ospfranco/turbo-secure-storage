package com.turbosecurestorage

import androidx.appcompat.app.AppCompatActivity
import com.facebook.react.bridge.*
import java.lang.Exception
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor
import java.util.concurrent.Semaphore

class TurboSecureStorageModule(reactContext: ReactApplicationContext?): NativeTurboSecureStorageSpec(reactContext) {
  private val cryptoManager = CryptoManager(this.reactApplicationContext)

  override fun setItem(key: String, value: String, options: ReadableMap): WritableMap {
    val obj = WritableNativeMap()
    try {
      val requiresBiometrics = options.hasKey("biometricAuthentication")
      if(requiresBiometrics) {
        val activity = this.currentActivity
        val executor: Executor = ContextCompat.getMainExecutor(this.reactApplicationContext)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
          .setTitle("Please authenticate")
          .setSubtitle("Biometric authentication is required to safely read/write data")
          .setNegativeButtonText("Cancel")
          .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
          .build()

        var mutex = Semaphore(0)
        var authenticationCallback = TSSAuthenticationCallback(mutex)

        activity?.runOnUiThread {

          var biometricPrompt = BiometricPrompt(activity as AppCompatActivity, executor, authenticationCallback)
          Log.w(Constants.TAG, "User should be prompted")
          biometricPrompt.authenticate(promptInfo)
          Log.w(Constants.TAG, "After prompt")
        }

        try {
          mutex.acquire()
        } catch (e: Exception) {
          Log.e("BLAH", "Interrupted mutex exception");
        }

        if(authenticationCallback.isAuthenticated) {
          cryptoManager.set(key, value, true)
        }
      } else {
        cryptoManager.set(key, value)
      }
    } catch (e: Exception) {
      Log.w("setItem", e.localizedMessage)
      obj.putString("error", "Could not save value")
    }
    return obj
  }

  override fun getItem(key: String, options: ReadableMap): WritableMap {
    val obj = WritableNativeMap()
    try {
      val requiresBiometrics = options.hasKey("biometricAuthentication")
      if(requiresBiometrics) {
        val activity = this.currentActivity
        val executor: Executor = ContextCompat.getMainExecutor(this.reactApplicationContext)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
          .setTitle("Please authenticate")
          .setSubtitle("Biometric authentication is required to safely read/write data")
          .setNegativeButtonText("Cancel")
          .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
          .build()

        var mutex = Semaphore(0)
        var authenticationCallback = TSSAuthenticationCallback(mutex)

        activity?.runOnUiThread {

          var biometricPrompt = BiometricPrompt(activity as AppCompatActivity, executor, authenticationCallback)
          Log.w(Constants.TAG, "User should be prompted")
          biometricPrompt.authenticate(promptInfo)
          Log.w(Constants.TAG, "After prompt")
        }

        try {
          mutex.acquire()
        } catch (e: Exception) {
          Log.e("BLAH", "Interrupted mutex exception");
        }

        if(authenticationCallback.isAuthenticated) {
          val value = cryptoManager.get(key, true)
          obj.putString("value", value)
        }
      } else {
        val value = cryptoManager.get(key)
        obj.putString("value", value)
      }

    } catch(e: Exception) {
      obj.putString("error", "Could not get value")
    }
    return obj
  }

  override fun deleteItem(key: String, options: ReadableMap): WritableMap {
    val obj = WritableNativeMap()
    try {
      val requiresBiometrics = options.hasKey("biometricAuthentication")
      if(requiresBiometrics) {
        val activity = this.currentActivity
        val executor: Executor = ContextCompat.getMainExecutor(this.reactApplicationContext)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
          .setTitle("Please authenticate")
          .setSubtitle("Biometric authentication is required to safely read/write data")
          .setNegativeButtonText("Cancel")
          .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
          .build()

        var mutex = Semaphore(0)
        var authenticationCallback = TSSAuthenticationCallback(mutex)

        activity?.runOnUiThread {

          var biometricPrompt = BiometricPrompt(activity as AppCompatActivity, executor, authenticationCallback)
          Log.w(Constants.TAG, "User should be prompted")
          biometricPrompt.authenticate(promptInfo)
          Log.w(Constants.TAG, "After prompt")
        }

        try {
          mutex.acquire()
        } catch (e: Exception) {
          Log.e("BLAH", "Interrupted mutex exception");
        }

        if(authenticationCallback.isAuthenticated) {
         cryptoManager.delete(key, true)
        }
      } else {
        cryptoManager.delete(key)
      }
    } catch(e: Exception) {
      obj.putString("error", "Could not get value")
    }

    return obj
  }

  override fun getName(): String {
    return NAME
  }
  
  companion object {
    const val NAME = "TurboSecureStorage"
  }
}
