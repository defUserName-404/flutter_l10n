package com.defusername.flutter_l10n.config

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
