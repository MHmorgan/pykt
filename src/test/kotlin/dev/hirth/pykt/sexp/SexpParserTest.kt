package dev.hirth.pykt.sexp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SexpParserTest {

    @Test
    fun testParseSimpleAtom() {
        val result = "hello".parseSexp()
        assertEquals(Sexp.atom("hello"), result)
    }
    
    @Test
    fun testParseQuotedAtom() {
        val result = "\"hello world\"".parseSexp()
        assertEquals(Sexp.atom("hello world"), result)
    }
    
    @Test
    fun testParseQuotedAtomWithEscapes() {
        val result = "\"hello \\\"quoted\\\" world\"".parseSexp()
        assertEquals(Sexp.atom("hello \"quoted\" world"), result)
    }
    
    @Test
    fun testParseSimpleList() {
        val result = "(a b c)".parseSexp()
        val expected = Sexp.list(
            Sexp.atom("a"),
            Sexp.atom("b"), 
            Sexp.atom("c")
        )
        assertEquals(expected, result)
    }
    
    @Test
    fun testParseEmptyList() {
        val result = "()".parseSexp()
        assertEquals(Sexp.list(), result)
    }
    
    @Test
    fun testParseNestedList() {
        val result = "(a (b c) d)".parseSexp()
        val expected = Sexp.list(
            Sexp.atom("a"),
            Sexp.list(Sexp.atom("b"), Sexp.atom("c")),
            Sexp.atom("d")
        )
        assertEquals(expected, result)
    }
    
    @Test
    fun testParseMultipleExpressions() {
        val result = "a (b c) d".parseSexps()
        val expected = listOf(
            Sexp.atom("a"),
            Sexp.list(Sexp.atom("b"), Sexp.atom("c")),
            Sexp.atom("d")
        )
        assertEquals(expected, result)
    }
    
    @Test
    fun testParseWithWhitespace() {
        val result = "  (  a   b  c  )  ".parseSexp()
        val expected = Sexp.list(
            Sexp.atom("a"),
            Sexp.atom("b"),
            Sexp.atom("c")
        )
        assertEquals(expected, result)
    }
    
    @Test
    fun testParseWithComments() {
        val input = """
            ; This is a comment
            (a b ; inline comment
             c) ; end comment
        """.trimIndent()
        
        val result = input.parseSexp()
        val expected = Sexp.list(
            Sexp.atom("a"),
            Sexp.atom("b"),
            Sexp.atom("c")
        )
        assertEquals(expected, result)
    }
    
    @Test
    fun testParseNumbers() {
        val result = "(123 -456 3.14 -2.71)".parseSexp()
        val expected = Sexp.list(
            Sexp.atom("123"),
            Sexp.atom("-456"),
            Sexp.atom("3.14"),
            Sexp.atom("-2.71")
        )
        assertEquals(expected, result)
    }
    
    @Test
    fun testParseComplex() {
        val input = """
            (config
             (server
              (host "localhost")
              (port 8080)
              (ssl true))
             (database
              (url "jdbc:postgresql://localhost/mydb")
              (user "admin")
              (password "secret")))
        """
        
        val result = input.parseSexp()
        assertTrue(result is Sexp.List)
        assertEquals(3, (result as Sexp.List).elements.size)
        assertEquals("config", (result as Sexp.List).elements[0].atomValue())
    }
    
    @Test
    fun testParseCallback() {
        val collected = mutableListOf<Sexp>()
        "a (b c) d".parseSexp { collected.add(it) }
        
        assertEquals(3, collected.size)
        assertEquals(Sexp.atom("a"), collected[0])
        assertEquals(Sexp.list(Sexp.atom("b"), Sexp.atom("c")), collected[1])
        assertEquals(Sexp.atom("d"), collected[2])
    }
    
    @Test
    fun testErrorUnmatchedOpenParen() {
        assertThrows<SexpParseException> {
            "(a b".parseSexp()
        }
    }
    
    @Test
    fun testErrorUnmatchedCloseParen() {
        assertThrows<SexpParseException> {
            "a b)".parseSexp()
        }
    }
    
    @Test
    fun testErrorUnterminatedString() {
        assertThrows<SexpParseException> {
            "\"unterminated".parseSexp()
        }
    }
    
    @Test
    fun testErrorEmptyInput() {
        assertThrows<SexpParseException> {
            "".parseSexp()
        }
    }
    
    @Test
    fun testErrorOnlyWhitespace() {
        assertThrows<SexpParseException> {
            "   ".parseSexp()
        }
    }
    
    @Test
    fun testErrorOnlyComments() {
        assertThrows<SexpParseException> {
            "; just a comment".parseSexp()
        }
    }
    
    @Test
    fun testParseMultipleAllowsEmpty() {
        val result = "".parseSexps()
        assertEquals(emptyList<Sexp>(), result)
    }
    
    @Test
    fun testParseMultipleAllowsWhitespace() {
        val result = "   ".parseSexps()
        assertEquals(emptyList<Sexp>(), result)
    }
    
    @Test
    fun testEscapeSequences() {
        val tests = mapOf(
            "\"\\n\"" to "\n",
            "\"\\r\"" to "\r", 
            "\"\\t\"" to "\t",
            "\"\\\\\"" to "\\",
            "\"\\\"\"" to "\"",
            "\"\\x\"" to "\\x" // Unknown escape sequence preserved
        )
        
        tests.forEach { (input, expected) ->
            val result = input.parseSexp()
            assertEquals(Sexp.atom(expected), result)
        }
    }
}