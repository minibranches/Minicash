package com.uramos.minicash.ui.screens.settings

import com.uramos.minicash.ui.utils.AppStrings
import androidx.compose.ui.res.stringResource
import com.uramos.minicash.R
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
import com.uramos.minicash.ui.components.MinicashRowGroup
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
            MinicashSubScreenTopBar(title = stringResource(R.string.set_group_about), onBack = onBackClick, scrollBehavior = scrollBehavior)
        }
    ) { paddingValues ->
        LazyColumn(
            state = fadeListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues.withoutTop())
                .underHeaderBlur(paddingValues.calculateTopPadding(), bottomBand = 64.dp, listState = fadeListState),
            contentPadding = PaddingValues(horizontal = spacing.screen, vertical = spacing.small).plusTop(paddingValues.calculateTopPadding()),
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
                        text = stringResource(R.string.set_about_tagline),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                        Text(
                            text = stringResource(R.string.set_about_version, versionName),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            item { MinicashSectionLabel(stringResource(R.string.set_about_connect)) }
            item {
                MinicashRowGroup {
                    MinicashOptionRow(
                        title = stringResource(R.string.set_about_github),
                        subtitle = "minibranches/minicash",
                        icon = Icons.Default.Code,
                        onClick = { openLink(context, REPOSITORY_URL) }
                    )
                }
            }

            item { MinicashSectionLabel(stringResource(R.string.set_about_share)) }
            item {
                MinicashRowGroup {
                    MinicashOptionRow(
                        title = stringResource(R.string.set_about_share_app),
                        subtitle = stringResource(R.string.set_about_share_sub),
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
        context.startActivity(Intent.createChooser(send, AppStrings.get(R.string.set_about_share_app)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
