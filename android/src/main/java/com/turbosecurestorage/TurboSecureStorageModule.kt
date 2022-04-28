package com.turbosecurestorage

import androidx.appcompat.app.AppCompatActivity
import com.facebook.react.bridge.*
import java.lang.Exception
import android.util.Log

class TurboSecureStorageModule(reactContext: ReactApplicationContext?): NativeTurboSecureStorageSpec(reactContext) {
  private val cryptoManager = CryptoManager(this.reactApplicationContext)

  override fun setItem(key: String, value: String, options: ReadableMap): WritableMap {
    val obj = WritableNativeMap()
    try {
      val requiresBiometrics = options.hasKey("biometricAuthentication")
      if(requiresBiometrics) {
        val activity = this.currentActivity
        activity?.runOnUiThread {
          cryptoManager.setWithAuthentication(activity as AppCompatActivity, key, value)
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
        val value = cryptoManager.getWithAuthentication(activity as AppCompatActivity, key)
        obj.putString("value", value)
      } else {
        val value = cryptoManager.get(key)
        obj.putString("value", value)
      }

    } catch(e: Exception) {
      obj.putString("error", "Could not get value")
    }
    return obj
  }

  override fun deleteItem(key: String): WritableMap {
    val obj = WritableNativeMap()
    try {
      cryptoManager.delete(key)
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
