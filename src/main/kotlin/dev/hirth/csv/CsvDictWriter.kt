package dev.hirth.csv

import java.io.StringWriter
import java.io.Writer

/**
 * A CSV writer that formats dictionaries as CSV rows.
 */
class CsvDictWriter(
    /**
     * The underlying [CsvWriter] used to write the CSV data.
     */
    val writer: CsvWriter,

    /**
     * The field names of the dictionary rows.
     */
    val fieldNames: List<String>,

    private val restValue: String = "",
    private val extrasAction: ExtrasAction = ExtrasAction.RAISE
) : AutoCloseable by writer {

    private var headerWritten = false

    enum class ExtrasAction {
        /** Raise an exception if extra keys are found. */
        RAISE,

        /** Ignore extra keys. */
        IGNORE
    }

    /**
     * Writes the header row with field names.
     */
    fun writeHeader() {
        writer.writeRow(fieldNames)
        headerWritten = true
    }

    /**
     * Writes a single dictionary as a CSV row.
     */
    fun writeRow(row: Map<String, Any?>) {
        if (!headerWritten) writeHeader()

        // Check for extra keys
        val extraKeys = row.keys - fieldNames.toSet()
        if (extraKeys.isNotEmpty() && extrasAction == ExtrasAction.RAISE) {
            val s = "Dictionary contains unknown keys: ${extraKeys.joinToString(", ")}"
            throw CsvError(s)
        }

        // Build row from field names
        val values = fieldNames.map { field ->
            row[field] ?: restValue
        }

        writer.writeRow(values)
    }

    /**
     * Writes multiple dictionaries as CSV rows.
     */
    fun writeRows(rows: Iterable<Map<String, Any?>>) {
        rows.forEach { writeRow(it) }
    }

    /**
     * Flushes the underlying writer.
     */
    fun flush() {
        writer.flush()
    }
}

/**
 * Build a CSV string, using [CsvDictWriter].
 */
fun buildDictCsv(
    fieldNames: List<String>,
    dialect: CsvDialect = CsvDialect.EXCEL,
    restValue: String = "",
    extrasAction: CsvDictWriter.ExtrasAction = CsvDictWriter.ExtrasAction.RAISE,
    block: CsvDictWriter.() -> Unit = {}
): String {
    val wr = StringWriter()
    val csvWr = CsvWriter(wr, dialect)
    CsvDictWriter(csvWr, fieldNames, restValue, extrasAction).use {
        it.block()
    }
    return wr.toString()
}

fun writeDictCsv(
    writer: Writer,
    fieldNames: List<String>,
    dialect: CsvDialect = CsvDialect.EXCEL,
    restValue: String = "",
    extrasAction: CsvDictWriter.ExtrasAction = CsvDictWriter.ExtrasAction.RAISE,
    block: CsvDictWriter.() -> Unit = {}
) {
    val csvWr = CsvWriter(writer, dialect)
    CsvDictWriter(csvWr, fieldNames, restValue, extrasAction).use {
        it.block()
    }
}

/**
 * Formats a list of maps as a CSV string.
 */
fun formatDictCsv(
    rows: Iterable<Map<String, Any?>>,
    fieldNames: List<String>,
    dialect: CsvDialect = CsvDialect.EXCEL,
    restValue: String = "",
    extrasAction: CsvDictWriter.ExtrasAction = CsvDictWriter.ExtrasAction.RAISE
): String {
    return buildDictCsv(fieldNames, dialect, restValue, extrasAction) {
        writeRows(rows)
    }
}
