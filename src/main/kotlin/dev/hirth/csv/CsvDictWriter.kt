package dev.hirth.csv

import java.io.StringWriter
import java.io.Writer

/**
 * A CSV writer that formats dictionaries as CSV rows.
 */
class CsvDictWriter(
    writer: Writer,
    private val initialFieldNames: List<String>,
    private val dialect: CsvDialect = CsvDialect.EXCEL,
    private val restValue: String = "",
    private val extrasAction: ExtrasAction = ExtrasAction.RAISE
) {
    
    private val csvWriter = CsvWriter(writer, dialect)
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
        csvWriter.writeRow(initialFieldNames)
        headerWritten = true
    }
    
    /**
     * Writes a single dictionary as a CSV row.
     */
    fun writeRow(row: Map<String, Any?>) {
        if (!headerWritten) {
            writeHeader()
        }
        
        // Check for extra keys
        val extraKeys = row.keys - initialFieldNames.toSet()
        if (extraKeys.isNotEmpty() && extrasAction == ExtrasAction.RAISE) {
            throw CsvError("Dictionary contains unknown keys: ${extraKeys.joinToString(", ")}")
        }
        
        // Build row from field names
        val values = initialFieldNames.map { fieldName ->
            row[fieldName] ?: restValue
        }
        
        csvWriter.writeRow(values)
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
        csvWriter.flush()
    }
    
    /**
     * Closes the underlying writer.
     */
    fun close() {
        csvWriter.close()
    }
    
    /**
     * The field names being used for the CSV columns.
     */
    val fieldNames: List<String> get() = initialFieldNames
}

/**
 * Creates a CSV dictionary writer that writes to a string and returns the result.
 */
fun csvDictWriter(
    fieldNames: List<String>,
    dialect: CsvDialect = CsvDialect.EXCEL,
    restValue: String = "",
    extrasAction: CsvDictWriter.ExtrasAction = CsvDictWriter.ExtrasAction.RAISE
): Pair<CsvDictWriter, () -> String> {
    val stringWriter = StringWriter()
    val csvWriter = CsvDictWriter(stringWriter, fieldNames, dialect, restValue, extrasAction)
    return csvWriter to { stringWriter.toString() }
}

/**
 * Creates a CSV dictionary writer that writes to the given Writer.
 */
fun csvDictWriter(
    writer: Writer,
    fieldNames: List<String>,
    dialect: CsvDialect = CsvDialect.EXCEL,
    restValue: String = "",
    extrasAction: CsvDictWriter.ExtrasAction = CsvDictWriter.ExtrasAction.RAISE
): CsvDictWriter = CsvDictWriter(writer, fieldNames, dialect, restValue, extrasAction)

/**
 * Formats a list of dictionaries as a CSV string.
 */
fun formatCsvDict(
    rows: Iterable<Map<String, Any?>>,
    fieldNames: List<String>,
    dialect: CsvDialect = CsvDialect.EXCEL,
    restValue: String = "",
    extrasAction: CsvDictWriter.ExtrasAction = CsvDictWriter.ExtrasAction.RAISE
): String {
    val (writer, getString) = csvDictWriter(fieldNames, dialect, restValue, extrasAction)
    writer.writeRows(rows)
    return getString()
}