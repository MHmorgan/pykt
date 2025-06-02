package dev.hirth.csv

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class CsvIntegrationTest {
    
    @Test
    fun testCompleteWorkflow() {
        // Create some test data
        val originalData = listOf(
            mapOf("name" to "John Doe", "age" to 30, "city" to "New York"),
            mapOf("name" to "Jane Smith", "age" to 25, "city" to "Los Angeles"),
            mapOf("name" to "Bob Johnson", "age" to 35, "city" to "Chicago")
        )
        
        val fieldNames = listOf("name", "age", "city")
        
        // Write data to CSV string
        val csvString = formatCsvDict(originalData, fieldNames)
        
        // Verify CSV format
        val expectedCsv = "name,age,city\r\nJohn Doe,30,New York\r\nJane Smith,25,Los Angeles\r\nBob Johnson,35,Chicago\r\n"
        assertEquals(expectedCsv, csvString)
        
        // Read data back from CSV string  
        val reader = csvDictReader(csvString)
        val readData = reader.asSequence().toList()
        
        // Verify data integrity
        assertEquals(3, readData.size)
        assertEquals("John Doe", readData[0]["name"])
        assertEquals("30", readData[0]["age"]) // Note: CSV reads as strings
        assertEquals("New York", readData[0]["city"])
        
        assertEquals("Jane Smith", readData[1]["name"])
        assertEquals("25", readData[1]["age"])
        assertEquals("Los Angeles", readData[1]["city"])
        
        assertEquals("Bob Johnson", readData[2]["name"])
        assertEquals("35", readData[2]["age"])
        assertEquals("Chicago", readData[2]["city"])
    }
    
    @Test
    fun testComplexQuotingScenario() {
        // Test data with various special characters that need quoting
        val testData = listOf(
            listOf("Simple", "123", "normal"),
            listOf("With,comma", "With\"quote", "With\nnewline"),
            listOf("", "With spaces ", " Leading space")
        )
        
        val csvString = formatCsv(testData)
        val reader = csvReader(csvString)
        val readData = reader.asSequence().toList()
        
        assertEquals(testData, readData)
    }
    
    @Test
    fun testDifferentDialects() {
        val data = listOf(listOf("a", "b", "c"), listOf("1", "2", "3"))
        
        // Test Excel dialect (default)
        val excelCsv = formatCsv(data, CsvDialect.EXCEL)
        assertEquals("a,b,c\r\n1,2,3\r\n", excelCsv)
        
        // Test Excel-Tab dialect
        val excelTabCsv = formatCsv(data, CsvDialect.EXCEL_TAB)
        assertEquals("a\tb\tc\r\n1\t2\t3\r\n", excelTabCsv)
        
        // Test Unix dialect
        val unixCsv = formatCsv(data, CsvDialect.UNIX)
        assertEquals("\"a\",\"b\",\"c\"\n\"1\",\"2\",\"3\"\n", unixCsv)
    }
    
    @Test
    fun testErrorHandling() {
        // Test CSV error for invalid quoted field in strict mode
        val badCsv = "a,\"unclosed quote,c"
        val strictDialect = CsvDialect(strict = true)
        
        assertThrows<CsvError> {
            csvReader(badCsv, strictDialect).asSequence().toList()
        }
        
        // Test that non-strict mode handles it gracefully
        val nonStrictResult = csvReader(badCsv, CsvDialect.EXCEL).asSequence().toList()
        assertNotNull(nonStrictResult)
    }
}