package dev.hirth.pykt.properties

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BooleanTest {

    @Test
    fun testBooleanRO() {
        val map = mapOf("existingKey" to "true")

        class TestClass {
            val existingKey by BooleanRO(map)
            val missingKey by BooleanRO(map) { true }
        }

        val testClass = TestClass()
        assertEquals(true, testClass.existingKey)
        assertEquals(true, testClass.missingKey)
    }

    @Test
    fun testNullableBooleanRO() {
        val map = mapOf("existingKey" to "true")

        class TestClass {
            val existingKey by NullableBooleanRO(map)
            val missingKey by NullableBooleanRO(map)
            val missingKeyWithDefault by NullableBooleanRO(map) { true }
        }

        val testClass = TestClass()
        assertEquals(true, testClass.existingKey)
        assertNull(testClass.missingKey)
        assertEquals(true, testClass.missingKeyWithDefault)
    }

    @Test
    fun testBooleanRW() {
        val map = mutableMapOf("existingKey" to "true")

        class TestClass {
            var existingKey by BooleanRW(map)
            var missingKey by BooleanRW(map) { true }
        }

        val testClass = TestClass()
        assertEquals(true, testClass.existingKey)
        assertEquals(true, testClass.missingKey)

        // Test write operations
        testClass.existingKey = false
        testClass.missingKey = false

        assertEquals("false", map["existingKey"])
        assertEquals("false", map["missingKey"])
        assertEquals(false, testClass.existingKey)
        assertEquals(false, testClass.missingKey)
    }

    @Test
    fun testNullableBooleanRW() {
        val map = mutableMapOf("existingKey" to "true")

        class TestClass {
            var existingKey by NullableBooleanRW(map)
            var missingKey by NullableBooleanRW(map)
            var missingKeyWithDefault by NullableBooleanRW(map) { true }
        }

        val testClass = TestClass()
        assertEquals(true, testClass.existingKey)
        assertNull(testClass.missingKey)
        assertEquals(true, testClass.missingKeyWithDefault)

        // Test write operations
        testClass.existingKey = false
        testClass.missingKey = true
        testClass.missingKeyWithDefault = null

        assertEquals("false", map["existingKey"])
        assertEquals("true", map["missingKey"])
        // Setting to null doesn't add a key
        assertNull(map["missingKeyWithDefault"])

        // Verify after write
        assertEquals(false, testClass.existingKey)
        assertEquals(true, testClass.missingKey)
        assertEquals(true, testClass.missingKeyWithDefault) // Should use default
    }
}
