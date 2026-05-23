package com.defusername.flutter_l10n

import com.defusername.flutter_l10n.extraction.PsiStringExtractor
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PsiStringExtractorLogicTest {
    @Test
    fun `unquote handles double quoted string`() {
        assertEquals("hello", PsiStringExtractor.unquote("\"hello\""))
    }

    @Test
    fun `unquote handles single quoted string`() {
        assertEquals("hello", PsiStringExtractor.unquote("'hello'"))
    }

    @Test
    fun `unquote handles empty quoted string`() {
        assertEquals("", PsiStringExtractor.unquote("\"\""))
    }

    @Test
    fun `unquote returns null for unquoted string`() {
        assertNull(PsiStringExtractor.unquote("hello"))
    }

    @Test
    fun `unquote handles escaped characters`() {
        assertEquals("hello\nworld", PsiStringExtractor.unquote("\"hello\\nworld\""))
        assertEquals("hello\tworld", PsiStringExtractor.unquote("\"hello\\tworld\""))
        assertEquals("quote\"here", PsiStringExtractor.unquote("\"quote\\\"here\""))
    }

    @Test
    fun `unquote handles raw strings`() {
        assertEquals("hello\\nworld", PsiStringExtractor.unquote("r\"hello\\nworld\""))
    }

    @Test
    fun `unquote handles multi-line strings`() {
        assertEquals("hello\nworld", PsiStringExtractor.unquote("\"\"\"hello\nworld\"\"\""))
    }

    @Test
    fun `unquote handles raw multi-line strings`() {
        assertEquals("hello\\nworld", PsiStringExtractor.unquote("r\"\"\"hello\\nworld\"\"\""))
    }

    @Test
    fun `isInterpolated detects dollar-brace interpolation`() {
        assertTrue(PsiStringExtractor.isInterpolated("\"\${name}\""))
        assertTrue(PsiStringExtractor.isInterpolated("\"hello \${name}\""))
    }

    @Test
    fun `isInterpolated detects dollar variable interpolation`() {
        assertTrue(PsiStringExtractor.isInterpolated("\"\$identifier\""))
        assertTrue(PsiStringExtractor.isInterpolated("\"hello \$identifier\""))
    }

    @Test
    fun `isInterpolated returns false for plain string`() {
        assertFalse(PsiStringExtractor.isInterpolated("\"hello world\""))
    }

    @Test
    fun `isNonUiText detects file paths`() {
        assertTrue(PsiStringExtractor.isNonUiText("assets/images/logo.png"))
        assertTrue(PsiStringExtractor.isNonUiText("fonts/roboto.otf"))
    }

    @Test
    fun `isNonUiText detects package references`() {
        assertTrue(PsiStringExtractor.isNonUiText("package:flutter/material.dart"))
    }

    @Test
    fun `isNonUiText detects URLs`() {
        assertTrue(PsiStringExtractor.isNonUiText("https://example.com/api"))
    }

    @Test
    fun `isNonUiText detects hex colors`() {
        assertTrue(PsiStringExtractor.isNonUiText("#FF5733"))
    }

    @Test
    fun `isNonUiText detects numbers`() {
        assertTrue(PsiStringExtractor.isNonUiText("42"))
        assertTrue(PsiStringExtractor.isNonUiText("3.14"))
    }

    @Test
    fun `isNonUiText detects emails`() {
        assertTrue(PsiStringExtractor.isNonUiText("user@example.com"))
    }

    @Test
    fun `isNonUiText returns false for actual UI text`() {
        assertFalse(PsiStringExtractor.isNonUiText("Hello World"))
        assertFalse(PsiStringExtractor.isNonUiText("Click here"))
        assertFalse(PsiStringExtractor.isNonUiText("Welcome back, user"))
    }

    @Test
    fun `isNonUiText detects file extensions`() {
        assertTrue(PsiStringExtractor.isNonUiText("image.png"))
        assertTrue(PsiStringExtractor.isNonUiText("icon.svg"))
        assertTrue(PsiStringExtractor.isNonUiText("data.json"))
        assertTrue(PsiStringExtractor.isNonUiText("main.dart"))
        assertTrue(PsiStringExtractor.isNonUiText("app_en.arb"))
    }
}
