package com.defusername.flutter_l10n.core

import com.intellij.openapi.project.Project
import com.defusername.flutter_l10n.config.L10nProjectConfig
import java.io.File

object L10nConfigLoader {
    fun load(project: Project): L10nProjectConfig {
        val basePath = project.basePath ?: return L10nProjectConfig()
        val l10nFile = File(basePath, "l10n.yaml")
        if (!l10nFile.exists()) return L10nProjectConfig()

        val scalarValues = mutableMapOf<String, String>()
        val preferredLocales = mutableListOf<String>()

        val lines = l10nFile.readLines()
        var inPreferredLocales = false

        for (rawLine in lines) {
            val noComment = rawLine.substringBefore('#')
            if (noComment.isBlank()) {
                inPreferredLocales = false
                continue
            }

            val keyMatch = Regex("""^\s*([a-zA-Z0-9-]+):\s*(.*?)\s*$""").find(noComment)
            if (keyMatch != null) {
                val key = keyMatch.groupValues[1]
                val value = keyMatch.groupValues[2]
                inPreferredLocales = key == "preferred-supported-locales"
                if (value.isNotBlank()) {
                    scalarValues[key] = value
                }
                continue
            }

            if (inPreferredLocales) {
                val localeMatch = Regex("""^\s*-\s*([A-Za-z0-9_-]+)\s*$""").find(noComment)
                if (localeMatch != null) {
                    preferredLocales.add(localeMatch.groupValues[1])
                }
            }
        }

        fun bool(key: String, fallback: Boolean): Boolean {
            return scalarValues[key]?.lowercase()?.let { it == "true" } ?: fallback
        }

        return L10nProjectConfig(
            arbDir = scalarValues["arb-dir"] ?: "assets/l10n",
            templateArbFile = scalarValues["template-arb-file"] ?: "app_en.arb",
            outputLocalizationFile = scalarValues["output-localization-file"] ?: "app_localizations.dart",
            outputClass = scalarValues["output-class"] ?: "AppLocalizations",
            outputDir = scalarValues["output-dir"] ?: "lib/core/config/l10n",
            preferredSupportedLocales = if (preferredLocales.isNotEmpty()) preferredLocales else listOf("en"),
            syntheticLocale = bool("synthetic-locale", false),
            nullableGetter = bool("nullable-getter", false),
        )
    }
}
