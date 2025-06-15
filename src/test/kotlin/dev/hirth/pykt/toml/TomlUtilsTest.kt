package dev.hirth.pykt.toml

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.StringReader

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
    fun testFileExtension() {
        val tomlContent = """
            title = "Test Document"
            number = 42
        """.trimIndent()

        val file = File(tempDir, "test.toml")
        file.writeText(tomlContent)

        val document = file.parseToml()
        assertEquals("Test Document", document.getString("title"))
        assertEquals(42L, document.getInteger("number"))
    }

    @Test
    fun testValueAccessMethods() {
        val tomlContent = """
            str_val = "hello"
            int_val = 42
            float_val = 3.14
            bool_val = true
            str_array = ["a", "b", "c"]
            int_array = [1, 2, 3]
            float_array = [1.1, 2.2, 3.3]
            bool_array = [true, false, true]
            inline = { x = "value", y = 10 }
        """.trimIndent()

        val document = tomlContent.parseToml()

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