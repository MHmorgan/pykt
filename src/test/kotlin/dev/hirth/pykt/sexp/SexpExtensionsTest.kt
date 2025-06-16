package dev.hirth.pykt.sexp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.io.StringReader

class SexpExtensionsTest {

    @Test
    fun testStringExtensions() {
        assertEquals(Sexp.atom("hello"), "hello".parseSexp())
        assertEquals(listOf(Sexp.atom("a"), Sexp.atom("b")), "a b".parseSexps())

        val collected = mutableListOf<Sexp>()
        "a b".parseSexp { collected.add(it) }
        assertEquals(2, collected.size)
    }

    @Test
    fun testReaderExtensions() {
        val reader = StringReader("(test value)")
        val result = reader.parseSexp()
        assertEquals(Sexp.list(Sexp.atom("test"), Sexp.atom("value")), result)
    }

    @Test
    fun testFileExtensions() {
        // Create a temporary file for testing
        val tempFile = File.createTempFile("sexp-test", ".sexp")
        tempFile.deleteOnExit()
        tempFile.writeText("(config (debug true))")

        val result = tempFile.parseSexp()
        assertEquals(
            Sexp.list(
                Sexp.atom("config"),
                Sexp.list(Sexp.atom("debug"), Sexp.atom("true"))
            ),
            result
        )

        val multiple = tempFile.parseSexps()
        assertEquals(1, multiple.size)
        assertEquals(result, multiple[0])

        val collected = mutableListOf<Sexp>()
        tempFile.parseSexp { collected.add(it) }
        assertEquals(1, collected.size)
        assertEquals(result, collected[0])
    }

    @Test
    fun testInputStreamExtensions() {
        val content = "(hello world)"
        val inputStream = content.byteInputStream()

        val result = inputStream.parseSexp()
        assertEquals(Sexp.list(Sexp.atom("hello"), Sexp.atom("world")), result)
    }

    @Test
    fun testConvenienceCreation() {
        assertEquals(Sexp.atom("test"), sexp("test"))
        assertEquals(Sexp.atom("42"), sexp(42))
        assertEquals(Sexp.atom("null"), sexp(null))

        val list = sexp(Sexp.atom("a"), Sexp.atom("b"))
        assertEquals(Sexp.list(Sexp.atom("a"), Sexp.atom("b")), list)

        val fromCollection = sexp(listOf(Sexp.atom("x"), Sexp.atom("y")))
        assertEquals(Sexp.list(Sexp.atom("x"), Sexp.atom("y")), fromCollection)
    }

    @Test
    fun testUtilityFunctions() {
        val atom = Sexp.atom("test")
        val list = Sexp.list(Sexp.atom("a"), Sexp.atom("b"))

        assertTrue(atom.isAtom())
        assertFalse(atom.isList())
        assertFalse(list.isAtom())
        assertTrue(list.isList())

        assertEquals("test", atom.asAtom())
        assertNull(list.asAtom())
        assertNull(atom.asList())
        assertEquals(listOf(Sexp.atom("a"), Sexp.atom("b")), list.asList())

        assertEquals("test", atom.atomValue())
        assertEquals(listOf(Sexp.atom("a"), Sexp.atom("b")), list.listElements())

        assertEquals(0, atom.size())
        assertEquals(2, list.size())

        assertFalse(atom.isEmpty())
        assertFalse(list.isEmpty())
        assertTrue(Sexp.list().isEmpty())
    }

    @Test
    fun testIndexAccess() {
        val list = Sexp.list(Sexp.atom("first"), Sexp.atom("second"))

        assertEquals(Sexp.atom("first"), list[0])
        assertEquals(Sexp.atom("second"), list[1])

        assertThrows<IndexOutOfBoundsException> { list[2] }
        assertThrows<IllegalStateException> { Sexp.atom("test")[0] }
    }

    @Test
    fun testForEachAndMap() {
        val list = Sexp.list(Sexp.atom("a"), Sexp.atom("b"), Sexp.atom("c"))

        val collected = mutableListOf<String>()
        list.forEach { collected.add(it.atomValue()) }
        assertEquals(listOf("a", "b", "c"), collected)

        val mapped = list.map { it.atomValue().uppercase() }
        assertEquals(listOf("A", "B", "C"), mapped)

        assertThrows<IllegalStateException> {
            Sexp.atom("test").forEach { }
        }

        assertThrows<IllegalStateException> {
            Sexp.atom("test").map { it }
        }
    }
}