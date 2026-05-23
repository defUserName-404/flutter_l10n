package com.defusername.flutter_l10n

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class L10nProjectConfigTest {
    @Test
    fun `default template locale is en`() {
        val config = L10nProjectConfig()
        assertEquals("en", config.templateLocale)
    }

    @Test
    fun `default template prefix is app`() {
        val config = L10nProjectConfig()
        assertEquals("app", config.templatePrefix)
    }

    @Test
    fun `arbFileNameForLocale generates correct filename`() {
        val config = L10nProjectConfig()
        assertEquals("app_fr.arb", config.arbFileNameForLocale("fr"))
        assertEquals("app_de.arb", config.arbFileNameForLocale("de"))
        assertEquals("app_zh_CN.arb", config.arbFileNameForLocale("zh_CN"))
    }

    @Test
    fun `localeFromArbFileName extracts locale`() {
        val config = L10nProjectConfig()
        assertEquals("en", config.localeFromArbFileName("app_en.arb"))
        assertEquals("fr", config.localeFromArbFileName("app_fr.arb"))
        assertEquals("zh_CN", config.localeFromArbFileName("app_zh_CN.arb"))
    }

    @Test
    fun `localeFromArbFileName returns null for non-matching file`() {
        val config = L10nProjectConfig()
        assertNull(config.localeFromArbFileName("other.arb"))
        assertNull(config.localeFromArbFileName("app_en.txt"))
        assertNull(config.localeFromArbFileName("en.arb"))
    }

    @Test
    fun `outputDirInPackage strips lib prefix`() {
        val config = L10nProjectConfig(outputDir = "lib/core/config/l10n")
        assertEquals("core/config/l10n", config.outputDirInPackage)
    }

    @Test
    fun `outputDirInPackage without lib prefix returns as-is`() {
        val config = L10nProjectConfig(outputDir = "core/config/l10n")
        assertEquals("core/config/l10n", config.outputDirInPackage)
    }

    @Test
    fun `custom template arb file yields correct prefix and locale`() {
        val config = L10nProjectConfig(templateArbFile = "messages_en.arb")
        assertEquals("messages", config.templatePrefix)
        assertEquals("en", config.templateLocale)
    }

    @Test
    fun `arbFileNameForLocale works with custom prefix`() {
        val config = L10nProjectConfig(templateArbFile = "messages_en.arb")
        assertEquals("messages_fr.arb", config.arbFileNameForLocale("fr"))
    }
}
