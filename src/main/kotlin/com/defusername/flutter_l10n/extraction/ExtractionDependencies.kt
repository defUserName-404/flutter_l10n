package com.defusername.flutter_l10n.extraction

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.defusername.flutter_l10n.ArbEntry
import com.defusername.flutter_l10n.ExtractedString
import com.defusername.flutter_l10n.SelectedEntry
import com.defusername.flutter_l10n.config.L10nProjectConfig
import com.defusername.flutter_l10n.imports.ImportManager
import com.defusername.flutter_l10n.replacement.ReplacementStrategy

interface StringExtractor {
    fun extract(psiFile: PsiFile): List<ExtractedString>
}

interface SelectionReview {
    fun selectEntries(project: Project, extracted: List<ExtractedString>): List<SelectedEntry>?
}

interface LocaleTranslationReview {
    fun collectTranslations(
        project: Project,
        selectedEntries: List<SelectedEntry>,
        availableLocales: List<String>,
        templateLocale: String,
    ): Map<String, Map<String, String>>?
}

interface LocaleProvider {
    fun availableLocales(project: Project, config: L10nProjectConfig): List<String>
}

interface ArbRepository {
    fun upsertEntries(project: Project, config: L10nProjectConfig, entries: List<ArbEntry>): Boolean
}

interface ReplacementResolver {
    fun resolve(psiFile: PsiFile, selectedEntries: List<SelectedEntry>, outputClass: String): List<ResolvedReplacement>
}

data class ResolvedReplacement(
    val extracted: ExtractedString,
    val key: String,
    val replacement: ReplacementStrategy.Result,
)

interface SourceRewriter {
    fun replaceRawStrings(project: Project, editor: Editor, psiFile: PsiFile, replacements: List<ResolvedReplacement>)
}

interface ImportCoordinator {
    fun ensureImports(
        project: Project,
        psiFile: PsiFile,
        editor: Editor,
        config: L10nProjectConfig,
        replacements: List<ResolvedReplacement>,
    )
}

interface LocalizationGenerator {
    fun run(project: Project)
}

interface ArbEntryMapper {
    fun toArbEntries(selectedEntries: List<SelectedEntry>, translationsByKey: Map<String, Map<String, String>>): List<ArbEntry>
}
