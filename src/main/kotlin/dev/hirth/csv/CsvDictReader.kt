package dev.hirth.csv

import java.io.Reader
import java.io.StringReader

/**
 * A CSV reader that maps rows to dictionaries using field names.
 */
class CsvDictReader(
    reader: Reader,
    private val initialFieldNames: List<String>? = null,
    private val dialect: CsvDialect = CsvDialect.EXCEL,
    private val restKey: String? = null,
    private val restValue: String = ""
) : Iterator<Map<String, String>> {
    
    private val csvReader = CsvReader(reader, dialect)
    private var headers: List<String>? = null
    private var nextRow: Map<String, String>? = null
    private var hasNext = true
    
    init {
        // If fieldNames not provided, read first row as headers
        if (initialFieldNames == null && csvReader.hasNext()) {
            headers = csvReader.next()
        } else {
            headers = initialFieldNames
        }
        readNext()
    }
    
    override fun hasNext(): Boolean = hasNext
    
    override fun next(): Map<String, String> {
        if (!hasNext) {
            throw NoSuchElementException("No more CSV rows")
        }
        val result = nextRow!!
        readNext()
        return result
    }
    
    private fun readNext() {
        if (!csvReader.hasNext()) {
            hasNext = false
            return
        }
        
        val row = csvReader.next()
        val headerList = headers ?: emptyList()
        
        val result = mutableMapOf<String, String>()
        
        // Map fields to headers
        for (i in headerList.indices) {
            val value = if (i < row.size) row[i] else restValue
            result[headerList[i]] = value
        }
        
        // Handle extra fields if restKey is provided
        if (restKey != null && row.size > headerList.size) {
            val extraValues = row.drop(headerList.size)
            result[restKey] = extraValues.joinToString(dialect.delimiter.toString())
        }
        
        nextRow = result
    }
    
    /**
     * The field names being used as dictionary keys.
     */
    val fieldNames: List<String>? get() = headers
    
    /**
     * The current line number being processed.
     */
    val lineNumber: Int get() = csvReader.lineNumber
}

/**
 * Creates a CSV dictionary reader from a string.
 */
fun csvDictReader(
    text: String,
    fieldNames: List<String>? = null,
    dialect: CsvDialect = CsvDialect.EXCEL,
    restKey: String? = null,
    restValue: String = ""
): CsvDictReader = CsvDictReader(StringReader(text), fieldNames, dialect, restKey, restValue)

/**
 * Creates a CSV dictionary reader from a Reader.
 */
fun csvDictReader(
    reader: Reader,
    fieldNames: List<String>? = null,
    dialect: CsvDialect = CsvDialect.EXCEL,
    restKey: String? = null,
    restValue: String = ""
): CsvDictReader = CsvDictReader(reader, fieldNames, dialect, restKey, restValue)