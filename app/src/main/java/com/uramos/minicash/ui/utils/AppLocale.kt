package com.uramos.minicash.ui.utils

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.text.SimpleDateFormat
import java.util.Locale

/** Idiomas que ofrece la app. Español es el predeterminado; cualquier otro idioma del sistema cae a él. */
enum class AppLanguage(val tag: String, val nativeName: String) {
    SPANISH("es", "Español"),
    ENGLISH("en", "English");

    companion object {
        /** El idioma en uso: el elegido en la app o, si no se eligió, el del sistema (español si no es uno de los dos). */
        fun current(): AppLanguage {
            val chosen = runCatching { AppCompatDelegate.getApplicationLocales() }.getOrNull()
            val language = if (chosen != null && !chosen.isEmpty) chosen[0]?.language else Locale.getDefault().language
            return if (language == ENGLISH.tag) ENGLISH else SPANISH
        }
    }
}

/** Cambia el idioma de la app; AppCompat lo guarda y vuelve a crear la pantalla con los textos nuevos. */
fun setAppLanguage(language: AppLanguage) {
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
}

/**
 * Configuración regional para fechas, nombres de mes y moneda: es-MX o en-US. El símbolo de moneda es `$` en ambas, y
 * el idioma de la interfaz decide el formato de números y fechas.
 */
fun appLocale(): Locale = when (AppLanguage.current()) {
    AppLanguage.ENGLISH -> Locale.US
    AppLanguage.SPANISH -> Locale.forLanguageTag("es-MX")
}

/** El patrón de fecha del idioma en uso: [es] para español, [en] para inglés. */
fun localizedPattern(es: String, en: String): String =
    if (AppLanguage.current() == AppLanguage.ENGLISH) en else es

private val dateFormatCache = ThreadLocal.withInitial { HashMap<String, SimpleDateFormat>() }

/** Formateador de fecha en el idioma en uso. Se guarda por patrón e idioma, así que cambiar de idioma no deja uno viejo. */
fun appDateFormat(es: String, en: String = es): SimpleDateFormat {
    val locale = appLocale()
    val pattern = localizedPattern(es, en)
    return dateFormatCache.get()!!.getOrPut("$pattern|${locale.toLanguageTag()}") { SimpleDateFormat(pattern, locale) }
}

/** Este contexto con el idioma de la app aplicado; sirve en lugares sin actividad (notificaciones, widgets, tiles). */
fun Context.localized(): Context {
    val configuration = Configuration(resources.configuration)
    configuration.setLocale(appLocale())
    return createConfigurationContext(configuration)
}

/** Acceso a los textos desde donde no hay `Context` a la mano (ViewModels, utilidades). Se inicia en [Application]. */
object AppStrings {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun get(@StringRes id: Int, vararg args: Any): String {
        val context = appContext.localized()
        // Sin argumentos no se formatea: así «Hola, %s» llega tal cual para completarse después
        return if (args.isEmpty()) context.getString(id) else context.getString(id, *args)
    }

    fun plural(@PluralsRes id: Int, quantity: Int, vararg args: Any): String =
        appContext.localized().resources.getQuantityString(id, quantity, *args)
}
