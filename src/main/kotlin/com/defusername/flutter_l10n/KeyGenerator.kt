package com.defusername.flutter_l10n

object KeyGenerator {
    fun generate(value: String): String {
        val words = value
            .replace(Regex("""[^a-zA-Z0-9\s]"""), " ")
            .trim()
            .split(Regex("""\s+"""))
            .filter { it.isNotBlank() }
            .take(6)

        if (words.isEmpty()) {
            return "str${value.hashCode().toString().replace('-', 'n')}"
        }

        val key = words.mapIndexed { index, word ->
            if (index == 0) {
                word.lowercase()
            } else {
                word.lowercase().replaceFirstChar { it.uppercase() }
            }
        }.joinToString("")

        return if (key.firstOrNull()?.isDigit() == true) "str$key" else key
    }
}
