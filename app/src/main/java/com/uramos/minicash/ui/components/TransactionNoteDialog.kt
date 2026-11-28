package com.uramos.minicash.ui.components

import androidx.compose.ui.res.stringResource
import com.uramos.minicash.R
import com.uramos.minicash.ui.theme.dialogSurface
import com.uramos.minicash.ui.theme.MinicashShapes
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TransactionNoteDialog(
    concept: String,
    note: String,
    onDismiss: () -> Unit
) {
    MinicashAlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = concept,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = note,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        confirmButton = {
            FilledTonalButton(
                onClick = onDismiss,
                shape = CircleShape
            ) {
                Text(stringResource(R.string.common_close), fontWeight = FontWeight.Bold)
            }
        },
        shape = MinicashShapes.Card,
        containerColor = MaterialTheme.colorScheme.dialogSurface
    )
}
