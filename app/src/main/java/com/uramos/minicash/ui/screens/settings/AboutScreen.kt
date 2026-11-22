package com.uramos.minicash.ui.screens.settings

import com.uramos.minicash.ui.theme.screenBackground
import com.uramos.minicash.ui.theme.sheetBackground
import com.uramos.minicash.ui.components.plusTop
import com.uramos.minicash.ui.components.withoutTop
import com.uramos.minicash.ui.components.underHeaderBlur
import androidx.compose.foundation.lazy.rememberLazyListState
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.uramos.minicash.ui.components.MinicashLogo
import com.uramos.minicash.ui.components.MinicashOptionGroup
import com.uramos.minicash.ui.components.MinicashOptionRow
import com.uramos.minicash.ui.components.MinicashSectionLabel
import com.uramos.minicash.ui.components.MinicashSubScreenTopBar
import com.uramos.minicash.ui.theme.MinicashThemeExtensions
import java.io.File

private const val REPOSITORY_URL = "https://github.com/minibranches/minicash"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBackClick: () -> Unit) {
    val spacing = MinicashThemeExtensions.spacing
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val versionName = remember { appVersionName(context) }

    val fadeListState = rememberLazyListState()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.screenBackground,
        topBar = {
            MinicashSubScreenTopBar(title = "Acerca de", onBack = onBackClick, scrollBehavior = scrollBehavior)
        }
    ) { paddingValues ->
        LazyColumn(
            state = fadeListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues.withoutTop())
                .underHeaderBlur(paddingValues.calculateTopPadding(), bottomBand = 64.dp, listState = fadeListState),
            contentPadding = PaddingValues(horizontal = spacing.standard, vertical = spacing.small).plusTop(paddingValues.calculateTopPadding()),
            verticalArrangement = Arrangement.spacedBy(spacing.small)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = spacing.large),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.size(132.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) { MinicashLogo(size = 88.dp) }
                    }
                    Spacer(Modifier.height(spacing.small))
                    Text(
                        text = "Minicash",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Finanzas personales, locales y sin cuentas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                        Text(
                            text = "Versión $versionName",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            item { MinicashSectionLabel("Conectar") }
            item {
                MinicashOptionGroup {
                    MinicashOptionRow(
                        title = "Código en GitHub",
                        subtitle = "minibranches/minicash",
                        icon = Icons.Default.Code,
                        onClick = { openLink(context, REPOSITORY_URL) }
                    )
                }
            }

            item { MinicashSectionLabel("Compartir") }
            item {
                MinicashOptionGroup {
                    MinicashOptionRow(
                        title = "Compartir Minicash",
                        subtitle = "Envía el instalador de esta versión a otra persona",
                        icon = Icons.Default.Share,
                        onClick = { shareApk(context, versionName) }
                    )
                }
            }
            item { Spacer(Modifier.height(96.dp)) }
        }
    }
}

private fun appVersionName(context: Context): String =
    runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
        .getOrNull() ?: "1.0"

private fun openLink(context: Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

/** Copia el APK instalado a la caché y abre el menú de compartir de Android. */
private fun shareApk(context: Context, versionName: String) {
    runCatching {
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val target = File(dir, "Minicash-$versionName.apk")
        File(context.applicationInfo.sourceDir).copyTo(target, overwrite = true)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", target)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.android.package-archive"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "Compartir Minicash").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
