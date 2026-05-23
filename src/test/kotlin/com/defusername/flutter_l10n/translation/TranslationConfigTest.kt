package com.defusername.flutter_l10n.translation

import com.defusername.flutter_l10n.config.TranslationConfig
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TranslationConfigTest {
    @Test
    fun `default host is localhost 11434`() {
        val config = TranslationConfig()
        assertEquals("http://localhost:11434", config.ollamaHost)
    }

    @Test
    fun `default model is phi3`() {
        val config = TranslationConfig()
        assertEquals("phi3", config.model)
    }

    @Test
    fun `promptFor replaces target placeholder`() {
        val config = TranslationConfig(batchPromptTemplate = "Translate to {target}")
        val prompt = config.promptFor("French")
        assertEquals("Translate to French", prompt)
    }

    @Test
    fun `promptFor handles multi-word language names`() {
        val config = TranslationConfig()
        val prompt = config.promptFor("Simplified Chinese")
        assertTrue(prompt.contains("Simplified Chinese"))
    }
}
