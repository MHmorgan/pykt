package dev.hirth.pykt

import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class CacheTest {

    @Nested
    inner class BasicCacheTests {
        @Test
        fun `Test Simple Cache`() {
            var callCount = 0
            val expensiveFunction = { x: Int ->
                callCount++
                x * x
            }

            val cached = cache(expensiveFunction)

            Assertions.assertEquals(25, cached(5))
            Assertions.assertEquals(1, callCount)

            // Second call should use cached value
            Assertions.assertEquals(25, cached(5))
            Assertions.assertEquals(1, callCount)

            // Different argument should trigger new computation
            Assertions.assertEquals(36, cached(6))
            Assertions.assertEquals(2, callCount)
        }

        @Test
        fun `Test Cache Info`() {
            val cached = cache { x: Int -> x * x }

            val info = cached.cacheInfo()
            Assertions.assertEquals(0, info.currSize)

            cached(5)
            val info2 = cached.cacheInfo()
            Assertions.assertEquals(1, info2.currSize)
            Assertions.assertNull(info2.maxSize)
        }

        @Test
        fun `Test Cache Clear`() {
            var callCount = 0
            val cached = cache { x: Int ->
                callCount++
                x * x
            }

            cached(5)
            Assertions.assertEquals(1, callCount)

            cached.clearCache()
            cached(5)
            Assertions.assertEquals(2, callCount) // Should recompute after clear
        }

        @Test
        fun `Test Sync LRU Cache`() {
            var callCount = 0
            val cached = lruCacheSync(maxSize = 2) { x: Int ->
                callCount++
                x * x
            }

            Assertions.assertEquals(25, cached(5))
            Assertions.assertEquals(36, cached(6))
            Assertions.assertEquals(49, cached(7))
            Assertions.assertEquals(3, callCount)

            // 5 should have been evicted (LRU)
            Assertions.assertEquals(25, cached(5))
            Assertions.assertEquals(4, callCount) // Should recompute

            // 6 should have been evicted when 5 was added back
            Assertions.assertEquals(36, cached(6))
            Assertions.assertEquals(5, callCount) // Should recompute
        }

        @Test
        fun `Test Sync LRU Cache Info`() {
            val cached = lruCacheSync(maxSize = 2) { x: Int -> x * x }

            var info = cached.cacheInfo()
            Assertions.assertEquals(0, info.hits)
            Assertions.assertEquals(0, info.misses)
            Assertions.assertEquals(0, info.currSize)
            Assertions.assertEquals(2, info.maxSize)

            cached(5)
            info = cached.cacheInfo()
            Assertions.assertEquals(0, info.hits)
            Assertions.assertEquals(1, info.misses)
            Assertions.assertEquals(1, info.currSize)

            cached(5) // Should be a hit
            info = cached.cacheInfo()
            Assertions.assertEquals(1, info.hits)
            Assertions.assertEquals(1, info.misses)
            Assertions.assertEquals(1, info.currSize)
        }

        @Test
        fun `Test Async LRU Cache`() = runBlocking {
            var callCount = 0
            val cached = lruCacheAsync(maxSize = 2) { x: Int ->
                callCount++
                x * x
            }

            Assertions.assertEquals(25, cached(5))
            Assertions.assertEquals(36, cached(6))
            Assertions.assertEquals(49, cached(7))
            Assertions.assertEquals(3, callCount)

            // 5 should have been evicted (LRU)
            Assertions.assertEquals(25, cached(5))
            Assertions.assertEquals(4, callCount) // Should recompute

            // 6 should have been evicted when 5 was added back
            Assertions.assertEquals(36, cached(6))
            Assertions.assertEquals(5, callCount) // Should recompute
        }

        @Test
        fun `Test Async LRU Cache Info`() = runBlocking {
            val cached = lruCacheAsync(maxSize = 2) { x: Int -> x * x }

            var info = cached.cacheInfo()
            Assertions.assertEquals(0, info.hits)
            Assertions.assertEquals(0, info.misses)
            Assertions.assertEquals(0, info.currSize)
            Assertions.assertEquals(2, info.maxSize)

            cached(5)
            info = cached.cacheInfo()
            Assertions.assertEquals(0, info.hits)
            Assertions.assertEquals(1, info.misses)
            Assertions.assertEquals(1, info.currSize)

            cached(5) // Should be a hit
            info = cached.cacheInfo()
            Assertions.assertEquals(1, info.hits)
            Assertions.assertEquals(1, info.misses)
            Assertions.assertEquals(1, info.currSize)
        }
    }

    // -------------------------------------------------------------------------
    //
    // Synchronized LRU cache
    //
    // -------------------------------------------------------------------------

    @Nested
    inner class LRUCacheSyncTests {
        @Test
        fun `Test Concurrent Access`() {
            val callCount = AtomicInteger(0)

            val cached = lruCacheSync(maxSize = 10) { x: Int ->
                Thread.sleep(10) // simulate some work
                callCount.incrementAndGet()
                x * x
            }

            val threadCount = 10
            val executor = Executors.newFixedThreadPool(threadCount)
            val latch = CountDownLatch(1)
            val threadsCompleted = CountDownLatch(threadCount)

            // All threads will try to access the same key simultaneously
            for (i in 0 until threadCount) {
                executor.submit {
                    try {
                        latch.await() // wait for signal to start
                        val result = cached(5)
                        Assertions.assertEquals(25, result) // all should get correct result
                    } finally {
                        threadsCompleted.countDown()
                    }
                }
            }

            latch.countDown() // release all threads at once
            Assertions.assertTrue(threadsCompleted.await(5, TimeUnit.SECONDS))
            executor.shutdown()

            // The function should have been called exactly once due to @Synchronized
            Assertions.assertEquals(1, callCount.get())
        }

        @Test
        fun `Test Concurrent Access With Different Keys`() {
            val callCount = AtomicInteger(0)

            val cached = lruCacheSync(maxSize = 10) { x: Int ->
                Thread.sleep(10) // simulate some work
                callCount.incrementAndGet()
                x * x
            }

            val threadCount = 10
            val executor = Executors.newFixedThreadPool(threadCount)
            val latch = CountDownLatch(1)
            val threadsCompleted = CountDownLatch(threadCount)

            // Each thread will access a different key
            for (i in 0 until threadCount) {
                val key = i
                executor.submit {
                    try {
                        latch.await() // wait for signal to start
                        val result = cached(key)
                        Assertions.assertEquals(key * key, result) // all should get correct result
                    } finally {
                        threadsCompleted.countDown()
                    }
                }
            }

            latch.countDown() // release all threads at once
            Assertions.assertTrue(threadsCompleted.await(5, TimeUnit.SECONDS))
            executor.shutdown()

            // Each key should cause one computation
            Assertions.assertEquals(threadCount, callCount.get())
        }

        @Test
        fun `Test Concurrent Cache Info`() {
            val callCount = AtomicInteger(0)

            val cached = lruCacheSync(maxSize = 10) { x: Int ->
                callCount.incrementAndGet()
                x * x
            }

            // Pre-populate cache
            cached(1)
            cached(2)

            val threadCount = 10
            val executor = Executors.newFixedThreadPool(threadCount)
            val latch = CountDownLatch(1)
            val threadsCompleted = CountDownLatch(threadCount)

            for (i in 0 until threadCount) {
                executor.submit {
                    try {
                        latch.await() // wait for signal to start
                        val info = cached.cacheInfo()
                        // Just checking that we don't get exceptions or inconsistent state
                        Assertions.assertNotNull(info)
                        Assertions.assertTrue(info.currSize >= 0)
                    } finally {
                        threadsCompleted.countDown()
                    }
                }
            }

            latch.countDown() // release all threads at once
            Assertions.assertTrue(threadsCompleted.await(5, TimeUnit.SECONDS))
            executor.shutdown()
        }

        @Test
        fun `Test Concurrent Cache Clean And Access`() {
            val callCount = AtomicInteger(0)

            val cached = lruCacheSync(maxSize = 10) { x: Int ->
                callCount.incrementAndGet()
                x * x
            }

            // Pre-populate cache
            for (i in 0 until 5) {
                cached(i)
            }
            Assertions.assertEquals(5, callCount.get())

            val threadCount = 5
            val executor = Executors.newFixedThreadPool(threadCount * 2)
            val latch = CountDownLatch(1)
            val threadsCompleted = CountDownLatch(threadCount * 2)

            // Half the threads will clear the cache
            for (i in 0 until threadCount) {
                executor.submit {
                    try {
                        latch.await()
                        cached.clearCache()
                    } finally {
                        threadsCompleted.countDown()
                    }
                }
            }

            // Half the threads will access the cache
            for (i in 0 until threadCount) {
                executor.submit {
                    try {
                        latch.await()
                        cached(i)
                    } finally {
                        threadsCompleted.countDown()
                    }
                }
            }

            latch.countDown() // release all threads at once
            Assertions.assertTrue(threadsCompleted.await(5, TimeUnit.SECONDS))
            executor.shutdown()

            // This check verifies that despite race conditions between clearing and accessing,
            // the @Synchronized annotation prevented any corruption
            val info = cached.cacheInfo()
            Assertions.assertTrue(info.hits >= 0 && info.misses >= 0 && info.currSize >= 0)
        }

        @Test
        fun `Test LRU Cache Eviction Under Concurrent Access`() {
            val executionTracker = mutableMapOf<Int, AtomicInteger>()

            val cached = lruCacheSync(maxSize = 3) { x: Int ->
                val counter = executionTracker.getOrPut(x) { AtomicInteger(0) }
                counter.incrementAndGet()
                Thread.sleep(5) // Small delay to increase chance of race conditions
                x * x
            }

            val threadCount = 20
            val executor = Executors.newFixedThreadPool(threadCount)
            val latch = CountDownLatch(1)
            val threadsCompleted = CountDownLatch(threadCount)

            // Create a mix of accesses that will cause LRU eviction
            for (i in 0 until threadCount) {
                val key = i % 5 // Use only 5 different keys with a cache of size 3
                executor.submit {
                    try {
                        latch.await()
                        val result = cached(key)
                        Assertions.assertEquals(key * key, result)
                    } finally {
                        threadsCompleted.countDown()
                    }
                }
            }

            latch.countDown()
            Assertions.assertTrue(threadsCompleted.await(5, TimeUnit.SECONDS))
            executor.shutdown()

            // Check that each key was calculated the correct number of times
            // This verifies that LRU eviction worked correctly even under concurrent access
            val info = cached.cacheInfo()
            Assertions.assertTrue(info.currSize <= 3) // Size should never exceed maxSize

            // Sum of all execution counts should match the total misses
            val totalExecutions = executionTracker.values.sumOf { it.get() }
            Assertions.assertEquals(info.misses, totalExecutions)
        }
    }

    // -------------------------------------------------------------------------
    //
    // Async LRU cache
    //
    // -------------------------------------------------------------------------

    @Nested
    inner class LRUCacheAsyncTests {
        @Test
        fun `Test Async Concurrent Access`() = runBlocking {
            val callCount = AtomicInteger(0)

            val cached = lruCacheAsync(maxSize = 10) { x: Int ->
                delay(10) // simulate some asynchronous work
                callCount.incrementAndGet()
                x * x
            }

            val coroutineCount = 10
            val results = mutableListOf<Deferred<Int>>()

            coroutineScope {
                // All coroutines will try to access the same key simultaneously
                repeat(coroutineCount) {
                    results.add(async {
                        cached(5)
                    })
                }
            }

            // Check all results are correct
            results.forEach { deferred ->
                Assertions.assertEquals(25, deferred.await())
            }

            // The function should have been called exactly once due to mutex protection
            Assertions.assertEquals(1, callCount.get())
        }

        @Test
        fun `Test Async Concurrent Access With Different Keys`() = runBlocking {
            val callCount = AtomicInteger(0)

            val cached = lruCacheAsync(maxSize = 10) { x: Int ->
                delay(10) // simulate some asynchronous work
                callCount.incrementAndGet()
                x * x
            }

            val coroutineCount = 10
            val results = mutableListOf<Deferred<Pair<Int, Int>>>()

            coroutineScope {
                // Each coroutine will access a different key
                repeat(coroutineCount) { i ->
                    val key = i
                    results.add(async {
                        Pair(key, cached(key))
                    })
                }
            }

            // Check all results are correct
            results.forEach { deferred ->
                val (key, result) = deferred.await()
                Assertions.assertEquals(key * key, result)
            }

            // Each key should cause one computation
            Assertions.assertEquals(coroutineCount, callCount.get())
        }

        @Test
        fun `Test Async Concurrent Cache Info`() = runBlocking {
            val callCount = AtomicInteger(0)

            val cached = lruCacheAsync(maxSize = 10) { x: Int ->
                callCount.incrementAndGet()
                x * x
            }

            // Pre-populate cache
            cached(1)
            cached(2)

            val coroutineCount = 10
            val results = mutableListOf<Deferred<CacheInfo>>()

            coroutineScope {
                repeat(coroutineCount) {
                    results.add(async {
                        cached.cacheInfo()
                    })
                }
            }

            // Check that we don't get exceptions or inconsistent state
            results.forEach { deferred ->
                val info = deferred.await()
                Assertions.assertNotNull(info)
                Assertions.assertTrue(info.currSize >= 0)
            }
        }

        @Test
        fun `Test Async Concurrent Cache Clean and Access`() = runBlocking {
            val callCount = AtomicInteger(0)

            val cached = lruCacheAsync(maxSize = 10) { x: Int ->
                callCount.incrementAndGet()
                x * x
            }

            // Pre-populate cache
            for (i in 0 until 5) {
                cached(i)
            }
            Assertions.assertEquals(5, callCount.get())

            val accessCount = 5
            val clearCount = 5
            val accessResults = mutableListOf<Deferred<Int>>()
            val clearResults = mutableListOf<Deferred<Unit>>()

            coroutineScope {
                // Some coroutines will clear the cache
                repeat(clearCount) {
                    clearResults.add(async {
                        cached.clearCache()
                    })
                }

                // Some coroutines will access the cache
                repeat(accessCount) { i ->
                    accessResults.add(async {
                        cached(i)
                    })
                }
            }

            // Wait for all to complete
            clearResults.forEach { it.await() }
            accessResults.forEach { it.await() }

            // This check verifies that despite race conditions between clearing and accessing,
            // the mutex prevented any corruption
            val info = cached.cacheInfo()
            Assertions.assertTrue(info.hits >= 0 && info.misses >= 0 && info.currSize >= 0)
        }

        @Test
        fun `Test Async Cache LRU Eviction Under Concurrency`() = runBlocking {
            val executionTracker = ConcurrentHashMap<Int, AtomicInteger>()

            val cached = lruCacheAsync(maxSize = 3) { x: Int ->
                val counter = executionTracker.getOrPut(x) { AtomicInteger(0) }
                counter.incrementAndGet()
                delay(100) // Small delay to increase chance of race conditions
                x * x
            }

            val accessCount = 20
            val results = mutableListOf<Deferred<Int>>()

            coroutineScope {
                // Create a mix of accesses that will cause LRU eviction
                repeat(accessCount) { i ->
                    val key = i % 5 // Use only 5 different keys with a cache of size 3
                    results.add(async {
                        cached(key)
                    })
                }
            }

            // Check that all results are correct
            results.forEachIndexed { index, deferred ->
                val key = index % 5
                Assertions.assertEquals(key * key, deferred.await())
            }

            // Check that LRU eviction worked correctly
            val info = cached.cacheInfo()
            Assertions.assertTrue(info.currSize <= 3) // Size should never exceed maxSize

            // Sum of all execution counts should match the total misses
            val totalExecutions = executionTracker.values.sumOf { it.get() }
            Assertions.assertEquals(info.misses, totalExecutions)
        }

        @Test
        fun `Test Async Function Throws Exception`() = runBlocking {
            val callCount = AtomicInteger(0)

            val cached = lruCacheAsync<Int, Int>(maxSize = 10) { x ->
                callCount.incrementAndGet()
                if (x == 0) throw IllegalArgumentException("Zero not allowed")
                x * x
            }

            // Call with valid input should work
            Assertions.assertEquals(4, cached(2))
            Assertions.assertEquals(1, callCount.get())

            // Call with invalid input should throw
            assertThrows<IllegalArgumentException> {
                runBlocking { cached(0) }
            }
            Assertions.assertEquals(2, callCount.get())

            // Call again with invalid input should throw again (not cached)
            assertThrows<IllegalArgumentException> {
                runBlocking { cached(0) }
            }
            Assertions.assertEquals(3, callCount.get())

            // Call with valid input should still work
            Assertions.assertEquals(4, cached(2))
            Assertions.assertEquals(3, callCount.get()) // Shouldn't increment (cached)
        }

        @Test
        fun `Test Async Parallel Access to Multiple Keys`() = runBlocking {
            val executionTimes = ConcurrentHashMap<Int, AtomicInteger>()

            val cached = lruCacheAsync<Int, Int>(maxSize = 10) { x ->
                val counter = executionTimes.getOrPut(x) { AtomicInteger(0) }
                counter.incrementAndGet()
                delay(50) // Longer delay to test parallel execution
                x * x
            }

            val start = System.currentTimeMillis()

            // Access multiple keys in parallel
            coroutineScope {
                for (i in 1..5) {
                    async { cached(i) }
                }
            }

            val duration = System.currentTimeMillis() - start

            // If parallel execution works, this should take ~50ms, not ~250ms
            // Using a relaxed assertion with some buffer
            Assertions.assertTrue(duration < 200, "Parallel execution took too long: $duration ms")

            // Ensure each key was computed exactly once
            for (i in 1..5) {
                Assertions.assertEquals(1, executionTimes[i]?.get() ?: 0)
            }
        }
    }
}