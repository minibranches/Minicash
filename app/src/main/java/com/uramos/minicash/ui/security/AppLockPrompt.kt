package com.uramos.minicash.ui.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import android.content.ContextWrapper
import androidx.fragment.app.FragmentActivity

/** Huella o rostro, con el PIN, patrón o contraseña del teléfono como respaldo. Todo lo resuelve el sistema. */
private const val AUTHENTICATORS = BIOMETRIC_WEAK or DEVICE_CREDENTIAL

/** Si el teléfono puede autenticar al usuario (tiene huella, rostro o un bloqueo de pantalla configurado). */
fun canUseAppLock(context: Context): Boolean =
    BiometricManager.from(context).canAuthenticate(AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS

/**
 * Pide al usuario desbloquear con el diálogo del sistema. Nada se guarda ni sale del dispositivo: la app solo
 * recibe si la verificación tuvo éxito o no. [onFailure] se llama si el usuario cancela o el sistema se rinde.
 */
fun showAppLockPrompt(
    activity: FragmentActivity,
    title: String,
    subtitle: String,
    onSuccess: () -> Unit,
    onFailure: () -> Unit = {}
) {
    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onSuccess()
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = onFailure()
    }
    val prompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), callback)
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setAllowedAuthenticators(AUTHENTICATORS)
        .build()
    prompt.authenticate(info)
}

/** La actividad que contiene a este contexto, si es una [FragmentActivity] (un contexto de Compose puede venir envuelto). */
fun Context.findFragmentActivity(): FragmentActivity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is FragmentActivity) return current
        current = current.baseContext
    }
    return null
}
