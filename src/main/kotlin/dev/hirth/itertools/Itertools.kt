package dev.hirth.itertools

/**
 * Kotlin implementation of Python's itertools module.
 *
 * This module provides various iterator building blocks, each returning a lazy Sequence
 * that can be used with standard Kotlin collection operations.
 */

/**
 * Returns an infinite arithmetic progression starting from start with step increment.
 *
 * @param start Starting value (default 0)
 * @param step Step increment (default 1)
 * @return Infinite sequence of numbers
 */
fun count(start: Long = 0, step: Long = 1): Sequence<Long> = sequence {
    var cur = start
    while (true) {
        yield(cur)
        cur += step
    }
}

/**
 * Returns an infinite sequence cycling through the elements of this iterable.
 *
 * @receiver Input iterable to cycle through
 * @return Infinite sequence cycling through elements
 */
fun <T> Iterable<T>.cycle(): Sequence<T> = sequence {
    val items = toList()
    if (items.isEmpty()) return@sequence

    while (true) yieldAll(items)
}

/**
 * Returns a sequence that repeats the receiver either infinitely or a specified number of times.
 *
 * @receiver Object to repeat
 * @param times Number of times to repeat (null for infinite)
 * @return Sequence repeating the receiver
 */
fun <T> T.repeat(times: Int? = null): Sequence<T> = sequence {
    if (times == null) while (true) {
        yield(this@repeat)
    } else repeat(times) {
        yield(this@repeat)
    }
}

/**
 * Flattens multiple iterables into a single sequence.
 *
 * @param iterables Variable number of iterables to chain
 * @return Flattened sequence
 */
fun <T> chain(vararg iterables: Iterable<T>): Sequence<T> = sequence {
    for (iter in iterables) yieldAll(iter)
}

/**
 * Filters elements from data based on corresponding selectors.
 *
 * @param data Data iterable
 * @param selectors Boolean selectors
 * @return Sequence of selected elements
 */
fun <T> compress(data: Iterable<T>, selectors: Iterable<Boolean>): Sequence<T> = sequence {
    val dataIter = data.iterator()
    val selectorIter = selectors.iterator()

    while (dataIter.hasNext() && selectorIter.hasNext()) {
        val item = dataIter.next()
        val select = selectorIter.next()
        if (select) {
            yield(item)
        }
    }
}

/**
 * Returns a slice of the iterable from start to stop with given step.
 *
 * @param iterable Input iterable
 * @param start Start index (default 0)
 * @param stop Stop index (exclusive)
 * @param step Step size (default 1)
 * @return Sliced sequence
 */
fun <T> islice(iterable: Iterable<T>, start: Int = 0, stop: Int, step: Int = 1): Sequence<T> = sequence {
    require(step > 0) { "Step must be positive" }

    val iterator = iterable.iterator()
    var index = 0

    // Skip to start
    while (index < start && iterator.hasNext()) {
        iterator.next()
        index++
    }

    // Yield elements from start to stop with step
    while (index < stop && iterator.hasNext()) {
        yield(iterator.next())
        index++

        // Skip step-1 elements
        repeat(step - 1) {
            if (iterator.hasNext()) {
                iterator.next()
                index++
            }
        }
    }
}

/**
 * Splits an iterable into n independent iterators.
 *
 * @receiver Input iterable
 * @param n Number of iterators to create (default 2)
 * @return List of n independent sequences
 */
fun <T> Iterable<T>.tee(n: Int = 2): List<Sequence<T>> {
    require(n >= 0) { "Number of iterators must be non-negative" }

    val items = toList()
    return (0 until n).map { items.asSequence() }
}

/**
 * Zips iterables of different lengths, filling shorter ones with fillvalue.
 *
 * @param iterables Variable number of iterables to zip
 * @param fillvalue Value to use for missing elements
 * @return Sequence of lists with elements from all iterables
 */
fun <T> zipLongest(vararg iterables: Iterable<T>, fillvalue: T): Sequence<List<T>> = sequence {
    val iters = iterables.map { it.iterator() }

    while (iters.any { it.hasNext() }) {
        val res = mutableListOf<T>()
        for (iterator in iters) when {
            iterator.hasNext() -> res.add(iterator.next())
            else -> res.add(fillvalue)
        }
        yield(res)
    }
}

/**
 * Extension function to make chaining easier.
 */
fun <T> Iterable<T>.chain(vararg others: Iterable<T>): Sequence<T> = sequence {
    yieldAll(this@chain)
    for (itr in others) yieldAll(itr)
}
