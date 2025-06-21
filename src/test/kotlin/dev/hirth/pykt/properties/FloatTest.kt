package dev.hirth.pykt.properties

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FloatTest {

    @Test
    fun testFloatRO() {
        val map = mapOf("existingKey" to "42.5")

        class TestClass {
            val existingKey by FloatRO(map)
            val missingKey by FloatRO(map) { 10.0f }
        }

        val testClass = TestClass()
        assertEquals(42.5f, testClass.existingKey)
        assertEquals(10.0f, testClass.missingKey)
    }

    @Test
    fun testNullableFloatRO() {
        val map = mapOf("existingKey" to "42.5")

        class TestClass {
            val existingKey by NullableFloatRO(map)
            val missingKey by NullableFloatRO(map)
            val missingKeyWithDefault by NullableFloatRO(map) { 19.0f }
        }

        val testClass = TestClass()
        assertEquals(42.5f, testClass.existingKey)
        assertNull(testClass.missingKey)
        assertEquals(19.0f, testClass.missingKeyWithDefault)
    }

    @Test
    fun testFloatRW() {
        val map = mutableMapOf("existingKey" to "42.5")

        class TestClass {
            var existingKey by FloatRW(map)
            var missingKey by FloatRW(map) { 10.0f }
        }

        val testClass = TestClass()
        assertEquals(42.5f, testClass.existingKey)
        assertEquals(10.0f, testClass.missingKey)

        // Test write operations
        testClass.existingKey = 99.9f
        testClass.missingKey = 77.7f

        assertEquals("99.9", map["existingKey"])
        assertEquals("77.7", map["missingKey"])
        assertEquals(99.9f, testClass.existingKey)
        assertEquals(77.7f, testClass.missingKey)
    }

    @Test
    fun testNullableFloatRW() {
        val map = mutableMapOf("existingKey" to "42.5")

        class TestClass {
            var existingKey by NullableFloatRW(map)
            var missingKey by NullableFloatRW(map)
            var missingKeyWithDefault by NullableFloatRW(map) { 19.0f }
        }

        val testClass = TestClass()
        assertEquals(42.5f, testClass.existingKey)
        assertNull(testClass.missingKey)
        assertEquals(19.0f, testClass.missingKeyWithDefault)

        // Test write operations
        testClass.existingKey = 99.9f
        testClass.missingKey = 77.7f
        testClass.missingKeyWithDefault = null

        assertEquals("99.9", map["existingKey"])
        assertEquals("77.7", map["missingKey"])
        // Setting to null doesn't add a key
        assertNull(map["missingKeyWithDefault"])

        // Verify after write
        assertEquals(99.9f, testClass.existingKey)
        assertEquals(77.7f, testClass.missingKey)
        assertEquals(19.0f, testClass.missingKeyWithDefault) // Should use default
    }
}
