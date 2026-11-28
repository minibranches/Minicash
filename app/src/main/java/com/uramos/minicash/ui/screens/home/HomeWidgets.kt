package com.uramos.minicash.ui.screens.home

import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.uramos.minicash.R
import com.uramos.minicash.data.local.entities.AccountType
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.runtime.remember
import androidx.compose.material.icons.filled.Savings
import androidx.compose.foundation.layout.fillMaxSize
import com.uramos.minicash.ui.theme.MinicashShapes
import com.uramos.minicash.ui.theme.cardSurface
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.uramos.minicash.data.local.entities.AccountEntity
import com.uramos.minicash.ui.components.CardTonalLevel
import com.uramos.minicash.ui.components.MinicashCard
import com.uramos.minicash.ui.theme.LocalAnimationRestriction
import com.uramos.minicash.ui.utils.MinicashColorUtils
import com.uramos.minicash.ui.utils.asIconTint
import com.uramos.minicash.ui.utils.getIconVectorByName
import com.uramos.minicash.ui.utils.toCurrencyFormat

/**
 * Tarjeta principal de Inicio: el patrimonio (suma de los saldos de todas las cuentas) como protagonista,
 * sobre el color primario del tema y con una forma expresiva de fondo. El monto se anima al cambiar.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PatrimonyCard(
    totalBalance: Long,
    accountCount: Int,
    modifier: Modifier = Modifier
) {
    val isRestricted = LocalAnimationRestriction.current
    val motionScheme = MaterialTheme.motionScheme
    val onContainer = MaterialTheme.colorScheme.onPrimaryContainer

    Surface(
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
    ) {
        Box {
            // Forma expresiva decorativa que asoma por la esquina superior derecha
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 36.dp, y = (-36).dp)
                    .size(156.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                        shape = MaterialShapes.SoftBurst.toShape()
                    )
            )

            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 22.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = onContainer.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = onContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.home_widget_patrimony),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = onContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                AnimatedContent(
                    targetState = totalBalance,
                    transitionSpec = {
                        if (isRestricted) {
                            fadeIn(animationSpec = snap()) togetherWith fadeOut(animationSpec = snap())
                        } else {
                            val spatialSpec = motionScheme.defaultSpatialSpec<IntOffset>()
                            val effectsSpec = motionScheme.defaultEffectsSpec<Float>()
                            if (targetState > initialState) {
                                (slideInVertically(animationSpec = spatialSpec) { it } + fadeIn(animationSpec = effectsSpec)) togetherWith
                                    (slideOutVertically(animationSpec = spatialSpec) { -it } + fadeOut(animationSpec = effectsSpec))
                            } else {
                                (slideInVertically(animationSpec = spatialSpec) { -it } + fadeIn(animationSpec = effectsSpec)) togetherWith
                                    (slideOutVertically(animationSpec = spatialSpec) { it } + fadeOut(animationSpec = effectsSpec))
                            }
                        }
                    },
                    label = "patrimony_odometer"
                ) { balance ->
                    Text(
                        text = (if (balance < 0) "- " else "") + kotlin.math.abs(balance).toCurrencyFormat(),
                        style = MaterialTheme.typography.displaySmall.copy(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.ExtraBold,
                        color = onContainer,
                        maxLines = 1
                    )
                }

                Text(
                    text = pluralStringResource(R.plurals.patrimony_sum, accountCount, accountCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = onContainer.copy(alpha = 0.72f)
                )
            }
        }
    }
}


/**
 * Patrimonio en una franja de una línea: ícono, etiqueta con el número de cuentas y la cifra a la derecha. Ocupa
 * ~64 dp en lugar de los ~210 de [PatrimonyCard].
 */
@Composable
fun PatrimonyStrip(
    totalBalance: Long,
    accountCount: Int,
    modifier: Modifier = Modifier
) {
    val onContainer = MaterialTheme.colorScheme.onPrimaryContainer
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(start = 20.dp, end = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = onContainer,
                modifier = Modifier.size(22.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_widget_patrimony),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = onContainer
                )
                Text(
                    text = pluralStringResource(R.plurals.patrimony_sum, accountCount, accountCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = onContainer.copy(alpha = 0.78f),
                    maxLines = 1
                )
            }
            Text(
                text = (if (totalBalance < 0) "- " else "") + kotlin.math.abs(totalBalance).toCurrencyFormat(),
                style = MaterialTheme.typography.headlineSmall.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.ExtraBold,
                color = onContainer,
                maxLines = 1
            )
        }
    }
}

/** Ancho y alto de [AccountTypeCard] en el carrusel de Inicio. */
val AccountTypeCardWidth = 264.dp
val AccountTypeCardHeight = 152.dp

