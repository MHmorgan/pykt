package dev.hirth.csv

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CsvDictReaderTest {
    
    @Test
    fun testBasicDictReading() {
        val csv = "name,age,city\nJohn,25,NYC\nJane,30,LA"
        val reader = csvDictReader(csv)
        
        val rows = reader.asSequence().toList()
        assertEquals(2, rows.size)
        
        assertEquals(mapOf("name" to "John", "age" to "25", "city" to "NYC"), rows[0])
        assertEquals(mapOf("name" to "Jane", "age" to "30", "city" to "LA"), rows[1])
    }
    
    @Test
    fun testExplicitFieldNames() {
        val csv = "John,25,NYC\nJane,30,LA"
        val fieldNames = listOf("name", "age", "city")
        val reader = csvDictReader(csv, fieldNames)
        
        val rows = reader.asSequence().toList()
        assertEquals(2, rows.size)
        
        assertEquals(mapOf("name" to "John", "age" to "25", "city" to "NYC"), rows[0])
        assertEquals(mapOf("name" to "Jane", "age" to "30", "city" to "LA"), rows[1])
    }
    
    @Test
    fun testMissingFields() {
        val csv = "name,age,city\nJohn,25\nJane,30,LA,Extra"
        val reader = csvDictReader(csv)
        
        val rows = reader.asSequence().toList()
        assertEquals(2, rows.size)
        
        assertEquals(mapOf("name" to "John", "age" to "25", "city" to ""), rows[0])
        assertEquals(mapOf("name" to "Jane", "age" to "30", "city" to "LA"), rows[1])
    }
    
    @Test
    fun testRestKey() {
        val csv = "name,age\nJohn,25,NYC,USA\nJane,30,LA"
        val reader = csvDictReader(csv, restKey = "extra")
        
        val rows = reader.asSequence().toList()
        assertEquals(2, rows.size)
        
        assertEquals(mapOf("name" to "John", "age" to "25", "extra" to "NYC,USA"), rows[0])
        assertEquals(mapOf("name" to "Jane", "age" to "30", "extra" to "LA"), rows[1]) // LA should be in extra
    }
    
    @Test
    fun testRestValue() {
        val csv = "name,age,city\nJohn,25\nJane,30,LA"
        val reader = csvDictReader(csv, restValue = "Unknown")
        
        val rows = reader.asSequence().toList()
        assertEquals(2, rows.size)
        
        assertEquals(mapOf("name" to "John", "age" to "25", "city" to "Unknown"), rows[0])
        assertEquals(mapOf("name" to "Jane", "age" to "30", "city" to "LA"), rows[1])
    }
    
    @Test
    fun testEmptyInput() {
        val reader = csvDictReader("")
        assertFalse(reader.hasNext())
    }
    
    @Test
    fun testHeaderOnlyInput() {
        val reader = csvDictReader("name,age,city")
        assertFalse(reader.hasNext())
        assertEquals(listOf("name", "age", "city"), reader.fieldNames)
    }
    
    @Test
    fun testFieldNamesProperty() {
        val csv = "name,age,city\nJohn,25,NYC"
        val reader = csvDictReader(csv)
        
        assertEquals(listOf("name", "age", "city"), reader.fieldNames)
    }
}