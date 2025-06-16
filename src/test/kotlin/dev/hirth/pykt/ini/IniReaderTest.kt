package dev.hirth.pykt.ini

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.StringReader

class IniReaderTest {

    @Test
    fun testEmptyInput() {
        val reader = IniReader(StringReader(""))
        val iniFile = reader.read()

        assertTrue(iniFile.sectionNames.isEmpty())
    }

    @Test
    fun testSimpleKeyValue() {
        val input = """
            key1=value1
            key2 = value2
            key3: value3
            key4 :  value4
        """.trimIndent()

        val reader = IniReader(StringReader(input))
        val iniFile = reader.read()

        assertEquals("value1", iniFile.get("DEFAULT", "key1"))
        assertEquals("value2", iniFile.get("DEFAULT", "key2"))
        assertEquals("value3", iniFile.get("DEFAULT", "key3"))
        assertEquals("value4", iniFile.get("DEFAULT", "key4"))
        assertEquals(1, iniFile.sectionNames.size)
    }

    @Test
    fun testSections() {
        val input = """
            global_key=global_value
            
            [section1]
            key1=value1
            key2=value2
            
            [section2]
            key1=value3
            key3=value4
        """.trimIndent()

        val reader = IniReader(StringReader(input))
        val iniFile = reader.read()

        assertEquals(3, iniFile.sectionNames.size)
        assertTrue(iniFile.hasSection("DEFAULT"))
        assertTrue(iniFile.hasSection("section1"))
        assertTrue(iniFile.hasSection("section2"))

        assertEquals("global_value", iniFile.get("DEFAULT", "global_key"))
        assertEquals("value1", iniFile.get("section1", "key1"))
        assertEquals("value2", iniFile.get("section1", "key2"))
        assertEquals("value3", iniFile.get("section2", "key1"))
        assertEquals("value4", iniFile.get("section2", "key3"))
    }

    @Test
    fun testComments() {
        val input = """
            # This is a comment
            key1=value1
            ; This is also a comment
            key2=value2  ; inline comment should be part of value
            #key3=value3  # commented out key
        """.trimIndent()

        val reader = IniReader(StringReader(input))
        val iniFile = reader.read()

        assertEquals("value1", iniFile.get("DEFAULT", "key1"))
        assertEquals("value2  ; inline comment should be part of value", iniFile.get("DEFAULT", "key2"))
        assertFalse(iniFile.has("DEFAULT", "key3"))
        assertEquals(2, iniFile.getSection("DEFAULT")!!.size())
    }

    @Test
    fun testEmptyValues() {
        val input = """
            empty_value=
            another_empty=
            spaced_empty =  
        """.trimIndent()

        val reader = IniReader(StringReader(input))
        val iniFile = reader.read()

        assertEquals("", iniFile.get("DEFAULT", "empty_value"))
        assertEquals("", iniFile.get("DEFAULT", "another_empty"))
        assertEquals("", iniFile.get("DEFAULT", "spaced_empty"))
    }

    @Test
    fun testMultilineValues() {
        val input = """
            multiline=first line
             second line
             third line
            normal=single line
            another_multiline: line 1
             line 2
             line 3
        """.trimIndent()

        val reader = IniReader(StringReader(input))
        val iniFile = reader.read()

        val expectedMultiline = "first line\nsecond line\nthird line"
        assertEquals(expectedMultiline, iniFile.get("DEFAULT", "multiline"))
        assertEquals("single line", iniFile.get("DEFAULT", "normal"))

        val expectedAnother = "line 1\nline 2\nline 3"
        assertEquals(expectedAnother, iniFile.get("DEFAULT", "another_multiline"))
    }

    @Test
    fun testWhitespaceHandling() {
        val input = """
            [  section1  ]
            key1 = value1
              key2  =  value2  
            key3= value3 
        """.trimIndent()

        val reader = IniReader(StringReader(input))
        val iniFile = reader.read()

        assertTrue(iniFile.hasSection("section1"))
        assertEquals("value1", iniFile.get("section1", "key1"))
        assertEquals("value2", iniFile.get("section1", "key2"))
        assertEquals("value3", iniFile.get("section1", "key3"))
    }

    @Test
    fun testKeysWithoutValues() {
        val input = """
            key_with_value=value
            key_without_value
            another_with_value=another
        """.trimIndent()

        val permissiveDialect = IniDialect.PERMISSIVE
        val reader = IniReader(StringReader(input), permissiveDialect)
        val iniFile = reader.read()

        assertEquals("value", iniFile.get("DEFAULT", "key_with_value"))
        assertEquals("", iniFile.get("DEFAULT", "key_without_value"))
        assertEquals("another", iniFile.get("DEFAULT", "another_with_value"))
    }

    @Test
    fun testStrictMode() {
        val input = """
            key_without_value
            valid_key=valid_value
        """.trimIndent()

        val strictDialect = IniDialect.DEFAULT
        val reader = IniReader(StringReader(input), strictDialect)

        assertThrows<IniError> {
            reader.read()
        }
    }

