package dev.hirth.csv

import java.io.Reader

/**
 * A CSV reader that parses CSV data into sequences of string lists.
 */
class CsvReader(
    private val reader: Reader,
    private val dialect: CsvDialect = CsvDialect.EXCEL,

    /**
     * Whether to skip the first row, which is assumed to be a header row.
     *
     * If this is `true` and [headers] is `null`, the first row is
     * set as the headers.
     * If this is `true` and [headers] is not `null`, the first row is compared
     * to the [headers] and an exception is thrown if they do not match.
     */
    private val hasHeaders: Boolean = false,

    /**
     * The headers to use for the CSV data.
     */
    private var headers: List<String>? = null,
) : Iterator<CsvRow> {

    /**
     * Returns the current line number being processed.
     */
    var lineNumber = 0
        private set

    private var nextRow: List<String>? = null
    private var hasNext = true

    init {
        readNext()

        if (hasHeaders && nextRow != null) {
            when (headers) {
                null -> headers = nextRow!!
                else -> if (headers != nextRow) {
                    val s = "Headers do not match data. Expected $headers but got $nextRow."
                    throw CsvError(s)
                }
            }
            readNext()
        }
    }

    override fun hasNext(): Boolean = hasNext

    override fun next(): CsvRow {
        if (!hasNext) {
            throw NoSuchElementException("No more CSV rows")
        }
        val result = CsvRow(lineNumber, nextRow!!, headers)
        readNext()
        return result
    }

    private fun readNext() {
        try {
            nextRow = parseRow()
            if (nextRow == null) {
                hasNext = false
            }
        } catch (e: Exception) {
            hasNext = false
            if (dialect.strict) {
                throw CsvError("Error parsing CSV at line $lineNumber", e)
            }
        }
    }

    private fun parseRow(): List<String>? {
        val fields = mutableListOf<String>()
        var field = StringBuilder()
        var inQuotes = false
        var c: Int

        lineNumber++

        while (true) {
            c = reader.read()

            if (c == -1) {
                // End of file
                if (inQuotes && dialect.strict) {
                    throw CsvError("Unexpected end of file in quoted field at line $lineNumber")
                }
                if (field.isNotEmpty() || fields.isNotEmpty()) {
                    fields.add(field.toString())
                    return fields
                }
                return null
            }

            val char = c.toChar()

            when {
                char == '\r' || char == '\n' -> {
                    if (inQuotes) {
                        field.append(char)
                    } else {
                        // End of line
                        if (char == '\r') {
                            // Check for \r\n
                            reader.mark(1)
                            val next = reader.read()
                            if (next != -1 && next.toChar() != '\n') {
                                reader.reset()
                            }
                        }
                        fields.add(field.toString())
                        return fields
                    }
                }

                char == dialect.quoteChar -> {
                    if (inQuotes) {
                        if (dialect.doubleQuote) {
                            // Check for doubled quote
                            reader.mark(1)
                            val next = reader.read()
                            if (next != -1 && next.toChar() == dialect.quoteChar) {
                                // Escaped quote
                                field.append(dialect.quoteChar)
                            } else {
                                // End of quoted field
                                reader.reset()
                                inQuotes = false
                            }
                        } else {
                            // End of quoted field
                            inQuotes = false
                        }
                    } else {
                        // Start of quoted field
                        inQuotes = true
                    }
                }

                char == dialect.delimiter -> {
                    if (inQuotes) {
                        field.append(char)
                    } else {
                        // End of field
                        fields.add(field.toString())
                        field = StringBuilder()

                        // Skip initial space if configured
                        if (dialect.skipInitialSpace) {
                            while (true) {
                                reader.mark(1)
                                val next = reader.read()
                                if (next == -1 || next.toChar() != ' ') {
                                    reader.reset()
                                    break
                                }
                            }
                        }
                    }
                }

                dialect.escapeChar != null && char == dialect.escapeChar -> {
                    if (!inQuotes && dialect.strict) {
                        throw CsvError("Escape character outside quoted field at line $lineNumber")
                    }
                    val next = reader.read()
                    if (next == -1) {
                        if (dialect.strict) {
                            throw CsvError("Unexpected end of file after escape character at line $lineNumber")
                        }
                        field.append(char)
                    } else {
                        field.append(next.toChar())
                    }
                }

                else -> {
                    field.append(char)
                }
            }
        }
    }
}

data class CsvRow(
    val lineNumber: Int,
    val row: List<String>,
    val headers: List<String>?,
) {
    /**
     * Get the value of a column by its index (0-based).
     *
     * If the column is empty, it returns `null`.
     */
    fun column(idx: Int) = row[idx].takeIf { it.isNotEmpty() }

    /**
     * Get the value of a column by its name.
     *
     * If the column is empty, it returns `null`.
     *
     * Throws [CsvError] if the headers are unknown.
     */
    fun column(name: String): String? {
        if (headers == null)
            throw CsvError("cannot use named columns: CSV headers unknown")

        val idx = headers.indexOf(name)
            .takeIf { it != -1 }
            ?: throw CsvError("unknown column: $name")
        return row[idx].takeIf { it.isNotEmpty() }
    }

    /**
     * Returns a map of the values in the row.
     */
    fun toMap(restValue: String = ""): Map<String, String> {
        if (headers == null)
            throw CsvError("cannot convert to map: CSV headers unknown")

        val map = mutableMapOf<String, String>()
        for (i in headers.indices) {
            map[headers[i]] = when {
                i < row.size -> row[i]
                else -> restValue
            }
        }
        return map
    }
}

