package dev.hirth.functools

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class PartialTest {
    
    @Test
    fun testPartialZeroArgs() {
        val func = { 42 }
        val partialFunc = partial(func)
        assertEquals(42, partialFunc())
    }
    
    @Test
    fun testPartialOneArg() {
        val add10 = { x: Int -> x + 10 }
        val partialFunc = partial(add10, 5)
        assertEquals(15, partialFunc())
    }
    
    @Test
    fun testPartialTwoArgs() {
        val add = { x: Int, y: Int -> x + y }
        val add5 = partial(add, 5)
        assertEquals(15, add5(10))
        
        val add5And10 = partial(add, 5, 10)
        assertEquals(15, add5And10())
    }
    
    @Test
    fun testPartialThreeArgs() {
        val sum3 = { x: Int, y: Int, z: Int -> x + y + z }
        val add5 = partial(sum3, 5)
        assertEquals(18, add5(10, 3))
        
        val add5And10 = partial(sum3, 5, 10)
        assertEquals(18, add5And10(3))
        
        val add5And10And3 = partial(sum3, 5, 10, 3)
        assertEquals(18, add5And10And3())
    }
    
    @Test
    fun testPartialWithStrings() {
        val concat = { a: String, b: String -> a + b }
        val hello = partial(concat, "Hello, ")
        assertEquals("Hello, World!", hello("World!"))
    }
}