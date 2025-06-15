package dev.hirth.pykt.ini

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.StringWriter

class IniWriterTest {
    
    @Test
    fun testWriteEmptyIniFile() {
        val iniFile = IniFile()
        val output = writeIni(iniFile)
        
        assertEquals("", output)
    }
    
    @Test
    fun testWriteIniFileWithDefaultSection() {
        val iniFile = IniFile()
        iniFile.set("DEFAULT", "key1", "value1")
        iniFile.set("DEFAULT", "key2", "value2")
        
        val output = writeIni(iniFile)
        val expected = """
            key1=value1
            key2=value2
        """.trimIndent()
        
        assertEquals(expected, output.trim())
    }
    
    @Test
    fun testWriteIniFileWithSections() {
        val iniFile = IniFile()
        iniFile.set("DEFAULT", "global_key", "global_value")
        iniFile.set("section1", "key1", "value1")
        iniFile.set("section1", "key2", "value2")
        iniFile.set("section2", "key3", "value3")
        
        val output = writeIni(iniFile)
        
        // The output should contain all sections and keys
        assertTrue(output.contains("global_key=global_value"))
        assertTrue(output.contains("[section1]"))
        assertTrue(output.contains("key1=value1"))
        assertTrue(output.contains("key2=value2"))
        assertTrue(output.contains("[section2]"))
        assertTrue(output.contains("key3=value3"))
        
        // Parse it back to verify correctness
        val parsedIni = readIni(output)
        assertEquals("global_value", parsedIni.get("DEFAULT", "global_key"))
        assertEquals("value1", parsedIni.get("section1", "key1"))
        assertEquals("value2", parsedIni.get("section1", "key2"))
        assertEquals("value3", parsedIni.get("section2", "key3"))
    }
    
    @Test
    fun testWriteIniFileWithMultilineValues() {
        val iniFile = IniFile()
        iniFile.set("DEFAULT", "single_line", "simple value")
        iniFile.set("DEFAULT", "multi_line", "line1\nline2\nline3")
        iniFile.set("section1", "another_multi", "first\nsecond")
        
        val output = writeIni(iniFile)
        
        // Should contain continuation lines
        assertTrue(output.contains("multi_line=line1"))
        assertTrue(output.contains(" line2"))
        assertTrue(output.contains(" line3"))
        assertTrue(output.contains("another_multi=first"))
        assertTrue(output.contains(" second"))
        
        // Parse it back to verify correctness
        val parsedIni = readIni(output)
        assertEquals("simple value", parsedIni.get("DEFAULT", "single_line"))
        assertEquals("line1\nline2\nline3", parsedIni.get("DEFAULT", "multi_line"))
        assertEquals("first\nsecond", parsedIni.get("section1", "another_multi"))
    }
    
    @Test
    fun testWriteWithCustomDialect() {
        val customDialect = IniDialect(
            separators = setOf(':'),
            defaultSectionName = "GLOBAL"
        )
        
        val iniFile = IniFile(customDialect)
        iniFile.set("GLOBAL", "key1", "value1")
        iniFile.set("section1", "key2", "value2")
        
        val output = writeIni(iniFile, customDialect)
        
        // Should use colon separator
        assertTrue(output.contains("key1:value1"))
        assertTrue(output.contains("key2:value2"))
        
        // Parse it back with the same dialect
        val parsedIni = readIni(output, customDialect)
        assertEquals("value1", parsedIni.get("GLOBAL", "key1"))
        assertEquals("value2", parsedIni.get("section1", "key2"))
    }
    
    @Test
    fun testWriteToWriter() {
        val iniFile = IniFile()
        iniFile.set("DEFAULT", "key", "value")
        iniFile.set("section", "key2", "value2")
        
        val writer = StringWriter()
        writeIni(writer, iniFile)
        val output = writer.toString()
        
        assertTrue(output.contains("key=value"))
        assertTrue(output.contains("[section]"))
        assertTrue(output.contains("key2=value2"))
    }
    
    @Test
    fun testRoundTripConversion() {
        val originalIni = """
            global_setting=true
            timeout=30
            
            [database]
            host=localhost
            port=5432
            
            [logging]
            level=INFO
            multiline_format=Line 1
             Line 2
             Line 3
        """.trimIndent()
        
        // Parse and write back
        val iniFile = readIni(originalIni)
        val output = writeIni(iniFile)
        
        // Parse the output again
        val reparsedIni = readIni(output)
        
        // Verify all values are preserved
        assertEquals("true", reparsedIni.get("DEFAULT", "global_setting"))
        assertEquals("30", reparsedIni.get("DEFAULT", "timeout"))
        assertEquals("localhost", reparsedIni.get("database", "host"))
        assertEquals("5432", reparsedIni.get("database", "port"))
        assertEquals("INFO", reparsedIni.get("logging", "level"))
        assertEquals("Line 1\nLine 2\nLine 3", reparsedIni.get("logging", "multiline_format"))
    }
    
    @Test
    fun testWriteEmptyValues() {
        val iniFile = IniFile()
        iniFile.set("DEFAULT", "empty_value", "")
        iniFile.set("DEFAULT", "normal_value", "test")
        
        val output = writeIni(iniFile)
        
        assertTrue(output.contains("empty_value="))
        assertTrue(output.contains("normal_value=test"))
        
        // Parse it back
        val parsedIni = readIni(output)
        assertEquals("", parsedIni.get("DEFAULT", "empty_value"))
        assertEquals("test", parsedIni.get("DEFAULT", "normal_value"))
    }
    
    @Test
    fun testWriteSpecialCharacters() {
        val iniFile = IniFile()
        iniFile.set("DEFAULT", "special_chars", "value with = and : and # characters")
        iniFile.set("section", "unicode", "Héllo Wörld! 🌍")
        
        val output = writeIni(iniFile)
        
        assertTrue(output.contains("special_chars=value with = and : and # characters"))
        assertTrue(output.contains("unicode=Héllo Wörld! 🌍"))
        
        // Parse it back
        val parsedIni = readIni(output)
        assertEquals("value with = and : and # characters", parsedIni.get("DEFAULT", "special_chars"))
        assertEquals("Héllo Wörld! 🌍", parsedIni.get("section", "unicode"))
    }
}