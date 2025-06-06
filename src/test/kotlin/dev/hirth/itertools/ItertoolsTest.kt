package dev.hirth.itertools

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class ItertoolsTest {

    @Test
    fun testCount() {
        val result = count().take(5).toList()
        assertEquals(listOf(0L, 1L, 2L, 3L, 4L), result)
    }

    @Test
    fun testCountWithStart() {
        val result = count(10).take(3).toList()
        assertEquals(listOf(10L, 11L, 12L), result)
    }

    @Test
    fun testCountWithStep() {
        val result = count(0, 3).take(4).toList()
        assertEquals(listOf(0L, 3L, 6L, 9L), result)
    }

    @Test
    fun testCountNegativeStep() {
        val result = count(10, -2).take(4).toList()
        assertEquals(listOf(10L, 8L, 6L, 4L), result)
    }

    @Test
    fun testCycle() {
        val result = listOf(1, 2, 3).cycle().take(7).toList()
        assertEquals(listOf(1, 2, 3, 1, 2, 3, 1), result)
    }

    @Test
    fun testCycleEmpty() {
        val result = emptyList<Int>().cycle().take(5).toList()
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun testCycleSingle() {
        val result = listOf("a").cycle().take(3).toList()
        assertEquals(listOf("a", "a", "a"), result)
    }

    @Test
    fun testRepeat() {
        val result = "hello".repeat().take(3).toList()
        assertEquals(listOf("hello", "hello", "hello"), result)
    }

    @Test
    fun testRepeatWithTimes() {
        val result = 42.repeat(4).toList()
        assertEquals(listOf(42, 42, 42, 42), result)
    }

    @Test
    fun testRepeatZeroTimes() {
        val result = "test".repeat(0).toList()
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun testChain() {
        val result = chain(listOf(1, 2), listOf(3, 4), listOf(5)).toList()
        assertEquals(listOf(1, 2, 3, 4, 5), result)
    }

    @Test
    fun testChainEmpty() {
        val result = chain(emptyList<Int>(), listOf(1, 2), emptyList<Int>()).toList()
        assertEquals(listOf(1, 2), result)
    }

    @Test
    fun testChainSingle() {
        val result = chain(listOf(1, 2, 3)).toList()
        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun testCompress() {
        val data = listOf("a", "b", "c", "d", "e")
        val selectors = listOf(true, false, true, false, true)
        val result = compress(data, selectors).toList()
        assertEquals(listOf("a", "c", "e"), result)
    }

    @Test
    fun testCompressAllFalse() {
        val result = compress(listOf(1, 2, 3), listOf(false, false, false)).toList()
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun testCompressAllTrue() {
        val result = compress(listOf(1, 2, 3), listOf(true, true, true)).toList()
        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun testCompressMismatchedLength() {
        val result = compress(listOf(1, 2, 3, 4), listOf(true, false)).toList()
        assertEquals(listOf(1), result)
    }

    @Test
    fun testIslice() {
        val data = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
        val result = islice(data, 2, 7, 2).toList()
        assertEquals(listOf(2, 4, 6), result)
    }

    @Test
    fun testIsliceFromStart() {
        val data = listOf("a", "b", "c", "d", "e")
        val result = islice(data, stop = 3).toList()
        assertEquals(listOf("a", "b", "c"), result)
    }

    @Test
    fun testIsliceWithStep() {
        val data = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
        val result = islice(data, 1, 8, 2).toList()
        assertEquals(listOf(1, 3, 5, 7), result)
    }

    @Test
    fun testIsliceInvalidStep() {
        assertThrows<IllegalArgumentException> {
            islice(listOf(1, 2, 3), 0, 3, 0).toList()
        }
    }

    @Test
    fun testTee() {
        val data = listOf(1, 2, 3, 4)
        val (iter1, iter2) = data.tee()
        
        assertEquals(listOf(1, 2, 3, 4), iter1.toList())
        assertEquals(listOf(1, 2, 3, 4), iter2.toList())
    }

    @Test
    fun testTeeThree() {
        val data = listOf("a", "b", "c")
        val iterators = data.tee(3)
        
        assertEquals(3, iterators.size)
        for (iterator in iterators) {
            assertEquals(listOf("a", "b", "c"), iterator.toList())
        }
    }

    @Test
    fun testTeeZero() {
        val iterators = listOf(1, 2, 3).tee(0)
        assertEquals(0, iterators.size)
    }

    @Test
    fun testTeeNegative() {
        assertThrows<IllegalArgumentException> {
            listOf(1, 2, 3).tee(-1)
        }
    }

    @Test
    fun testZipLongest() {
        val iter1 = listOf(1, 2, 3)
        val iter2 = listOf("a", "b")
        val iter3 = listOf(true, false, true, false)
        
        val result = zipLongest(iter1, iter2, iter3, fillvalue = null).toList()
        
        assertEquals(4, result.size)
        assertEquals(listOf(1, "a", true), result[0])
        assertEquals(listOf(2, "b", false), result[1])
        assertEquals(listOf(3, null, true), result[2])
        assertEquals(listOf(null, null, false), result[3])
    }

    @Test
    fun testZipLongestSameLength() {
        val result = zipLongest(listOf(1, 2), listOf("a", "b"), fillvalue = null).toList()
        
        assertEquals(2, result.size)
        assertEquals(listOf(1, "a"), result[0])
        assertEquals(listOf(2, "b"), result[1])
    }

    @Test
    fun testZipLongestEmpty() {
        val result = zipLongest(emptyList<Int>(), emptyList<String>(), fillvalue = null).toList()
        assertEquals(emptyList<List<Any?>>(), result)
    }

    @Test
    fun testChainExtension() {
        val result = listOf(1, 2).chain(listOf(3, 4), listOf(5)).toList()
        assertEquals(listOf(1, 2, 3, 4, 5), result)
    }

    @Test
    fun testCombinedIterators() {
        val evens = count(0, 2).take(3)  // [0, 2, 4]
        val odds = count(1, 2).take(3)   // [1, 3, 5]
        
        val result = chain(evens.asIterable(), odds.asIterable()).toList()
        assertEquals(listOf(0L, 2L, 4L, 1L, 3L, 5L), result)
    }
}