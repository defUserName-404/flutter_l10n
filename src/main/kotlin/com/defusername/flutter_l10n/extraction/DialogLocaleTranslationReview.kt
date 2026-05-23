package com.defusername.flutter_l10n.extraction

import com.intellij.openapi.project.Project
import com.defusername.flutter_l10n.SelectedEntry
import com.defusername.flutter_l10n.translation.TranslationService
import com.defusername.flutter_l10n.ui.MultiLocaleDialog

class DialogLocaleTranslationReview(
    private val translationService: TranslationService,
) : LocaleTranslationReview {
    override fun collectTranslations(
        project: Project,
        selectedEntries: List<SelectedEntry>,
        availableLocales: List<String>,
        templateLocale: String,
    ): Map<String, Map<String, String>>? {
        if (availableLocales.size <= 1) return emptyMap()
        val dialog = MultiLocaleDialog(project, selectedEntries, availableLocales, templateLocale, translationService)
        return if (dialog.showAndGet()) dialog.getTranslationsByKey() else null
    }
}
