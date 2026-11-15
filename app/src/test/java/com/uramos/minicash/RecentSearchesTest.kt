package com.uramos.minicash

import com.uramos.minicash.data.util.addRecentSearch
import com.uramos.minicash.data.util.decodeRecentSearches
import com.uramos.minicash.data.util.encodeRecentSearches
import org.junit.Assert.assertEquals
import org.junit.Test

class RecentSearchesTest {
    @Test
    fun newSearchGoesFirst() {
        assertEquals(listOf("oxxo", "super"), addRecentSearch(listOf("super"), "oxxo"))
    }

    @Test
    fun repeatedSearchMovesToFrontWithoutDuplicating() {
        assertEquals(listOf("Super", "oxxo"), addRecentSearch(listOf("oxxo", "super"), "  Super "))
    }

    @Test
    fun shortOrBlankSearchesAreIgnored() {
        val current = listOf("oxxo")
        assertEquals(current, addRecentSearch(current, ""))
        assertEquals(current, addRecentSearch(current, "  a "))
    }

    @Test
    fun listIsCappedAndKeepsTheNewest() {
        val many = (1..10).map { "busqueda$it" }
        val result = addRecentSearch(many, "nueva", max = 4)
        assertEquals(listOf("nueva", "busqueda1", "busqueda2", "busqueda3"), result)
    }

    @Test
    fun encodeAndDecodeRoundTrip() {
        val list = listOf("super", "hot dogs", "oxxo")
        assertEquals(list, decodeRecentSearches(encodeRecentSearches(list)))
        assertEquals(emptyList<String>(), decodeRecentSearches(null))
        assertEquals(emptyList<String>(), decodeRecentSearches(""))
    }
}
