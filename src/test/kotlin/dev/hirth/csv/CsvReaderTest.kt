package dev.hirth.csv

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CsvReaderTest {

    @Test
    fun testBasicReading() {
        val csv = "a,b,c\n1,2,3\n4,5,6"
        val rows = csv.parseCsv().toList()
        assertEquals(3, rows.size)
        assertEquals(listOf("a", "b", "c"), rows[0])
        assertEquals(listOf("1", "2", "3"), rows[1])
        assertEquals(listOf("4", "5", "6"), rows[2])
    }

    @Test
    fun testQuotedFields() {
        val csv = """
            a,"b,c",d
            1,"2,3",4
        """.trimIndent()

        val rows = csv.parseCsv().toList()
        assertEquals(2, rows.size)
        assertEquals(listOf("a", "b,c", "d"), rows[0])
        assertEquals(listOf("1", "2,3", "4"), rows[1])
    }

    @Test
    fun testEscapedQuotes() {
        val csv = """
            a,"b""c",d
            1,"2""3",4
        """.trimIndent()

        val rows = csv.parseCsv().toList()
        assertEquals(2, rows.size)
        assertEquals(listOf("a", "b\"c", "d"), rows[0])
        assertEquals(listOf("1", "2\"3", "4"), rows[1])
    }

    @Test
    fun testEmptyFields() {
        val csv = "a,,c\n,2,\n,,,"

        val rows = csv.parseCsv().toList()
        assertEquals(3, rows.size)
        assertEquals(listOf("a", "", "c"), rows[0])
        assertEquals(listOf("", "2", ""), rows[1])
        assertEquals(listOf("", "", "", ""), rows[2]) // ",,," has 3 commas = 4 fields
    }

    @Test
    fun testCustomDialect() {
        val csv = "a|b|c\n1|2|3"
        val dialect = CsvDialect(delimiter = '|')

        val rows = csv.parseCsv(dialect).toList()
        assertEquals(2, rows.size)
        assertEquals(listOf("a", "b", "c"), rows[0])
        assertEquals(listOf("1", "2", "3"), rows[1])
    }

    @Test
    fun testLineNumberTracking() {
        val csv = "a,b,c\n1,2,3\n4,5,6"

        var num = 1
        csv.parseCsv {
            assertEquals(num++, it.lineNumber)
        }
    }

    @Test
    fun testEmptyInput() {
        val rows = "".parseCsv().toList()
        assertTrue(rows.isEmpty())
    }

    @Test
    fun testSingleEmptyLine() {
        val rows = "\n".parseCsv().toList()
        assertEquals(1, rows.size)
        assertEquals(listOf(""), rows[0])
    }

    @Test
    fun `Test Row Mapping`() {
        data class Foo(val s: String, val i: Int)

        val csv = "s,i\nhello,2\nworld,5"

        val rows = csv.parseCsv(hasHeaders = true) { row ->
            Foo(row.column("s")!!, row.column("i")!!.toInt())
        }.toList()

        assertEquals(2, rows.size)
        assertEquals(Foo("hello", 2), rows[0])
        assertEquals(Foo("world", 5), rows[1])
    }

    @Test
    fun `Test CSV Map`() {
        val csv = "hello,2\nworld,5"

        val rows = csv.parseCsvMap(headers = listOf("s", "i")).toList()
        assertEquals(2, rows.size)
        assertEquals(mapOf("s" to "hello", "i" to "2"), rows[0])
        assertEquals(mapOf("s" to "world", "i" to "5"), rows[1])
    }
}