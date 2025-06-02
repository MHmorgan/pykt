package dev.hirth.csv

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CsvDialectTest {
    
    @Test
    fun testExcelDialect() {
        val dialect = CsvDialect.EXCEL
        
        assertEquals(',', dialect.delimiter)
        assertEquals('"', dialect.quoteChar)
        assertEquals(null, dialect.escapeChar)
        assertEquals(false, dialect.skipInitialSpace)
        assertEquals("\r\n", dialect.lineTerminator)
        assertEquals(QuotingStyle.MINIMAL, dialect.quoting)
        assertEquals(true, dialect.doubleQuote)
        assertEquals(false, dialect.strict)
    }
    
    @Test
    fun testExcelTabDialect() {
        val dialect = CsvDialect.EXCEL_TAB
        
        assertEquals('\t', dialect.delimiter)
        assertEquals('"', dialect.quoteChar)
        assertEquals(null, dialect.escapeChar)
        assertEquals(false, dialect.skipInitialSpace)
        assertEquals("\r\n", dialect.lineTerminator)
        assertEquals(QuotingStyle.MINIMAL, dialect.quoting)
        assertEquals(true, dialect.doubleQuote)
        assertEquals(false, dialect.strict)
    }
    
    @Test
    fun testUnixDialect() {
        val dialect = CsvDialect.UNIX
        
        assertEquals(',', dialect.delimiter)
        assertEquals('"', dialect.quoteChar)
        assertEquals(null, dialect.escapeChar)
        assertEquals(false, dialect.skipInitialSpace)
        assertEquals("\n", dialect.lineTerminator)
        assertEquals(QuotingStyle.ALL, dialect.quoting)
        assertEquals(true, dialect.doubleQuote)
        assertEquals(false, dialect.strict)
    }
    
    @Test
    fun testCustomDialect() {
        val dialect = CsvDialect(
            delimiter = '|',
            quoteChar = '\'',
            escapeChar = '\\',
            skipInitialSpace = true,
            lineTerminator = "\n",
            quoting = QuotingStyle.NON_NUMERIC,
            doubleQuote = false,
            strict = true
        )
        
        assertEquals('|', dialect.delimiter)
        assertEquals('\'', dialect.quoteChar)
        assertEquals('\\', dialect.escapeChar)
        assertEquals(true, dialect.skipInitialSpace)
        assertEquals("\n", dialect.lineTerminator)
        assertEquals(QuotingStyle.NON_NUMERIC, dialect.quoting)
        assertEquals(false, dialect.doubleQuote)
        assertEquals(true, dialect.strict)
    }
    
    @Test
    fun testQuotingStyles() {
        assertEquals(4, QuotingStyle.values().size)
        assertTrue(QuotingStyle.values().contains(QuotingStyle.ALL))
        assertTrue(QuotingStyle.values().contains(QuotingStyle.MINIMAL))
        assertTrue(QuotingStyle.values().contains(QuotingStyle.NON_NUMERIC))
        assertTrue(QuotingStyle.values().contains(QuotingStyle.NONE))
    }
}