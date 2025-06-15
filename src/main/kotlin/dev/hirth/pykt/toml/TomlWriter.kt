package dev.hirth.pykt.toml

import java.io.StringWriter
import java.io.Writer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * TOML writer that formats TOML documents and writes them to a Writer.
 */
class TomlWriter(
    private val writer: Writer,
    private val indent: String = "  "
) : AutoCloseable {

    private var currentIndentLevel = 0

    /**
     * Writes a TOML document to the writer.
     */
    fun writeDocument(document: TomlDocument) {
        writeTable(document.rootTable, "")
    }

    /**
     * Writes a TOML table.
     */
    private fun writeTable(table: TomlTable, tablePath: String, isArrayTable: Boolean = false) {
        // Write table header if not root
        if (tablePath.isNotEmpty()) {
            if (isArrayTable) {
                writer.write("[[")
                writer.write(tablePath)
                writer.write("]]")
            } else {
                writer.write("[")
                writer.write(tablePath)
                writer.write("]")
            }
            writer.write("\n")
        }

        // Write key-value pairs
        for ((key, value) in table.values) {
            writeIndent()
            writer.write(escapeKey(key))
            writer.write(" = ")
            writeValue(value)
            writer.write("\n")
        }

        // Add blank line after values if there are nested tables
        if (table.values.isNotEmpty() && (table.tables.isNotEmpty() || table.arrayTables.isNotEmpty())) {
            writer.write("\n")
        }

        // Write nested tables
        for ((key, nestedTable) in table.tables) {
            val nestedPath = if (tablePath.isEmpty()) key else "$tablePath.$key"
            writeTable(nestedTable, nestedPath)
            writer.write("\n")
        }

        // Write array tables
        for ((key, arrayTables) in table.arrayTables) {
            for (arrayTable in arrayTables) {
                val arrayPath = if (tablePath.isEmpty()) key else "$tablePath.$key"
                writeTable(arrayTable, arrayPath, isArrayTable = true)
                writer.write("\n")
            }
        }
    }

    /**
     * Writes a TOML value.
     */
    private fun writeValue(value: TomlValue) {
        when (value) {
            is TomlValue.String -> writeString(value.value)
            is TomlValue.Integer -> writer.write(value.value.toString())
            is TomlValue.Float -> writeFloat(value.value)
            is TomlValue.Boolean -> writer.write(value.value.toString())
            is TomlValue.OffsetDateTime -> writer.write(value.value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            is TomlValue.LocalDateTime -> writer.write(value.value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            is TomlValue.LocalDate -> writer.write(value.value.format(DateTimeFormatter.ISO_LOCAL_DATE))
            is TomlValue.LocalTime -> writer.write(value.value.format(DateTimeFormatter.ISO_LOCAL_TIME))
            is TomlValue.Array -> writeArray(value.value)
            is TomlValue.InlineTable -> writeInlineTable(value.value)
        }
    }

    /**
     * Writes a string value with proper escaping.
     */
    private fun writeString(value: String) {
        writer.write("\"")
        for (char in value) {
            when (char) {
                '"' -> writer.write("\\\"")
                '\\' -> writer.write("\\\\")
                '\b' -> writer.write("\\b")
                '\u000C' -> writer.write("\\f")
                '\n' -> writer.write("\\n")
                '\r' -> writer.write("\\r")
                '\t' -> writer.write("\\t")
                else -> {
                    if (char.code < 32 || char.code == 127) {
                        writer.write("\\u${char.code.toString(16).padStart(4, '0')}")
                    } else {
                        writer.write(char.toString())
                    }
                }
            }
        }
        writer.write("\"")
    }

    /**
     * Writes a float value with special handling for infinity and NaN.
     */
    private fun writeFloat(value: Double) {
        when {
            value.isInfinite() -> writer.write(if (value > 0) "inf" else "-inf")
            value.isNaN() -> writer.write("nan")
            else -> writer.write(value.toString())
        }
    }

    /**
     * Writes an array value.
     */
    private fun writeArray(array: List<TomlValue>) {
        writer.write("[")
        
        for (i in array.indices) {
            if (i > 0) {
                writer.write(", ")
            }
            writeValue(array[i])
        }
        
        writer.write("]")
    }

    /**
     * Writes an inline table.
     */
    private fun writeInlineTable(table: Map<String, TomlValue>) {
        writer.write("{")
        
        val entries = table.entries.toList()
        for (i in entries.indices) {
            if (i > 0) {
                writer.write(", ")
            }
            val (key, value) = entries[i]
            writer.write(escapeKey(key))
            writer.write(" = ")
            writeValue(value)
        }
        
        writer.write("}")
    }

    /**
     * Escapes a key if necessary.
     */
    private fun escapeKey(key: String): String {
        return if (key.all { it.isLetterOrDigit() || it == '_' || it == '-' }) {
            key
        } else {
            "\"${key.replace("\"", "\\\"")}\""
        }
    }

    /**
     * Writes current indentation.
     */
    private fun writeIndent() {
        repeat(currentIndentLevel) {
            writer.write(indent)
        }
    }

    /**
     * Flushes the underlying writer.
     */
    fun flush() {
        writer.flush()
    }

    /**
     * Closes the underlying writer.
     */
    override fun close() {
        writer.close()
    }
}

