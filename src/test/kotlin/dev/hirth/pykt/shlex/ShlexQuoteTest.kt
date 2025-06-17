package dev.hirth.pykt.shlex

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ShlexQuoteTest {
    
    @Test
    fun testQuoteSimpleString() {
        assertEquals("hello", quote("hello"))
    }
    
    @Test
    fun testQuoteEmptyString() {
        assertEquals("''", quote(""))
    }
    
    @Test
    fun testQuoteWithSpaces() {
        assertEquals("'hello world'", quote("hello world"))
    }
    
    @Test
    fun testQuoteWithSingleQuote() {
        assertEquals("\"hello'world\"", quote("hello'world"))
    }
    
    @Test
    fun testQuoteWithDoubleQuote() {
        assertEquals("'hello\"world'", quote("hello\"world"))
    }
    
    @Test
    fun testQuoteWithBackslash() {
        assertEquals("\"hello\\\\world\"", quote("hello\\world"))
    }
    
    @Test
    fun testQuoteWithSpecialChars() {
        assertEquals("'hello|world'", quote("hello|world"))
        assertEquals("'hello&world'", quote("hello&world"))
        assertEquals("'hello;world'", quote("hello;world"))
    }
    
    @Test
    fun testQuoteWithDollarSign() {
        assertEquals("\"hello\\\$world\"", quote("hello${"$"}world"))
    }
    
    @Test
    fun testQuoteWithBacktick() {
        assertEquals("\"hello\\`world\"", quote("hello`world"))
    }
    
    @Test
    fun testJoinEmptyList() {
        assertEquals("", join(emptyList()))
    }
    
    @Test
    fun testJoinSingleItem() {
        assertEquals("hello", join(listOf("hello")))
    }
    
    @Test
    fun testJoinMultipleItems() {
        assertEquals("hello world", join(listOf("hello", "world")))
    }
    
    @Test
    fun testJoinWithQuoting() {
        assertEquals("hello 'world with spaces'", join(listOf("hello", "world with spaces")))
    }
    
    @Test
    fun testJoinComplexItems() {
        assertEquals("ls -la 'file with spaces' \"file'with'quotes\"", 
                     join(listOf("ls", "-la", "file with spaces", "file'with'quotes")))
    }
    
    @Test
    fun testRoundTripSplitJoin() {
        val original = listOf("hello", "world with spaces", "file'with'quotes", "path/to/file")
        val joined = join(original)
        val split = split(joined)
        assertEquals(original, split)
    }
}