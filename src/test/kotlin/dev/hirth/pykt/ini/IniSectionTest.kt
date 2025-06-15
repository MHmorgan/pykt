package dev.hirth.pykt.ini

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class IniSectionTest {
    
    @Test
    fun testEmptySection() {
        val section = IniSection("test")
        
        assertEquals("test", section.name)
        assertTrue(section.isEmpty())
        assertEquals(0, section.size())
        assertEquals(emptyMap<String, String>(), section.entries)
        assertEquals(emptySet<String>(), section.keys)
        assertFalse(section.containsKey("key"))
        assertNull(section.get("key"))
        assertEquals("default", section.get("key", "default"))
    }
    
    @Test
    fun testSectionWithEntries() {
        val section = IniSection("config")
        
        section.set("key1", "value1")
        section.set("key2", "value2")
        
        assertFalse(section.isEmpty())
        assertEquals(2, section.size())
        assertEquals(setOf("key1", "key2"), section.keys)
        assertTrue(section.containsKey("key1"))
        assertTrue(section.containsKey("key2"))
        assertFalse(section.containsKey("key3"))
        
        assertEquals("value1", section.get("key1"))
        assertEquals("value2", section.get("key2"))
        assertNull(section.get("key3"))
        assertEquals("default", section.get("key3", "default"))
        
        val expected = mapOf("key1" to "value1", "key2" to "value2")
        assertEquals(expected, section.entries)
    }
    
    @Test
    fun testSectionModification() {
        val section = IniSection("test")
        
        // Add entries
        section.set("key1", "value1")
        section.set("key2", "value2")
        assertEquals(2, section.size())
        
        // Update existing entry
        section.set("key1", "new_value1")
        assertEquals("new_value1", section.get("key1"))
        assertEquals(2, section.size())
        
        // Remove entry
        val removed = section.remove("key1")
        assertEquals("new_value1", removed)
        assertEquals(1, section.size())
        assertFalse(section.containsKey("key1"))
        assertTrue(section.containsKey("key2"))
        
        // Remove non-existent entry
        val removedNull = section.remove("non_existent")
        assertNull(removedNull)
        assertEquals(1, section.size())
        
        // Clear all
        section.remove("key2")
        assertTrue(section.isEmpty())
        assertEquals(0, section.size())
    }
    
    @Test
    fun testSectionWithSpecialValues() {
        val section = IniSection("special")
        
        // Empty value
        section.set("empty", "")
        assertEquals("", section.get("empty"))
        assertTrue(section.containsKey("empty"))
        
        // Whitespace value
        section.set("whitespace", "  \t  ")
        assertEquals("  \t  ", section.get("whitespace"))
        
        // Multiline value
        section.set("multiline", "line1\nline2\nline3")
        assertEquals("line1\nline2\nline3", section.get("multiline"))
        
        // Special characters
        section.set("special", "value with = and : and # and ;")
        assertEquals("value with = and : and # and ;", section.get("special"))
    }
}