package dev.hirth.functools

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CacheTest {
    
    @Test
    fun testSimpleCache() {
        var callCount = 0
        val expensiveFunction = { x: Int -> 
            callCount++
            x * x 
        }
        
        val cached = cache(expensiveFunction)
        
        assertEquals(25, cached(5))
        assertEquals(1, callCount)
        
        // Second call should use cached value
        assertEquals(25, cached(5))
        assertEquals(1, callCount)
        
        // Different argument should trigger new computation
        assertEquals(36, cached(6))
        assertEquals(2, callCount)
    }
    
    @Test
    fun testCacheInfo() {
        val cached = cache { x: Int -> x * x }
        
        val info = cached.cacheInfo()
        assertEquals(0, info.currSize)
        
        cached(5)
        val info2 = cached.cacheInfo()
        assertEquals(1, info2.currSize)
        assertNull(info2.maxSize)
    }
    
    @Test
    fun testCacheClear() {
        var callCount = 0
        val cached = cache { x: Int -> 
            callCount++
            x * x 
        }
        
        cached(5)
        assertEquals(1, callCount)
        
        cached.clearCache()
        cached(5)
        assertEquals(2, callCount) // Should recompute after clear
    }
    
    @Test
    fun testLruCache() {
        var callCount = 0
        val cached = lruCache(maxSize = 2) { x: Int -> 
            callCount++
            x * x 
        }
        
        assertEquals(25, cached(5))
        assertEquals(36, cached(6))
        assertEquals(49, cached(7))
        assertEquals(3, callCount)
        
        // 5 should have been evicted (LRU)
        assertEquals(25, cached(5))
        assertEquals(4, callCount) // Should recompute
        
        // 6 should have been evicted when 5 was added back
        assertEquals(36, cached(6))
        assertEquals(5, callCount) // Should recompute
    }
    
    @Test
    fun testLruCacheInfo() {
        val cached = lruCache(maxSize = 2) { x: Int -> x * x }
        
        var info = cached.cacheInfo()
        assertEquals(0, info.hits)
        assertEquals(0, info.misses)
        assertEquals(0, info.currSize)
        assertEquals(2, info.maxSize)
        
        cached(5)
        info = cached.cacheInfo()
        assertEquals(0, info.hits)
        assertEquals(1, info.misses)
        assertEquals(1, info.currSize)
        
        cached(5) // Should be a hit
        info = cached.cacheInfo()
        assertEquals(1, info.hits)
        assertEquals(1, info.misses)
        assertEquals(1, info.currSize)
    }
    
    @Test
    fun testCachedExtension() {
        var callCount = 0
        val original = { x: Int -> 
            callCount++
            x * x 
        }
        
        val cached = original.cached()
        assertEquals(25, cached(5))
        assertEquals(1, callCount)
        
        assertEquals(25, cached(5))
        assertEquals(1, callCount) // Should use cache
    }
    
    @Test
    fun testLruCachedExtension() {
        var callCount = 0
        val original = { x: Int -> 
            callCount++
            x * x 
        }
        
        val cached = original.lruCached(maxSize = 1)
        assertEquals(25, cached(5))
        assertEquals(1, callCount)
        
        assertEquals(25, cached(5))
        assertEquals(1, callCount) // Should use cache
        
        assertEquals(36, cached(6))
        assertEquals(2, callCount)
        
        assertEquals(25, cached(5))
        assertEquals(3, callCount) // Should recompute due to eviction
    }
}