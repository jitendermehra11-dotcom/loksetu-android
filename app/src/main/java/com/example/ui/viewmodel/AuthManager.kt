package com.example.ui.viewmodel

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class AuthManager {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    /**
     * Step 1: Send 6-digit OTP to user's mobile number via Firebase
     */
    fun sendOtpToPhoneNumber(
        phoneNumber: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$phoneNumber") // भारत का कंट्री कोड (+91)
            .setTimeout(60L, TimeUnit.SECONDS) // 60 सेकंड्स का टाइमआउट
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    /**
     * Step 2: Verify the OTP entered by the user
     */
    fun verifyOtpCode(verificationId: String, smsCode: String): PhoneAuthCredential {
        return PhoneAuthProvider.getCredential(verificationId, smsCode)
    }
}
