package com.defusername.flutter_l10n.extraction

import com.defusername.flutter_l10n.ExtractedString
import com.defusername.flutter_l10n.SelectedEntry
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DefaultArbEntryMapperTest {
    private val mapper = DefaultArbEntryMapper()

    @Test
    fun `toArbEntries uses manually edited value`() {
        val selected = listOf(
            SelectedEntry(
                extracted = ExtractedString(raw = "Original", suggestedKey = "original", startOffset = 1, endOffset = 10),
                editedValue = "Edited by user",
                key = "greeting",
            ),
        )

        val mapped = mapper.toArbEntries(selected, emptyMap())

        assertEquals(1, mapped.size)
        assertEquals("greeting", mapped[0].key)
        assertEquals("Edited by user", mapped[0].value)
    }

    @Test
    fun `toArbEntries includes locale translations for key`() {
        val selected = listOf(
            SelectedEntry(
                extracted = ExtractedString(raw = "Hi", suggestedKey = "hi", startOffset = 1, endOffset = 4),
                editedValue = "Hello",
                key = "hello",
            ),
        )
        val translations = mapOf("hello" to mapOf("fr" to "Bonjour", "es" to "Hola"))

        val mapped = mapper.toArbEntries(selected, translations)

        assertEquals(mapOf("fr" to "Bonjour", "es" to "Hola"), mapped[0].translations)
    }
}
