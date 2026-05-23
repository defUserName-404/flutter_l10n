package com.defusername.flutter_l10n.core

import com.defusername.flutter_l10n.config.TranslationConfig
import com.defusername.flutter_l10n.extraction.ArbLocaleProvider
import com.defusername.flutter_l10n.extraction.ArbManagerRepository
import com.defusername.flutter_l10n.extraction.DefaultArbEntryMapper
import com.defusername.flutter_l10n.extraction.DefaultImportCoordinator
import com.defusername.flutter_l10n.extraction.DefaultLocalizationGenerator
import com.defusername.flutter_l10n.extraction.DefaultReplacementResolver
import com.defusername.flutter_l10n.extraction.DialogLocaleTranslationReview
import com.defusername.flutter_l10n.extraction.DialogSelectionReview
import com.defusername.flutter_l10n.extraction.DocumentSourceRewriter
import com.defusername.flutter_l10n.extraction.ExtractionCoordinator
import com.defusername.flutter_l10n.extraction.PsiStringExtractorService
import com.defusername.flutter_l10n.gen.GenL10nRunner
import com.defusername.flutter_l10n.translation.TranslationService

object ServiceGraph {
    @Volatile
    private var customTranslator: TranslationService? = null

    fun overrideTranslator(service: TranslationService) {
        customTranslator = service
    }

    fun resetOverrides() {
        customTranslator = null
    }

    fun coordinator(): ExtractionCoordinator {
        val translator = customTranslator ?: TranslationService(TranslationConfig())
        return ExtractionCoordinator(
            stringExtractor = PsiStringExtractorService(),
            selectionReview = DialogSelectionReview(),
            localeProvider = ArbLocaleProvider(),
            localeTranslationReview = DialogLocaleTranslationReview(translator),
            arbEntryMapper = DefaultArbEntryMapper(),
            arbRepository = ArbManagerRepository(),
            replacementResolver = DefaultReplacementResolver(),
            sourceRewriter = DocumentSourceRewriter(),
            importCoordinator = DefaultImportCoordinator(),
            localizationGenerator = DefaultLocalizationGenerator(GenL10nRunner::run),
        )
    }
}
