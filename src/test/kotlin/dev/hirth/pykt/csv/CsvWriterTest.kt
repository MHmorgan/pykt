package dev.hirth.pykt.csv

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CsvWriterTest {

    @Test
    fun testBasicWriting() {
        val actual = buildCsv {
            writeRow(listOf("a", "b", "c"))
            writeRow(listOf("1", "2", "3"))
            writeRow(listOf("4", "5", "6"))
        }

        val expected = "a,b,c\r\n1,2,3\r\n4,5,6\r\n"
        assertEquals(expected, actual)
    }

    @Test
    fun testQuotingMinimal() {
        val actual = buildCsv {
            writeRow(listOf("a", "b,c", "d"))
            writeRow(listOf("1", "2\"3", "4"))
        }

        val expected = "a,\"b,c\",d\r\n1,\"2\"\"3\",4\r\n"
        assertEquals(expected, actual)
    }

    @Test
    fun testQuotingAll() {
        val dialect = CsvDialect(quoting = QuotingStyle.ALL)

        val actual = buildCsv(dialect) {
            writeRow(listOf("a", "b", "c"))
        }

        val expected = "\"a\",\"b\",\"c\"\r\n"
        assertEquals(expected, actual)
    }

    @Test
    fun testQuotingNone() {
        val dialect = CsvDialect(quoting = QuotingStyle.NONE)

        val actual = buildCsv(dialect) {
            writeRow(listOf("a", "b", "c"))
        }

        val expected = "a,b,c\r\n"
        assertEquals(expected, actual)
    }

    @Test
    fun testQuotingNonNumeric() {
        val dialect = CsvDialect(quoting = QuotingStyle.NON_NUMERIC)

        val actual = buildCsv(dialect) {
            writeRow(listOf("a", "123", "45.6"))
        }

        val expected = "\"a\",123,45.6\r\n"
        assertEquals(expected, actual)
    }

    @Test
    fun testCustomDelimiter() {
        val dialect = CsvDialect(delimiter = '|')

        val actual = buildCsv(dialect) {
            writeRow(listOf("a", "b", "c"))
        }

        val expected = "a|b|c\r\n"
        assertEquals(expected, actual)
    }

    @Test
    fun testEmptyFields() {
        val actual = buildCsv {
            writeRow(listOf("", "b", ""))
        }

        val expected = ",b,\r\n"
        assertEquals(expected, actual)
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
        val actual = buildCsv {
            writeRow(listOf("a", null, "c"))
        }

        val expected = "a,,c\r\n"
        assertEquals(expected, actual)
    }
}