/**
 * DSL builder for creating TOML documents.
 */
class TomlBuilder {
    private val document = TomlDocument()
    internal var currentTable = document.rootTable

    /**
     * Sets a string value.
     */
    fun string(key: String, value: String) {
        currentTable.setValue(key, TomlValue.String(value))
    }

    /**
     * Sets an integer value.
     */
    fun integer(key: String, value: Long) {
        currentTable.setValue(key, TomlValue.Integer(value))
    }

    /**
     * Sets an integer value from Int.
     */
    fun integer(key: String, value: Int) {
        currentTable.setValue(key, TomlValue.Integer(value.toLong()))
    }

    /**
     * Sets a float value.
     */
    fun float(key: String, value: Double) {
        currentTable.setValue(key, TomlValue.Float(value))
    }

    /**
     * Sets a float value from Float.
     */
    fun float(key: String, value: Float) {
        currentTable.setValue(key, TomlValue.Float(value.toDouble()))
    }

    /**
     * Sets a boolean value.
     */
    fun boolean(key: String, value: Boolean) {
        currentTable.setValue(key, TomlValue.Boolean(value))
    }

    /**
     * Sets an offset date-time value.
     */
    fun offsetDateTime(key: String, value: OffsetDateTime) {
        currentTable.setValue(key, TomlValue.OffsetDateTime(value))
    }

    /**
     * Sets a local date-time value.
     */
    fun localDateTime(key: String, value: LocalDateTime) {
        currentTable.setValue(key, TomlValue.LocalDateTime(value))
    }

    /**
     * Sets a local date value.
     */
    fun localDate(key: String, value: LocalDate) {
        currentTable.setValue(key, TomlValue.LocalDate(value))
    }

    /**
     * Sets a local time value.
     */
    fun localTime(key: String, value: LocalTime) {
        currentTable.setValue(key, TomlValue.LocalTime(value))
    }

    /**
     * Sets an array value.
     */
    fun array(key: String, vararg values: Any) {
        val tomlValues = values.map { convertToTomlValue(it) }
        currentTable.setValue(key, TomlValue.Array(tomlValues))
    }

    /**
     * Sets an array value from a list.
     */
    fun array(key: String, values: List<Any>) {
        val tomlValues = values.map { convertToTomlValue(it) }
        currentTable.setValue(key, TomlValue.Array(tomlValues))
    }

