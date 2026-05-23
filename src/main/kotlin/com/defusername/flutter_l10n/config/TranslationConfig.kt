package com.defusername.flutter_l10n.config

data class TranslationConfig(
    val ollamaHost: String = "http://localhost:11434",
    val model: String = "phi3",
    val generateEndpoint: String = "/api/generate",
    val timeoutMillis: Int = 120_000,
    val batchPromptTemplate: String = "Translate each line below to {target}. Output ONLY the translation for each input line, one line per translation. Do not number them. Do not include the original text. Do not add any explanations or extra text.",
) {
    fun promptFor(targetLanguage: String): String =
        batchPromptTemplate.replace("{target}", targetLanguage)
}
