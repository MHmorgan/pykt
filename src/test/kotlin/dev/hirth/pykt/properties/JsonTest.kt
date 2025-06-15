package dev.hirth.pykt.properties

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JsonTest {

    @Serializable
    data class Person(val name: String, val age: Int)

    @Test
    fun testJsonRO() {
        val person = Person("John", 30)
        val json = Json { ignoreUnknownKeys = true }
        val map = mapOf("existingKey" to json.encodeToString(Person.serializer(), person))

        class TestClass {
            val existingKey by JsonRO(map, Person::class, json)
            val missingKey by JsonRO(map, Person::class, json) { Person("Default for $it", 0) }
        }

        val testClass = TestClass()
        assertEquals(person, testClass.existingKey)
        assertEquals(Person("Default for missingKey", 0), testClass.missingKey)
    }

    @Test
    fun testNullableJsonRO() {
        val person = Person("John", 30)
        val json = Json { ignoreUnknownKeys = true }
        val map = mapOf("existingKey" to json.encodeToString(Person.serializer(), person))

        class TestClass {
            val existingKey by NullableJsonRO(map, Person::class, json)
            val missingKey by NullableJsonRO(map, Person::class, json)
            val missingKeyWithDefault by NullableJsonRO(map, Person::class, json) { Person("Default for $it", 0) }
        }

        val testClass = TestClass()
        assertEquals(person, testClass.existingKey)
        assertNull(testClass.missingKey)
        assertEquals(Person("Default for missingKeyWithDefault", 0), testClass.missingKeyWithDefault)
    }

    @Test
    fun testJsonRW() {
        val person = Person("John", 30)
        val json = Json { ignoreUnknownKeys = true }
        val map = mutableMapOf("existingKey" to json.encodeToString(Person.serializer(), person))

        class TestClass {
            var existingKey by JsonRW(map, Person::class, json)
            var missingKey by JsonRW(map, Person::class, json) { Person("Default for $it", 0) }
        }

        val testClass = TestClass()
        assertEquals(person, testClass.existingKey)
        assertEquals(Person("Default for missingKey", 0), testClass.missingKey)

        // Test write operations
        val newPerson = Person("Jane", 25)
        testClass.existingKey = newPerson
        testClass.missingKey = Person("Bob", 40)

        assertEquals(json.encodeToString(Person.serializer(), newPerson), map["existingKey"])
        assertEquals(json.encodeToString(Person.serializer(), Person("Bob", 40)), map["missingKey"])
    }

    @Test
    fun testNullableJsonRW() {
        val person = Person("John", 30)
        val json = Json { ignoreUnknownKeys = true }
        val map = mutableMapOf("existingKey" to json.encodeToString(Person.serializer(), person))

        class TestClass {
            var existingKey by NullableJsonRW(map, Person::class, json)
            var missingKey by NullableJsonRW(map, Person::class, json)
            var missingKeyWithDefault by NullableJsonRW(map, Person::class, json) { Person("Default for $it", 0) }
        }

        val testClass = TestClass()
        assertEquals(person, testClass.existingKey)
        assertNull(testClass.missingKey)
        assertEquals(Person("Default for missingKeyWithDefault", 0), testClass.missingKeyWithDefault)

        // Test write operations
        val newPerson = Person("Jane", 25)
        testClass.existingKey = newPerson
        testClass.missingKey = Person("Bob", 40)
        testClass.missingKeyWithDefault = null

        assertEquals(json.encodeToString(Person.serializer(), newPerson), map["existingKey"])
        assertEquals(json.encodeToString(Person.serializer(), Person("Bob", 40)), map["missingKey"])
        // Setting to null doesn't add a key
        assertNull(map["missingKeyWithDefault"])

        // Verify after write
        assertEquals(newPerson, testClass.existingKey)
        assertEquals(Person("Bob", 40), testClass.missingKey)
        assertEquals(Person("Default for missingKeyWithDefault", 0), testClass.missingKeyWithDefault) // Should use default
    }
}
