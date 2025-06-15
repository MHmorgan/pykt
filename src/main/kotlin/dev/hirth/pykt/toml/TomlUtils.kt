package dev.hirth.pykt.toml

import java.io.File
import java.io.Reader
import java.io.Writer

/**
 * Convenience functions for TOML parsing and writing.
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

/**
 * Writes TOML content to this [File] using DSL.
 */
fun File.writeToml(block: TomlBuilder.() -> Unit) {
    this.writer().use { writeToml(it, block) }
}

// -----------------------------------------------------------------------------
// Writer extensions
// -----------------------------------------------------------------------------

/**
 * Writes TOML content to this [Writer] using DSL.
 */
fun Writer.writeToml(block: TomlBuilder.() -> Unit) {
    writeToml(this, block)
}

// -----------------------------------------------------------------------------
// TomlDocument extensions
// -----------------------------------------------------------------------------

/**
 * Converts this [TomlDocument] to a TOML string.
 */
fun TomlDocument.toTomlString(): String {
    return buildTomlString { 
        // Copy all values from the document
        copyFrom(this@toTomlString)
    }
}

/**
 * Writes this [TomlDocument] to a [Writer].
 */
fun TomlDocument.writeTo(writer: Writer) {
    TomlWriter(writer).use { it.writeDocument(this) }
}

/**
 * Writes this [TomlDocument] to a [File].
 */
fun TomlDocument.writeTo(file: File) {
    file.writer().use { writeTo(it) }
}

// -----------------------------------------------------------------------------
// Helper extension for copying document content
// -----------------------------------------------------------------------------

private fun TomlBuilder.copyFrom(document: TomlDocument) {
    copyTable(document.rootTable, "")
}

private fun TomlBuilder.copyTable(table: TomlTable, path: String) {
    // Copy values
    for ((key, value) in table.values) {
        val fullKey = if (path.isEmpty()) key else "$path.$key"
        copyValue(fullKey, value)
    }
    
    // Copy nested tables
    for ((key, nestedTable) in table.tables) {
        val fullPath = if (path.isEmpty()) key else "$path.$key"
        table(fullPath) {
            copyTable(nestedTable, "")
        }
    }
    
    // Copy array tables
    for ((key, arrayTables) in table.arrayTables) {
        val fullPath = if (path.isEmpty()) key else "$path.$key"
        arrayTable(fullPath) {
            for (arrayTable in arrayTables) {
                table {
                    copyTable(arrayTable, "")
                }
            }
        }
    }
}

private fun TomlBuilder.copyValue(key: String, value: TomlValue) {
    when (value) {
        is TomlValue.String -> string(key, value.value)
        is TomlValue.Integer -> integer(key, value.value)
        is TomlValue.Float -> float(key, value.value)
        is TomlValue.Boolean -> boolean(key, value.value)
        is TomlValue.OffsetDateTime -> offsetDateTime(key, value.value)
        is TomlValue.LocalDateTime -> localDateTime(key, value.value)
        is TomlValue.LocalDate -> localDate(key, value.value)
        is TomlValue.LocalTime -> localTime(key, value.value)
        is TomlValue.Array -> {
            val kotlinValues = value.value.map { tomlValueToKotlinValue(it) }
            array(key, kotlinValues)
        }
        is TomlValue.InlineTable -> {
            inlineTable(key) {
                for ((k, v) in value.value) {
                    copyInlineValue(k, v)
                }
            }
        }
    }
}

private fun InlineTableBuilder.copyInlineValue(key: String, value: TomlValue) {
    when (value) {
        is TomlValue.String -> string(key, value.value)
        is TomlValue.Integer -> integer(key, value.value)
        is TomlValue.Float -> float(key, value.value)
        is TomlValue.Boolean -> boolean(key, value.value)
        // Note: Date/time values are not typically used in inline tables
        else -> throw IllegalArgumentException("Unsupported value type in inline table: ${value::class.simpleName}")
    }
}

private fun tomlValueToKotlinValue(value: TomlValue): Any {
    return when (value) {
        is TomlValue.String -> value.value
        is TomlValue.Integer -> value.value
        is TomlValue.Float -> value.value
        is TomlValue.Boolean -> value.value
        is TomlValue.OffsetDateTime -> value.value
        is TomlValue.LocalDateTime -> value.value
        is TomlValue.LocalDate -> value.value
        is TomlValue.LocalTime -> value.value
        is TomlValue.Array -> value.value.map { tomlValueToKotlinValue(it) }
        is TomlValue.InlineTable -> value.value.mapValues { tomlValueToKotlinValue(it.value) }
    }
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