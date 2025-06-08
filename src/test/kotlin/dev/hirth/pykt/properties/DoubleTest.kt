package dev.hirth.pykt.properties

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DoubleTest {

    @Test
    fun testDoubleRO() {
        val map = mapOf("existingKey" to "42.5")

        class TestClass {
            val existingKey by DoubleRO(map)
            val missingKey by DoubleRO(map) { it.length.toDouble() }
        }

        val testClass = TestClass()
        assertEquals(42.5, testClass.existingKey)
        assertEquals(10.0, testClass.missingKey) // "missingKey".length = 10
    }

    @Test
    fun testNullableDoubleRO() {
        val map = mapOf("existingKey" to "42.5")

        class TestClass {
            val existingKey by NullableDoubleRO(map)
            val missingKey by NullableDoubleRO(map)
            val missingKeyWithDefault by NullableDoubleRO(map) { 19.0 }
        }

        val testClass = TestClass()
        assertEquals(42.5, testClass.existingKey)
        assertNull(testClass.missingKey)
        assertEquals(19.0, testClass.missingKeyWithDefault) // "missingKeyWithDefault".length = 19
    }

    @Test
    fun testDoubleRW() {
        val map = mutableMapOf("existingKey" to "42.5")

        class TestClass {
            var existingKey by DoubleRW(map)
            var missingKey by DoubleRW(map) { it.length.toDouble() }
        }

        val testClass = TestClass()
        assertEquals(42.5, testClass.existingKey)
        assertEquals(10.0, testClass.missingKey)

        // Test write operations
        testClass.existingKey = 99.9
        testClass.missingKey = 77.7

        assertEquals("99.9", map["existingKey"])
        assertEquals("77.7", map["missingKey"])
        assertEquals(99.9, testClass.existingKey)
        assertEquals(77.7, testClass.missingKey)
    }

    @Test
    fun testNullableDoubleRW() {
        val map = mutableMapOf("existingKey" to "42.5")

        class TestClass {
            var existingKey by NullableDoubleRW(map)
            var missingKey by NullableDoubleRW(map)
            var missingKeyWithDefault by NullableDoubleRW(map) { 19.0 }
        }

        val testClass = TestClass()
        assertEquals(42.5, testClass.existingKey)
        assertNull(testClass.missingKey)
        assertEquals(19.0, testClass.missingKeyWithDefault)

        // Test write operations
        testClass.existingKey = 99.9
        testClass.missingKey = 77.7
        testClass.missingKeyWithDefault = null

        assertEquals("99.9", map["existingKey"])
        assertEquals("77.7", map["missingKey"])
        // Setting to null doesn't add a key
        assertNull(map["missingKeyWithDefault"])

        // Verify after write
        assertEquals(99.9, testClass.existingKey)
        assertEquals(77.7, testClass.missingKey)
        assertEquals(19.0, testClass.missingKeyWithDefault) // Should use default
    }
}
