package dev.hirth.pykt.csv

import java.io.StringWriter
import java.io.Writer

/**
 * A CSV writer that formats data as CSV and writes to a Writer.
 */
class CsvWriter(
    private val writer: Writer,
    private val dialect: CsvDialect = CsvDialect.EXCEL
): AutoCloseable {

    /**
     * Writes a single CSV row.
     */
    fun writeRow(row: List<Any?>) {
        val fields = row.map {
            it?.toString()?.formatField() ?: ""
        }
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
    override fun close() {
        writer.close()
    }

    private fun String.formatField(): String {
        when (dialect.quoting) {
            QuotingStyle.ALL -> return quoteField(this)
            QuotingStyle.NONE -> {
                if (needsQuoting(this) && dialect.strict) {
                    throw CsvError("Field contains special characters but quoting is disabled: '$this'")
                }
                return escapeField(this)
            }

            QuotingStyle.NON_NUMERIC -> {
                if (!isNumeric(this)) {
                    return quoteField(this)
                }
                return this
            }

            QuotingStyle.MINIMAL -> {
                if (needsQuoting(this)) {
                    return quoteField(this)
                }
                return this
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
        val ch = dialect.quoteChar.toString()
        val escaped = when {
            dialect.doubleQuote -> field.replace(ch, "$ch$ch")
            dialect.escapeChar != null -> field.replace(ch, "${dialect.escapeChar}$ch")
            else -> field
        }
        return "$ch$escaped$ch"
    }

    private fun escapeField(field: String): String {
        if (dialect.escapeChar == null) return field

        val esc = dialect.escapeChar.toString()
        val del = dialect.delimiter.toString()

        return field
            .replace(esc, "$esc$esc")
            .replace(del, "$esc$del")
            .replace("\r", "$esc\r")
            .replace("\n", "$esc\n")
    }
}

/**
 * Build a CSV string, using [CsvWriter].
 */
fun buildCsv(
    dialect: CsvDialect = CsvDialect.EXCEL,
    block: CsvWriter.() -> Unit = {}
): String {
    val wr = StringWriter()
    CsvWriter(wr, dialect).use {
        it.block()
    }
    return wr.toString()
}

/**
 * Write CSV data to a writer, using [CsvWriter].
 */
fun writeCsv(
    writer: Writer,
    dialect: CsvDialect = CsvDialect.EXCEL,
    block: CsvWriter.() -> Unit = {}
) {
    CsvWriter(writer, dialect).use {
        it.block()
    }
}

/**
 * Formats a list of rows as a CSV string.
 */
fun formatCsv(
    rows: Iterable<List<Any?>>,
    dialect: CsvDialect = CsvDialect.EXCEL
): String {
    return buildCsv {
        writeRows(rows)
    }
}
