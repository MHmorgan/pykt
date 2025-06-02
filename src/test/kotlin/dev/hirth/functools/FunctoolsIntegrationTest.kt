package dev.hirth.functools

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class FunctoolsIntegrationTest {
    
    @Test
    fun testPartialWithCache() {
        var callCount = 0
        val expensiveAdd = { x: Int, y: Int -> 
            callCount++
            x + y 
        }
        
        val add10 = partial(expensiveAdd, 10).cached()
        
        assertEquals(15, add10(5))
        assertEquals(1, callCount)
        
        assertEquals(15, add10(5)) // Should use cache
        assertEquals(1, callCount)
        
        assertEquals(20, add10(10))
        assertEquals(2, callCount)
    }
    
    @Test
    fun testReduceWithNumbers() {
        val numbers = listOf(2, 3, 4)
        val result = reduce(Int::times, numbers)
        
        assertEquals(24, result) // 2 * 3 * 4
    }
    
    @Test
    fun testSingleDispatchWithCaching() {
        var stringCallCount = 0
        var intCallCount = 0
        
        val cachedStringProcessor: (String) -> String = { str: String -> 
            stringCallCount++
            "String: $str" 
        }.cached()
        
        val cachedIntProcessor: (Int) -> String = { num: Int -> 
            intCallCount++
            "Number: $num" 
        }.cached()
        
        val processor = singledispatch<Any?, String> { "default" }
        
        processor.register<String> { str -> cachedStringProcessor(str) }
        processor.register<Int> { num -> cachedIntProcessor(num) }
        
        assertEquals("String: hello", processor("hello"))
        assertEquals("Number: 42", processor(42))
        assertEquals("String: hello", processor("hello")) // Should use cache
        
        assertEquals(1, stringCallCount) // Should be cached
        assertEquals(1, intCallCount)
    }
    
    @Test
    fun testComplexChaining() {
        // A complex example combining multiple functools features
        class Calculator {
            private var operationCount = 0
            
            val expensiveOperation by cachedProperty {
                operationCount++
                { x: Int -> x * x + 1 }
            }
            
            fun getOperationCount() = operationCount
        }
        
        val calc = Calculator()
        val square = calc.expensiveOperation
        val squarePlus10: (Int) -> Int = partial({ a: Int, b: Int -> a + b }, 10)
        
        // Chain: square, then add 10, with caching
        val cachedChain: (Int) -> Int = { x: Int -> squarePlus10(square(x)) }.cached()
        
        assertEquals(36, cachedChain(5)) // (5*5 + 1) + 10 = 36
        assertEquals(1, calc.getOperationCount())
        
        assertEquals(36, cachedChain(5)) // Should use cache
        assertEquals(1, calc.getOperationCount())
        
        assertEquals(111, cachedChain(10)) // (10*10 + 1) + 10 = 111
        assertEquals(1, calc.getOperationCount()) // expensiveOperation is cached at property level
    }
    
    @Test
    fun testFunctionalProgrammingStyle() {
        // Demonstrate functional programming patterns with functools
        val numbers = listOf(1, 2, 3, 4, 5)
        
        // Create a cached factorial function
        val factorial = { n: Int ->
            if (n <= 1) 1 else reduce(Int::times, (1..n).toList())
        }.cached()
        
        // Create a partial function for adding a specific value
        val add100: (Int) -> Int = partial({ a: Int, b: Int -> a + b }, 100)
        
        // Process numbers: factorial then add 100
        val results = numbers.map { n ->
            add100(factorial(n))
        }
        
        assertEquals(listOf(101, 102, 106, 124, 220), results)
        // factorial(1) = 1 + 100 = 101
        // factorial(2) = 2 + 100 = 102  
        // factorial(3) = 6 + 100 = 106
        // factorial(4) = 24 + 100 = 124
        // factorial(5) = 120 + 100 = 220
    }
    
    @Test
    fun testErrorHandlingWithFunctools() {
        // Test that errors are properly handled through functools utilities
        val divideByZero = { x: Int, y: Int -> x / y }
        val safeDivide: (Int) -> Int = partial(divideByZero, 10)
        
        assertEquals(5, safeDivide(2))
        
        assertThrows<ArithmeticException> {
            safeDivide(0)
        }
    }
}