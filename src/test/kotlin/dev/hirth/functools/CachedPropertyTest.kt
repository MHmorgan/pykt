package dev.hirth.functools

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CachedPropertyTest {
    
    class TestClass {
        private var computeCount = 0
        
        val expensiveProperty by cachedProperty { 
            computeCount++
            "computed-$computeCount"
        }
        
        val threadSafeProperty by cachedPropertyThreadSafe {
            computeCount++
            "threadsafe-$computeCount"
        }
        
        val unsafeProperty by cachedPropertyUnsafe {
            computeCount++
            "unsafe-$computeCount"
        }
        
        fun getComputeCount() = computeCount
    }
    
    @Test
    fun testCachedProperty() {
        val obj = TestClass()
        
        assertEquals("computed-1", obj.expensiveProperty)
        assertEquals(1, obj.getComputeCount())
        
        // Second access should return cached value
        assertEquals("computed-1", obj.expensiveProperty)
        assertEquals(1, obj.getComputeCount())
    }
    
    @Test
    fun testThreadSafeCachedProperty() {
        class TestThreadSafeClass {
            private var computeCount = 0
            
            val threadSafeProperty by cachedPropertyThreadSafe {
                computeCount++
                "threadsafe-$computeCount"
            }
            
            fun getComputeCount() = computeCount
        }
        
        val obj = TestThreadSafeClass()
        
        assertEquals("threadsafe-1", obj.threadSafeProperty)
        assertEquals(1, obj.getComputeCount())
        
        // Second access should return cached value
        assertEquals("threadsafe-1", obj.threadSafeProperty)
        assertEquals(1, obj.getComputeCount())
    }
    
    @Test
    fun testUnsafeCachedProperty() {
        class TestUnsafeClass {
            private var computeCount = 0
            
            val unsafeProperty by cachedPropertyUnsafe {
                computeCount++
                "unsafe-$computeCount"
            }
            
            fun getComputeCount() = computeCount
        }
        
        val obj = TestUnsafeClass()
        
        assertEquals("unsafe-1", obj.unsafeProperty)
        assertEquals(1, obj.getComputeCount())
        
        // Second access should return cached value
        assertEquals("unsafe-1", obj.unsafeProperty)
        assertEquals(1, obj.getComputeCount())
    }
    
    @Test
    fun testCachedPropertyClearCache() {
        class TestClassWithClearable {
            private var computeCount = 0
            private val delegate = cachedProperty { 
                computeCount++
                "computed-$computeCount"
            }
            
            val property by delegate
            
            fun clearCache() = delegate.clearCache()
            fun isCached() = delegate.isCached()
            fun getComputeCount() = computeCount
        }
        
        val obj = TestClassWithClearable()
        
        assertFalse(obj.isCached())
        
        assertEquals("computed-1", obj.property)
        assertEquals(1, obj.getComputeCount())
        assertTrue(obj.isCached())
        
        obj.clearCache()
        assertFalse(obj.isCached())
        
        assertEquals("computed-2", obj.property)
        assertEquals(2, obj.getComputeCount())
        assertTrue(obj.isCached())
    }
    
    @Test
    fun testMultipleInstances() {
        val obj1 = TestClass()
        val obj2 = TestClass()
        
        assertEquals("computed-1", obj1.expensiveProperty)
        assertEquals("computed-1", obj2.expensiveProperty)
        
        // Each instance should have its own cache
        assertEquals(1, obj1.getComputeCount())
        assertEquals(1, obj2.getComputeCount())
    }
}