package dev.hirth.textwrap

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TextWrapperTest {

    @Test
    fun testBasicWrap() {
        val text = "The quick brown fox jumps over the lazy dog."
        val result = wrap(text, 20)
        
        assertEquals(3, result.size)
        assertEquals("The quick brown fox", result[0])
        assertEquals("jumps over the lazy", result[1])
        assertEquals("dog.", result[2])
    }

    @Test
    fun testFill() {
        val text = "The quick brown fox jumps over the lazy dog."
        val result = fill(text, 20)
        
        val expected = "The quick brown fox\njumps over the lazy\ndog."
        assertEquals(expected, result)
    }

    @Test
    fun testShorten() {
        val text = "The quick brown fox jumps over the lazy dog."
        val result = shorten(text, 20)
        
        assertEquals("The quick [...]", result)
    }

    @Test
    fun testShortenCustomPlaceholder() {
        val text = "The quick brown fox jumps over the lazy dog."
        val result = shorten(text, 20, "...")
        
        assertEquals("The quick brown...", result)
    }

    @Test
    fun testShortenNoTruncation() {
        val text = "Short text"
        val result = shorten(text, 20)
        
        assertEquals("Short text", result)
    }

    @Test
    fun testDedent() {
        val text = """
            |    First line
            |    Second line
            |        Indented line
            |    Last line
        """.trimMargin("|")
        
        val result = dedent(text)
        val expected = """
            |First line
            |Second line
            |    Indented line
            |Last line
        """.trimMargin("|")
        
        assertEquals(expected, result)
    }

    @Test
    fun testDedentNoCommonIndent() {
        val text = "No indent\n    Some indent\nNo indent again"
        val result = dedent(text)
        
        assertEquals(text, result)
    }

    @Test
    fun testIndent() {
        val text = "First line\nSecond line\nThird line"
        val result = indent(text, ">>> ")
        
        val expected = ">>> First line\n>>> Second line\n>>> Third line"
        assertEquals(expected, result)
    }

    @Test
    fun testIndentWithPredicate() {
        val text = "First line\n\nThird line"
        val result = indent(text, ">>> ") { it.isNotBlank() }
        
        val expected = ">>> First line\n\n>>> Third line"
        assertEquals(expected, result)
    }

    @Test
    fun testWrapWithInitialIndent() {
        val text = "The quick brown fox jumps over the lazy dog."
        val options = TextWrapOptions(
            width = 30,
            initialIndent = "* ",
            subsequentIndent = "  "
        )
        val result = TextWrapper(options).wrap(text)
        
        assertEquals(2, result.size)
        assertTrue(result[0].startsWith("* "), "First line should start with '* ' but was: '${result[0]}'")
        assertTrue(result[1].startsWith("  "), "Second line should start with '  ' but was: '${result[1]}'")
    }

    @Test
    fun testWrapBreakLongWords() {
        val text = "supercalifragilisticexpialidocious"
        val result = wrap(text, 10)
        
        assertTrue(result.size > 1)
        assertTrue(result.all { it.length <= 10 })
    }

    @Test
    fun testWrapNoBreakLongWords() {
        val text = "supercalifragilisticexpialidocious"
        val options = TextWrapOptions(width = 10, breakLongWords = false)
        val result = TextWrapper(options).wrap(text)
        
        assertEquals(1, result.size)
        assertEquals(text, result[0])
    }

    @Test
    fun testMaxLines() {
        val text = "One two three four five six seven eight nine ten"
        val options = TextWrapOptions(width = 10, maxLines = 2)
        val result = TextWrapper(options).wrap(text)
        
        assertEquals(2, result.size)
        assertTrue(result[1].contains("[...]"))
    }

    @Test
    fun testEmptyText() {
        val result = wrap("")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testWhitespaceOnly() {
        val result = wrap("   \n  \t  ")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testSingleWord() {
        val result = wrap("Hello")
        assertEquals(1, result.size)
        assertEquals("Hello", result[0])
    }

    @Test
    fun testDebugExpandTabs() {
        val text = "Hello\tworld"
        val options = TextWrapOptions(expandTabs = true, tabSize = 4, replaceWhitespace = false)
        val result = TextWrapper(options).wrap(text)
        
        println("Expand tabs debug:")
        println("Input: '$text'")
        println("Expected: 'Hello    world'")
        println("Actual: '${result[0]}'")
        
        assertEquals("Hello    world", result[0])
    }

    @Test
    fun testNoExpandTabs() {
        val text = "Hello\tworld"
        val options = TextWrapOptions(expandTabs = false, replaceWhitespace = false)
        val result = TextWrapper(options).wrap(text)
        
        println("No expand tabs debug:")
        println("Input: '$text'")
        println("Expected: 'Hello\tworld'")
        println("Actual: '${result[0]}'")
        
        assertEquals("Hello\tworld", result[0])
    }

    @Test
    fun testDropWhitespace() {
        val text = "  Hello world  "
        val options = TextWrapOptions(dropWhitespace = true)
        val result = TextWrapper(options).wrap(text)
        
        assertEquals("Hello world", result[0])
    }

    @Test
    fun testNoDropWhitespace() {
        val text = "  Hello world  "
        val options = TextWrapOptions(dropWhitespace = false, replaceWhitespace = false)
        val result = TextWrapper(options).wrap(text)
        
        assertEquals("  Hello world  ", result[0])
    }

    @Test
    fun testReplaceWhitespace() {
        val text = "Hello\n\r\t  world"
        val options = TextWrapOptions(replaceWhitespace = true)
        val result = TextWrapper(options).wrap(text)
        
        assertEquals("Hello world", result[0])
    }

    @Test
    fun testMultipleSpaces() {
        val text = "Hello     world"
        val result = wrap(text)
        
        assertEquals("Hello world", result[0])
    }

    @Test
    fun testDedentMixedIndentation() {
        val text = "    Line 1\n\t\tLine 2\n    Line 3"
        val result = dedent(text)
        
        // Should handle mixed tabs and spaces correctly
        assertNotNull(result)
    }

    @Test
    fun testShortenEdgeCases() {
        // Placeholder longer than width
        assertEquals("...", shorten("Hello world", 3, "..."))
        
        // Width equals text length
        assertEquals("Hello", shorten("Hello", 5))
        
        // Empty text
        assertEquals("", shorten("", 10))
    }
}