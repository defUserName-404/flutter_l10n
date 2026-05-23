package com.defusername.flutter_l10n.extraction

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.defusername.flutter_l10n.config.L10nProjectConfig
import com.defusername.flutter_l10n.replacement.ReplacementStrategy

class ExtractionCoordinator(
    private val stringExtractor: StringExtractor,
    private val selectionReview: SelectionReview,
    private val localeProvider: LocaleProvider,
    private val localeTranslationReview: LocaleTranslationReview,
    private val arbEntryMapper: ArbEntryMapper,
    private val arbRepository: ArbRepository,
    private val replacementResolver: ReplacementResolver,
    private val sourceRewriter: SourceRewriter,
    private val importCoordinator: ImportCoordinator,
    private val localizationGenerator: LocalizationGenerator,
) {
    fun execute(
        project: Project,
        editor: Editor,
        psiFile: PsiFile,
        config: L10nProjectConfig,
    ): ExtractionOutcome {
        val extractedStrings = stringExtractor.extract(psiFile)
        if (extractedStrings.isEmpty()) return ExtractionOutcome.NoExtractableStrings

        val selectedEntries = selectionReview.selectEntries(project, extractedStrings) ?: return ExtractionOutcome.Cancelled
        if (selectedEntries.isEmpty()) return ExtractionOutcome.NothingSelected

        val availableLocales = localeProvider.availableLocales(project, config)
        val translationsByKey = localeTranslationReview.collectTranslations(
            project = project,
            selectedEntries = selectedEntries,
            availableLocales = availableLocales,
            templateLocale = config.templateLocale,
        ) ?: return ExtractionOutcome.Cancelled

        val arbEntries = arbEntryMapper.toArbEntries(selectedEntries, translationsByKey)
        if (!arbRepository.upsertEntries(project, config, arbEntries)) {
            return ExtractionOutcome.Failed("Failed to update ARB files.")
        }

        val resolvedReplacements = replacementResolver.resolve(psiFile, selectedEntries, config.outputClass)
        sourceRewriter.replaceRawStrings(project, editor, psiFile, resolvedReplacements)
        importCoordinator.ensureImports(project, psiFile, editor, config, resolvedReplacements)
        localizationGenerator.run(project)

        val summary = ExtractionSummary(
            totalCount = resolvedReplacements.size,
            riverpodCount = resolvedReplacements.count { it.replacement.kind == ReplacementStrategy.Kind.RiverpodRef },
            contextCount = resolvedReplacements.count { it.replacement.kind == ReplacementStrategy.Kind.ContextL10n },
            fallbackCount = resolvedReplacements.count { it.replacement.kind == ReplacementStrategy.Kind.ContextRaw },
            staticErrorCount = resolvedReplacements.count { it.replacement.kind == ReplacementStrategy.Kind.StaticError },
        )

        return ExtractionOutcome.Completed(summary)
    }
}
