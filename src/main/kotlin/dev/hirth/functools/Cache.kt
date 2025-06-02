package dev.hirth.functools

import java.util.concurrent.ConcurrentHashMap

/**
 * A simple cache wrapper for functions.
 */
class Cache<K, V>(private val function: (K) -> V) : (K) -> V {
    private val cache = ConcurrentHashMap<K, V>()
    
    override fun invoke(key: K): V {
        return cache.computeIfAbsent(key, function)
    }
    
    /**
     * Clears the cache.
     */
    fun clearCache() {
        cache.clear()
    }
    
    /**
     * Returns cache information.
     */
    fun cacheInfo(): CacheInfo {
        return CacheInfo(hits = 0, misses = 0, maxSize = null, currSize = cache.size)
    }
}

/**
 * LRU Cache implementation with size limits.
 */
class LruCache<K, V>(
    private val function: (K) -> V,
    private val maxSize: Int = 128
) : (K) -> V {
    
    private val cache = object : LinkedHashMap<K, V>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
            return size > maxSize
        }
    }
    
    private var hits = 0
    private var misses = 0
    
    @Synchronized
    override fun invoke(key: K): V {
        val cached = cache[key]
        return if (cached != null) {
            hits++
            cached
        } else {
            misses++
            val result = function(key)
            cache[key] = result
            result
        }
    }
    
    /**
     * Clears the cache.
     */
    @Synchronized
    fun clearCache() {
        cache.clear()
        hits = 0
        misses = 0
    }
    
    /**
     * Returns cache information.
     */
    @Synchronized
    fun cacheInfo(): CacheInfo {
        return CacheInfo(hits = hits, misses = misses, maxSize = maxSize, currSize = cache.size)
    }
}

/**
 * Cache information data class.
 */
data class CacheInfo(
    val hits: Int,
    val misses: Int,
    val maxSize: Int?,
    val currSize: Int
)

/**
 * Creates a simple cache wrapper for a function.
 */
fun <K, V> cache(function: (K) -> V): Cache<K, V> {
    return Cache(function)
}

/**
 * Creates an LRU cache wrapper for a function.
 */
fun <K, V> lruCache(maxSize: Int = 128, function: (K) -> V): LruCache<K, V> {
    return LruCache(function, maxSize)
}

/**
 * Extension function to add caching to any function.
 */
fun <K, V> ((K) -> V).cached(): Cache<K, V> = cache(this)

/**
 * Extension function to add LRU caching to any function.
 */
fun <K, V> ((K) -> V).lruCached(maxSize: Int = 128): LruCache<K, V> = lruCache(maxSize, this)