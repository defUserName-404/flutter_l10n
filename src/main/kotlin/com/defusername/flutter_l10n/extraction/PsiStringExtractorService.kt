package com.defusername.flutter_l10n.extraction

import com.intellij.psi.PsiFile
import com.defusername.flutter_l10n.ExtractedString

class PsiStringExtractorService : StringExtractor {
    override fun extract(psiFile: PsiFile): List<ExtractedString> = PsiStringExtractor.extract(psiFile)
}
