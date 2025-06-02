package dev.hirth.functools

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class ReduceTest {
    
    @Test
    fun testReduceIterable() {
        val numbers = listOf(1, 2, 3, 4, 5)
        val sum = reduce(Int::plus, numbers)
        assertEquals(15, sum)
        
        val product = reduce(Int::times, numbers)
        assertEquals(120, product)
    }
    
    @Test
    fun testReduceWithInitialValue() {
        val numbers = listOf(1, 2, 3, 4, 5)
        val sum = reduce(Int::plus, numbers, 10)
        assertEquals(25, sum)
        
        val product = reduce(Int::times, numbers, 2)
        assertEquals(240, product)
    }
    
    @Test
    fun testReduceEmptyWithoutInitial() {
        val empty = emptyList<Int>()
        assertThrows<IllegalArgumentException> {
            reduce(Int::plus, empty)
        }
    }
    
    @Test
    fun testReduceEmptyWithInitial() {
        val empty = emptyList<Int>()
        val result = reduce(Int::plus, empty, 42)
        assertEquals(42, result)
    }
    
    @Test
    fun testReduceSingleElement() {
        val single = listOf(42)
        val result = reduce(Int::plus, single)
        assertEquals(42, result)
    }
    
    @Test
    fun testReduceSequence() {
        val numbers = sequenceOf(1, 2, 3, 4, 5)
        val sum = reduce(Int::plus, numbers)
        assertEquals(15, sum)
        
        val sumWithInitial = reduce(Int::plus, numbers, 10)
        assertEquals(25, sumWithInitial)
    }
    
    @Test
    fun testReduceArray() {
        val numbers = arrayOf(1, 2, 3, 4, 5)
        val sum = reduce(Int::plus, numbers)
        assertEquals(15, sum)
        
        val sumWithInitial = reduce(Int::plus, numbers, 10)
        assertEquals(25, sumWithInitial)
    }
    
    @Test
    fun testReduceStrings() {
        val words = listOf("Hello", " ", "World", "!")
        val sentence = reduce(String::plus, words)
        assertEquals("Hello World!", sentence)
        
        val prefixed = reduce(String::plus, words, "Say: ")
        assertEquals("Say: Hello World!", prefixed)
    }
    
    @Test
    fun testReduceDifferentTypes() {
        val numbers = listOf(1, 2, 3, 4, 5)
        val concatenated = reduce({ acc: String, n: Int -> acc + n.toString() }, numbers, "")
        assertEquals("12345", concatenated)
    }
}