// -----------------------------------------------------------------------------
//
// Reader
//
// -----------------------------------------------------------------------------

/**
 * Reads CSV data from a [Reader] and returns a sequence of the
 * values produced by the [rowMapper] function.
 */
fun <T> readCsv(
    reader: Reader,
    dialect: CsvDialect = CsvDialect.EXCEL,
    hasHeaders: Boolean = false,
    headers: List<String>? = null,
    rowMapper: (CsvRow) -> T,
): Sequence<T> {
    val rd = CsvReader(reader, dialect, hasHeaders, headers)
    return sequence {
        while (rd.hasNext()) {
            val row = rd.next()
            yield(rowMapper(row))
        }
    }
}

/**
 * Reads CSV data from a [Reader] and returns a sequence of the
 * parsed rows.
 */
fun readCsv(
    reader: Reader,
    dialect: CsvDialect = CsvDialect.EXCEL,
    hasHeaders: Boolean = false,
    headers: List<String>? = null,
): Sequence<List<String>> {
    return readCsv(reader, dialect, hasHeaders, headers) { it.row }
}

/**
 * Reads CSV data from a [Reader] and returns a sequence of the
 * parsed rows as a map.
 */
fun readCsvMap(
    reader: Reader,
    dialect: CsvDialect = CsvDialect.EXCEL,
    hasHeaders: Boolean = false,
    headers: List<String>? = null,
    restKey: String = "",
): Sequence<Map<String, String>> {
    return readCsv(reader, dialect, hasHeaders, headers) {
        it.toMap(restKey)
    }
}

/**
 * Reads CSV data from this [Reader] and returns a sequence of the
 * values produced by the [rowMapper] function.
 */
fun <T> Reader.parseCsv(
    dialect: CsvDialect = CsvDialect.EXCEL,
    hasHeaders: Boolean = false,
    headers: List<String>? = null,
    rowMapper: (CsvRow) -> T,
): Sequence<T> {
    return readCsv(this, dialect, hasHeaders, headers, rowMapper)
}

/**
 * Reads CSV data from this [Reader] and returns a sequence of the
 * parsed rows.
 */
fun Reader.parseCsv(
    dialect: CsvDialect = CsvDialect.EXCEL,
    hasHeaders: Boolean = false,
    headers: List<String>? = null,
): Sequence<List<String>> {
    return readCsv(this, dialect, hasHeaders, headers) {
        it.row
    }
}

/**
 * Reads CSV data from this [Reader] and returns a sequence of the
 * parsed rows as a map.
 */
fun Reader.parseCsvMap(
    dialect: CsvDialect = CsvDialect.EXCEL,
    hasHeaders: Boolean = false,
    headers: List<String>? = null,
    restKey: String = "",
): Sequence<Map<String, String>> {
    return readCsv(this, dialect, hasHeaders, headers) {
        it.toMap(restKey)
    }
}

// -----------------------------------------------------------------------------
//
// String
//
// -----------------------------------------------------------------------------

/**
 * Reads CSV data from a string and returns a list of the
 * values produced by the [rowMapper] function.
 */
fun <T> readCsv(
    text: String,
    dialect: CsvDialect = CsvDialect.EXCEL,
    hasHeaders: Boolean = false,
    headers: List<String>? = null,
    rowMapper: (CsvRow) -> T,
): Sequence<T> {
    return readCsv(text.reader(), dialect, hasHeaders, headers, rowMapper)
}

/**
 * Reads CSV data from a string and returns a sequence of the
 * parsed rows.
 */
fun readCsv(
    text: String,
    dialect: CsvDialect = CsvDialect.EXCEL,
    hasHeaders: Boolean = false,
    headers: List<String>? = null,
): Sequence<List<String>> {
    return readCsv(text.reader(), dialect, hasHeaders, headers) { it.row }
}

/**
 * Reads CSV data from a string and returns a sequence of the
 * parsed rows as a map.
 */
fun readCsvMap(
    text: String,
    dialect: CsvDialect = CsvDialect.EXCEL,
    hasHeaders: Boolean = false,
    headers: List<String>? = null,
    restKey: String = "",
): Sequence<Map<String, String>> {
    return readCsv(text.reader(), dialect, hasHeaders, headers) {
        it.toMap(restKey)
    }
}

/**
 * Reads CSV data from this [String] and returns a sequence of the
 * values produced by the [rowMapper] function.
 */
fun <T> String.parseCsv(
    dialect: CsvDialect = CsvDialect.EXCEL,
    hasHeaders: Boolean = false,
    headers: List<String>? = null,
    rowMapper: (CsvRow) -> T,
): Sequence<T> {
    return readCsv(this, dialect, hasHeaders, headers, rowMapper)
}

/**
 * Reads CSV data from this [String] and returns a sequence of the
 * parsed rows
 */
fun String.parseCsv(
    dialect: CsvDialect = CsvDialect.EXCEL,
    hasHeaders: Boolean = false,
    headers: List<String>? = null,
): Sequence<List<String>> {
    return readCsv(this, dialect, hasHeaders, headers) {
        it.row
    }
}

/**
 * Reads CSV data from this [String] and returns a sequence of the
 * parsed rows as a map.
 */
fun String.parseCsvMap(
    dialect: CsvDialect = CsvDialect.EXCEL,
    hasHeaders: Boolean = false,
    headers: List<String>? = null,
    restKey: String = "",
): Sequence<Map<String, String>> {
    return readCsv(this, dialect, hasHeaders, headers) {
        it.toMap(restKey)
    }
}
