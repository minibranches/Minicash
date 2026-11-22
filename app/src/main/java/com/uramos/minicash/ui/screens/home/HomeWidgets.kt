package com.uramos.minicash.ui.screens.home

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
                                text = "Patrimonio",
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
                    text = if (accountCount == 1) "Suma de tu cuenta" else "Suma de tus $accountCount cuentas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = onContainer.copy(alpha = 0.72f)
                )
            }
        }
    }
}

/** Tarjeta de cuenta alta: ícono en una forma expresiva, nombre, tipo de cuenta y saldo. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AccountCard(
    account: AccountEntity,
    modifier: Modifier = Modifier.width(156.dp).height(148.dp),
    onClick: (() -> Unit)? = null
) {
    val accountColor = MinicashColorUtils.parseHexColor(account.colorHex, MaterialTheme.colorScheme.primary)
    val containerColor = MinicashColorUtils.tintedContainerColor(
        baseColor = accountColor,
        surfaceColor = MaterialTheme.colorScheme.cardSurface,
        ratio = 0.18f
    )
    val iconShape = MaterialShapes.Cookie9Sided.toShape()

    MinicashCard(
        level = CardTonalLevel.LOW,
        containerColor = containerColor,
        shape = MinicashShapes.Card,
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = iconShape,
                color = accountColor.copy(alpha = 0.28f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getIconVectorByName(account.iconName),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = accountColor.asIconTint()
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = account.type.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = (if (account.balance < 0) "- " else "") + kotlin.math.abs(account.balance).toCurrencyFormat(),
                    style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.ExtraBold,
                    color = if (account.balance < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
        }
    }
}
