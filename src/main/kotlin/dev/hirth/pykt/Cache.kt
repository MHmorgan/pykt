package dev.hirth.pykt

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * A simple cache wrapper for functions.
 */
class Cache<K, V>(private val function: (K) -> V) : (K) -> V {
    private val cache = ConcurrentHashMap<K, V>()

    private var hits = 0
    private var misses = 0

    override fun invoke(key: K): V {
        return when (val cached = cache[key]) {
            null -> {
                misses++
                function(key).also {
                    cache[key] = it
                }
            }

            else -> {
                hits++
                cached
            }
        }
    }

    /**
     * Evicts a specific key from the cache.
     */
    fun evict(key: K): V? = cache.remove(key)

    /**
     * Pre-warms the cache with multiple keys in parallel.
     */
    fun prewarm(keys: Iterable<K>) {
        for (key in keys) cache[key] = function(key)
    }

    /**
     * Clears the cache.
     */
    fun clearCache() {
        cache.clear()
        misses = 0
        hits = 0
    }

    /**
     * Returns a map of the current cache contents.
     */
    fun asMap(): Map<K, V> = cache.toMap()

    /**
     * Returns the value corresponding to the given [key], if present.
     */
    operator fun get(key: K): V? = cache[key]

    /**
     * Puts a key/value pair into the cache, or updates the existing value.
     */
    operator fun set(key: K, value: V) {
        cache[key] = value
    }

    /**
     * Returns cache information.
     */
    fun cacheInfo(): CacheInfo {
        return CacheInfo(
            hits = hits,
            misses = misses,
            maxSize = null,
            currSize = cache.size
        )
    }
}

/**
 * LRU Cache implementation with size limits.
 *
 * This cache uses `@Synchronized` to avoid race conditions.
 * Because of this, it is not suited for use with coroutines.
 *
 * See [LruCacheAsync] for use with coroutines.
 */
class LruCacheSync<K, V>(
    private val maxSize: Int = 128,
    private val function: (K) -> V,
) : (K) -> V {

    private val cache = object : LinkedHashMap<K, V>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
            return size > maxSize
        }
    }

    /**
     * Map of the active computations.
     * This is used to avoid multiple computations of the same key in parallel.
     */
    private val computationLocks = ConcurrentHashMap<K, Any>()

    private val lock = ReentrantReadWriteLock()

    private val hits = AtomicInteger(0)
    private var misses = AtomicInteger(0)

    @Synchronized
    override fun invoke(key: K): V {
        lock.read {
            cache[key]?.let {
                hits.incrementAndGet()
                return it
            }
        }

        val keyLock = computationLocks.computeIfAbsent(key) { Any() }

        synchronized(keyLock) {
            // Double-check with read lock
            lock.read {
                cache[key]?.let {
                    hits.incrementAndGet()
                    return it
                }
            }
            misses.incrementAndGet()

            val value = function(key)
            lock.write { cache[key] = value }
            computationLocks.remove(key)

            return value
        }
    }

    /**
     * Evicts a specific key from the cache.
     */
    fun evict(key: K): V? = lock.write {
        cache.remove(key)
    }

    /**
     * Pre-warms the cache with multiple keys in parallel.
     */
    fun prewarm(keys: Iterable<K>) = lock.write {
        keys.forEach { cache[it] = function(it) }
    }

    /**
     * Clears the cache.
     */
    @Synchronized
    fun clearCache() {
        cache.clear()
        hits.set(0)
        misses.set(0)
    }

    /**
     * Returns a map of the current cache contents.
     */
    fun asMap(): Map<K, V> = lock.read {
        cache.toMap()
    }

    /**
     * Returns the value corresponding to the given [key], if present.
     */
    operator fun get(key: K): V? = lock.read {
        cache[key]
    }

    /**
     * Puts a key/value pair into the cache, or updates the existing value.
     */
    operator fun set(key: K, value: V) = lock.write {
        cache[key] = value
    }

    /**
     * Returns cache information.
     */
    @Synchronized
    fun cacheInfo(): CacheInfo {
        return CacheInfo(
            hits = hits.get(),
            misses = misses.get(),
            maxSize = maxSize,
            currSize = cache.size
        )
    }
}

