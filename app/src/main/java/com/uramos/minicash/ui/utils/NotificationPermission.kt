package com.uramos.minicash.ui.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.uramos.minicash.data.util.NotificationNeeds

/** Si el sistema deja que la app notifique (permiso concedido y notificaciones de la app no bloqueadas). */
fun notificationsAllowed(context: Context): Boolean = NotificationManagerCompat.from(context).areNotificationsEnabled()

/** Igual que [notificationsAllowed], pero se vuelve a leer cada vez que la app regresa de la configuración del sistema. */
@Composable
fun rememberNotificationsAllowed(): Boolean {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var allowed by remember { mutableStateOf(notificationsAllowed(context)) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) allowed = notificationsAllowed(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return allowed
}

/** Abre la configuración de notificaciones de Minicash en el sistema. */
fun openAppNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
}

/**
 * Pide el permiso de notificaciones una sola vez cuando hay algo que lo necesita (recordatorio diario o avisos de
 * pago activos), por ejemplo tras instalar y restaurar una copia con los avisos ya encendidos. Si se rechaza no se
 * vuelve a insistir: Ajustes muestra un aviso con acceso directo a la configuración del sistema.
 */
@Composable
fun RequestNotificationPermissionOnce(needed: Boolean, alreadyAsked: Boolean, onAsked: () -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(needed, alreadyAsked) {
        if (NotificationNeeds.shouldRequestNow(needed, alreadyAsked, Build.VERSION.SDK_INT, notificationsAllowed(context))) {
            onAsked()
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