/**
 * Cuenta de Inicio con el acabado de su tipo, todas del mismo tamaño y con el color de la propia cuenta:
 * débito con chip y círculos decorativos, crédito con borde y la etiqueta «A pagar», efectivo como billete
 * (borde punteado y un «$» de fondo) y ahorro con monedas. Solo usa nombre, tipo, color, ícono y saldo.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AccountTypeCard(
    account: AccountEntity,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val accountColor = MinicashColorUtils.parseHexColor(account.colorHex, MaterialTheme.colorScheme.primary)
    val surface = MaterialTheme.colorScheme.cardSurface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val ratio = when (account.type) {
        AccountType.DEBIT -> 0.30f
        AccountType.CREDIT_CARD -> 0.10f
        AccountType.CASH -> 0.24f
        AccountType.SAVINGS -> 0.28f
    }
    val containerColor = MinicashColorUtils.tintedContainerColor(accountColor, surface, ratio)
    val border = if (account.type == AccountType.CREDIT_CARD) {
        androidx.compose.foundation.BorderStroke(2.dp, accountColor.copy(alpha = 0.55f))
    } else null
    val decoColor = accountColor.copy(alpha = 0.16f)
    val lineColor = accountColor.copy(alpha = 0.9f)
    val dashed = remember { androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(14f, 12f)) }

    MinicashCard(
        level = CardTonalLevel.LOW,
        containerColor = containerColor,
        shape = MinicashShapes.Card,
        border = border,
        onClick = onClick,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val w = size.width
                    val h = size.height
                    when (account.type) {
                        AccountType.DEBIT -> {
                            drawCircle(decoColor, radius = h * 0.62f, center = Offset(w * 0.92f, h * 1.02f))
                            drawCircle(decoColor, radius = h * 0.36f, center = Offset(w * 0.66f, h * 1.12f))
                        }
                        AccountType.CREDIT_CARD -> {
                            drawCircle(decoColor, radius = h * 0.7f, center = Offset(w * 0.02f, -h * 0.05f))
                        }
                        AccountType.CASH -> {
                            drawRoundRect(
                                color = accountColor.copy(alpha = 0.45f),
                                topLeft = Offset(10.dp.toPx(), 10.dp.toPx()),
                                size = androidx.compose.ui.geometry.Size(w - 20.dp.toPx(), h - 20.dp.toPx()),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(22.dp.toPx()),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx(), pathEffect = dashed)
                            )
                        }
                        AccountType.SAVINGS -> {
                            drawCircle(decoColor, radius = h * 0.62f, center = Offset(w * 0.96f, -h * 0.08f))
                            drawCircle(decoColor, radius = h * 0.36f, center = Offset(w * 0.82f, h * 0.02f))
                        }
                    }
                    if (account.type == AccountType.DEBIT || account.type == AccountType.CREDIT_CARD) {
                        // Chip: rectángulo redondeado con una línea al centro
                        val chipW = 34.dp.toPx()
                        val chipH = 26.dp.toPx()
                        val left = w - chipW - 18.dp.toPx()
                        val top = h - chipH - 16.dp.toPx()
                        drawRoundRect(
                            color = lineColor,
                            topLeft = Offset(left, top),
                            size = androidx.compose.ui.geometry.Size(chipW, chipH),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                        )
                        drawLine(lineColor, Offset(left + chipW * 0.35f, top), Offset(left + chipW * 0.35f, top + chipH), strokeWidth = 2.dp.toPx())
                    }
                }
        ) {
            if (account.type == AccountType.CASH) {
                Text(
                    text = "$",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 104.sp),
                    fontWeight = FontWeight.Bold,
                    color = accountColor.copy(alpha = 0.14f),
                    modifier = Modifier.align(Alignment.TopEnd).padding(end = 26.dp, top = 6.dp)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = account.type.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    when (account.type) {
                        AccountType.CREDIT_CARD -> Surface(
                            shape = CircleShape,
                            color = accountColor.copy(alpha = 0.22f)
                        ) {
                            Text(
                                text = stringResource(R.string.account_to_pay),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        AccountType.SAVINGS -> Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = accountColor.asIconTint(),
                            modifier = Modifier.size(28.dp)
                        )
                        else -> Icon(
                            imageVector = getIconVectorByName(account.iconName),
                            contentDescription = null,
                            tint = accountColor.asIconTint(),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Text(
                    text = (if (account.balance < 0) "- " else "") + kotlin.math.abs(account.balance).toCurrencyFormat(),
                    style = MaterialTheme.typography.headlineSmall.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.ExtraBold,
                    color = if (account.balance < 0 && account.type != AccountType.CREDIT_CARD) MaterialTheme.colorScheme.error else onSurface,
                    maxLines = 1
                )
            }
        }
    }
}
