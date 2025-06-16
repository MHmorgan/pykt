package dev.hirth.pykt.shlex

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ShlexTest {
    
    @Test
    fun testBasicSplit() {
        val result = split("hello world")
        assertEquals(listOf("hello", "world"), result)
    }
    
    @Test
    fun testEmptyString() {
        val result = split("")
        assertEquals(emptyList<String>(), result)
    }
    
    @Test
    fun testWhitespaceOnly() {
        val result = split("   \t\n  ")
        assertEquals(emptyList<String>(), result)
    }
    
    @Test
    fun testSingleQuotes() {
        val result = split("'hello world'")
        assertEquals(listOf("hello world"), result)
    }
    
    @Test
    fun testDoubleQuotes() {
        val result = split("\"hello world\"")
        assertEquals(listOf("hello world"), result)
    }
    
    @Test
    fun testMixedQuotes() {
        val result = split("'hello' \"world\"")
        assertEquals(listOf("hello", "world"), result)
    }
    
    @Test
    fun testEscapedCharacters() {
        val result = split("hello\\ world")
        assertEquals(listOf("hello world"), result)
    }
    
    @Test
    fun testEscapedQuotesInDoubleQuotes() {
        val result = split("\"hello \\\"world\\\"\"")
        assertEquals(listOf("hello \"world\""), result)
    }
    
    @Test
    fun testCommentsEnabled() {
        val result = split("hello world # this is a comment", comments = true)
        assertEquals(listOf("hello", "world"), result)
    }
    
    @Test
    fun testCommentsDisabled() {
        val result = split("hello world # this is not a comment", comments = false)
        assertEquals(listOf("hello", "world", "#", "this", "is", "not", "a", "comment"), result)
    }
    
    @Test
    fun testUnterminatedSingleQuote() {
        assertThrows<ShlexException> {
            split("'unterminated")
        }
    }
    
    @Test
    fun testUnterminatedDoubleQuote() {
        assertThrows<ShlexException> {
            split("\"unterminated")
        }
    }
    
    @Test
    fun testEscapeAtEnd() {
        assertThrows<ShlexException> {
            split("hello\\")
        }
    }
    
    @Test
    fun testComplexCommand() {
        val result = split("ls -la '/home/user/file with spaces.txt' \"another file\"")
        assertEquals(listOf("ls", "-la", "/home/user/file with spaces.txt", "another file"), result)
    }
    
    @Test
    fun testNonPosixMode() {
        val result = split("hello 'world'", posix = false)
        assertEquals(listOf("hello", "'world'"), result)
    }
}