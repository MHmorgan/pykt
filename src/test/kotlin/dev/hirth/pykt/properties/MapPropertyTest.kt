package dev.hirth.pykt.properties

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MapPropertyTest {

    @Test
    fun testMapPropertyRO() {
        val map = mapOf("existingKey" to "42")

        class TestClass {
            val existingKey by MapPropertyRO(map) { it.toInt() }
            val missingKey by MapPropertyRO(map, 100) { it.toInt() }
        }

        val testClass = TestClass()
        assertEquals(42, testClass.existingKey)
        assertEquals(100, testClass.missingKey)
    }

    @Test
    fun testNullableMapPropertyRO() {
        val map = mapOf("existingKey" to "42")

        class TestClass {
            val existingKey by NullableMapPropertyRO(map) { it.toInt() }
            val missingKey by NullableMapPropertyRO(map) { it.toInt() }
            val missingKeyWithDefault by NullableMapPropertyRO(map, 100) { it.toInt() }
        }

        val testClass = TestClass()
        assertEquals(42, testClass.existingKey)
        assertNull(testClass.missingKey)
        assertEquals(100, testClass.missingKeyWithDefault)
    }

    @Test
    fun testMapPropertyRW() {
        val map = mutableMapOf("existingKey" to "42")

        class TestClass {
            var existingKey by MapPropertyRW(map) { it.toInt() }
            var missingKey by MapPropertyRW(map, 100) { it.toInt() }
        }

        val testClass = TestClass()
        assertEquals(42, testClass.existingKey)
        assertEquals(100, testClass.missingKey)

        // Test write operations
        testClass.existingKey = 84
        testClass.missingKey = 200

        assertEquals("84", map["existingKey"])
        assertEquals("200", map["missingKey"])
        assertEquals(84, testClass.existingKey)
        assertEquals(200, testClass.missingKey)
    }

    @Test
    fun testNullableMapPropertyRW() {
        val map = mutableMapOf("existingKey" to "42")

        class TestClass {
            var existingKey by NullableMapPropertyRW(map) { it.toInt() }
            var missingKey by NullableMapPropertyRW(map) { it.toInt() }
            var missingKeyWithDefault by NullableMapPropertyRW(map, 100) { it.toInt() }
        }

        val testClass = TestClass()
        assertEquals(42, testClass.existingKey)
        assertNull(testClass.missingKey)
        assertEquals(100, testClass.missingKeyWithDefault)

        // Test write operations
        testClass.existingKey = 84
        testClass.missingKey = 200
        testClass.missingKeyWithDefault = null

        assertEquals("84", map["existingKey"])
        assertEquals("200", map["missingKey"])
        // Setting to null doesn't add a key
        assertNull(map["missingKeyWithDefault"])

        // Verify after write
        assertEquals(84, testClass.existingKey)
        assertEquals(200, testClass.missingKey)
        assertEquals(100, testClass.missingKeyWithDefault) // Should use default
    }
}