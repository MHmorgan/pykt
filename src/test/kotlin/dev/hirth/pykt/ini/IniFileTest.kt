package dev.hirth.pykt.ini

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class IniFileTest {

    @Test
    fun testEmptyIniFile() {
        val iniFile = IniFile()

        assertTrue(iniFile.sectionNames.isEmpty())
        assertTrue(iniFile.sections.isEmpty())
        assertFalse(iniFile.hasSection("test"))
        assertNull(iniFile.getSection("test"))
        assertNull(iniFile.get("test", "key"))
        assertEquals("default", iniFile.get("test", "key", "default"))
        assertFalse(iniFile.has("test", "key"))
        assertTrue(iniFile.toFlatMap().isEmpty())
        assertTrue(iniFile.getDefaults().isEmpty())
    }

    @Test
    fun testSectionManagement() {
        val iniFile = IniFile()

        // Create section using section() method
        val section1 = iniFile.section("section1")
        assertEquals("section1", section1.name)
        assertTrue(iniFile.hasSection("section1"))
        assertEquals(section1, iniFile.getSection("section1"))
        assertEquals(setOf("section1"), iniFile.sectionNames)

        // Add section directly
        val section2 = IniSection("section2")
        section2.set("key", "value")
        iniFile.addSection(section2)
        assertTrue(iniFile.hasSection("section2"))
        assertEquals(section2, iniFile.getSection("section2"))
        assertEquals(setOf("section1", "section2"), iniFile.sectionNames)

        // Remove section
        val removed = iniFile.removeSection("section1")
        assertEquals(section1, removed)
        assertFalse(iniFile.hasSection("section1"))
        assertEquals(setOf("section2"), iniFile.sectionNames)

        // Remove non-existent section
        val removedNull = iniFile.removeSection("non_existent")
        assertNull(removedNull)
    }

    @Test
    fun testCaseSensitivity() {
        val caseSensitive = IniFile(IniDialect(caseSensitive = true))
        val caseInsensitive = IniFile(IniDialect(caseSensitive = false))

        // Case sensitive
        caseSensitive.set("Section", "key", "value1")
        caseSensitive.set("section", "key", "value2")

        assertTrue(caseSensitive.hasSection("Section"))
        assertTrue(caseSensitive.hasSection("section"))
        assertEquals("value1", caseSensitive.get("Section", "key"))
        assertEquals("value2", caseSensitive.get("section", "key"))
        assertEquals(2, caseSensitive.sectionNames.size)

        // Case insensitive
        caseInsensitive.set("Section", "key", "value1")
        caseInsensitive.set("section", "key", "value2")

        assertTrue(caseInsensitive.hasSection("Section"))
        assertTrue(caseInsensitive.hasSection("section"))
        assertTrue(caseInsensitive.hasSection("SECTION"))
        assertEquals("value2", caseInsensitive.get("Section", "key"))
        assertEquals("value2", caseInsensitive.get("section", "key"))
        assertEquals("value2", caseInsensitive.get("SECTION", "key"))
        assertEquals(1, caseInsensitive.sectionNames.size)
    }

    @Test
    fun testKeyValueOperations() {
        val iniFile = IniFile()

        // Set values
        iniFile.set("section1", "key1", "value1")
        iniFile.set("section1", "key2", "value2")
        iniFile.set("section2", "key1", "value3")

        // Get values
        assertEquals("value1", iniFile.get("section1", "key1"))
        assertEquals("value2", iniFile.get("section1", "key2"))
        assertEquals("value3", iniFile.get("section2", "key1"))
        assertNull(iniFile.get("section1", "key3"))
        assertNull(iniFile.get("section3", "key1"))

        // Get with defaults
        assertEquals("value1", iniFile.get("section1", "key1", "default"))
        assertEquals("default", iniFile.get("section1", "key3", "default"))
        assertEquals("default", iniFile.get("section3", "key1", "default"))

        // Check existence
        assertTrue(iniFile.has("section1", "key1"))
        assertTrue(iniFile.has("section1", "key2"))
        assertTrue(iniFile.has("section2", "key1"))
        assertFalse(iniFile.has("section1", "key3"))
        assertFalse(iniFile.has("section3", "key1"))

        // Remove values
        assertEquals("value1", iniFile.remove("section1", "key1"))
        assertFalse(iniFile.has("section1", "key1"))
        assertNull(iniFile.remove("section1", "key1")) // Already removed
        assertNull(iniFile.remove("section3", "key1")) // Non-existent section/key
    }

    @Test
    fun testFlatMap() {
        val iniFile = IniFile()

        iniFile.set("section1", "key1", "value1")
        iniFile.set("section1", "key2", "value2")
        iniFile.set("section2", "key1", "value3")
        iniFile.set("DEFAULT", "default_key", "default_value")

        val flatMap = iniFile.toFlatMap()

        val expected = mapOf(
            "section1.key1" to "value1",
            "section1.key2" to "value2",
            "section2.key1" to "value3",
            "default_key" to "default_value"
        )

        assertEquals(expected, flatMap)
    }

    @Test
    fun testDefaultSection() {
        val iniFile = IniFile()

        iniFile.set("DEFAULT", "key1", "value1")
        iniFile.set("DEFAULT", "key2", "value2")
        iniFile.set("section1", "key1", "value3")

        val defaults = iniFile.getDefaults()
        val expected = mapOf("key1" to "value1", "key2" to "value2")

        assertEquals(expected, defaults)
    }

    @Test
    fun testCustomDefaultSection() {
        val iniFile = IniFile(IniDialect(defaultSectionName = "GLOBAL"))

        iniFile.set("GLOBAL", "key1", "value1")
        iniFile.set("section1", "key1", "value2")

        val defaults = iniFile.getDefaults()
        assertEquals(mapOf("key1" to "value1"), defaults)

        val flatMap = iniFile.toFlatMap()
        val expected = mapOf(
            "key1" to "value1",
            "section1.key1" to "value2"
        )
        assertEquals(expected, flatMap)
    }
}