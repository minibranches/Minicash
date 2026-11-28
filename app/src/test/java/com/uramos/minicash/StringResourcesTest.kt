package com.uramos.minicash

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Español (predeterminado) e inglés deben tener exactamente las mismas claves, y ninguna vacía. */
class StringResourcesTest {

    private val resDir = listOf(File("src/main/res"), File("app/src/main/res")).first { it.exists() }

    private fun keys(folder: String): Map<String, String> {
        val result = linkedMapOf<String, String>()
        File(resDir, folder).listFiles { f -> f.name.startsWith("strings") && f.extension == "xml" }.orEmpty().forEach { file ->
            Regex("""<(string|plurals)\s+name="([^"]+)"[^>]*>(.*?)</\1>""", RegexOption.DOT_MATCHES_ALL)
                .findAll(file.readText()).forEach { m ->
                    val key = (if (m.groupValues[1] == "plurals") "plurals:" else "") + m.groupValues[2]
                    assertTrue("Clave repetida $key en $folder", result.put(key, m.groupValues[3]) == null)
                }
        }
        return result
    }

    @Test
    fun `ingles y espanol tienen las mismas claves`() {
        val es = keys("values").keys
        val en = keys("values-en").keys
        assertEquals("Faltan en inglés: ${es - en}", emptySet<String>(), es - en)
        assertEquals("Sobran en inglés: ${en - es}", emptySet<String>(), en - es)
    }

    @Test
    fun `ninguna cadena esta vacia`() {
        listOf("values", "values-en").forEach { folder ->
            keys(folder).forEach { (key, value) -> assertTrue("$key vacía en $folder", value.isNotBlank()) }
        }
    }

    @Test
    fun `los marcadores de formato coinciden entre idiomas`() {
        val es = keys("values")
        val en = keys("values-en")
        val marker = Regex("""%\d+\$[sd]""")
        es.forEach { (key, value) ->
            if (key.startsWith("plurals:")) return@forEach
            val a = marker.findAll(value).map { it.value }.toSortedSet()
            val b = marker.findAll(en[key].orEmpty()).map { it.value }.toSortedSet()
            assertEquals("Marcadores distintos en $key", a, b)
        }
    }
}
