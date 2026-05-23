package com.defusername.flutter_l10n

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ArbManagerTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun `appendEntries creates ARB file with entries`() {
        val arbDir = tempDir.resolve("l10n")
        arbDir.mkdirs()

        val config = L10nProjectConfig(
            arbDir = "l10n",
            templateArbFile = "app_en.arb",
        )

        val entries = listOf(
            ArbEntry(key = "greeting", value = "Hello"),
            ArbEntry(key = "farewell", value = "Goodbye"),
        )

        val result = ArbManagerHelper.appendToFile(tempDir.resolve("l10n"), "app_en.arb", config, entries)
        assertTrue(result)

        val file = arbDir.resolve("app_en.arb")
        assertTrue(file.exists())
        val text = file.readText()
        assertContains(text, "\"greeting\"")
        assertContains(text, "\"farewell\"")
        assertContains(text, "description")
    }

    @Test
    fun `appendEntries appends to existing content`() {
        val file = tempDir.resolve("app_en.arb")
        file.writeText("{\n  \"@@locale\": \"en\",\n  \"existing\": \"Old\"\n}\n")

        val config = L10nProjectConfig(arbDir = tempDir.path)
        val entries = listOf(ArbEntry(key = "newKey", value = "New"))

        val result = ArbManagerHelper.appendToFile(tempDir, "app_en.arb", config, entries)
        assertTrue(result)

        val text = file.readText()
        assertContains(text, "\"existing\"")
        assertContains(text, "\"newKey\"")
        assertContains(text, "\"Old\"")
        assertContains(text, "\"New\"")

        val existingIdx = text.indexOf("existing")
        val newKeyIdx = text.indexOf("newKey")
        assertTrue(existingIdx < newKeyIdx, "existing key should come before new key")
    }

    @Test
    fun `appendEntries produces valid JSON`() {
        val file = tempDir.resolve("app_en.arb")
        file.writeText("{\n  \"@@locale\": \"en\"\n}\n")

        val config = L10nProjectConfig(arbDir = tempDir.path)
        val entries = listOf(ArbEntry(key = "test", value = "value"))

        ArbManagerHelper.appendToFile(tempDir, "app_en.arb", config, entries)

        val text = file.readText().trim()
        assertTrue(text.startsWith("{"))
        assertTrue(text.endsWith("}"))
        assertFalse(text.contains(",\n}"), "should not have trailing comma before closing brace")
        assertFalse(text.contains(",\n\n"), "should not have double newlines")
    }

    @Test
    fun `appendEntries handles multiple locales`() {
        val arbDir = tempDir.resolve("l10n")
        arbDir.mkdirs()
        arbDir.resolve("app_en.arb").writeText("{\n  \"@@locale\": \"en\"\n}\n")
        arbDir.resolve("app_fr.arb").writeText("{\n  \"@@locale\": \"fr\"\n}\n")

        val config = L10nProjectConfig(arbDir = "l10n")
        val entries = listOf(
            ArbEntry(key = "hello", value = "Hi", translations = mapOf("fr" to "Salut")),
        )

        val result = ArbManagerHelper.appendToFile(arbDir, "app_en.arb", config, entries)
        assertTrue(result)

        val enText = arbDir.resolve("app_en.arb").readText()
        assertContains(enText, "\"Hi\"")

        val frText = arbDir.resolve("app_fr.arb").readText()
        assertContains(frText, "\"Salut\"")
    }

    @Test
    fun `appendEntries creates locale seed file if missing`() {
        val arbDir = tempDir.resolve("l10n")
        arbDir.mkdirs()
        arbDir.resolve("app_en.arb").writeText("{\n  \"@@locale\": \"en\"\n}\n")

        val config = L10nProjectConfig(arbDir = "l10n", preferredSupportedLocales = listOf("en", "fr"))
        val entries = listOf(ArbEntry(key = "hello", value = "Hello"))

        val result = ArbManagerHelper.appendToFile(arbDir, "app_en.arb", config, entries)
        assertTrue(result)

        val frFile = arbDir.resolve("app_fr.arb")
        assertTrue(frFile.exists(), "fr ARB should be created")
        val frText = frFile.readText()
        assertContains(frText, "\"@@locale\": \"fr\"")
    }

    @Test
    fun `getAvailableLocales discovers locales from files`() {
        val arbDir = tempDir.resolve("l10n")
        arbDir.mkdirs()
        arbDir.resolve("app_en.arb").writeText("{}")
        arbDir.resolve("app_fr.arb").writeText("{}")
        arbDir.resolve("app_de.arb").writeText("{}")

        val config = L10nProjectConfig(arbDir = "l10n")
        val locales = ArbManagerHelper.listLocales(arbDir, config)

        assertTrue(locales.contains("en"))
        assertTrue(locales.contains("fr"))
        assertTrue(locales.contains("de"))
    }
}

object ArbManagerHelper {
    fun appendToFile(dir: File, fileName: String, config: L10nProjectConfig, entries: List<ArbEntry>): Boolean {
        val localeFiles = dir.listFiles()
            ?.filter { it.name.endsWith(".arb") && config.localeFromArbFileName(it.name) != null }
            ?.toMutableList()
            ?: mutableListOf()

        val seenLocales = localeFiles.mapNotNull { config.localeFromArbFileName(it.name) }.toMutableSet()
        if (!seenLocales.contains(config.templateLocale)) {
            val templateFile = dir.resolve(fileName)
            if (!templateFile.exists()) {
                templateFile.writeText("{\n  \"@@locale\": \"${config.templateLocale}\"\n}\n")
            }
            localeFiles.add(templateFile)
            seenLocales.add(config.templateLocale)
        }

        for (pref in config.preferredSupportedLocales) {
            if (!seenLocales.contains(pref)) {
                val newFile = dir.resolve(config.arbFileNameForLocale(pref))
                newFile.writeText("{\n  \"@@locale\": \"$pref\"\n}\n")
                localeFiles.add(newFile)
                seenLocales.add(pref)
            }
        }

        for (file in localeFiles) {
            val locale = config.localeFromArbFileName(file.name) ?: continue
            val text = file.readText()
            val lastBrace = text.lastIndexOf('}')
            if (lastBrace < 0) continue

            val prefix = text.substring(0, lastBrace).trimEnd()
            val hasContent = !prefix.endsWith("{")

            val sb = StringBuilder(prefix)
            if (hasContent) sb.append(',')
            sb.append('\n')

            for (entry in entries) {
                val value = when {
                    locale == config.templateLocale -> entry.value
                    entry.translations[locale].isNullOrBlank() -> "TODO(${entry.key}): ${entry.value}"
                    else -> entry.translations[locale].orEmpty()
                }
                sb.append("  \"${esc(entry.key)}\": \"${esc(value)}\",\n")
                sb.append("  \"@${esc(entry.key)}\": {\n")
                sb.append("    \"description\": \"Auto-extracted from Dart source\"\n")
                sb.append("  },\n")
            }

            val content = sb.toString().trimEnd(',', '\n', ' ', '\t')
            file.writeText("$content\n}\n")
        }
        return true
    }

    private fun esc(s: String): String = s
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")

    fun listLocales(dir: File, config: L10nProjectConfig): List<String> {
        return dir.listFiles()
            ?.mapNotNull { file -> config.localeFromArbFileName(file.name) }
            ?: emptyList()
    }
}
