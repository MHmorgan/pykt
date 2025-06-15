package dev.hirth.pykt.toml

import java.io.File
import java.io.Reader

/**
 * Convenience functions for TOML parsing.
 */

// -----------------------------------------------------------------------------
// Reading functions
// -----------------------------------------------------------------------------

/**
 * Parses TOML content from a string and returns a [TomlDocument].
 */
fun readToml(content: String): TomlDocument {
    return TomlReader().parseToml(content)
}

/**
 * Parses TOML content from a [Reader] and returns a [TomlDocument].
 */
fun readToml(reader: Reader): TomlDocument {
    return TomlReader().parseToml(reader)
}

/**
 * Parses TOML content from a [File] and returns a [TomlDocument].
 */
fun readToml(file: File): TomlDocument {
    return file.reader().use { readToml(it) }
}

// -----------------------------------------------------------------------------
// String extensions
// -----------------------------------------------------------------------------

/**
 * Parses this string as TOML and returns a [TomlDocument].
 */
fun String.parseToml(): TomlDocument {
    return readToml(this)
}

// -----------------------------------------------------------------------------
// Reader extensions
// -----------------------------------------------------------------------------

/**
 * Parses TOML content from this [Reader] and returns a [TomlDocument].
 */
fun Reader.parseToml(): TomlDocument {
    return readToml(this)
}

// -----------------------------------------------------------------------------
// File extensions
// -----------------------------------------------------------------------------

/**
 * Parses TOML content from this [File] and returns a [TomlDocument].
 */
fun File.parseToml(): TomlDocument {
    return readToml(this)
}

// -----------------------------------------------------------------------------
// Value access convenience functions
// -----------------------------------------------------------------------------

/**
 * Gets a string value from the document.
 */
fun TomlDocument.getString(key: String): String? {
    return (getValue(key) as? TomlValue.String)?.value
}

/**
 * Gets an integer value from the document.
 */
fun TomlDocument.getInteger(key: String): Long? {
    return (getValue(key) as? TomlValue.Integer)?.value
}

/**
 * Gets a float value from the document.
 */
fun TomlDocument.getFloat(key: String): Double? {
    return (getValue(key) as? TomlValue.Float)?.value
}

/**
 * Gets a boolean value from the document.
 */
fun TomlDocument.getBoolean(key: String): Boolean? {
    return (getValue(key) as? TomlValue.Boolean)?.value
}

/**
 * Gets an array value from the document.
 */
fun TomlDocument.getArray(key: String): List<TomlValue>? {
    return (getValue(key) as? TomlValue.Array)?.value
}

/**
 * Gets an inline table value from the document.
 */
fun TomlDocument.getInlineTable(key: String): Map<String, TomlValue>? {
    return (getValue(key) as? TomlValue.InlineTable)?.value
}

/**
 * Gets string values from an array.
 */
fun TomlDocument.getStringArray(key: String): List<String>? {
    return getArray(key)?.mapNotNull { (it as? TomlValue.String)?.value }
}

/**
 * Gets integer values from an array.
 */
fun TomlDocument.getIntegerArray(key: String): List<Long>? {
    return getArray(key)?.mapNotNull { (it as? TomlValue.Integer)?.value }
}

/**
 * Gets float values from an array.
 */
fun TomlDocument.getFloatArray(key: String): List<Double>? {
    return getArray(key)?.mapNotNull { (it as? TomlValue.Float)?.value }
}

/**
 * Gets boolean values from an array.
 */
fun TomlDocument.getBooleanArray(key: String): List<Boolean>? {
    return getArray(key)?.mapNotNull { (it as? TomlValue.Boolean)?.value }
}