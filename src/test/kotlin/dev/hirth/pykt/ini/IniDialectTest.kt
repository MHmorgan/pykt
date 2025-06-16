package dev.hirth.pykt.ini

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class IniDialectTest {
    
    @Test
    fun testDefaultDialect() {
        val dialect = IniDialect.DEFAULT
        
        assertEquals(setOf('=', ':'), dialect.separators)
        assertEquals(setOf('#', ';'), dialect.commentPrefixes)
        assertEquals(false, dialect.caseSensitive)
        assertEquals(true, dialect.trimWhitespace)
        assertEquals(true, dialect.allowEmptyValues)
        assertEquals(false, dialect.allowKeysWithoutValues)
        assertEquals(true, dialect.allowMultilineValues)
        assertEquals(" ", dialect.continuationPrefix)
        assertEquals(false, dialect.allowInterpolation)
        assertEquals("DEFAULT", dialect.defaultSectionName)
        assertEquals(true, dialect.strict)
    }
    
    @Test
    fun testPermissiveDialect() {
        val dialect = IniDialect.PERMISSIVE
        
        assertEquals(setOf('=', ':'), dialect.separators)
        assertEquals(setOf('#', ';'), dialect.commentPrefixes)
        assertEquals(false, dialect.caseSensitive)
        assertEquals(true, dialect.trimWhitespace)
        assertEquals(true, dialect.allowEmptyValues)
        assertEquals(true, dialect.allowKeysWithoutValues)
        assertEquals(true, dialect.allowMultilineValues)
        assertEquals(" ", dialect.continuationPrefix)
        assertEquals(false, dialect.allowInterpolation)
        assertEquals("DEFAULT", dialect.defaultSectionName)
        assertEquals(false, dialect.strict)
    }
    
    @Test
    fun testPropertiesDialect() {
        val dialect = IniDialect.PROPERTIES
        
        assertEquals(setOf('=', ':'), dialect.separators)
        assertEquals(setOf('#', '!'), dialect.commentPrefixes)
        assertEquals(false, dialect.caseSensitive)
        assertEquals(true, dialect.trimWhitespace)
        assertEquals(true, dialect.allowEmptyValues)
        assertEquals(false, dialect.allowKeysWithoutValues)
        assertEquals(true, dialect.allowMultilineValues)
        assertEquals(" ", dialect.continuationPrefix)
        assertEquals(false, dialect.allowInterpolation)
        assertEquals("", dialect.defaultSectionName)
        assertEquals(true, dialect.strict)
    }
    
    @Test
    fun testCustomDialect() {
        val dialect = IniDialect(
            separators = setOf('='),
            commentPrefixes = setOf('#'),
            caseSensitive = true,
            trimWhitespace = false,
            strict = false
        )
        
        assertEquals(setOf('='), dialect.separators)
        assertEquals(setOf('#'), dialect.commentPrefixes)
        assertEquals(true, dialect.caseSensitive)
        assertEquals(false, dialect.trimWhitespace)
        assertEquals(false, dialect.strict)
    }
}