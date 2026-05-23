package com.defusername.flutter_l10n.extraction

import com.intellij.psi.PsiFile
import com.defusername.flutter_l10n.SelectedEntry
import com.defusername.flutter_l10n.replacement.ReplacementStrategy

class DefaultReplacementResolver : ReplacementResolver {
    override fun resolve(
        psiFile: PsiFile,
        selectedEntries: List<SelectedEntry>,
        outputClass: String,
    ): List<ResolvedReplacement> {
        return selectedEntries.map { selected ->
            val psiAtOffset = psiFile.findElementAt(selected.extracted.startOffset)
            val resolved = if (psiAtOffset != null) {
                ReplacementStrategy.resolve(psiAtOffset, selected.key, outputClass)
            } else {
                ReplacementStrategy.Result(
                    expression = "ref.l10n.${selected.key}",
                    kind = ReplacementStrategy.Kind.RiverpodRef,
                    needsL10nProvidersImport = true,
                    needsAppLocalizationsImport = false,
                    needsL10nExtensionImport = true,
                )
            }

            ResolvedReplacement(
                extracted = selected.extracted,
                key = selected.key,
                replacement = resolved,
            )
        }
    }
}
