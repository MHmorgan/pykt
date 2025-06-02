package dev.hirth.functools

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class SingleDispatchTest {
    
    @Test
    fun testBasicSingleDispatch() {
        val process = singledispatch<Any?, String> { obj ->
            "Unknown type: ${obj?.let { it::class.simpleName }}"
        }
        
        process.register<String> { str -> "String: $str" }
        process.register<Int> { num -> "Number: $num" }
        
        assertEquals("String: hello", process("hello"))
        assertEquals("Number: 42", process(42))
        assertEquals("Unknown type: Double", process(3.14))
    }
    
    @Test
    fun testSingleDispatchWithNullDefault() {
        val process = singledispatch<Any?, String> { "null value" }
        
        process.register<String> { str -> "String: $str" }
        
        assertEquals("String: test", process("test"))
        assertEquals("null value", process(null))
    }
    
    @Test
    fun testSingleDispatchRegistry() {
        val process = singledispatch<Any?, String> { "default" }
        
        assertTrue(process.getRegistry().isEmpty())
        
        process.register<String> { str -> "String: $str" }
        process.register<Int> { num -> "Number: $num" }
        
        assertEquals(2, process.getRegistry().size)
        assertTrue(process.getRegistry().containsKey(String::class))
        assertTrue(process.getRegistry().containsKey(Int::class))
    }
    
    @Test
    fun testSingleDispatchClearRegistry() {
        val process = singledispatch<Any?, String> { "default" }
        
        process.register<String> { str -> "String: $str" }
        process.register<Int> { num -> "Number: $num" }
        assertEquals(2, process.getRegistry().size)
        
        process.clearRegistry()
        assertTrue(process.getRegistry().isEmpty())
        
        // Should fall back to default
        assertEquals("default", process("test"))
        assertEquals("default", process(42))
    }
    
    @Test
    fun testSingleDispatchWithInheritance() {
        open class Animal(val name: String)
        class Dog(name: String) : Animal(name)
        class Cat(name: String) : Animal(name)
        
        val process = singledispatch<Animal, String> { animal ->
            "Unknown animal: ${animal.name}"
        }
        
        process.register<Dog> { dog -> "Dog: ${dog.name}" }
        process.register<Cat> { cat -> "Cat: ${cat.name}" }
        
        assertEquals("Dog: Buddy", process(Dog("Buddy")))
        assertEquals("Cat: Whiskers", process(Cat("Whiskers")))
        
        // Base class should use default
        assertEquals("Unknown animal: Generic", process(Animal("Generic")))
    }
    
    @Test
    fun testSingleDispatchOverrideRegistration() {
        val process = singledispatch<Any?, String> { "default" }
        
        process.register<String> { str -> "First: $str" }
        assertEquals("First: test", process("test"))
        
        // Override the registration
        process.register<String> { str -> "Second: $str" }
        assertEquals("Second: test", process("test"))
    }
    
    @Test
    fun testSingleDispatchWithNotImplemented() {
        val process = singledispatch<Any?, String>()
        
        process.register<String> { str -> "String: $str" }
        
        assertEquals("String: test", process("test"))
        
        assertThrows<NotImplementedError> {
            process(42)
        }
    }
    
    @Test
    fun testSingleDispatchWithComplexTypes() {
        val process = singledispatch<Any?, String> { "default" }
        
        val list = listOf(1, 2, 3)
        val map = mapOf("a" to 1, "b" to 2)
        val array = arrayOf(1, 2, 3, 4)
        
        // Register with actual runtime types
        process.register(list::class) { list -> "List with ${(list as List<*>).size} elements" }
        process.register(map::class) { map -> "Map with ${(map as Map<*, *>).size} entries" }
        process.register(array::class) { array -> "Array with ${(array as Array<*>).size} elements" }
        
        assertEquals("List with 3 elements", process(list))
        assertEquals("Map with 2 entries", process(map))
        assertEquals("Array with 4 elements", process(array))
    }
}