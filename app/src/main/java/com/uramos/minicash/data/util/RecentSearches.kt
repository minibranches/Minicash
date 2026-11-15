package com.uramos.minicash.data.util

/** Cuántas búsquedas recientes se guardan. */
const val MAX_RECENT_SEARCHES = 8

/** Búsquedas más cortas que esto no se guardan: casi nunca son lo que el usuario quiere repetir. */
const val MIN_RECENT_SEARCH_LENGTH = 2

/**
 * Agrega [query] al inicio de las búsquedas recientes. Se ignora si es muy corta y, si ya estaba (sin importar
 * mayúsculas), se mueve al inicio en lugar de duplicarse. La lista nunca pasa de [max].
 */
fun addRecentSearch(current: List<String>, query: String, max: Int = MAX_RECENT_SEARCHES): List<String> {
    val clean = query.trim().replace(Regex("\\s+"), " ")
    if (clean.length < MIN_RECENT_SEARCH_LENGTH) return current
    return (listOf(clean) + current.filterNot { it.equals(clean, ignoreCase = true) }).take(max)
}

/** Guardado en una sola cadena: una búsqueda por línea (las búsquedas nunca contienen saltos de línea). */
fun encodeRecentSearches(searches: List<String>): String = searches.joinToString("\n")

fun decodeRecentSearches(raw: String?): List<String> =
    raw.orEmpty().split('\n').map { it.trim() }.filter { it.isNotEmpty() }
