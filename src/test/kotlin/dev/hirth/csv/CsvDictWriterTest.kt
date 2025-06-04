package dev.hirth.csv

import dev.hirth.csv.CsvDictWriter.ExtrasAction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class CsvDictWriterTest {

    @Test
    fun testBasicDictWriting() {
        val fieldNames = listOf("name", "age", "city")

        val actual = buildDictCsv(fieldNames) {
            writeRow(mapOf("name" to "John", "age" to 25, "city" to "NYC"))
            writeRow(mapOf("name" to "Jane", "age" to 30, "city" to "LA"))
        }

        val expected = "name,age,city\r\nJohn,25,NYC\r\nJane,30,LA\r\n"
        assertEquals(expected, actual)
    }

    @Test
    fun testMissingFields() {
        val fieldNames = listOf("name", "age", "city")

        val actual = buildDictCsv(fieldNames) {
            writeRow(mapOf("name" to "John", "age" to 25))
        }

        val expected = "name,age,city\r\nJohn,25,\r\n"
        assertEquals(expected, actual)
    }

    @Test
    fun testRestValue() {
        val fieldNames = listOf("name", "age", "city")

        val actual = buildDictCsv(fieldNames, restValue = "Unknown") {
            writeRow(mapOf("name" to "John", "age" to 25))
        }

        val expected = "name,age,city\r\nJohn,25,Unknown\r\n"
        assertEquals(expected, actual)
    }

    @Test
    fun testExtraFieldsRaise() {
        val fieldNames = listOf("name", "age")

        assertThrows<CsvError> {
            buildDictCsv(fieldNames, extrasAction = ExtrasAction.RAISE) {
                writeRow(mapOf("name" to "John", "age" to 25, "city" to "NYC"))
            }
        }
    }

    @Test
    fun testExtraFieldsIgnore() {
        val fieldNames = listOf("name", "age")

        val actual = buildDictCsv(fieldNames, extrasAction = ExtrasAction.IGNORE) {
            writeRow(mapOf("name" to "John", "age" to 25, "city" to "NYC"))
        }

        val expected = "name,age\r\nJohn,25\r\n"
        assertEquals(expected, actual)
    }

    @Test
    fun testManualHeaderWrite() {
        val fieldNames = listOf("name", "age")

        val actual = buildDictCsv(fieldNames) {
            writeHeader()
            writeRow(mapOf("name" to "John", "age" to 25))
        }

        val expected = "name,age\r\nJohn,25\r\n"
        assertEquals(expected, actual)
    }

    @Test
    fun testFormatCsvDictUtility() {
        val fieldNames = listOf("name", "age")
        val rows = listOf(
            mapOf("name" to "John", "age" to 25),
            mapOf("name" to "Jane", "age" to 30)
        )

        val result = formatDictCsv(rows, fieldNames)
        val expected = "name,age\r\nJohn,25\r\nJane,30\r\n"
        assertEquals(expected, result)
    }

    @Test
    fun testNullValues() {
        val fieldNames = listOf("name", "age", "city")

        val actual = buildDictCsv(fieldNames) {
            writeRow(mapOf("name" to "John", "age" to null, "city" to "NYC"))
        }

        val expected = "name,age,city\r\nJohn,,NYC\r\n"
        assertEquals(expected, actual)
    }
}