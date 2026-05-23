package com.defusername.flutter_l10n.arb

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.defusername.flutter_l10n.ArbEntry
import com.defusername.flutter_l10n.config.L10nProjectConfig
import java.io.File

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

    fun upsertEntries(project: Project, config: L10nProjectConfig, entries: List<ArbEntry>): Boolean {
        if (entries.isEmpty()) return true
        if (project.basePath == null) return false

        val locales = getAvailableLocales(project, config)

        return runCatching {
            for (locale in locales) {
                val file = getOrCreateArbFile(project, config, locale) ?: continue
                val isTemplate = locale == config.templateLocale
                val text = file.readText()
                val updated = ArbUpsertService.upsertContent(text, entries, locale, isTemplate)
                file.writeText(updated)
                LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)
            }
            true
        }.getOrElse { false }
    }

    private fun getOrCreateArbFile(project: Project, config: L10nProjectConfig, locale: String): File? {
        val basePath = project.basePath ?: return null
        val file = File(basePath, "${config.arbDir}/${config.arbFileNameForLocale(locale)}")
        file.parentFile?.mkdirs()
        if (!file.exists()) {
            file.writeText("{\n  \"@@locale\": \"$locale\"\n}\n")
        }
        return file
    }
}
