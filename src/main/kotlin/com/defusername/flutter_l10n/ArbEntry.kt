package com.defusername.flutter_l10n

data class ArbEntry(
    val key: String,
    val value: String,
    val translations: Map<String, String> = emptyMap(),
)
