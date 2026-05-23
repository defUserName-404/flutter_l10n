package com.defusername.flutter_l10n.extraction

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.lang.dart.psi.DartStringLiteralExpression
import com.defusername.flutter_l10n.ExtractedString
import com.defusername.flutter_l10n.KeyGenerator

object PsiStringExtractor {
    fun extract(psiFile: PsiFile): List<ExtractedString> {
        val stringNodes = PsiTreeUtil.findChildrenOfType(
            psiFile,
            DartStringLiteralExpression::class.java,
        ).sortedBy { it.textRange.startOffset }

        val results = mutableListOf<ExtractedString>()

        for (node in stringNodes) {
            val literalText = node.text ?: continue
            if (isInterpolated(literalText)) continue
            if (isInSkippedPsiContext(node)) continue

            val raw = unquote(literalText) ?: continue
            if (raw.isBlank()) continue
            if (raw.length < 2) continue
            if (isNonUiText(raw)) continue

            results.add(
                ExtractedString(
                    raw = raw,
                    suggestedKey = KeyGenerator.generate(raw),
                    startOffset = node.textRange.startOffset,
                    endOffset = node.textRange.endOffset,
                ),
            )
        }

        return results
    }

    fun isInterpolated(literalText: String): Boolean {
        return literalText.contains("\${") || Regex("""\$[a-zA-Z_]""").containsMatchIn(literalText)
    }

    private fun isInSkippedPsiContext(node: PsiElement): Boolean {
        var current: PsiElement? = node
        var depth = 0
        while (current != null && depth < 12) {
            val className = current.javaClass.simpleName
            val text = current.text ?: ""

            if (className.contains("Import", ignoreCase = true) ||
                className.contains("Export", ignoreCase = true) ||
                className.contains("Part", ignoreCase = true)
            ) {
                return true
            }

            if (className.contains("Metadata", ignoreCase = true) ||
                className.contains("Annotation", ignoreCase = true)
            ) {
                return true
            }

            if (text.contains("AppLocalizations") ||
                text.contains(".l10n.") ||
                text.contains("l10nProvider")
            ) {
                return true
            }

            current = current.parent
            depth++
        }

        return false
    }

    fun isNonUiText(value: String): Boolean {
        val trimmed = value.trim()

        if (trimmed.startsWith("assets/") || trimmed.startsWith("fonts/")) return true
        if (trimmed.startsWith("package:")) return true
        if (trimmed.matches(Regex("""/[a-zA-Z0-9_\-/]*"""))) return true
        if (trimmed.matches(Regex("""#[0-9a-fA-F]{3,8}"""))) return true
        if (trimmed.matches(Regex("""[A-Za-z0-9+/=]{24,}"""))) return true
        if (trimmed.matches(Regex("""[a-z][a-z0-9]*_[a-z0-9_]+"""))) return true
        if (trimmed.matches(Regex("""\d+(\.\d+)?"""))) return true
        if (trimmed.contains("://")) return true
        if (trimmed.matches(Regex("""[\w._%+\-]+@[\w.\-]+\.[a-zA-Z]{2,}"""))) return true
        if (trimmed.contains(".png") ||
            trimmed.contains(".jpg") ||
            trimmed.contains(".jpeg") ||
            trimmed.contains(".svg") ||
            trimmed.contains(".webp") ||
            trimmed.contains(".otf") ||
            trimmed.contains(".ttf") ||
            trimmed.contains(".json") ||
            trimmed.contains(".dart") ||
            trimmed.contains(".arb")
        ) {
            return true
        }

        return false
    }

    fun unquote(literalText: String): String? {
        var text = literalText.trim()
        var rawString = false

        if (text.startsWith("r\"\"\"")) rawString = true
        if (text.startsWith("r'''")) rawString = true
        if (text.startsWith("r\"")) rawString = true
        if (text.startsWith("r'")) rawString = true

        if (rawString) {
            text = text.substring(1)
        }

        val content = when {
            text.startsWith("\"\"\"") && text.endsWith("\"\"\"") && text.length >= 6 ->
                text.substring(3, text.length - 3)

            text.startsWith("'''") && text.endsWith("'''") && text.length >= 6 ->
                text.substring(3, text.length - 3)

            text.startsWith('"') && text.endsWith('"') && text.length >= 2 ->
                text.substring(1, text.length - 1)

            text.startsWith('\'') && text.endsWith('\'') && text.length >= 2 ->
                text.substring(1, text.length - 1)

            else -> return null
        }

        if (rawString) {
            return content
        }

        return content
            .replace("\\n", "\n")
            .replace("\\t", "\t")
            .replace("\\r", "\r")
            .replace("\\\"", "\"")
            .replace("\\'", "'")
            .replace("\\\\", "\\")
    }
}
