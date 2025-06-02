package dev.hirth.functools

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class WrapsTest {
    
    @Test
    fun testBasicWraps() {
        fun originalFunction(): String = "original"
        
        fun decoratedFunction(): String {
            return wraps(::originalFunction) {
                "decorated: ${it()}"
            }
        }
        
        assertEquals("decorated: original", decoratedFunction())
    }
    
    @Test
    fun testWrapsWithParameters() {
        fun greet(name: String): String = "Hello, $name!"
        
        fun loggedGreet(name: String): String {
            return wraps(::greet) { func ->
                val result = func(name)
                println("Called greet with: $name")
                result
            }
        }
        
        assertEquals("Hello, World!", loggedGreet("World"))
    }
    
    @Test
    fun testUpdateWrapper() {
        fun original(): String = "test"
        fun wrapper(): String = "wrapped"
        
        val updated = updateWrapper(::original, ::wrapper)
        assertEquals("wrapped", updated())
    }
    
    @Test
    fun testFunctionWrapper() {
        val wrapper = FunctionWrapper(
            function = { x: Int -> x * 2 },
            name = "doubler",
            doc = "Doubles the input",
            module = "math",
            qualname = "math.doubler"
        )
        
        assertEquals("doubler", wrapper.name)
        assertEquals("Doubles the input", wrapper.doc)
        assertEquals("math", wrapper.module)
        assertEquals("math.doubler", wrapper.qualname)
        assertEquals(10, wrapper.function(5))
    }
    
    @Test
    fun testWrapsPreservesFunction() {
        fun multiply(a: Int, b: Int): Int = a * b
        
        val decorated = wraps(::multiply) { func ->
            { a: Int, b: Int -> func(a, b) * 2 }
        }
        
        assertEquals(20, decorated(2, 5)) // (2 * 5) * 2 = 20
    }
}