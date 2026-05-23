package com.defusername.flutter_l10n.extraction

sealed interface ExtractionOutcome {
    data object Cancelled : ExtractionOutcome

    data object NoExtractableStrings : ExtractionOutcome

    data object NothingSelected : ExtractionOutcome

    data class Failed(val reason: String) : ExtractionOutcome

    data class Completed(val summary: ExtractionSummary) : ExtractionOutcome
}

data class ExtractionSummary(
    val totalCount: Int,
    val riverpodCount: Int,
    val contextCount: Int,
    val fallbackCount: Int,
    val staticErrorCount: Int,
)
