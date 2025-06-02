package dev.hirth.csv

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class CsvDictWriterTest {
    
    @Test
    fun testBasicDictWriting() {
        val fieldNames = listOf("name", "age", "city")
        val (writer, getString) = csvDictWriter(fieldNames)
        
        writer.writeRow(mapOf("name" to "John", "age" to 25, "city" to "NYC"))
        writer.writeRow(mapOf("name" to "Jane", "age" to 30, "city" to "LA"))
        
        val expected = "name,age,city\r\nJohn,25,NYC\r\nJane,30,LA\r\n"
        assertEquals(expected, getString())
    }
    
    @Test
    fun testMissingFields() {
        val fieldNames = listOf("name", "age", "city")
        val (writer, getString) = csvDictWriter(fieldNames)
        
        writer.writeRow(mapOf("name" to "John", "age" to 25))
        
        val expected = "name,age,city\r\nJohn,25,\r\n"
        assertEquals(expected, getString())
    }
    
    @Test
    fun testRestValue() {
        val fieldNames = listOf("name", "age", "city")
        val (writer, getString) = csvDictWriter(fieldNames, restValue = "Unknown")
        
        writer.writeRow(mapOf("name" to "John", "age" to 25))
        
        val expected = "name,age,city\r\nJohn,25,Unknown\r\n"
        assertEquals(expected, getString())
    }
    
    @Test
    fun testExtraFieldsRaise() {
        val fieldNames = listOf("name", "age")
        val (writer, _) = csvDictWriter(fieldNames, extrasAction = CsvDictWriter.ExtrasAction.RAISE)
        
        assertThrows<CsvError> {
            writer.writeRow(mapOf("name" to "John", "age" to 25, "city" to "NYC"))
        }
    }
    
    @Test
    fun testExtraFieldsIgnore() {
        val fieldNames = listOf("name", "age")
        val (writer, getString) = csvDictWriter(fieldNames, extrasAction = CsvDictWriter.ExtrasAction.IGNORE)
        
        writer.writeRow(mapOf("name" to "John", "age" to 25, "city" to "NYC"))
        
        val expected = "name,age\r\nJohn,25\r\n"
        assertEquals(expected, getString())
    }
    
    @Test
    fun testManualHeaderWrite() {
        val fieldNames = listOf("name", "age")
        val (writer, getString) = csvDictWriter(fieldNames)
        
        writer.writeHeader()
        writer.writeRow(mapOf("name" to "John", "age" to 25))
        
        val expected = "name,age\r\nJohn,25\r\n"
        assertEquals(expected, getString())
    }
    
    @Test
    fun testFormatCsvDictUtility() {
        val fieldNames = listOf("name", "age")
        val rows = listOf(
            mapOf("name" to "John", "age" to 25),
            mapOf("name" to "Jane", "age" to 30)
        )
        
        val result = formatCsvDict(rows, fieldNames)
        val expected = "name,age\r\nJohn,25\r\nJane,30\r\n"
        assertEquals(expected, result)
    }
    
    @Test
    fun testFieldNamesProperty() {
        val fieldNames = listOf("name", "age", "city")
        val (writer, _) = csvDictWriter(fieldNames)
        
        assertEquals(fieldNames, writer.fieldNames)
    }
    
    @Test
    fun testNullValues() {
        val fieldNames = listOf("name", "age", "city")
        val (writer, getString) = csvDictWriter(fieldNames)
        
        writer.writeRow(mapOf("name" to "John", "age" to null, "city" to "NYC"))
        
        val expected = "name,age,city\r\nJohn,,NYC\r\n"
        assertEquals(expected, getString())
    }
}