package com.defusername.flutter_l10n.translation

object TranslationResponseParser {
    fun parse(raw: String, expectedCount: Int): List<String?> {
        val lines = raw.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { line ->
                line.removePrefix("- ")
                    .removePrefix("* ")
                    .removePrefix("\"")
                    .removeSuffix("\"")
                    .removePrefix("'")
                    .removeSuffix("'")
                    .trim()
            }
            .filter { it.isNotBlank() }

        return if (lines.size >= expectedCount) {
            lines.take(expectedCount)
        } else {
            (lines + List(expectedCount - lines.size) { null }).take(expectedCount)
        }
    }
}
