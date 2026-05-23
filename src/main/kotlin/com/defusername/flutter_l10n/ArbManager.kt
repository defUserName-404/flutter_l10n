package com.defusername.flutter_l10n

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import org.json.JSONObject
import java.io.File

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

    fun readArb(project: Project, config: L10nProjectConfig, locale: String = config.templateLocale): JSONObject {
        val file = getArbFile(project, config, locale)
        if (file == null || !file.exists()) return JSONObject()
        return runCatching { JSONObject(file.readText()) }.getOrElse { JSONObject() }
    }

    fun existingMessageKeys(project: Project, config: L10nProjectConfig): Set<String> {
        val json = readArb(project, config)
        val keys = mutableSetOf<String>()
        val iter = json.keys()
        while (iter.hasNext()) {
            val key = iter.next()
            if (!key.startsWith("@")) {
                keys.add(key)
            }
        }
        return keys
    }

    fun addEntries(project: Project, config: L10nProjectConfig, entries: List<ArbEntry>): Boolean {
        if (entries.isEmpty()) return true

        val locales = getAvailableLocales(project, config)

        return runCatching {
            for (locale in locales) {
                val file = getOrCreateArbFile(project, config, locale) ?: continue
                val json = runCatching { JSONObject(file.readText()) }
                    .getOrElse { JSONObject().put("@@locale", locale) }

                for (entry in entries) {
                    if (!json.has(entry.key)) {
                        val localizedValue = when {
                            locale == config.templateLocale -> entry.value
                            entry.translations[locale].isNullOrBlank() -> "TODO(${entry.key}): ${entry.value}"
                            else -> entry.translations[locale].orEmpty()
                        }

                        json.put(entry.key, localizedValue)
                    }

                    val metaKey = "@${entry.key}"
                    if (!json.has(metaKey)) {
                        json.put(
                            metaKey,
                            JSONObject().put("description", "Auto-extracted from Dart source"),
                        )
                    }
                }

                file.writeText(json.toString(2) + "\n")
                LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)
            }
            true
        }.getOrElse {
            false
        }
    }

    private fun getArbFile(project: Project, config: L10nProjectConfig, locale: String): File? {
        val basePath = project.basePath ?: return null
        return File(basePath, "${config.arbDir}/${config.arbFileNameForLocale(locale)}")
    }

    private fun getOrCreateArbFile(project: Project, config: L10nProjectConfig, locale: String): File? {
        val file = getArbFile(project, config, locale) ?: return null
        file.parentFile?.mkdirs()
        if (!file.exists()) {
            val seed = JSONObject().put("@@locale", locale)
            file.writeText(seed.toString(2) + "\n")
        }
        return file
    }
}
