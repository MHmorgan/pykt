package dev.hirth.pykt.sexp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SexpTest {

    @Test
    fun testAtomCreation() {
        val atom = Sexp.atom("hello")
        assertEquals("hello", atom.value)
        assertEquals("hello", atom.toString())
    }

    @Test
    fun testAtomWithQuotesNeeded() {
        val atom = Sexp.atom("hello world")
        assertEquals("hello world", atom.value)
        assertEquals("\"hello world\"", atom.toString())
    }

    @Test
    fun testAtomWithSpecialCharacters() {
        val atom = Sexp.atom("hello\"world")
        assertEquals("hello\"world", atom.value)
        assertEquals("\"hello\\\"world\"", atom.toString())
    }

    @Test
    fun testEmptyAtom() {
        val atom = Sexp.atom("")
        assertEquals("", atom.value)
        assertEquals("\"\"", atom.toString())
    }

    @Test
    fun testListCreation() {
        val list = Sexp.list(Sexp.atom("a"), Sexp.atom("b"))
        assertEquals(2, list.elements.size)
        assertEquals("a", (list.elements[0] as Sexp.Atom).value)
        assertEquals("b", (list.elements[1] as Sexp.Atom).value)
        assertEquals("(a b)", list.toString())
    }

    @Test
    fun testEmptyList() {
        val list = Sexp.list()
        assertEquals(0, list.elements.size)
        assertEquals("()", list.toString())
    }

    @Test
    fun testNestedList() {
        val inner = Sexp.list(Sexp.atom("x"), Sexp.atom("y"))
        val outer = Sexp.list(Sexp.atom("a"), inner, Sexp.atom("b"))

        assertEquals("(a (x y) b)", outer.toString())
    }

    @Test
    fun testConvenienceFunctions() {
        val atom = sexp("hello")
        assertTrue(atom is Sexp.Atom)
        assertEquals("hello", atom.value)

        val list = sexp(Sexp.atom("a"), Sexp.atom("b"))
        assertTrue(list is Sexp.List)
        assertEquals(2, list.elements.size)
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
        assertEquals(2, list.asList()?.size)

        assertEquals("test", atom.atomValue())
        assertEquals(listOf(Sexp.atom("a"), Sexp.atom("b")), list.listElements())

        assertThrows<IllegalStateException> { atom.listElements() }
        assertThrows<IllegalStateException> { list.atomValue() }
    }

    @Test
    fun testSizeAndEmpty() {
        val atom = Sexp.atom("test")
        val emptyList = Sexp.list()
        val list = Sexp.list(Sexp.atom("a"), Sexp.atom("b"))

        assertEquals(0, atom.size())
        assertEquals(0, emptyList.size())
        assertEquals(2, list.size())

        assertFalse(atom.isEmpty())
        assertTrue(emptyList.isEmpty())
        assertFalse(list.isEmpty())
    }

    @Test
    fun testIndexing() {
        val list = Sexp.list(Sexp.atom("a"), Sexp.atom("b"), Sexp.atom("c"))

        assertEquals(Sexp.atom("a"), list[0])
        assertEquals(Sexp.atom("b"), list[1])
        assertEquals(Sexp.atom("c"), list[2])

        assertThrows<IllegalStateException> { Sexp.atom("test")[0] }
        assertThrows<IndexOutOfBoundsException> { list[3] }
    }

    @Test
    fun testForEach() {
        val list = Sexp.list(Sexp.atom("a"), Sexp.atom("b"))
        val collected = mutableListOf<String>()

        list.forEach { collected.add(it.atomValue()) }

        assertEquals(listOf("a", "b"), collected)

        assertThrows<IllegalStateException> {
            Sexp.atom("test").forEach { }
        }
    }

    @Test
    fun testMap() {
        val list = Sexp.list(Sexp.atom("a"), Sexp.atom("b"))
        val mapped = list.map { it.atomValue().uppercase() }

        assertEquals(listOf("A", "B"), mapped)

        assertThrows<IllegalStateException> {
            Sexp.atom("test").map { it }
        }
    }
}