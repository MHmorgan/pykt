package dev.hirth.pykt.properties

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class IntTest {

    @Test
    fun testIntRO() {
        val map = mapOf("existingKey" to "42")

        class TestClass {
            val existingKey by IntRO(map)
            val missingKey by IntRO(map) { it.length }
        }

        val testClass = TestClass()
        assertEquals(42, testClass.existingKey)
        assertEquals(10, testClass.missingKey) // "missingKey".length = 10
    }

    @Test
    fun testNullableIntRO() {
        val map = mapOf("existingKey" to "42")

        class TestClass {
            val existingKey by NullableIntRO(map)
            val missingKey by NullableIntRO(map)
            val missingKeyWithDefault by NullableIntRO(map) { 19 }
        }

        val testClass = TestClass()
        assertEquals(42, testClass.existingKey)
        assertNull(testClass.missingKey)
        assertEquals(19, testClass.missingKeyWithDefault) // "missingKeyWithDefault".length = 19
    }

    @Test
    fun testIntRW() {
        val map = mutableMapOf("existingKey" to "42")

        class TestClass {
            var existingKey by IntRW(map)
            var missingKey by IntRW(map) { it.length }
        }

        val testClass = TestClass()
        assertEquals(42, testClass.existingKey)
        assertEquals(10, testClass.missingKey)

        // Test write operations
        testClass.existingKey = 99
        testClass.missingKey = 77

        assertEquals("99", map["existingKey"])
        assertEquals("77", map["missingKey"])
        assertEquals(99, testClass.existingKey)
        assertEquals(77, testClass.missingKey)
    }

    @Test
    fun testNullableIntRW() {
        val map = mutableMapOf("existingKey" to "42")

        class TestClass {
            var existingKey by NullableIntRW(map)
            var missingKey by NullableIntRW(map)
            var missingKeyWithDefault by NullableIntRW(map) { 19 }
        }

        val testClass = TestClass()
        assertEquals(42, testClass.existingKey)
        assertNull(testClass.missingKey)
        assertEquals(19, testClass.missingKeyWithDefault)

        // Test write operations
        testClass.existingKey = 99
        testClass.missingKey = 77
        testClass.missingKeyWithDefault = null

        assertEquals("99", map["existingKey"])
        assertEquals("77", map["missingKey"])
        // Setting to null doesn't add a key
        assertNull(map["missingKeyWithDefault"])

        // Verify after write
        assertEquals(99, testClass.existingKey)
        assertEquals(77, testClass.missingKey)
        assertEquals(19, testClass.missingKeyWithDefault) // Should use default
    }
}
