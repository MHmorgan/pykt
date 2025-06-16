package dev.hirth.pykt.ini

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.StringReader

class IniExtensionsTest {

    private val sampleIni = """
        global_key=global_value
        
        [section1]
        key1=value1
        key2=value2
        
        [section2]
        key3=value3
    """.trimIndent()

    @Test
    fun testReadIniFromReader() {
        val reader = StringReader(sampleIni)
        val iniFile = readIni(reader)

        assertEquals("global_value", iniFile.get("DEFAULT", "global_key"))
        assertEquals("value1", iniFile.get("section1", "key1"))
        assertEquals("value2", iniFile.get("section1", "key2"))
        assertEquals("value3", iniFile.get("section2", "key3"))
    }

    @Test
    fun testReadIniFromString() {
        val iniFile = readIni(sampleIni)

        assertEquals("global_value", iniFile.get("DEFAULT", "global_key"))
        assertEquals("value1", iniFile.get("section1", "key1"))
        assertEquals("value2", iniFile.get("section1", "key2"))
        assertEquals("value3", iniFile.get("section2", "key3"))
    }

    @Test
    fun testReaderParseIniExtension() {
        val reader = StringReader(sampleIni)
        val iniFile = reader.parseIni()

        assertEquals("global_value", iniFile.get("DEFAULT", "global_key"))
        assertEquals("value1", iniFile.get("section1", "key1"))
        assertEquals("value2", iniFile.get("section1", "key2"))
        assertEquals("value3", iniFile.get("section2", "key3"))
    }

    @Test
    fun testStringParseIniExtension() {
        val iniFile = sampleIni.parseIni()

        assertEquals("global_value", iniFile.get("DEFAULT", "global_key"))
        assertEquals("value1", iniFile.get("section1", "key1"))
        assertEquals("value2", iniFile.get("section1", "key2"))
        assertEquals("value3", iniFile.get("section2", "key3"))
    }

    @Test
    fun testReadIniWithCustomDialect() {
        val customDialect = IniDialect(caseSensitive = true)
        val iniFile = readIni(sampleIni, customDialect)

        assertEquals("global_value", iniFile.get("DEFAULT", "global_key"))
        assertEquals("value1", iniFile.get("section1", "key1"))
    }

    @Test
    fun testReadIniMapFromReader() {
        val reader = StringReader(sampleIni)
        val map = readIniMap(reader)

        val expected = mapOf(
            "global_key" to "global_value",
            "section1.key1" to "value1",
            "section1.key2" to "value2",
            "section2.key3" to "value3"
        )

        assertEquals(expected, map)
    }

    @Test
    fun testReadIniMapFromString() {
        val map = readIniMap(sampleIni)

        val expected = mapOf(
            "global_key" to "global_value",
            "section1.key1" to "value1",
            "section1.key2" to "value2",
            "section2.key3" to "value3"
        )

        assertEquals(expected, map)
    }

    @Test
    fun testReaderParseIniMapExtension() {
        val reader = StringReader(sampleIni)
        val map = reader.parseIniMap()

        val expected = mapOf(
            "global_key" to "global_value",
            "section1.key1" to "value1",
            "section1.key2" to "value2",
            "section2.key3" to "value3"
        )

        assertEquals(expected, map)
    }

    @Test
    fun testStringParseIniMapExtension() {
        val map = sampleIni.parseIniMap()

        val expected = mapOf(
            "global_key" to "global_value",
            "section1.key1" to "value1",
            "section1.key2" to "value2",
            "section2.key3" to "value3"
        )

        assertEquals(expected, map)
    }

    @Test
    fun testReadIniMapWithCustomDialect() {
        val input = """
            [SECTION]
            Key=Value
        """.trimIndent()

        val caseSensitiveDialect = IniDialect(caseSensitive = true)
        val map = readIniMap(input, caseSensitiveDialect)

        assertEquals(mapOf("SECTION.Key" to "Value"), map)
    }

    @Test
    fun testReadIniMapWithPropertiesDialect() {
        val input = """
            key1=value1
            key2=value2
        """.trimIndent()

        val map = readIniMap(input, IniDialect.PROPERTIES)

        val expected = mapOf(
            "key1" to "value1",
            "key2" to "value2"
        )

        assertEquals(expected, map)
    }

    @Test
    fun testEmptyInput() {
        val emptyIni = ""
        val iniFile = readIni(emptyIni)
        val map = readIniMap(emptyIni)

        assertTrue(iniFile.sectionNames.isEmpty())
        assertTrue(map.isEmpty())
    }

    @Test
    fun testOnlyCommentsAndWhitespace() {
        val input = """
            # This is a comment
            ; Another comment
            
            # More comments
        """.trimIndent()

        val iniFile = readIni(input)
        val map = readIniMap(input)

        assertTrue(iniFile.sectionNames.isEmpty())
        assertTrue(map.isEmpty())
    }
}