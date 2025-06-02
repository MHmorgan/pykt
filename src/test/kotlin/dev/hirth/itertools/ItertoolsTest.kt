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
        val result = cycle(listOf(1, 2, 3)).take(7).toList()
        assertEquals(listOf(1, 2, 3, 1, 2, 3, 1), result)
    }

    @Test
    fun testCycleEmpty() {
        val result = cycle(emptyList<Int>()).take(5).toList()
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun testCycleSingle() {
        val result = cycle(listOf("a")).take(3).toList()
        assertEquals(listOf("a", "a", "a"), result)
    }

    @Test
    fun testRepeat() {
        val result = repeat("hello").take(3).toList()
        assertEquals(listOf("hello", "hello", "hello"), result)
    }

    @Test
    fun testRepeatWithTimes() {
        val result = repeat(42, 4).toList()
        assertEquals(listOf(42, 42, 42, 42), result)
    }

    @Test
    fun testRepeatZeroTimes() {
        val result = repeat("test", 0).toList()
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun testAccumulateInt() {
        val result = accumulate(listOf(1, 2, 3, 4)) { a, b -> a + b }.toList()
        assertEquals(listOf(1, 3, 6, 10), result)
    }

    @Test
    fun testAccumulateIntWithInitial() {
        val result = accumulate(listOf(1, 2, 3), { a, b -> a + b }, 10).toList()
        assertEquals(listOf(10, 11, 13, 16), result)
    }

    @Test
    fun testAccumulateLong() {
        val result = accumulate(listOf(1L, 2L, 3L)).toList()
        assertEquals(listOf(1L, 3L, 6L), result)
    }

    @Test
    fun testAccumulateDouble() {
        val result = accumulate(listOf(1.0, 2.5, 1.5)).toList()
        assertEquals(listOf(1.0, 3.5, 5.0), result)
    }

    @Test
    fun testAccumulateEmpty() {
        val result = accumulate(emptyList<Int>()).toList()
        assertEquals(emptyList<Int>(), result)
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
    fun testDropwhile() {
        val result = dropwhile(listOf(1, 3, 5, 6, 7, 8)) { it % 2 == 1 }.toList()
        assertEquals(listOf(6, 7, 8), result)
    }

    @Test
    fun testDropwhileNoneDropped() {
        val result = dropwhile(listOf(2, 4, 6)) { it % 2 == 1 }.toList()
        assertEquals(listOf(2, 4, 6), result)
    }

    @Test
    fun testDropwhileAllDropped() {
        val result = dropwhile(listOf(1, 3, 5)) { it % 2 == 1 }.toList()
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun testTakewhile() {
        val result = takewhile(listOf(1, 3, 5, 6, 7, 8)) { it % 2 == 1 }.toList()
        assertEquals(listOf(1, 3, 5), result)
    }

    @Test
    fun testTakewhileNoneTaken() {
        val result = takewhile(listOf(2, 4, 6)) { it % 2 == 1 }.toList()
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun testTakewhileAllTaken() {
        val result = takewhile(listOf(1, 3, 5)) { it % 2 == 1 }.toList()
        assertEquals(listOf(1, 3, 5), result)
    }

    @Test
    fun testFilterfalse() {
        val result = filterfalse(listOf(1, 2, 3, 4, 5)) { it % 2 == 0 }.toList()
        assertEquals(listOf(1, 3, 5), result)
    }

    @Test
    fun testFilterfalseAllFiltered() {
        val result = filterfalse(listOf(1, 3, 5)) { it % 2 == 1 }.toList()
        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun testFilterfalseNoneFiltered() {
        val result = filterfalse(listOf(2, 4, 6)) { it % 2 == 1 }.toList()
        assertEquals(listOf(2, 4, 6), result)
    }

    @Test
    fun testGroupby() {
        val data = listOf("a", "a", "b", "b", "b", "c", "a")
        val result = groupby(data).toList()
        
        assertEquals(4, result.size)
        assertEquals(Group("a", listOf("a", "a")), result[0])
        assertEquals(Group("b", listOf("b", "b", "b")), result[1])
        assertEquals(Group("c", listOf("c")), result[2])
        assertEquals(Group("a", listOf("a")), result[3])
    }

    @Test
    fun testGroupbyWithKeySelector() {
        val data = listOf(1, 1, 2, 2, 2, 3, 1)
        val result = groupby(data) { it % 2 }.toList()
        
        assertEquals(4, result.size)
        assertEquals(Group(1, listOf(1, 1)), result[0])
        assertEquals(Group(0, listOf(2, 2, 2)), result[1])
        assertEquals(Group(1, listOf(3)), result[2])
        assertEquals(Group(1, listOf(1)), result[3])
    }

    @Test
    fun testGroupbyEmpty() {
        val result = groupby(emptyList<String>()).toList()
        assertEquals(emptyList<Group<String, String>>(), result)
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
    fun testPairwise() {
        val result = pairwise(listOf("a", "b", "c", "d")).toList()
        assertEquals(
            listOf(
                Pair("a", "b"),
                Pair("b", "c"),
                Pair("c", "d")
            ), 
            result
        )
    }

    @Test
    fun testPairwiseEmpty() {
        val result = pairwise(emptyList<Int>()).toList()
        assertEquals(emptyList<Pair<Int, Int>>(), result)
    }

    @Test
    fun testPairwiseSingle() {
        val result = pairwise(listOf(1)).toList()
        assertEquals(emptyList<Pair<Int, Int>>(), result)
    }

    @Test
    fun testStarmapWithLists() {
        val data = listOf(listOf(2, 3), listOf(4, 5))
        val result = starmap(data) { args -> args[0] * args[1] }.toList()
        assertEquals(listOf(6, 20), result)
    }

    @Test
    fun testStarmapWithPairs() {
        val data = listOf(Pair(2, 3), Pair(4, 5))
        val result = starmap(data) { a, b -> a * b }.toList()
        assertEquals(listOf(6, 20), result)
    }

    @Test
    fun testTee() {
        val data = listOf(1, 2, 3, 4)
        val (iter1, iter2) = tee(data)
        
        assertEquals(listOf(1, 2, 3, 4), iter1.toList())
        assertEquals(listOf(1, 2, 3, 4), iter2.toList())
    }

    @Test
    fun testTeeThree() {
        val data = listOf("a", "b", "c")
        val iterators = tee(data, 3)
        
        assertEquals(3, iterators.size)
        for (iterator in iterators) {
            assertEquals(listOf("a", "b", "c"), iterator.toList())
        }
    }

    @Test
    fun testTeeZero() {
        val iterators = tee(listOf(1, 2, 3), 0)
        assertEquals(0, iterators.size)
    }

    @Test
    fun testTeeNegative() {
        assertThrows<IllegalArgumentException> {
            tee(listOf(1, 2, 3), -1)
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
        val result = zipLongest(listOf(1, 2), listOf("a", "b")).toList()
        
        assertEquals(2, result.size)
        assertEquals(listOf(1, "a"), result[0])
        assertEquals(listOf(2, "b"), result[1])
    }

    @Test
    fun testZipLongestEmpty() {
        val result = zipLongest(emptyList<Int>(), emptyList<String>()).toList()
        assertEquals(emptyList<List<Any?>>(), result)
    }

    @Test
    fun testChainExtension() {
        val result = listOf(1, 2).chain(listOf(3, 4), listOf(5)).toList()
        assertEquals(listOf(1, 2, 3, 4, 5), result)
    }

    // Integration tests
    @Test
    fun testComplexPipeline() {
        // count(1) -> take(10) -> filter even -> accumulate -> dropwhile < 10
        val result = count(1)
            .take(10)
            .filter { it % 2 == 0L }
            .map { it.toInt() }
            .let { accumulate(it.asIterable()) }
            .let { dropwhile(it.asIterable()) { it < 10 } }
            .toList()
        
        assertEquals(listOf(10, 16), result)
    }

    @Test
    fun testCombinedIterators() {
        val evens = count(0, 2).take(3)  // [0, 2, 4]
        val odds = count(1, 2).take(3)   // [1, 3, 5]
        
        val result = chain(evens.asIterable(), odds.asIterable()).toList()
        assertEquals(listOf(0L, 2L, 4L, 1L, 3L, 5L), result)
    }
}