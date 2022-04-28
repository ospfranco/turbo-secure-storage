package com.turbosecurestorage

import com.facebook.react.bridge.*
import android.util.Log
import java.lang.Exception

class TurboSecureStorageModule(reactContext: ReactApplicationContext?): NativeTurboSecureStorageSpec(reactContext) {
  private val cryptoManager = CryptoManager(this.reactApplicationContext)

  override fun setItem(key: String, value: String, options: ReadableMap): WritableMap {
    val obj = WritableNativeMap()
    try {
      cryptoManager.save(key, value)
    } catch (e: Exception) {
      obj.putString("error", "Could not save value")
    }
    return obj
  }

  override fun getItem(key: String): WritableMap {
    val obj = WritableNativeMap()
    try {
      val value = cryptoManager.get(key)
      obj.putString("value", value)
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