/**
 * LRU Cache implementation with size limits.
 *
 * This cache is designed to be used with coroutines.
 */
class LruCacheAsync<K, V>(
    private val maxSize: Int = 128,
    private val function: suspend (K) -> V,
) : suspend (K) -> V {

    private val cache = object : LinkedHashMap<K, V>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
            return size > maxSize
        }
    }

    /**
     * Map of the active computations.
     * This is used to avoid multiple computations of the same key in parallel.
     */
    private val computations = ConcurrentHashMap<K, Deferred<V>>()

    private var hits = AtomicInteger(0)
    private var misses = AtomicInteger(0)

    private val mutex = Mutex()

    override suspend fun invoke(key: K): V = coroutineScope {
        mutex.withLock { cache[key] }?.let {
            hits.incrementAndGet()
            return@coroutineScope it
        }

        val computation = computations.computeIfAbsent(key) {
            async {
                misses.incrementAndGet()
                try {
                    val value = function(key)
                    mutex.withLock { cache[key] = value }
                    return@async value
                } finally {
                    computations.remove(key)
                }
            }
        }

        try {
            computation.await()
        } catch (e: Exception) {
            computations.remove(key)
            throw e
        }
    }

    /**
     * Evicts a specific key from the cache.
     */
    suspend fun evict(key: K): V? {
        computations.remove(key)?.cancel()
        return mutex.withLock { cache.remove(key) }
    }

    /**
     * Pre-warms the cache with multiple keys in parallel.
     */
    suspend fun prewarm(keys: Iterable<K>) = coroutineScope {
        keys.map {
            async { invoke(it) }
        }.awaitAll()
    }

    /**
     * Clears the cache.
     */
    suspend fun clearCache() = mutex.withLock {
        computations.values.forEach { it.cancel() }
        computations.clear()

        cache.clear()
        hits.set(0)
        misses.set(0)
    }

    /**
     * Returns a map of the current cache contents.
     */
    suspend fun asMap(): Map<K, V> = mutex.withLock {
        cache.toMap()
    }

    /**
     * Returns the value corresponding to the given [key], if present.
     */
    suspend fun get(key: K): V? = mutex.withLock {
        cache[key]
    }

    /**
     * Puts a key/value pair into the cache, or updates the existing value.
     */
    suspend fun set(key: K, value: V): V? = mutex.withLock {
        cache.put(key, value)
    }

    /**
     * Returns cache information.
     */
    fun cacheInfo(): CacheInfo {
        return CacheInfo(
            hits = hits.get(),
            misses = misses.get(),
            maxSize = maxSize,
            currSize = cache.size
        )
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
) {
    val hitRate: Double
        get() = when (val sum = hits + misses) {
            0 -> 0.0
            else -> hits / sum.toDouble()
        }
}

/**
 * Creates a simple cache wrapper for a function.
 */
fun <K, V> cache(function: (K) -> V) = Cache(function)

/**
 * Creates an LRU cache wrapper for a function.
 */
fun <K, V> lruCacheSync(maxSize: Int = 128, function: (K) -> V) =
    LruCacheSync(maxSize, function)

/**
 * Creates an LRU cache wrapper for a function.
 */
fun <K, V> lruCacheAsync(maxSize: Int = 128, function: suspend (K) -> V) =
    LruCacheAsync(maxSize, function)

/**
 * Extension function to add caching to any function.
 */
fun <K, V> ((K) -> V).cached(): Cache<K, V> = cache(this)

/**
 * Extension function to add LRU caching to any function.
 */
fun <K, V> ((K) -> V).lruCachedSync(maxSize: Int = 128) =
    lruCacheSync(maxSize, this)

/**
 * Extension function to add LRU caching to any function.
 */
fun <K, V> (suspend (K) -> V).lruCachedAsync(maxSize: Int = 128) =
    lruCacheAsync(maxSize, this)
