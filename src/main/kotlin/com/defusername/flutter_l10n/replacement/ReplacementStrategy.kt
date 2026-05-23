package com.defusername.flutter_l10n.replacement

import com.intellij.psi.PsiElement
import com.defusername.flutter_l10n.detection.ContextDetector
import com.defusername.flutter_l10n.detection.RiverpodDetector

object ReplacementStrategy {
    enum class Kind {
        RiverpodRef,
        ContextL10n,
        ContextRaw,
        StaticError,
    }

    data class Result(
        val expression: String,
        val kind: Kind,
        val needsL10nProvidersImport: Boolean,
        val needsAppLocalizationsImport: Boolean,
        val needsL10nExtensionImport: Boolean,
    )

    fun resolve(element: PsiElement, key: String, outputClass: String): Result {
        if (isInStaticContext(element)) {
            return Result(
                expression = "/* TODO: '$key' needs l10n — not available in static context */",
                kind = Kind.StaticError,
                needsL10nProvidersImport = false,
                needsAppLocalizationsImport = false,
                needsL10nExtensionImport = false,
            )
        }

        val riverpodScope = RiverpodDetector.detect(element)
        val hasContext = ContextDetector.hasBuildContextInScope(element)

        return when (riverpodScope) {
            RiverpodDetector.RefScope.WidgetRefParam,
            RiverpodDetector.RefScope.NotifierField,
            RiverpodDetector.RefScope.ProviderRefParam,
            -> Result(
                expression = "ref.l10n.$key",
                kind = Kind.RiverpodRef,
                needsL10nProvidersImport = true,
                needsAppLocalizationsImport = false,
                needsL10nExtensionImport = true,
            )

            RiverpodDetector.RefScope.None -> {
                if (hasContext) {
                    Result(
                        expression = "context.l10n.$key",
                        kind = Kind.ContextL10n,
                        needsL10nProvidersImport = false,
                        needsAppLocalizationsImport = true,
                        needsL10nExtensionImport = true,
                    )
                } else {
                    Result(
                        expression = "ref.l10n.$key",
                        kind = Kind.RiverpodRef,
                        needsL10nProvidersImport = true,
                        needsAppLocalizationsImport = false,
                        needsL10nExtensionImport = true,
                    )
                }
            }
        }
    }

    fun isInStaticContext(element: PsiElement): Boolean {
        var current: PsiElement? = element
        var depth = 0
        while (current != null && depth < 12) {
            val className = current.javaClass.simpleName
            val text = current.text ?: ""

            if ((className.contains("Field", ignoreCase = true) ||
                        className.contains("Declaration", ignoreCase = true) ||
                        className.contains("Variable", ignoreCase = true)) &&
                text.contains("static")
            ) {
                return true
            }

            if (className.contains("Method", ignoreCase = true) &&
                className.contains("Declaration", ignoreCase = true) &&
                text.contains("static")
            ) {
                return true
            }

            if (text.contains("static const") || text.contains("static final")) {
                return true
            }

            current = current.parent
            depth++
        }

        return false
    }
}
