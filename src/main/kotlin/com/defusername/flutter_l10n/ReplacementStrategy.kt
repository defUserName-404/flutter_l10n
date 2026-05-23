package com.defusername.flutter_l10n

import com.intellij.psi.PsiElement

object ReplacementStrategy {
    enum class Kind {
        RiverpodRef,
        ContextL10n,
        ContextRaw,
    }

    data class Result(
        val expression: String,
        val kind: Kind,
        val needsL10nProvidersImport: Boolean,
        val needsAppLocalizationsImport: Boolean,
        val needsL10nExtensionImport: Boolean,
    )

    fun resolve(element: PsiElement, key: String, outputClass: String): Result {
        val riverpodScope = RiverpodDetector.detect(element)
        val hasContext = ContextDetector.hasBuildContextInScope(element)

        return when (riverpodScope) {
            RiverpodDetector.RefScope.WidgetRefParam,
            RiverpodDetector.RefScope.NotifierField,
            RiverpodDetector.RefScope.ProviderRefParam,
            -> Result(
                expression = "ref.watch(l10nProvider).$key",
                kind = Kind.RiverpodRef,
                needsL10nProvidersImport = true,
                needsAppLocalizationsImport = false,
                needsL10nExtensionImport = false,
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
                        expression = "$outputClass.of(context)!.$key",
                        kind = Kind.ContextRaw,
                        needsL10nProvidersImport = false,
                        needsAppLocalizationsImport = true,
                        needsL10nExtensionImport = false,
                    )
                }
            }
        }
    }
}
