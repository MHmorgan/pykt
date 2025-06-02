package dev.hirth.bisect

/**
 * Array bisection algorithms for maintaining sorted lists.
 * 
 * This module provides functions for finding insertion points in sorted lists
 * and maintaining sorted order when inserting elements.
 */

/**
 * Locate the insertion point for x in a sorted list to maintain sorted order.
 * The return value is such that all elements in list[0..i] have elem < x,
 * and all elements in list[i..] have elem >= x.
 * 
 * @param list The sorted list to search
 * @param x The value to locate
 * @param lo Lower bound (inclusive)
 * @param hi Upper bound (exclusive)
 * @param comparator Custom comparator function
 * @return The insertion point
 */
fun <T : Comparable<T>> bisectLeft(
    list: List<T>, 
    x: T, 
    lo: Int = 0, 
    hi: Int = list.size
): Int {
    return bisectLeftWithComparator(list, x, lo, hi) { a, b -> a.compareTo(b) }
}

/**
 * Locate the insertion point for x in a sorted list using a custom comparator.
 */
fun <T> bisectLeftWithComparator(
    list: List<T>, 
    x: T, 
    lo: Int = 0, 
    hi: Int = list.size,
    comparator: (T, T) -> Int
): Int {
    if (lo < 0) throw IllegalArgumentException("lo must be non-negative")
    if (hi > list.size) throw IllegalArgumentException("hi must not exceed list size")
    
    var left = lo
    var right = hi
    
    while (left < right) {
        val mid = (left + right) / 2
        if (comparator(list[mid], x) < 0) {
            left = mid + 1
        } else {
            right = mid
        }
    }
    
    return left
}

fun <T> bisectLeft(
    list: List<T>, 
    x: T, 
    lo: Int = 0, 
    hi: Int = list.size,
    comparator: (T, T) -> Int
): Int = bisectLeftWithComparator(list, x, lo, hi, comparator)

/**
 * Locate the insertion point for x in a sorted list to maintain sorted order.
 * The return value is such that all elements in list[0..i] have elem <= x,
 * and all elements in list[i..] have elem > x.
 * 
 * @param list The sorted list to search
 * @param x The value to locate
 * @param lo Lower bound (inclusive)
 * @param hi Upper bound (exclusive)
 * @return The insertion point
 */
fun <T : Comparable<T>> bisectRight(
    list: List<T>, 
    x: T, 
    lo: Int = 0, 
    hi: Int = list.size
): Int {
    return bisectRightWithComparator(list, x, lo, hi) { a, b -> a.compareTo(b) }
}

/**
 * Locate the insertion point for x in a sorted list using a custom comparator.
 */
fun <T> bisectRightWithComparator(
    list: List<T>, 
    x: T, 
    lo: Int = 0, 
    hi: Int = list.size,
    comparator: (T, T) -> Int
): Int {
    if (lo < 0) throw IllegalArgumentException("lo must be non-negative")
    if (hi > list.size) throw IllegalArgumentException("hi must not exceed list size")
    
    var left = lo
    var right = hi
    
    while (left < right) {
        val mid = (left + right) / 2
        if (comparator(x, list[mid]) < 0) {
            right = mid
        } else {
            left = mid + 1
        }
    }
    
    return left
}

fun <T> bisectRight(
    list: List<T>, 
    x: T, 
    lo: Int = 0, 
    hi: Int = list.size,
    comparator: (T, T) -> Int
): Int = bisectRightWithComparator(list, x, lo, hi, comparator)

/**
 * Alias for bisectRight (following Python convention).
 */
fun <T : Comparable<T>> bisect(
    list: List<T>, 
    x: T, 
    lo: Int = 0, 
    hi: Int = list.size
): Int = bisectRight(list, x, lo, hi)

/**
 * Insert x in list in sorted order.
 * This is equivalent to list.add(bisectLeft(list, x), x) assuming list is sorted.
 * 
 * @param list The sorted mutable list
 * @param x The value to insert
 * @param lo Lower bound (inclusive)
 * @param hi Upper bound (exclusive)
 */
fun <T : Comparable<T>> insortLeft(
    list: MutableList<T>, 
    x: T, 
    lo: Int = 0, 
    hi: Int = list.size
) {
    val index = bisectLeft(list, x, lo, hi)
    list.add(index, x)
}

fun <T> insortLeft(
    list: MutableList<T>, 
    x: T, 
    lo: Int = 0, 
    hi: Int = list.size,
    comparator: (T, T) -> Int
) {
    val index = bisectLeftWithComparator(list, x, lo, hi, comparator)
    list.add(index, x)
}

/**
 * Insert x in list in sorted order.
 * This is equivalent to list.add(bisectRight(list, x), x) assuming list is sorted.
 * 
 * @param list The sorted mutable list
 * @param x The value to insert
 * @param lo Lower bound (inclusive)
 * @param hi Upper bound (exclusive)
 */
fun <T : Comparable<T>> insortRight(
    list: MutableList<T>, 
    x: T, 
    lo: Int = 0, 
    hi: Int = list.size
) {
    val index = bisectRight(list, x, lo, hi)
    list.add(index, x)
}

fun <T> insortRight(
    list: MutableList<T>, 
    x: T, 
    lo: Int = 0, 
    hi: Int = list.size,
    comparator: (T, T) -> Int
) {
    val index = bisectRightWithComparator(list, x, lo, hi, comparator)
    list.add(index, x)
}

/**
 * Alias for insortRight (following Python convention).
 */
fun <T : Comparable<T>> insort(
    list: MutableList<T>, 
    x: T, 
    lo: Int = 0, 
    hi: Int = list.size
) = insortRight(list, x, lo, hi)

