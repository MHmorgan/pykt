package dev.hirth.csv

import java.io.Reader
import java.io.StringReader

/**
 * A CSV reader that parses CSV data into sequences of string lists.
 */
class CsvReader(
    private val reader: Reader,
    private val dialect: CsvDialect = CsvDialect.EXCEL
) : Iterator<List<String>> {
    
    private var currentLine = 0
    private var buffer = StringBuilder()
    private var nextRow: List<String>? = null
    private var hasNext = true
    
    init {
        readNext()
    }
    
    override fun hasNext(): Boolean = hasNext
    
    override fun next(): List<String> {
        if (!hasNext) {
            throw NoSuchElementException("No more CSV rows")
        }
        val result = nextRow!!
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
                throw CsvError("Error parsing CSV at line $currentLine", e)
            }
        }
    }
    
    private fun parseRow(): List<String>? {
        val fields = mutableListOf<String>()
        var field = StringBuilder()
        var inQuotes = false
        var c: Int
        
        currentLine++ // Increment line number at start of each row
        
        while (true) {
            c = reader.read()
            
            if (c == -1) {
                // End of file
                if (inQuotes && dialect.strict) {
                    throw CsvError("Unexpected end of file in quoted field at line $currentLine")
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
                        throw CsvError("Escape character outside quoted field at line $currentLine")
                    }
                    val next = reader.read()
                    if (next == -1) {
                        if (dialect.strict) {
                            throw CsvError("Unexpected end of file after escape character at line $currentLine")
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
    
    /**
     * Returns the current line number being processed.
     */
    val lineNumber: Int get() = currentLine
}

/**
 * Creates a CSV reader from a string.
 */
fun csvReader(
    text: String,
    dialect: CsvDialect = CsvDialect.EXCEL
): CsvReader = CsvReader(StringReader(text), dialect)

/**
 * Creates a CSV reader from a Reader.
 */
fun csvReader(
    reader: Reader,
    dialect: CsvDialect = CsvDialect.EXCEL
): CsvReader = CsvReader(reader, dialect)