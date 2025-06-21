package dev.hirth.pykt.properties

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LongTest {

    @Test
    fun testLongRO() {
        val map = mapOf("existingKey" to "9223372036854775807") // Max Long value

        class TestClass {
            val existingKey by LongRO(map)
            val missingKey by LongRO(map) { 10 }
        }

        val testClass = TestClass()
        assertEquals(9223372036854775807L, testClass.existingKey)
        assertEquals(10L, testClass.missingKey)
    }

    @Test
    fun testNullableLongRO() {
        val map = mapOf("existingKey" to "9223372036854775807")

        class TestClass {
            val existingKey by NullableLongRO(map)
            val missingKey by NullableLongRO(map)
            val missingKeyWithDefault by NullableLongRO(map) { 19L }
        }

        val testClass = TestClass()
        assertEquals(9223372036854775807L, testClass.existingKey)
        assertNull(testClass.missingKey)
        assertEquals(19L, testClass.missingKeyWithDefault)
    }

    @Test
    fun testLongRW() {
        val map = mutableMapOf("existingKey" to "9223372036854775807")

        class TestClass {
            var existingKey by LongRW(map)
            var missingKey by LongRW(map) { 10L }
        }

        val testClass = TestClass()
        assertEquals(9223372036854775807L, testClass.existingKey)
        assertEquals(10L, testClass.missingKey)

        // Test write operations
        testClass.existingKey = 42L
        testClass.missingKey = 1234567890L

        assertEquals("42", map["existingKey"])
        assertEquals("1234567890", map["missingKey"])
        assertEquals(42L, testClass.existingKey)
        assertEquals(1234567890L, testClass.missingKey)
    }

    @Test
    fun testNullableLongRW() {
        val map = mutableMapOf("existingKey" to "9223372036854775807")

        class TestClass {
            var existingKey by NullableLongRW(map)
            var missingKey by NullableLongRW(map)
            var missingKeyWithDefault by NullableLongRW(map) { 19L }
        }

        val testClass = TestClass()
        assertEquals(9223372036854775807L, testClass.existingKey)
        assertNull(testClass.missingKey)
        assertEquals(19L, testClass.missingKeyWithDefault)

        // Test write operations
        testClass.existingKey = 42L
        testClass.missingKey = 1234567890L
        testClass.missingKeyWithDefault = null

        assertEquals("42", map["existingKey"])
        assertEquals("1234567890", map["missingKey"])
        // Setting to null doesn't add a key
        assertNull(map["missingKeyWithDefault"])

        // Verify after write
        assertEquals(42L, testClass.existingKey)
        assertEquals(1234567890L, testClass.missingKey)
        assertEquals(19L, testClass.missingKeyWithDefault) // Should use default
    }
}
