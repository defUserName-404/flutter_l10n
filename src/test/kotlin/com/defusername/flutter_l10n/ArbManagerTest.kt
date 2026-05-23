package com.defusername.flutter_l10n

import com.defusername.flutter_l10n.arb.ArbUpsertService
import com.defusername.flutter_l10n.config.L10nProjectConfig
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertContains
import kotlin.test.assertTrue

class ArbManagerTest {
    @TempDir
    lateinit var tempDir: File

    private fun config() = L10nProjectConfig(arbDir = "l10n")

    private fun entries(vararg pairs: Pair<String, String>): List<ArbEntry> =
        pairs.map { ArbEntry(key = it.first, value = it.second) }

    @Test
    fun `upsertContent adds new entries to empty file`() {
        val text = "{\n  \"@@locale\": \"en\"\n}\n"
        val result = ArbUpsertService.upsertContent(text, entries("greeting" to "Hello"), "en", isTemplate = true)

        assertContains(result, "\"greeting\"")
        assertContains(result, "\"Hello\"")
        assertContains(result, "\"@greeting\"")
        assertContains(result, "\"description\"")
        assertTrue(result.trim().endsWith("}"), "should end with closing brace")
    }

    @Test
    fun `upsertContent overwrites existing template value`() {
        val text = "{\n  \"@@locale\": \"en\",\n  \"greeting\": \"Old\",\n  \"@greeting\": {\n    \"description\": \"Original\"\n  }\n}\n"
        val result = ArbUpsertService.upsertContent(text, entries("greeting" to "New"), "en", isTemplate = true)

        assertContains(result, "\"greeting\"")
        assertContains(result, "\"New\"")
        assertTrue(!result.contains("\"Old\""), "old value should be replaced")
        assertContains(result, "\"Auto-extracted from Dart source\"")
    }

    @Test
    fun `upsertContent preserves non-updated existing entries`() {
        val text = "{\n  \"@@locale\": \"en\",\n  \"existing\": \"Old\",\n  \"@existing\": {\n    \"description\": \"Existing\"\n  }\n}\n"
        val result = ArbUpsertService.upsertContent(text, entries("greeting" to "New"), "en", isTemplate = true)

        assertContains(result, "\"existing\"")
        assertContains(result, "\"Old\"")
        assertContains(result, "\"greeting\"")
        assertContains(result, "\"New\"")
    }

    @Test
    fun `upsertContent handles non-template locale with TODO`() {
        val text = "{\n  \"@@locale\": \"fr\"\n}\n"
        val result = ArbUpsertService.upsertContent(
            text,
            listOf(ArbEntry(key = "hello", value = "Hi")),
            "fr",
            isTemplate = false,
        )

        assertContains(result, "\"hello\"")
        assertContains(result, "TODO")
        assertContains(result, "Hi")
    }

    @Test
    fun `upsertContent uses translation for non-template locale`() {
        val text = "{\n  \"@@locale\": \"fr\"\n}\n"
        val result = ArbUpsertService.upsertContent(
            text,
            listOf(ArbEntry(key = "hello", value = "Hi", translations = mapOf("fr" to "Salut"))),
            "fr",
            isTemplate = false,
        )

        assertContains(result, "\"hello\"")
        assertContains(result, "\"Salut\"")
        assertTrue(!result.contains("TODO"), "should not contain TODO when translation is provided")
    }

    @Test
    fun `upsertContent produces valid JSON structure`() {
        val text = "{\n  \"@@locale\": \"en\"\n}\n"
        val result = ArbUpsertService.upsertContent(text, entries("test" to "value"), "en", isTemplate = true)
        val trimmed = result.trim()

        assertTrue(trimmed.startsWith("{"))
        assertTrue(trimmed.endsWith("}"))
        assertTrue(!trimmed.contains(",\n}"), "should not have trailing comma before closing brace")
    }

    @Test
    fun `upsertContent handles multiple entries`() {
        val text = "{\n  \"@@locale\": \"en\"\n}\n"
        val result = ArbUpsertService.upsertContent(
            text,
            entries("hello" to "Hi", "farewell" to "Bye"),
            "en",
            isTemplate = true,
        )

        assertContains(result, "\"hello\"")
        assertContains(result, "\"farewell\"")
        assertContains(result, "\"Hi\"")
        assertContains(result, "\"Bye\"")
    }

    @Test
    fun `upsertContent overwrites existing key metadata`() {
        val text = "{\n  \"@@locale\": \"en\",\n  \"user\": \"John\",\n  \"@user\": {\n    \"description\": \"Old description\"\n  }\n}\n"
        val result = ArbUpsertService.upsertContent(text, entries("user" to "Jane"), "en", isTemplate = true)

        assertContains(result, "\"user\"")
        assertContains(result, "\"Jane\"")
        assertTrue(!result.contains("Old description"), "metadata description should be replaced")
        assertContains(result, "Auto-extracted from Dart source")
    }
}
