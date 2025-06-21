package dev.hirth.pykt.properties

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class StringTest {

    @Test
    fun testStringRO() {
        val map = mapOf("existingKey" to "value")

        class TestClass {
            val existingKey by StringRO(map)
            val missingKey by StringRO(map) { "prefix-missingKey" }
        }

        val testClass = TestClass()
        assertEquals("value", testClass.existingKey)
        assertEquals("prefix-missingKey", testClass.missingKey)
    }

    @Test
    fun testNullableStringRO() {
        val map = mapOf("existingKey" to "value")

        class TestClass {
            val existingKey by NullableStringRO(map)
            val missingKey by NullableStringRO(map)
            val missingKeyWithDefault by NullableStringRO(map) { "prefix-missingKeyWithDefault" }
        }

        val testClass = TestClass()
        assertEquals("value", testClass.existingKey)
        assertNull(testClass.missingKey)
        assertEquals("prefix-missingKeyWithDefault", testClass.missingKeyWithDefault)
    }

    @Test
    fun testStringRW() {
        val map = mutableMapOf("existingKey" to "value")

        class TestClass {
            var existingKey by StringRW(map)
            var missingKey by StringRW(map) { "prefix-missingKey" }
        }

        val testClass = TestClass()
        assertEquals("value", testClass.existingKey)
        assertEquals("prefix-missingKey", testClass.missingKey)

        // Test write operations
        testClass.existingKey = "newValue"
        testClass.missingKey = "anotherValue"

        assertEquals("newValue", map["existingKey"])
        assertEquals("anotherValue", map["missingKey"])
        assertEquals("newValue", testClass.existingKey)
        assertEquals("anotherValue", testClass.missingKey)
    }

    @Test
    fun testNullableStringRW() {
        val map = mutableMapOf("existingKey" to "value")

        class TestClass {
            var existingKey by NullableStringRW(map)
            var missingKey by NullableStringRW(map)
            var missingKeyWithDefault by NullableStringRW(map) { "prefix-missingKeyWithDefault" }
        }

        val testClass = TestClass()
        assertEquals("value", testClass.existingKey)
        assertNull(testClass.missingKey)
        assertEquals("prefix-missingKeyWithDefault", testClass.missingKeyWithDefault)

        // Test write operations
        testClass.existingKey = "newValue"
        testClass.missingKey = "anotherValue"
        testClass.missingKeyWithDefault = null

        assertEquals("newValue", map["existingKey"])
        assertEquals("anotherValue", map["missingKey"])
        // Setting to null doesn't add a key
        assertNull(map["missingKeyWithDefault"])

        // Verify after write
        assertEquals("newValue", testClass.existingKey)
        assertEquals("anotherValue", testClass.missingKey)
        assertEquals("prefix-missingKeyWithDefault", testClass.missingKeyWithDefault) // Should use default
    }
}
