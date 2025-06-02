package dev.hirth.csv

import java.io.StringWriter
import java.io.Writer

/**
 * A CSV writer that formats data as CSV and writes to a Writer.
 */
class CsvWriter(
    private val writer: Writer,
    private val dialect: CsvDialect = CsvDialect.EXCEL
) {
    
    /**
     * Writes a single CSV row.
     */
    fun writeRow(row: List<Any?>) {
        val fields = row.map { formatField(it?.toString() ?: "") }
        writer.write(fields.joinToString(dialect.delimiter.toString()))
        writer.write(dialect.lineTerminator)
    }
    
    /**
     * Writes multiple CSV rows.
     */
    fun writeRows(rows: Iterable<List<Any?>>) {
        rows.forEach { writeRow(it) }
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
    fun close() {
        writer.close()
    }
    
    private fun formatField(field: String): String {
        when (dialect.quoting) {
            QuotingStyle.ALL -> return quoteField(field)
            QuotingStyle.NONE -> {
                if (needsQuoting(field) && dialect.strict) {
                    throw CsvError("Field contains special characters but quoting is disabled: '$field'")
                }
                return escapeField(field)
            }
            QuotingStyle.NON_NUMERIC -> {
                if (!isNumeric(field)) {
                    return quoteField(field)
                }
                return field
            }
            QuotingStyle.MINIMAL -> {
                if (needsQuoting(field)) {
                    return quoteField(field)
                }
                return field
            }
        }
    }
    
    private fun needsQuoting(field: String): Boolean {
        return field.contains(dialect.delimiter) ||
               field.contains(dialect.quoteChar) ||
               field.contains('\r') ||
               field.contains('\n') ||
               (dialect.skipInitialSpace && field.startsWith(' '))
    }
    
    private fun isNumeric(field: String): Boolean {
        if (field.isEmpty()) return false
        return try {
            field.toDouble()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }
    
    private fun quoteField(field: String): String {
        val escaped = if (dialect.doubleQuote) {
            field.replace(dialect.quoteChar.toString(), "${dialect.quoteChar}${dialect.quoteChar}")
        } else if (dialect.escapeChar != null) {
            field.replace(dialect.quoteChar.toString(), "${dialect.escapeChar}${dialect.quoteChar}")
        } else {
            field
        }
        return "${dialect.quoteChar}$escaped${dialect.quoteChar}"
    }
    
    private fun escapeField(field: String): String {
        if (dialect.escapeChar == null) {
            return field
        }
        
        return field
            .replace(dialect.escapeChar.toString(), "${dialect.escapeChar}${dialect.escapeChar}")
            .replace(dialect.delimiter.toString(), "${dialect.escapeChar}${dialect.delimiter}")
            .replace("\r", "${dialect.escapeChar}\r")
            .replace("\n", "${dialect.escapeChar}\n")
    }
}

/**
 * Creates a CSV writer that writes to a string and returns the result.
 */
fun csvWriter(dialect: CsvDialect = CsvDialect.EXCEL): Pair<CsvWriter, () -> String> {
    val stringWriter = StringWriter()
    val csvWriter = CsvWriter(stringWriter, dialect)
    return csvWriter to { stringWriter.toString() }
}

/**
 * Creates a CSV writer that writes to the given Writer.
 */
fun csvWriter(
    writer: Writer,
    dialect: CsvDialect = CsvDialect.EXCEL
): CsvWriter = CsvWriter(writer, dialect)

/**
 * Formats a list of rows as a CSV string.
 */
fun formatCsv(
    rows: Iterable<List<Any?>>,
    dialect: CsvDialect = CsvDialect.EXCEL
): String {
    val (writer, getString) = csvWriter(dialect)
    writer.writeRows(rows)
    return getString()
}