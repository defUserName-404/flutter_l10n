package com.defusername.flutter_l10n.translation

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TranslationResponseParserTest {
    @Test
    fun `parse extracts simple lines`() {
        val raw = "Hello\nBonjour\nHola"
        val result = TranslationResponseParser.parse(raw, 3)
        assertEquals(listOf("Hello", "Bonjour", "Hola"), result)
    }

    @Test
    fun `parse strips markdown list markers`() {
        val raw = "- Hello\n- Bonjour\n- Hola"
        val result = TranslationResponseParser.parse(raw, 3)
        assertEquals(listOf("Hello", "Bonjour", "Hola"), result)
    }

    @Test
    fun `parse strips quote artifacts`() {
        val raw = "\"Hello\"\n'Bonjour'\n\"Hola\""
        val result = TranslationResponseParser.parse(raw, 3)
        assertEquals(listOf("Hello", "Bonjour", "Hola"), result)
    }

    @Test
    fun `parse pads with nulls when fewer lines than expected`() {
        val raw = "Hello\nBonjour"
        val result = TranslationResponseParser.parse(raw, 3)
        assertEquals(3, result.size)
        assertEquals("Hello", result[0])
        assertEquals("Bonjour", result[1])
        assertNull(result[2])
    }

    @Test
    fun `parse filters blank lines`() {
        val raw = "Hello\n\nBonjour\n  \nHola"
        val result = TranslationResponseParser.parse(raw, 3)
        assertEquals(listOf("Hello", "Bonjour", "Hola"), result)
    }

    @Test
    fun `parse returns empty list for empty input`() {
        val result = TranslationResponseParser.parse("", 0)
        assertEquals(emptyList(), result)
    }

    @Test
    fun `parse strips asterisk markers`() {
        val raw = "* Hello\n* Bonjour"
        val result = TranslationResponseParser.parse(raw, 2)
        assertEquals(listOf("Hello", "Bonjour"), result)
    }
}
