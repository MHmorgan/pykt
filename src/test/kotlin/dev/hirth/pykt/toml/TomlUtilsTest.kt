package dev.hirth.pykt.toml

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.StringReader
import java.io.StringWriter

class TomlUtilsTest {

    @TempDir
    lateinit var tempDir: File

    @Test
    fun testReadTomlFromString() {
        val tomlContent = """
            title = "Test Document"
            number = 42
        """.trimIndent()

        val document = readToml(tomlContent)
        assertEquals("Test Document", document.getString("title"))
        assertEquals(42L, document.getInteger("number"))
    }

    @Test
    fun testReadTomlFromReader() {
        val tomlContent = """
            title = "Test Document"
            number = 42
        """.trimIndent()

        val document = StringReader(tomlContent).use { readToml(it) }
        assertEquals("Test Document", document.getString("title"))
        assertEquals(42L, document.getInteger("number"))
    }

    @Test
    fun testReadTomlFromFile() {
        val tomlContent = """
            title = "Test Document"
            number = 42
        """.trimIndent()

        val file = File(tempDir, "test.toml")
        file.writeText(tomlContent)

        val document = readToml(file)
        assertEquals("Test Document", document.getString("title"))
        assertEquals(42L, document.getInteger("number"))
    }

    @Test
    fun testStringExtension() {
        val tomlContent = """
            title = "Test Document"
            number = 42
        """.trimIndent()

        val document = tomlContent.parseToml()
        assertEquals("Test Document", document.getString("title"))
        assertEquals(42L, document.getInteger("number"))
    }

    @Test
    fun testReaderExtension() {
        val tomlContent = """
            title = "Test Document"
            number = 42
        """.trimIndent()

        val document = StringReader(tomlContent).use { it.parseToml() }
        assertEquals("Test Document", document.getString("title"))
        assertEquals(42L, document.getInteger("number"))
    }

    @Test
    fun testFileExtensions() {
        val tomlContent = """
            title = "Test Document"
            number = 42
        """.trimIndent()

        val file = File(tempDir, "test.toml")
        file.writeText(tomlContent)

        val document = file.parseToml()
        assertEquals("Test Document", document.getString("title"))
        assertEquals(42L, document.getInteger("number"))

        // Test writing back to file
        val outputFile = File(tempDir, "output.toml")
        outputFile.writeToml {
            string("title", "New Title")
            integer("number", 100)
        }

        val newDocument = outputFile.parseToml()
        assertEquals("New Title", newDocument.getString("title"))
        assertEquals(100L, newDocument.getInteger("number"))
    }

    @Test
    fun testWriterExtension() {
        val writer = StringWriter()
        writer.writeToml {
            string("title", "Test")
            integer("number", 42)
        }

        val tomlContent = writer.toString()
        val document = tomlContent.parseToml()
        assertEquals("Test", document.getString("title"))
        assertEquals(42L, document.getInteger("number"))
    }

    @Test
    fun testTomlDocumentExtensions() {
        val document = buildToml {
            string("title", "Test Document")
            integer("number", 42)
            array("items", "a", "b", "c")
        }

        // Test toTomlString
        val tomlString = document.toTomlString()
        val reparsedDocument = tomlString.parseToml()
        assertEquals("Test Document", reparsedDocument.getString("title"))
        assertEquals(42L, reparsedDocument.getInteger("number"))
        assertEquals(listOf("a", "b", "c"), reparsedDocument.getStringArray("items"))

        // Test writeTo with Writer
        val writer = StringWriter()
        document.writeTo(writer)
        val writerContent = writer.toString()
        val writerDocument = writerContent.parseToml()
        assertEquals("Test Document", writerDocument.getString("title"))
        assertEquals(42L, writerDocument.getInteger("number"))

        // Test writeTo with File
        val file = File(tempDir, "output.toml")
        document.writeTo(file)
        val fileDocument = file.parseToml()
        assertEquals("Test Document", fileDocument.getString("title"))
        assertEquals(42L, fileDocument.getInteger("number"))
    }

    @Test
    fun testValueAccessMethods() {
        val document = buildToml {
            string("str_val", "hello")
            integer("int_val", 42)
            float("float_val", 3.14)
            boolean("bool_val", true)
            array("str_array", "a", "b", "c")
            array("int_array", 1, 2, 3)
            array("float_array", 1.1, 2.2, 3.3)
            array("bool_array", true, false, true)
            inlineTable("inline") {
                string("x", "value")
                integer("y", 10)
            }
        }

        assertEquals("hello", document.getString("str_val"))
        assertEquals(42L, document.getInteger("int_val"))
        assertEquals(3.14, document.getFloat("float_val"))
        assertEquals(true, document.getBoolean("bool_val"))

        assertEquals(listOf("a", "b", "c"), document.getStringArray("str_array"))
        assertEquals(listOf(1L, 2L, 3L), document.getIntegerArray("int_array"))
        assertEquals(listOf(1.1, 2.2, 3.3), document.getFloatArray("float_array"))
        assertEquals(listOf(true, false, true), document.getBooleanArray("bool_array"))

        val inlineTable = document.getInlineTable("inline")!!
        assertEquals(2, inlineTable.size)
        assertEquals(TomlValue.String("value"), inlineTable["x"])
        assertEquals(TomlValue.Integer(10), inlineTable["y"])

        // Test null returns for non-existent keys
        assertNull(document.getString("non_existent"))
        assertNull(document.getInteger("non_existent"))
        assertNull(document.getFloat("non_existent"))
        assertNull(document.getBoolean("non_existent"))
        assertNull(document.getArray("non_existent"))
        assertNull(document.getInlineTable("non_existent"))
    }
}