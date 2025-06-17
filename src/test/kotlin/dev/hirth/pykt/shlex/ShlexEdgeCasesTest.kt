package dev.hirth.pykt.shlex

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ShlexEdgeCasesTest {
    
    @Test
    fun testMixedQuotingStyles() {
        val result = split("'single' \"double\" normal")
        assertEquals(listOf("single", "double", "normal"), result)
    }
    
    @Test
    fun testNestedQuotes() {
        val result = split("'He said \"hello\" to me'")
        assertEquals(listOf("He said \"hello\" to me"), result)
    }
    
    @Test
    fun testBackslashEscaping() {
        val result = split("hello\\ world\\!\\@")
        assertEquals(listOf("hello world!@"), result)
    }
    
    @Test
    fun testTabsAndNewlines() {
        val result = split("hello\tworld\nfoo\rbar")
        assertEquals(listOf("hello", "world", "foo", "bar"), result)
    }
    
    @Test
    fun testMultipleSpaces() {
        val result = split("hello     world")
        assertEquals(listOf("hello", "world"), result)
    }
    
    @Test
    fun testLeadingTrailingWhitespace() {
        val result = split("   hello world   ")
        assertEquals(listOf("hello", "world"), result)
    }
    
    @Test
    fun testCommentAtBeginning() {
        val result = split("# this is a comment", comments = true)
        assertEquals(emptyList<String>(), result)
    }
    
    @Test
    fun testCommentInMiddle() {
        val result = split("hello # world comment", comments = true)
        assertEquals(listOf("hello"), result)
    }
    
    @Test
    fun testEmptyQuotes() {
        val result = split("'' \"\"")
        assertEquals(listOf("", ""), result)
    }
    
    @Test
    fun testOnlyQuotes() {
        val result = split("''")
        assertEquals(listOf(""), result)
    }
    
    @Test
    fun testSpecialCharacters() {
        val result = split("hello|world hello&world hello;world")
        assertEquals(listOf("hello|world", "hello&world", "hello;world"), result)
    }
    
    @Test
    fun testLongString() {
        val longString = "a".repeat(1000)
        val result = split(longString)
        assertEquals(listOf(longString), result)
    }
    
    @Test
    fun testUnicodeCharacters() {
        val result = split("hello 世界 мир")
        assertEquals(listOf("hello", "世界", "мир"), result)
    }
    
    @Test
    fun testQuotedUnicode() {
        val result = split("'hello 世界' \"мир друг\"")
        assertEquals(listOf("hello 世界", "мир друг"), result)
    }
    
    @Test
    fun testComplexShellCommand() {
        val command = "grep -r 'search term' /path/to/dir --exclude=\"*.log\" | head -10"
        val result = split(command)
        assertEquals(
            listOf("grep", "-r", "search term", "/path/to/dir", "--exclude=*.log", "|", "head", "-10"),
            result
        )
    }
    
    @Test
    fun testNonPosixQuoting() {
        val result = split("don't \"can't\" won't", posix = false)
        assertEquals(listOf("don't", "\"can't\"", "won't"), result)
    }
    
    @Test
    fun testJoinSplitRoundTrip() {
        val original = listOf("file with spaces", "file'with'quotes", "file\"with\"doublequotes", "file\\with\\backslashes")
        val joined = join(original)
        val split = split(joined)
        assertEquals(original, split)
    }
    
    @Test
    fun testQuoteSpecialShellCharacters() {
        val testCases = mapOf(
            "hello world" to "'hello world'",
            "hello|world" to "'hello|world'",
            "hello&world" to "'hello&world'",
            "hello;world" to "'hello;world'",
            "hello>world" to "'hello>world'",
            "hello<world" to "'hello<world'",
            "hello(world)" to "'hello(world)'",
            "hello*world" to "'hello*world'",
            "hello?world" to "'hello?world'",
            "hello[world]" to "'hello[world]'",
            "hello#world" to "'hello#world'"
        )
        
        testCases.forEach { (input, expected) ->
            assertEquals(expected, quote(input), "Failed for input: $input")
        }
    }
    
    @Test
    fun testUnterminatedQuotePosition() {
        val exception = assertThrows<ShlexException> {
            split("hello 'unterminated quote")
        }
        assertTrue(exception.position > 0, "Exception should report position")
    }
}