package com.defusername.flutter_l10n

import com.intellij.openapi.project.Project
import java.io.File

data class L10nProjectConfig(
    val arbDir: String = "assets/l10n",
    val templateArbFile: String = "app_en.arb",
    val outputLocalizationFile: String = "app_localizations.dart",
    val outputClass: String = "AppLocalizations",
    val outputDir: String = "lib/core/config/l10n",
    val preferredSupportedLocales: List<String> = listOf("en"),
    val syntheticLocale: Boolean = false,
    val nullableGetter: Boolean = false,
) {
    private val templateMatch = Regex("""^(.*)_([A-Za-z0-9_-]+)\.arb$""").find(templateArbFile)

    val templatePrefix: String
        get() = templateMatch?.groupValues?.get(1) ?: templateArbFile.removeSuffix(".arb")

    val templateLocale: String
        get() = templateMatch?.groupValues?.get(2) ?: "en"

    val outputDirInPackage: String
        get() = outputDir.removePrefix("lib/").trim('/')

    fun arbFileNameForLocale(locale: String): String {
        val match = templateMatch
        return if (match != null) {
            "${match.groupValues[1]}_${locale}.arb"
        } else {
            "${templateArbFile.removeSuffix(".arb")}_${locale}.arb"
        }
    }

    fun localeFromArbFileName(fileName: String): String? {
        val escapedPrefix = Regex.escape(templatePrefix)
        val regex = Regex("^${escapedPrefix}_([A-Za-z0-9_-]+)\\.arb$")
        return regex.find(fileName)?.groupValues?.get(1)
    }
}

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
