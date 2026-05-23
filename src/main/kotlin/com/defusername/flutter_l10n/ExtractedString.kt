package com.defusername.flutter_l10n

data class ExtractedString(
    val raw: String,
    val suggestedKey: String,
    val startOffset: Int,
    val endOffset: Int,
)
