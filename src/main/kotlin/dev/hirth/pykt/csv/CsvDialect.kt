package dev.hirth.pykt.csv

/**
 * Defines the quoting behavior for CSV fields.
 */
enum class QuotingStyle {
    /** Quote all fields regardless of type. */
    ALL,
    
    /** Quote fields only when necessary (contains delimiter, quote char, or newline). */
    MINIMAL,
    
    /** Quote all non-numeric fields. */
    NON_NUMERIC,
    
    /** Never quote fields. */
    NONE
}

/**
 * Defines the properties of a CSV dialect.
 */
data class CsvDialect(
    /** The delimiter character used to separate fields. */
    val delimiter: Char = ',',
    
    /** The character used to quote fields. */
    val quoteChar: Char = '"',
    
    /** The character used to escape special characters. */
    val escapeChar: Char? = null,
    
    /** Whether to skip initial whitespace after delimiter. */
    val skipInitialSpace: Boolean = false,
    
    /** The line terminator string. */
    val lineTerminator: String = "\r\n",
    
    /** The quoting style to use. */
    val quoting: QuotingStyle = QuotingStyle.MINIMAL,
    
    /** Whether quotes should be doubled to escape them. */
    val doubleQuote: Boolean = true,
    
    /** Whether to have strict parsing (raise exceptions on bad CSV). */
    val strict: Boolean = false
) {
    companion object {
        /** Standard Excel CSV dialect. */
        val EXCEL = CsvDialect()
        
        /** Excel dialect with tab delimiters. */
        val EXCEL_TAB = CsvDialect(delimiter = '\t')
        
        /** Unix-style CSV dialect. */
        val UNIX = CsvDialect(
            quoting = QuotingStyle.ALL,
            lineTerminator = "\n"
        )
    }
}