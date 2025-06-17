package dev.hirth.pykt.shlex

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ShlexLexerTest {
    
    @Test
    fun testBasicTokenization() {
        val lexer = ShlexLexer("hello world")
        assertEquals(listOf("hello", "world"), lexer.tokenize())
    }
    
    @Test
    fun testNextTokenIteration() {
        val lexer = ShlexLexer("one two three")
        assertEquals("one", lexer.nextToken())
        assertEquals("two", lexer.nextToken())
        assertEquals("three", lexer.nextToken())
        assertNull(lexer.nextToken())
    }
    
    @Test
    fun testPosixMode() {
        val posixLexer = ShlexLexer("'hello world'", posix = true)
        assertEquals(listOf("hello world"), posixLexer.tokenize())
        
        val nonPosixLexer = ShlexLexer("'hello world'", posix = false)
        assertEquals(listOf("'hello", "world'"), nonPosixLexer.tokenize())
    }
    
    @Test
    fun testCommentsMode() {
        val withComments = ShlexLexer("hello world # comment", comments = true)
        assertEquals(listOf("hello", "world"), withComments.tokenize())
        
        val withoutComments = ShlexLexer("hello world # comment", comments = false)
        assertEquals(listOf("hello", "world", "#", "comment"), withoutComments.tokenize())
    }
    
    @Test
    fun testCustomWordChars() {
        val lexer = ShlexLexer("hello-world")
        lexer.wordChars = lexer.wordChars + '-'
        assertEquals(listOf("hello-world"), lexer.tokenize())
    }
    
    @Test
    fun testCustomWhitespace() {
        val lexer = ShlexLexer("hello,world")
        lexer.whitespace = lexer.whitespace + ','
        assertEquals(listOf("hello", "world"), lexer.tokenize())
    }
    
    @Test
    fun testCustomCommenters() {
        val lexer = ShlexLexer("hello world ; comment", comments = true)
        lexer.commenters = setOf(';')
        assertEquals(listOf("hello", "world"), lexer.tokenize())
    }
    
    @Test
    fun testCustomQuotes() {
        val lexer = ShlexLexer("hello `quoted text` world")
        lexer.quotes = setOf('`')
        assertEquals(listOf("hello", "quoted text", "world"), lexer.tokenize())
    }
    
    @Test
    fun testCustomEscape() {
        val lexer = ShlexLexer("hello^ world")
        lexer.escape = '^'
        assertEquals(listOf("hello world"), lexer.tokenize())
    }
    
    @Test
    fun testErrorPositionReporting() {
        val lexer = ShlexLexer("'unterminated string")
        val exception = assertThrows<ShlexException> {
            lexer.tokenize()
        }
        assertTrue(exception.position >= 0)
    }
}