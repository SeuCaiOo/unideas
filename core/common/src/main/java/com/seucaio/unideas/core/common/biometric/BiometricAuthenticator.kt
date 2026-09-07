package com.seucaio.unideas.core.common.biometric

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

sealed interface BiometricAuthResult {
    data object Success : BiometricAuthResult
    data object Unavailable : BiometricAuthResult
    data object Failed : BiometricAuthResult
}

/**
 * `BIOMETRIC_WEAK` alone would exclude device PIN/pattern; `DEVICE_CREDENTIAL` alone would
 * exclude fingerprint/face on devices that have both. Combining both is what lets any device
 * lock the user already set up work here — no separate PIN/password of the app's own.
 */
private const val ALLOWED_AUTHENTICATORS =
    BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL

object BiometricAuthenticator {

    fun authenticate(
        activity: FragmentActivity,
        title: String,
        onResult: (BiometricAuthResult) -> Unit,
    ) {
        val biometricsUnavailable = BiometricManager.from(activity)
            .canAuthenticate(ALLOWED_AUTHENTICATORS) != BiometricManager.BIOMETRIC_SUCCESS
        if (biometricsUnavailable) {
            onResult(BiometricAuthResult.Unavailable)
            return
        }
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
            .build()
        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onResult(BiometricAuthResult.Success)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onResult(BiometricAuthResult.Failed)
                }
            },
        )
        prompt.authenticate(promptInfo)
    }
}
