package com.defusername.flutter_l10n

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File
import org.json.JSONObject

data class ArbEntry(
    val key: String,
    val value: String,
    val translations: Map<String, String> = emptyMap(),
)

object ArbManager {
    fun getAvailableLocales(project: Project, config: L10nProjectConfig): List<String> {
        val basePath = project.basePath ?: return config.preferredSupportedLocales
        val arbDir = File(basePath, config.arbDir)
        if (!arbDir.exists()) {
            return config.preferredSupportedLocales
        }

        val locales = arbDir.listFiles()
            ?.mapNotNull { file -> config.localeFromArbFileName(file.name) }
            ?.toSet()
            ?.toMutableSet()
            ?: mutableSetOf()

        locales.add(config.templateLocale)
        locales.addAll(config.preferredSupportedLocales)

        return locales.sorted()
    }

    fun appendEntries(project: Project, config: L10nProjectConfig, entries: List<ArbEntry>): Boolean {
        if (entries.isEmpty()) return true
        if (project.basePath == null) return false

        val locales = getAvailableLocales(project, config)

        return runCatching {
            for (locale in locales) {
                val file = getOrCreateArbFile(project, config, locale) ?: continue
                val text = file.readText()

                val existingKeys = parseExistingKeys(text)
                val newEntries = entries.filter { it.key !in existingKeys }
                if (newEntries.isEmpty()) continue

                val lastBrace = text.lastIndexOf('}')
                if (lastBrace < 0) {
                    file.writeText(buildFreshContent(locale, config, newEntries))
                    LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)
                    continue
                }

                val prefix = text.substring(0, lastBrace).trimEnd()
                val hasContent = !prefix.endsWith("{")

                val sb = StringBuilder(prefix)
                if (hasContent) sb.append(',')
                sb.append('\n')

                for (entry in newEntries) {
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
                LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)
            }
            true
        }.getOrElse {
            false
        }
    }

    private fun parseExistingKeys(text: String): Set<String> {
        return runCatching {
            val json = JSONObject(text)
            val keys = mutableSetOf<String>()
            val iter = json.keys()
            while (iter.hasNext()) {
                val key = iter.next()
                if (!key.startsWith("@") && key != "@@locale") keys.add(key)
            }
            keys
        }.getOrElse { emptySet() }
    }

    private fun buildFreshContent(locale: String, config: L10nProjectConfig, entries: List<ArbEntry>): String {
        val sb = StringBuilder("{\n  \"@@locale\": \"$locale\"")
        for (entry in entries) {
            val value = if (locale == config.templateLocale) entry.value else entry.value
            sb.append(",\n  \"${esc(entry.key)}\": \"${esc(value)}\",\n")
            sb.append("  \"@${esc(entry.key)}\": {\n")
            sb.append("    \"description\": \"Auto-extracted from Dart source\"\n")
            sb.append("  }")
        }
        sb.append("\n}\n")
        return sb.toString()
    }

    private fun esc(s: String): String = s
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")

    private fun getArbFile(project: Project, config: L10nProjectConfig, locale: String): File? {
        val basePath = project.basePath ?: return null
        return File(basePath, "${config.arbDir}/${config.arbFileNameForLocale(locale)}")
    }

    private fun getOrCreateArbFile(project: Project, config: L10nProjectConfig, locale: String): File? {
        val file = getArbFile(project, config, locale) ?: return null
        file.parentFile?.mkdirs()
        if (!file.exists()) {
            file.writeText("{\n  \"@@locale\": \"$locale\"\n}\n")
        }
        return file
    }
}
