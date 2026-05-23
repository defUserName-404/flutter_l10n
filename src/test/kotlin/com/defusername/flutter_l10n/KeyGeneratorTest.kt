package com.defusername.flutter_l10n

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KeyGeneratorTest {
    @Test
    fun `simple string becomes camelCase`() {
        assertEquals("helloWorld", KeyGenerator.generate("Hello World"))
    }

    @Test
    fun `single word is lowercased`() {
        assertEquals("hello", KeyGenerator.generate("Hello"))
    }

    @Test
    fun `special chars are replaced with spaces`() {
        assertEquals("helloWorld", KeyGenerator.generate("Hello, World!"))
    }

    @Test
    fun `more than 6 words are truncated`() {
        assertEquals(
            "oneTwoThreeFourFiveSix",
            KeyGenerator.generate("one two three four five six seven eight nine"),
        )
    }

    @Test
    fun `string starting with digit gets str prefix`() {
        assertTrue(KeyGenerator.generate("123 hello").startsWith("str"))
    }

    @Test
    fun `empty string returns hash-based key`() {
        val key = KeyGenerator.generate("")
        assertTrue(key.startsWith("str"))
    }

    @Test
    fun `whitespace only string returns hash-based key`() {
        val key = KeyGenerator.generate("   ")
        assertTrue(key.startsWith("str"))
    }

    @Test
    fun `produces valid identifier`() {
        val inputs = listOf(
            "User Profile Page",
            "Click here to continue",
            "Total: 42 items",
            "hello_world_test",
            "don't do that",
        )
        for (input in inputs) {
            val key = KeyGenerator.generate(input)
            assertTrue(key.isNotEmpty(), "key for '$input' should not be empty")
            assertTrue(key.first().isLetter() || key.first() == '_' || key.startsWith("str"),
                "key '$key' for '$input' should start with letter, _, or str prefix")
        }
    }
}
