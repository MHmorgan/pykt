package dev.hirth.pykt.sexp

import java.io.File
import java.io.InputStream
import java.io.Reader

/**
 * Extension function to parse a single S-expression from a string.
 */
fun String.parseSexp(): Sexp = sexpReader.readOne(this)

/**
 * Extension function to parse multiple S-expressions from a string.
 */
fun String.parseSexps(): List<Sexp> = sexpReader.readMany(this)

/**
 * Extension function to parse S-expressions from a string with a callback.
 */
fun String.parseSexp(callback: (Sexp) -> Unit) = sexpReader.readWithCallback(this, callback)

/**
 * Extension function to parse S-expressions from a File.
 */
fun File.parseSexp(): Sexp = readText().parseSexp()

/**
 * Extension function to parse multiple S-expressions from a File.
 */
fun File.parseSexps(): List<Sexp> = readText().parseSexps()

/**
 * Extension function to parse S-expressions from a File with a callback.
 */
fun File.parseSexp(callback: (Sexp) -> Unit) = readText().parseSexp(callback)

/**
 * Extension function to parse S-expressions from an InputStream.
 */
fun InputStream.parseSexp(): Sexp = reader().use { it.readText().parseSexp() }

/**
 * Extension function to parse multiple S-expressions from an InputStream.
 */
fun InputStream.parseSexps(): List<Sexp> = reader().use { it.readText().parseSexps() }

/**
 * Extension function to parse S-expressions from an InputStream with a callback.
 */
fun InputStream.parseSexp(callback: (Sexp) -> Unit) =
    reader().use { it.readText().parseSexp(callback) }

/**
 * Extension function to parse S-expressions from a Reader.
 */
fun Reader.parseSexp(): Sexp = readText().parseSexp()

/**
 * Extension function to parse multiple S-expressions from a Reader.
 */
fun Reader.parseSexps(): List<Sexp> = readText().parseSexps()

/**
 * Extension function to parse S-expressions from a Reader with a callback.
 */
fun Reader.parseSexp(callback: (Sexp) -> Unit) = readText().parseSexp(callback)

/**
 * Convenience functions for creating S-expressions.
 */

/**
 * Creates an atom S-expression from any value by converting it to string.
 */
fun sexp(value: Any?): Sexp.Atom = Sexp.atom(value.toString())

/**
 * Creates a list S-expression from varargs.
 */
fun sexp(vararg elements: Sexp): Sexp.List = Sexp.list(*elements)

/**
 * Creates a list S-expression from a collection.
 */
fun sexp(elements: Collection<Sexp>): Sexp.List = Sexp.list(elements)

/**
 * Utility functions for working with S-expressions.
 */

/**
 * Checks if this S-expression is an atom.
 */
fun Sexp.isAtom(): Boolean = this is Sexp.Atom

/**
 * Checks if this S-expression is a list.
 */
fun Sexp.isList(): Boolean = this is Sexp.List

/**
 * Gets the atom value if this is an atom, or null otherwise.
 */
fun Sexp.asAtom(): String? = (this as? Sexp.Atom)?.value

/**
 * Gets the list elements if this is a list, or null otherwise.
 */
fun Sexp.asList(): List<Sexp>? = (this as? Sexp.List)?.elements

/**
 * Gets the atom value if this is an atom, or throws an exception.
 */
fun Sexp.atomValue(): String = when (this) {
    is Sexp.Atom -> value
    is Sexp.List -> throw IllegalStateException("Expected atom but got list: $this")
}

/**
 * Gets the list elements if this is a list, or throws an exception.
 */
fun Sexp.listElements(): List<Sexp> = when (this) {
    is Sexp.Atom -> throw IllegalStateException("Expected list but got atom: $this")
    is Sexp.List -> elements
}

/**
 * Returns the size of the list, or 0 if this is an atom.
 */
fun Sexp.size(): Int = when (this) {
    is Sexp.Atom -> 0
    is Sexp.List -> elements.size
}

/**
 * Returns true if this is an empty list.
 */
fun Sexp.isEmpty(): Boolean = when (this) {
    is Sexp.Atom -> false
    is Sexp.List -> elements.isEmpty()
}

/**
 * Gets the element at the specified index if this is a list.
 */
operator fun Sexp.get(index: Int): Sexp = when (this) {
    is Sexp.Atom -> throw IllegalStateException("Cannot index into atom: $this")
    is Sexp.List -> elements[index]
}

/**
 * Iterates over the elements if this is a list.
 */
fun Sexp.forEach(action: (Sexp) -> Unit) = when (this) {
    is Sexp.Atom -> throw IllegalStateException("Cannot iterate over atom: $this")
    is Sexp.List -> elements.forEach(action)
}

/**
 * Maps over the elements if this is a list.
 */
fun <T> Sexp.map(transform: (Sexp) -> T): List<T> = when (this) {
    is Sexp.Atom -> throw IllegalStateException("Cannot map over atom: $this")
    is Sexp.List -> elements.map(transform)
}