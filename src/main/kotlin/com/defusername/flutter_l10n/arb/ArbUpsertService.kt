package com.defusername.flutter_l10n.arb

import com.defusername.flutter_l10n.ArbEntry
import org.json.JSONObject

object ArbUpsertService {
    fun upsertContent(
        existingText: String,
        entries: List<ArbEntry>,
        locale: String,
        isTemplate: Boolean,
    ): String {
        if (entries.isEmpty()) return existingText

        val parsedFile = parseArbText(existingText)
        val mergedEntries = parsedFile.entries.toMutableMap()

        for (entry in entries) {
            val resolvedValue = resolveEntryValue(
                entry = entry,
                locale = locale,
                isTemplateLocale = isTemplate,
                hasExistingValue = mergedEntries.containsKey(entry.key),
            ) ?: continue

            mergedEntries[entry.key] = resolvedValue
        }

        return buildArbText(
            locale = locale,
            preservedHeaders = parsedFile.preservedHeaders,
            entries = mergedEntries,
        )
    }

    private data class ParsedArbFile(
        val preservedHeaders: List<String>,
        val entries: LinkedHashMap<String, String>,
    )

    private fun parseArbText(text: String): ParsedArbFile {
        val parsedJson = runCatching { JSONObject(text) }.getOrNull()
            ?: return ParsedArbFile(emptyList(), linkedMapOf())

        val headers = mutableListOf<String>()
        val entries = linkedMapOf<String, String>()
        val keys = parsedJson.keys()

        while (keys.hasNext()) {
            val key = keys.next()
            when {
                key.startsWith("@@") -> {
                    val rawHeaderValue = parsedJson.opt(key)
                    headers.add("\"$key\": ${formatJsonValue(rawHeaderValue)}")
                }

                key.startsWith("@") -> {
                    // Per-entry metadata is regenerated from the merged entry map.
                }

                else -> {
                    entries[key] = parsedJson.optString(key, "")
                }
            }
        }

        return ParsedArbFile(headers, entries)
    }

    private fun resolveEntryValue(
        entry: ArbEntry,
        locale: String,
        isTemplateLocale: Boolean,
        hasExistingValue: Boolean,
    ): String? {
        if (isTemplateLocale) return entry.value

        val translatedValue = entry.translations[locale]
        if (!translatedValue.isNullOrBlank()) return translatedValue

        if (hasExistingValue) return null

        return "TODO(${entry.key}): ${entry.value}"
    }

    private fun buildArbText(
        locale: String,
        preservedHeaders: List<String>,
        entries: Map<String, String>,
    ): String {
        val documentLines = mutableListOf<String>()
        documentLines.add("{")

        val headerLines = buildHeaderLines(locale, preservedHeaders)
        if (headerLines.isNotEmpty()) {
            documentLines.addAll(withCommas(headerLines).map { "  $it" })
        }

        val entryLines = buildEntryLines(entries)
        if (entryLines.isNotEmpty()) {
            val needsLeadingComma = headerLines.isNotEmpty()
            val prefixedEntryLines = if (needsLeadingComma) {
                withLeadingComma(entryLines)
            } else {
                entryLines
            }
            documentLines.addAll(prefixedEntryLines.map { "  $it" })
        }

        documentLines.add("}")
        return documentLines.joinToString(separator = "\n", postfix = "\n")
    }

    private fun buildHeaderLines(locale: String, preservedHeaders: List<String>): List<String> {
        val headerLines = mutableListOf<String>()
        headerLines.add("\"@@locale\": \"$locale\"")
        headerLines.addAll(
            preservedHeaders.filterNot { it.startsWith("\"@@locale\"") },
        )
        return headerLines
    }

    private fun buildEntryLines(entries: Map<String, String>): List<String> {
        val lines = mutableListOf<String>()
        val pairs = entries.entries.toList()

        pairs.forEachIndexed { index, pair ->
            val isLastEntry = index == pairs.lastIndex
            val valueLine = "\"${escapeForJson(pair.key)}\": \"${escapeForJson(pair.value)}\""
            val metadataOpen = "\"@${escapeForJson(pair.key)}\": {"
            val metadataBody = "  \"description\": \"Auto-extracted from Dart source\""
            val metadataClose = "}"

            lines.add("$valueLine,")
            lines.add(metadataOpen)
            lines.add(metadataBody)
            lines.add(if (isLastEntry) metadataClose else "$metadataClose,")
        }

        return lines
    }

    private fun withCommas(lines: List<String>): List<String> {
        if (lines.isEmpty()) return lines
        val result = lines.toMutableList()
        for (i in 0 until result.lastIndex) {
            result[i] = "${result[i]},"
        }
        return result
    }

    private fun withLeadingComma(lines: List<String>): List<String> {
        if (lines.isEmpty()) return lines
        val result = lines.toMutableList()
        result[0] = ",${result[0]}"
        return result
    }

    private fun formatJsonValue(value: Any?): String {
        return when (value) {
            is String -> "\"${escapeForJson(value)}\""
            is Number -> value.toString()
            is Boolean -> value.toString()
            null -> "null"
            else -> value.toString()
        }
    }

    private fun escapeForJson(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
