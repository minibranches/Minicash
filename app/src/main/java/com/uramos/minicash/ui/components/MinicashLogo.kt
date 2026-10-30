package com.uramos.minicash.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.uramos.minicash.R

@Composable
fun MinicashLogo(
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    Image(
        painter = painterResource(id = R.drawable.ic_minicash_coin),
        contentDescription = "Minicash",
        modifier = modifier.size(size)
    )
}

@Composable
fun MinicashLogoWithText(
    modifier: Modifier = Modifier,
    logoSize: Dp = 36.dp
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MinicashLogo(size = logoSize)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Minicash",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
