package com.uramos.minicash.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.util.TypedValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.core.content.res.ResourcesCompat
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.unit.ColorProvider
import com.uramos.minicash.R
import com.uramos.minicash.data.preferences.FontPreference

/** Peso del texto del widget; coincide con las tres tipografías incluidas de cada fuente. */
enum class WidgetWeight { REGULAR, SEMIBOLD, BOLD }

private fun fontRes(font: FontPreference, weight: WidgetWeight): Int = when (font) {
    FontPreference.ROUNDED_QUICKSAND -> when (weight) {
        WidgetWeight.REGULAR -> R.font.quicksand_regular
        WidgetWeight.SEMIBOLD -> R.font.quicksand_semibold
        WidgetWeight.BOLD -> R.font.quicksand_bold
    }
    FontPreference.GOOGLE_SANS_FLEX -> when (weight) {
        WidgetWeight.REGULAR -> R.font.google_sans_flex_regular
        WidgetWeight.SEMIBOLD -> R.font.google_sans_flex_semibold
        WidgetWeight.BOLD -> R.font.google_sans_flex_bold
    }
}

/** Tipografía real de la app (los TTF de res/font) para el peso pedido; si no carga, la del sistema. */
fun widgetTypeface(context: Context, font: FontPreference, weight: WidgetWeight): Typeface =
    ResourcesCompat.getFont(context, fontRes(font, weight)) ?: Typeface.DEFAULT

/**
 * Dibuja [text] en blanco sobre fondo transparente con la fuente elegida en la app. Los widgets (RemoteViews)
 * no pueden usar fuentes propias, así que el texto se entrega como imagen; al ir en blanco se tiñe después con el
 * color del tema, que sigue el modo claro u oscuro del sistema. Si no cabe en [maxWidthPx] se reduce el tamaño.
 */
fun renderWidgetText(
    context: Context,
    text: String,
    sizeSp: Float,
    typeface: Typeface,
    tabularFigures: Boolean,
    maxWidthPx: Int?
): Bitmap {
    val metrics = context.resources.displayMetrics
    var sizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sizeSp, metrics)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        this.typeface = typeface
        if (tabularFigures) fontFeatureSettings = "tnum"
    }
    paint.textSize = sizePx
    var width = paint.measureText(text)
    if (maxWidthPx != null && width > maxWidthPx && width > 0f) {
        sizePx *= maxWidthPx / width
        paint.textSize = sizePx
        width = paint.measureText(text)
    }
    val fm = paint.fontMetrics
    val bitmap = Bitmap.createBitmap(
        width.toInt().coerceAtLeast(1) + 2,
        (fm.descent - fm.ascent).toInt().coerceAtLeast(1) + 2,
        Bitmap.Config.ARGB_8888
    )
    Canvas(bitmap).drawText(text, 1f, 1f - fm.ascent, paint)
    return bitmap
}

/** Texto del widget con la fuente de la app, teñido con [color] del tema y con el texto como descripción. */
@Composable
fun FontText(
    text: String,
    sizeSp: Float,
    weight: WidgetWeight,
    color: ColorProvider,
    font: FontPreference,
    modifier: GlanceModifier = GlanceModifier,
    tabularFigures: Boolean = false,
    maxWidth: Dp? = null
) {
    val context = LocalContext.current
    val maxWidthPx = maxWidth?.let { TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, it.value, context.resources.displayMetrics).toInt() }
    val bitmap = remember(text, sizeSp, weight, font, tabularFigures, maxWidthPx) {
        renderWidgetText(context, text, sizeSp, widgetTypeface(context, font, weight), tabularFigures, maxWidthPx)
    }
    Image(
        provider = ImageProvider(bitmap),
        contentDescription = text,
        colorFilter = ColorFilter.tint(color),
        modifier = modifier
    )
}