    /**
     * Creates an inline table.
     */
    fun inlineTable(key: String, block: InlineTableBuilder.() -> Unit) {
        val builder = InlineTableBuilder()
        builder.block()
        currentTable.setValue(key, TomlValue.InlineTable(builder.build()))
    }

    /**
     * Creates a table section.
     */
    fun table(key: String, block: TomlBuilder.() -> Unit) {
        val tableBuilder = TomlBuilder()
        tableBuilder.currentTable = document.getOrCreateTable(key)
        tableBuilder.block()
    }

    /**
     * Creates an array of tables.
     */
    fun arrayTable(key: String, block: ArrayTableBuilder.() -> Unit) {
        val builder = ArrayTableBuilder(document, key)
        builder.block()
    }

    /**
     * Converts a Kotlin value to a TOML value.
     */
    private fun convertToTomlValue(value: Any): TomlValue {
        return when (value) {
            is String -> TomlValue.String(value)
            is Int -> TomlValue.Integer(value.toLong())
            is Long -> TomlValue.Integer(value)
            is Float -> TomlValue.Float(value.toDouble())
            is Double -> TomlValue.Float(value)
            is Boolean -> TomlValue.Boolean(value)
            is OffsetDateTime -> TomlValue.OffsetDateTime(value)
            is LocalDateTime -> TomlValue.LocalDateTime(value)
            is LocalDate -> TomlValue.LocalDate(value)
            is LocalTime -> TomlValue.LocalTime(value)
            is List<*> -> TomlValue.Array(value.map { convertToTomlValue(it!!) })
            is Map<*, *> -> {
                val stringMap = value.entries.associate { (k, v) -> k.toString() to convertToTomlValue(v!!) }
                TomlValue.InlineTable(stringMap)
            }
            else -> TomlValue.String(value.toString())
        }
    }

    /**
     * Builds the TOML document.
     */
    fun build(): TomlDocument = document
}

/**
 * Builder for inline tables.
 */
class InlineTableBuilder {
    private val table = mutableMapOf<String, TomlValue>()

    fun string(key: String, value: String) {
        table[key] = TomlValue.String(value)
    }

    fun integer(key: String, value: Long) {
        table[key] = TomlValue.Integer(value)
    }

    fun integer(key: String, value: Int) {
        table[key] = TomlValue.Integer(value.toLong())
    }

    fun float(key: String, value: Double) {
        table[key] = TomlValue.Float(value)
    }

    fun float(key: String, value: Float) {
        table[key] = TomlValue.Float(value.toDouble())
    }

    fun boolean(key: String, value: Boolean) {
        table[key] = TomlValue.Boolean(value)
    }

    fun build(): Map<String, TomlValue> = table
}

/**
 * Builder for array of tables.
 */
class ArrayTableBuilder(private val document: TomlDocument, private val tablePath: String) {
    
    /**
     * Adds a table to the array.
     */
    fun table(block: TomlBuilder.() -> Unit) {
        val arrayTables = document.rootTable.arrayTables.getOrPut(tablePath) { mutableListOf() }
        val newTable = TomlTable()
        arrayTables.add(newTable)
        
        val builder = TomlBuilder()
        builder.currentTable = newTable
        builder.block()
    }
}

/**
 * Builds a TOML document using DSL.
 */
fun buildToml(block: TomlBuilder.() -> Unit): TomlDocument {
    val builder = TomlBuilder()
    builder.block()
    return builder.build()
}

/**
 * Builds a TOML string using DSL.
 */
fun buildTomlString(block: TomlBuilder.() -> Unit): String {
    val document = buildToml(block)
    val writer = StringWriter()
    TomlWriter(writer).use { it.writeDocument(document) }
    return writer.toString()
}

/**
 * Writes TOML data to a writer.
 */
fun writeToml(
    writer: Writer,
    block: TomlBuilder.() -> Unit
) {
    val document = buildToml(block)
    TomlWriter(writer).use { it.writeDocument(document) }
}