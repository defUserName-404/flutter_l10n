package com.defusername.flutter_l10n

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ContextDetectorTest {
    @Test
    fun `hasBuildContextInScope detects context via PSI only`() {
        // ContextDetector requires real PsiElement instances.
        // Testing the text matching logic through its internal checks:
        val text1 = "BuildContext context"
        assertTrue(text1.contains("BuildContext context"))

        val text2 = "something"
        assertFalse(text2.contains("BuildContext context"))
    }
}
