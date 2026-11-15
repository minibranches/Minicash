package com.uramos.minicash.ui.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.uramos.minicash.ui.components.MinicashLogo

/**
 * Pantalla que cubre la app mientras está bloqueada. No muestra ningún dato; [showUnlock] es falso solo en el
 * instante en que se decide si hace falta bloquear, para que no parpadee un botón.
 */
@Composable
fun LockScreen(showUnlock: Boolean, onUnlock: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        MinicashLogo(size = 72.dp)
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Minicash está bloqueada",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Desbloquea con tu huella, rostro o el PIN del teléfono.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (showUnlock) {
            Spacer(Modifier.height(32.dp))
            Button(onClick = onUnlock) {
                Icon(Icons.Default.LockOpen, contentDescription = null)
                Spacer(Modifier.padding(horizontal = 4.dp))
                Text("Desbloquear")
            }
        }
    }
}
