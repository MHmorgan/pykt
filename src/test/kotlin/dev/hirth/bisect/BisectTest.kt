package dev.hirth.bisect

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class BisectTest {

    @Test
    fun testBisectLeft() {
        val list = listOf(1, 2, 4, 4, 5, 6)
        
        assertEquals(0, bisectLeft(list, 0))
        assertEquals(0, bisectLeft(list, 1))
        assertEquals(1, bisectLeft(list, 2))
        assertEquals(1, bisectLeft(list, 3))
        assertEquals(2, bisectLeft(list, 4))
        assertEquals(4, bisectLeft(list, 5))
        assertEquals(5, bisectLeft(list, 6))
        assertEquals(6, bisectLeft(list, 7))
    }

    @Test
    fun testBisectRight() {
        val list = listOf(1, 2, 4, 4, 5, 6)
        
        assertEquals(0, bisectRight(list, 0))
        assertEquals(1, bisectRight(list, 1))
        assertEquals(2, bisectRight(list, 2))
        assertEquals(2, bisectRight(list, 3))
        assertEquals(4, bisectRight(list, 4))
        assertEquals(5, bisectRight(list, 5))
        assertEquals(6, bisectRight(list, 6))
        assertEquals(6, bisectRight(list, 7))
    }

    @Test
    fun testBisectAlias() {
        val list = listOf(1, 2, 4, 4, 5, 6)
        
        // bisect should be an alias for bisectRight
        assertEquals(bisectRight(list, 4), bisect(list, 4))
        assertEquals(bisectRight(list, 3), bisect(list, 3))
    }

    @Test
    fun testBisectWithBounds() {
        val list = listOf(1, 2, 4, 4, 5, 6)
        
        // Test with custom lo and hi
        assertEquals(2, bisectLeft(list, 4, 1, 4))
        assertEquals(4, bisectRight(list, 4, 1, 5))
        
        // Test edge cases
        assertEquals(0, bisectLeft(list, 0, 0, 0))
        assertEquals(0, bisectRight(list, 0, 0, 0))
    }

    @Test
    fun testBisectInvalidBounds() {
        val list = listOf(1, 2, 3)
        
        assertThrows<IllegalArgumentException> {
            bisectLeft(list, 2, -1, 3)
        }
        
        assertThrows<IllegalArgumentException> {
            bisectLeft(list, 2, 0, 4)
        }
        
        assertThrows<IllegalArgumentException> {
            bisectRight(list, 2, -1, 3)
        }
        
        assertThrows<IllegalArgumentException> {
            bisectRight(list, 2, 0, 4)
        }
    }

    @Test
    fun testBisectEmptyList() {
        val list = emptyList<Int>()
        
        assertEquals(0, bisectLeft(list, 1))
        assertEquals(0, bisectRight(list, 1))
    }

    @Test
    fun testBisectSingleElement() {
        val list = listOf(5)
        
        assertEquals(0, bisectLeft(list, 3))
        assertEquals(0, bisectLeft(list, 5))
        assertEquals(1, bisectLeft(list, 7))
        
        assertEquals(0, bisectRight(list, 3))
        assertEquals(1, bisectRight(list, 5))
        assertEquals(1, bisectRight(list, 7))
    }

    @Test
    fun testInsortLeft() {
        val list = mutableListOf(1, 2, 4, 4, 5, 6)
        
        insortLeft(list, 3)
        assertEquals(listOf(1, 2, 3, 4, 4, 5, 6), list)
        
        insortLeft(list, 4)
        assertEquals(listOf(1, 2, 3, 4, 4, 4, 5, 6), list)
        
        insortLeft(list, 0)
        assertEquals(listOf(0, 1, 2, 3, 4, 4, 4, 5, 6), list)
        
        insortLeft(list, 10)
        assertEquals(listOf(0, 1, 2, 3, 4, 4, 4, 5, 6, 10), list)
    }

    @Test
    fun testInsortRight() {
        val list = mutableListOf(1, 2, 4, 4, 5, 6)
        
        insortRight(list, 3)
        assertEquals(listOf(1, 2, 3, 4, 4, 5, 6), list)
        
        insortRight(list, 4)
        assertEquals(listOf(1, 2, 3, 4, 4, 4, 5, 6), list)
        
        insortRight(list, 0)
        assertEquals(listOf(0, 1, 2, 3, 4, 4, 4, 5, 6), list)
        
        insortRight(list, 10)
        assertEquals(listOf(0, 1, 2, 3, 4, 4, 4, 5, 6, 10), list)
    }

    @Test
    fun testInsortAlias() {
        val list1 = mutableListOf(1, 2, 4, 4, 5, 6)
        val list2 = mutableListOf(1, 2, 4, 4, 5, 6)
        
        insort(list1, 3)
        insortRight(list2, 3)
        
        assertEquals(list2, list1)
    }

    @Test
    fun testInsortWithBounds() {
        val list = mutableListOf(1, 2, 4, 4, 5, 6)
        
        insortLeft(list, 3, 1, 4)
        assertEquals(listOf(1, 2, 3, 4, 4, 5, 6), list)
    }

    @Test
    fun testBisectWithCustomComparator() {
        val list = listOf("a", "bb", "ccc", "dddd")
        val comparator: (String, String) -> Int = { a, b -> a.length.compareTo(b.length) }
        
        assertEquals(0, bisectLeftWithComparator(list, "", 0, list.size, comparator))
        assertEquals(1, bisectLeftWithComparator(list, "x", 0, list.size, comparator))
        assertEquals(2, bisectLeftWithComparator(list, "xx", 0, list.size, comparator))
        assertEquals(3, bisectLeftWithComparator(list, "xxx", 0, list.size, comparator))
        assertEquals(4, bisectLeftWithComparator(list, "xxxx", 0, list.size, comparator))
        assertEquals(4, bisectLeftWithComparator(list, "xxxxx", 0, list.size, comparator))
    }

    @Test
    fun testInsortWithCustomComparator() {
        val list = mutableListOf("a", "bb", "ccc", "dddd")
        val comparator: (String, String) -> Int = { a, b -> a.length.compareTo(b.length) }
        
        insortLeft(list, "xx", 0, list.size, comparator)
        assertEquals(listOf("a", "bb", "xx", "ccc", "dddd"), list)
        
        insortRight(list, "yy", 0, list.size, comparator)
        assertEquals(listOf("a", "bb", "xx", "yy", "ccc", "dddd"), list)
    }

    @Test
    fun testBisectLeftRightDifference() {
        val list = listOf(1, 2, 2, 2, 3, 4)
        
        // For duplicate values, left and right should return different positions
        assertEquals(1, bisectLeft(list, 2))   // Insert before existing 2s
        assertEquals(4, bisectRight(list, 2))  // Insert after existing 2s
        
        // For non-existing values, they should be the same
        assertEquals(4, bisectLeft(list, 3))
        assertEquals(4, bisectRight(list, 3))
    }

    @Test
    fun testInsortMaintainsSortedOrder() {
        val list = mutableListOf<Int>()
        val values = listOf(5, 2, 8, 1, 9, 3, 7, 4, 6)
        
        for (value in values) {
            insort(list, value)
        }
        
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9), list)
    }

    @Test
    fun testLargeList() {
        val list = (0 until 1000 step 2).toList() // [0, 2, 4, 6, ...]
        
        assertEquals(500, bisectLeft(list, 999))
        assertEquals(500, bisectRight(list, 999))
        assertEquals(250, bisectLeft(list, 500))
        assertEquals(251, bisectRight(list, 500))
    }

    @Test
    fun testStringComparison() {
        val list = listOf("apple", "banana", "cherry", "date")
        
        assertEquals(0, bisectLeft(list, "aardvark"))
        assertEquals(1, bisectLeft(list, "apple"))
        assertEquals(2, bisectLeft(list, "cherry"))
        assertEquals(4, bisectLeft(list, "zebra"))
        
        assertEquals(0, bisectRight(list, "aardvark"))
        assertEquals(1, bisectRight(list, "apple"))
        assertEquals(3, bisectRight(list, "cherry"))
        assertEquals(4, bisectRight(list, "zebra"))
    }

    @Test
    fun testNegativeNumbers() {
        val list = listOf(-10, -5, -2, 0, 3, 7)
        
        assertEquals(0, bisectLeft(list, -15))
        assertEquals(3, bisectLeft(list, -1))
        assertEquals(4, bisectLeft(list, 0))
        assertEquals(6, bisectLeft(list, 10))
    }
}