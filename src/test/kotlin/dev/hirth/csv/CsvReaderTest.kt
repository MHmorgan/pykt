package dev.hirth.csv

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class CsvReaderTest {
    
    @Test
    fun testBasicReading() {
        val csv = "a,b,c\n1,2,3\n4,5,6"
        val reader = csvReader(csv)
        
        val rows = reader.asSequence().toList()
        assertEquals(3, rows.size)
        assertEquals(listOf("a", "b", "c"), rows[0])
        assertEquals(listOf("1", "2", "3"), rows[1])
        assertEquals(listOf("4", "5", "6"), rows[2])
    }
    
    @Test
    fun testQuotedFields() {
        val csv = """a,"b,c",d
1,"2,3",4"""
        val reader = csvReader(csv)
        
        val rows = reader.asSequence().toList()
        assertEquals(2, rows.size)
        assertEquals(listOf("a", "b,c", "d"), rows[0])
        assertEquals(listOf("1", "2,3", "4"), rows[1])
    }
    
    @Test
    fun testEscapedQuotes() {
        val csv = """a,"b""c",d
1,"2""3",4"""
        val reader = csvReader(csv)
        
        val rows = reader.asSequence().toList()
        assertEquals(2, rows.size)
        assertEquals(listOf("a", "b\"c", "d"), rows[0])
        assertEquals(listOf("1", "2\"3", "4"), rows[1])
    }
    
    @Test
    fun testEmptyFields() {
        val csv = "a,,c\n,2,\n,,,"
        val reader = csvReader(csv)
        
        val rows = reader.asSequence().toList()
        assertEquals(3, rows.size)
        assertEquals(listOf("a", "", "c"), rows[0])
        assertEquals(listOf("", "2", ""), rows[1])
        assertEquals(listOf("", "", "", ""), rows[2]) // ",,," has 3 commas = 4 fields
    }
    
    @Test
    fun testCustomDialect() {
        val csv = "a|b|c\n1|2|3"
        val dialect = CsvDialect(delimiter = '|')
        val reader = csvReader(csv, dialect)
        
        val rows = reader.asSequence().toList()
        assertEquals(2, rows.size)
        assertEquals(listOf("a", "b", "c"), rows[0])
        assertEquals(listOf("1", "2", "3"), rows[1])
    }
    
    @Test
    fun testLineNumberTracking() {
        val csv = "a,b,c\n1,2,3\n4,5,6"
        val reader = csvReader(csv)
        
        assertEquals(1, reader.lineNumber)
        reader.next()
        assertEquals(2, reader.lineNumber)
        reader.next()
        assertEquals(3, reader.lineNumber)
        reader.next()
        assertEquals(4, reader.lineNumber)
    }
    
    @Test
    fun testEmptyInput() {
        val reader = csvReader("")
        assertFalse(reader.hasNext())
    }
    
    @Test
    fun testSingleEmptyLine() {
        val reader = csvReader("\n")
        assertTrue(reader.hasNext())
        assertEquals(listOf(""), reader.next())
        assertFalse(reader.hasNext())
    }
}