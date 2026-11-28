package com.uramos.minicash.ui.screens.credits

import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.uramos.minicash.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

const val NO_REMINDER = -1
private val REMINDER_OPTIONS = listOf(NO_REMINDER, 0, 1, 2, 3, 5, 7)

/** Campo del día de pago y opciones de aviso; lo comparten el formulario de compras y el de instituciones. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
fun PaymentDayFields(
    paymentDayStr: String,
    onPaymentDayChange: (String) -> Unit,
    paymentInvalid: Boolean,
    reminderDays: Int,
    onReminderChange: (Int) -> Unit
) {
    val hasPaymentDay = paymentDayStr.isNotEmpty() && !paymentInvalid

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = paymentDayStr,
            onValueChange = onPaymentDayChange,
            label = { Text(stringResource(R.string.credit_pay_day_label)) },
            placeholder = { Text(stringResource(R.string.credit_pay_day_hint)) },
            supportingText = {
                Text(
                    if (paymentInvalid) stringResource(R.string.credit_pay_day_invalid)
                    else stringResource(R.string.credit_pay_day_note)
                )
            },
            isError = paymentInvalid,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        AnimatedVisibility(visible = hasPaymentDay) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.credit_remind_me),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    REMINDER_OPTIONS.forEach { option ->
                        ToggleButton(
                            checked = option == reminderDays,
                            onCheckedChange = { onReminderChange(option) }
                        ) {
                            Text(
                                text = when (option) {
                                    NO_REMINDER -> stringResource(R.string.credit_remind_none)
                                    0 -> stringResource(R.string.credit_remind_same)
                                    else -> pluralStringResource(R.plurals.credit_remind_before, option, option)
                                },
                                maxLines = 1
                            )
                        }
                    }
                }

                Text(
                    text = when (reminderDays) {
                        NO_REMINDER -> stringResource(R.string.credit_remind_none_text)
                        0 -> stringResource(R.string.credit_remind_same_text)
                        else -> pluralStringResource(R.plurals.credit_remind_before_text, reminderDays, reminderDays)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
