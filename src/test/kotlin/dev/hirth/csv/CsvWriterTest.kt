package dev.hirth.csv

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CsvWriterTest {
    
    @Test
    fun testBasicWriting() {
        val (writer, getString) = csvWriter()
        
        writer.writeRow(listOf("a", "b", "c"))
        writer.writeRow(listOf("1", "2", "3"))
        writer.writeRow(listOf("4", "5", "6"))
        
        val expected = "a,b,c\r\n1,2,3\r\n4,5,6\r\n"
        assertEquals(expected, getString())
    }
    
    @Test
    fun testQuotingMinimal() {
        val (writer, getString) = csvWriter()
        
        writer.writeRow(listOf("a", "b,c", "d"))
        writer.writeRow(listOf("1", "2\"3", "4"))
        
        val expected = "a,\"b,c\",d\r\n1,\"2\"\"3\",4\r\n"
        assertEquals(expected, getString())
    }
    
    @Test
    fun testQuotingAll() {
        val dialect = CsvDialect(quoting = QuotingStyle.ALL)
        val (writer, getString) = csvWriter(dialect)
        
        writer.writeRow(listOf("a", "b", "c"))
        
        val expected = "\"a\",\"b\",\"c\"\r\n"
        assertEquals(expected, getString())
    }
    
    @Test
    fun testQuotingNone() {
        val dialect = CsvDialect(quoting = QuotingStyle.NONE)
        val (writer, getString) = csvWriter(dialect)
        
        writer.writeRow(listOf("a", "b", "c"))
        
        val expected = "a,b,c\r\n"
        assertEquals(expected, getString())
    }
    
    @Test
    fun testQuotingNonNumeric() {
        val dialect = CsvDialect(quoting = QuotingStyle.NON_NUMERIC)
        val (writer, getString) = csvWriter(dialect)
        
        writer.writeRow(listOf("a", "123", "45.6"))
        
        val expected = "\"a\",123,45.6\r\n"
        assertEquals(expected, getString())
    }
    
    @Test
    fun testCustomDelimiter() {
        val dialect = CsvDialect(delimiter = '|')
        val (writer, getString) = csvWriter(dialect)
        
        writer.writeRow(listOf("a", "b", "c"))
        
        val expected = "a|b|c\r\n"
        assertEquals(expected, getString())
    }
    
    @Test
    fun testEmptyFields() {
        val (writer, getString) = csvWriter()
        
        writer.writeRow(listOf("", "b", ""))
        
        val expected = ",b,\r\n"
        assertEquals(expected, getString())
    }
    
    @Test
    fun testFormatCsvUtility() {
        val rows = listOf(
            listOf("a", "b", "c"),
            listOf("1", "2", "3")
        )
        
        val result = formatCsv(rows)
        val expected = "a,b,c\r\n1,2,3\r\n"
        assertEquals(expected, result)
    }
    
    @Test
    fun testNullValues() {
        val (writer, getString) = csvWriter()
        
        writer.writeRow(listOf("a", null, "c"))
        
        val expected = "a,,c\r\n"
        assertEquals(expected, getString())
    }
}