package com.turbosecurestorage

import android.util.Log
import androidx.biometric.BiometricPrompt
import java.util.concurrent.Semaphore;

class TSSAuthenticationCallback(val mutex: Semaphore): BiometricPrompt.AuthenticationCallback() {
    var isAuthenticated = false

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        Log.e(Constants.TAG, "authentication error $errString")
        super.onAuthenticationError(errorCode, errString)
        mutex.release()
    }

    override fun onAuthenticationFailed() {
        Log.e(Constants.TAG, "authentication failed!")
        super.onAuthenticationFailed()
        mutex.release()
    }

    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        isAuthenticated = true;
        mutex.release()
    }
}