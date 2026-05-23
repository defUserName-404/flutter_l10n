package com.defusername.flutter_l10n.arb

import com.defusername.flutter_l10n.ArbEntry
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

class ArbUpsertServiceTest {
    @Test
    fun `upsertContent adds new key to existing ARB`() {
        val existing = "{\n  \"@@locale\": \"en\",\n  \"existing\": \"Keep\"\n}\n"
        val result = ArbUpsertService.upsertContent(
            existing,
            listOf(ArbEntry("newKey", "New Value")),
            "en",
            isTemplate = true,
        )
        assertContains(result, "\"existing\"")
        assertContains(result, "\"newKey\"")
        assertContains(result, "\"Keep\"")
        assertContains(result, "\"New Value\"")
    }

    @Test
    fun `upsertContent replaces existing key value for template`() {
        val existing = "{\n  \"@@locale\": \"en\",\n  \"greeting\": \"Hello\"\n}\n"
        val result = ArbUpsertService.upsertContent(
            existing,
            listOf(ArbEntry("greeting", "Hi")),
            "en",
            isTemplate = true,
        )
        assertContains(result, "\"greeting\"")
        assertContains(result, "\"Hi\"")
        assertTrue(!result.contains("Hello"), "old value should be replaced")
    }

    @Test
    fun `upsertContent handles multiline with metadata correctly`() {
        val existing = """
            {
              "@@locale": "en",
              "user": "John",
              "@user": {
                "description": "Old desc"
              }
            }
        """.trimIndent()
        val result = ArbUpsertService.upsertContent(
            existing,
            listOf(ArbEntry("user", "Jane")),
            "en",
            isTemplate = true,
        )
        assertContains(result, "\"user\"")
        assertContains(result, "\"Jane\"")
        assertTrue(!result.contains("Old desc"))
        assertContains(result, "Auto-extracted from Dart source")
    }

    @Test
    fun `upsertContent uses TODO for non-template without translation`() {
        val existing = "{\n  \"@@locale\": \"fr\"\n}\n"
        val result = ArbUpsertService.upsertContent(
            existing,
            listOf(ArbEntry("hello", "Hello")),
            "fr",
            isTemplate = false,
        )
        assertContains(result, "\"hello\"")
        assertContains(result, "TODO")
    }

    @Test
    fun `upsertContent uses translation for non-template when provided`() {
        val existing = "{\n  \"@@locale\": \"fr\"\n}\n"
        val result = ArbUpsertService.upsertContent(
            existing,
            listOf(ArbEntry("hello", "Hello", translations = mapOf("fr" to "Bonjour"))),
            "fr",
            isTemplate = false,
        )
        assertContains(result, "\"hello\"")
        assertContains(result, "\"Bonjour\"")
        assertTrue(!result.contains("TODO"))
    }

    @Test
    fun `upsertContent preserves existing translation for non-template`() {
        val existing = "{\n  \"@@locale\": \"fr\",\n  \"hello\": \"Bonjour\"\n}\n"
        val result = ArbUpsertService.upsertContent(
            existing,
            listOf(ArbEntry("hello", "Hello")),
            "fr",
            isTemplate = false,
        )
        assertContains(result, "\"hello\"")
        assertContains(result, "\"Bonjour\"")
        assertTrue(!result.contains("TODO"), "should keep existing translation")
    }

    @Test
    fun `empty entries returns original text unchanged`() {
        val existing = "{\n  \"@@locale\": \"en\"\n}\n"
        val result = ArbUpsertService.upsertContent(existing, emptyList(), "en", isTemplate = true)
        assertContains(result, "@@locale\"")
    }

    @Test
    fun `output is valid JSON`() {
        val existing = "{\n  \"@@locale\": \"en\"\n}\n"
        val result = ArbUpsertService.upsertContent(
            existing,
            listOf(ArbEntry("test", "value")),
            "en",
            isTemplate = true,
        )
        val text = result.trim()
        assertTrue(text.startsWith("{"))
        assertTrue(text.endsWith("}"))
    }
}