    @Test
    fun testNonStrictMode() {
        val input = """
            key_without_value
            =value_without_key
            valid_key=valid_value
        """.trimIndent()

        val permissiveDialect = IniDialect.PERMISSIVE
        val reader = IniReader(StringReader(input), permissiveDialect)
        val iniFile = reader.read()

        // In non-strict mode, should handle errors gracefully
        assertTrue(iniFile.has("DEFAULT", "valid_key"))
        assertEquals("valid_value", iniFile.get("DEFAULT", "valid_key"))
    }

    @Test
    fun testCaseSensitivity() {
        val input = """
            [Section1]
            Key1=value1
            
            [section1]
            key1=value2
        """.trimIndent()

        val caseSensitiveDialect = IniDialect(caseSensitive = true)
        val caseInsensitiveDialect = IniDialect(caseSensitive = false)

        // Case sensitive
        val caseSensitiveReader = IniReader(StringReader(input), caseSensitiveDialect)
        val caseSensitiveFile = caseSensitiveReader.read()

        assertEquals(2, caseSensitiveFile.sectionNames.size)
        assertEquals("value1", caseSensitiveFile.get("Section1", "Key1"))
        assertEquals("value2", caseSensitiveFile.get("section1", "key1"))

        // Case insensitive  
        val caseInsensitiveReader = IniReader(StringReader(input), caseInsensitiveDialect)
        val caseInsensitiveFile = caseInsensitiveReader.read()

        assertEquals(1, caseInsensitiveFile.sectionNames.size)
        // Should have the second value (overwrites first)
        assertEquals("value2", caseInsensitiveFile.get("section1", "key1"))
    }

    @Test
    fun testCustomSeparators() {
        val input = """
            key1=value1
            key2:value2
            key3 value3
        """.trimIndent()

        val customDialect = IniDialect(separators = setOf('=', ':', ' '))
        val reader = IniReader(StringReader(input), customDialect)
        val iniFile = reader.read()

        assertEquals("value1", iniFile.get("DEFAULT", "key1"))
        assertEquals("value2", iniFile.get("DEFAULT", "key2"))
        assertEquals("value3", iniFile.get("DEFAULT", "key3"))
    }

    @Test
    fun testCustomCommentPrefixes() {
        val input = """
            # This should be ignored
            key1=value1
            ! This should also be ignored
            key2=value2
            ; This should NOT be ignored with custom dialect
            semicolon_key=semicolon_value
        """.trimIndent()

        val customDialect = IniDialect(commentPrefixes = setOf('#', '!'), allowKeysWithoutValues = true)
        val reader = IniReader(StringReader(input), customDialect)
        val iniFile = reader.read()

        assertEquals("value1", iniFile.get("DEFAULT", "key1"))
        assertEquals("value2", iniFile.get("DEFAULT", "key2"))
        assertEquals("", iniFile.get("DEFAULT", "; This should NOT be ignored with custom dialect"))
        assertEquals("semicolon_value", iniFile.get("DEFAULT", "semicolon_key"))
        assertEquals(4, iniFile.getSection("DEFAULT")!!.size())
    }

    @Test
    fun testInvalidSectionHeader() {
        val input = """
            [incomplete section
            key=value
        """.trimIndent()

        val reader = IniReader(StringReader(input))

        assertThrows<IniError> {
            reader.read()
        }
    }

    @Test
    fun testComplexExample() {
        val input = """
            # Global configuration
            global_setting=true
            debug_level=2
            
            [database]
            host=localhost
            port=5432
            username=admin
            password=secret
            # Connection pool settings
            max_connections=10
            
            [logging]
            level=INFO
            file=/var/log/app.log
            format=%(asctime)s - %(name)s - %(levelname)s - %(message)s
            
            [features]
            feature_a=enabled
            feature_b=disabled
            experimental_feature=
        """.trimIndent()

        val reader = IniReader(StringReader(input))
        val iniFile = reader.read()

        assertEquals(4, iniFile.sectionNames.size)

        // Global settings
        assertEquals("true", iniFile.get("DEFAULT", "global_setting"))
        assertEquals("2", iniFile.get("DEFAULT", "debug_level"))

        // Database section
        assertEquals("localhost", iniFile.get("database", "host"))
        assertEquals("5432", iniFile.get("database", "port"))
        assertEquals("admin", iniFile.get("database", "username"))
        assertEquals("secret", iniFile.get("database", "password"))
        assertEquals("10", iniFile.get("database", "max_connections"))

        // Logging section
        assertEquals("INFO", iniFile.get("logging", "level"))
        assertEquals("/var/log/app.log", iniFile.get("logging", "file"))
        assertEquals("%(asctime)s - %(name)s - %(levelname)s - %(message)s", iniFile.get("logging", "format"))

        // Features section
        assertEquals("enabled", iniFile.get("features", "feature_a"))
        assertEquals("disabled", iniFile.get("features", "feature_b"))
        assertEquals("", iniFile.get("features", "experimental_feature"))
    }
}