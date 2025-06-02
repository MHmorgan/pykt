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
    var current = start
    while (true) {
        yield(current)
        current += step
    }
}

/**
 * Returns an infinite sequence cycling through the elements of the input iterable.
 * 
 * @param iterable Input iterable to cycle through
 * @return Infinite sequence cycling through elements
 */
fun <T> cycle(iterable: Iterable<T>): Sequence<T> = sequence {
    val items = iterable.toList()
    if (items.isEmpty()) return@sequence
    
    while (true) {
        yieldAll(items)
    }
}

/**
 * Returns a sequence that repeats the object either infinitely or a specified number of times.
 * 
 * @param obj Object to repeat
 * @param times Number of times to repeat (null for infinite)
 * @return Sequence repeating the object
 */
fun <T> repeat(obj: T, times: Int? = null): Sequence<T> = sequence {
    if (times == null) {
        while (true) {
            yield(obj)
        }
    } else {
        repeat(times) {
            yield(obj)
        }
    }
}

/**
 * Returns a sequence of running totals using a specified binary operation.
 * 
 * @param iterable Input iterable
 * @param operation Binary operation to apply
 * @param initial Optional initial value
 * @return Sequence of accumulated values
 */
fun <T> accumulate(
    iterable: Iterable<T>, 
    operation: (T, T) -> T,
    initial: T? = null
): Sequence<T> = sequence {
    val iterator = iterable.iterator()
    if (!iterator.hasNext()) return@sequence
    
    var accumulator = initial ?: iterator.next()
    yield(accumulator)
    
    while (iterator.hasNext()) {
        accumulator = operation(accumulator, iterator.next())
        yield(accumulator)
    }
}

/**
 * Returns a sequence of running totals for numeric types.
 */
fun accumulate(iterable: Iterable<Int>, initial: Int? = null): Sequence<Int> = sequence {
    val iterator = iterable.iterator()
    if (!iterator.hasNext()) return@sequence
    
    var accumulator = initial ?: iterator.next()
    yield(accumulator)
    
    while (iterator.hasNext()) {
        accumulator += iterator.next()
        yield(accumulator)
    }
}

/**
 * Returns a sequence of running totals for Long numeric types.
 */
fun accumulate(iterable: Iterable<Long>, initial: Long? = null): Sequence<Long> = sequence {
    val iterator = iterable.iterator()
    if (!iterator.hasNext()) return@sequence
    
    var accumulator = initial ?: iterator.next()
    yield(accumulator)
    
    while (iterator.hasNext()) {
        accumulator += iterator.next()
        yield(accumulator)
    }
}

/**
 * Returns a sequence of running totals for Double numeric types.
 */
fun accumulate(iterable: Iterable<Double>, initial: Double? = null): Sequence<Double> = sequence {
    val iterator = iterable.iterator()
    if (!iterator.hasNext()) return@sequence
    
    var accumulator = initial ?: iterator.next()
    yield(accumulator)
    
    while (iterator.hasNext()) {
        accumulator += iterator.next()
        yield(accumulator)
    }
}

/**
 * Flattens multiple iterables into a single sequence.
 * 
 * @param iterables Variable number of iterables to chain
 * @return Flattened sequence
 */
fun <T> chain(vararg iterables: Iterable<T>): Sequence<T> = sequence {
    for (iterable in iterables) {
        yieldAll(iterable)
    }
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
 * Drops elements from the iterable while the predicate is true.
 * 
 * @param iterable Input iterable
 * @param predicate Predicate function
 * @return Sequence without dropped elements
 */
fun <T> dropwhile(iterable: Iterable<T>, predicate: (T) -> Boolean): Sequence<T> = sequence {
    val iterator = iterable.iterator()
    
    // Drop while predicate is true
    while (iterator.hasNext()) {
        val item = iterator.next()
        if (!predicate(item)) {
            yield(item)
            break
        }
    }
    
    // Yield remaining items
    while (iterator.hasNext()) {
        yield(iterator.next())
    }
}

/**
 * Takes elements from the iterable while the predicate is true.
 * 
 * @param iterable Input iterable
 * @param predicate Predicate function
 * @return Sequence of taken elements
 */
fun <T> takewhile(iterable: Iterable<T>, predicate: (T) -> Boolean): Sequence<T> = sequence {
    for (item in iterable) {
        if (predicate(item)) {
            yield(item)
        } else {
            break
        }
    }
}

/**
 * Filters elements where the predicate is false (opposite of filter).
 * 
 * @param iterable Input iterable
 * @param predicate Predicate function
 * @return Sequence of elements where predicate is false
 */
fun <T> filterfalse(iterable: Iterable<T>, predicate: (T) -> Boolean): Sequence<T> = sequence {
    for (item in iterable) {
        if (!predicate(item)) {
            yield(item)
        }
    }
}

/**
 * Data class representing a group of consecutive equal elements.
 */
data class Group<K, V>(val key: K, val values: List<V>)

/**
 * Groups consecutive equal elements from the iterable using identity as key selector.
 * 
 * @param iterable Input iterable
 * @return Sequence of groups
 */
fun <T> groupby(iterable: Iterable<T>): Sequence<Group<T, T>> = groupby(iterable) { it }

/**
 * Groups consecutive equal elements from the iterable.
 * 
 * @param iterable Input iterable
 * @param keySelector Function to extract key for grouping
 * @return Sequence of groups
 */
fun <T, K> groupby(iterable: Iterable<T>, keySelector: (T) -> K): Sequence<Group<K, T>> = sequence {
    val iterator = iterable.iterator()
    if (!iterator.hasNext()) return@sequence
    
    var currentKey = keySelector(iterator.next())
    var currentGroup = mutableListOf<T>()
    
    // Add the first element we already consumed
    currentGroup.add(iterable.first())
    
    // Process remaining elements
    for (item in iterable.drop(1)) {
        val key = keySelector(item)
        if (key == currentKey) {
            currentGroup.add(item)
        } else {
            yield(Group(currentKey, currentGroup.toList()))
            currentKey = key
            currentGroup = mutableListOf(item)
        }
    }
    
    // Yield the final group
    if (currentGroup.isNotEmpty()) {
        yield(Group(currentKey, currentGroup.toList()))
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
 * Returns successive overlapping pairs from the iterable.
 * 
 * @param iterable Input iterable
 * @return Sequence of pairs
 */
fun <T> pairwise(iterable: Iterable<T>): Sequence<Pair<T, T>> = sequence {
    val iterator = iterable.iterator()
    if (!iterator.hasNext()) return@sequence
    
    var previous = iterator.next()
    while (iterator.hasNext()) {
        val current = iterator.next()
        yield(Pair(previous, current))
        previous = current
    }
}

/**
 * Applies function to arguments from the iterable (like map but unpacks tuples).
 * 
 * @param iterable Iterable of argument lists
 * @param function Function to apply
 * @return Sequence of function results
 */
fun <T, R> starmap(iterable: Iterable<List<T>>, function: (List<T>) -> R): Sequence<R> = sequence {
    for (args in iterable) {
        yield(function(args))
    }
}

/**
 * Applies a binary function to arguments from pairs.
 */
fun <T1, T2, R> starmap(iterable: Iterable<Pair<T1, T2>>, function: (T1, T2) -> R): Sequence<R> = sequence {
    for ((a, b) in iterable) {
        yield(function(a, b))
    }
}

/**
 * Splits an iterable into n independent iterators.
 * 
 * @param iterable Input iterable
 * @param n Number of iterators to create (default 2)
 * @return List of n independent sequences
 */
fun <T> tee(iterable: Iterable<T>, n: Int = 2): List<Sequence<T>> {
    require(n >= 0) { "Number of iterators must be non-negative" }
    
    val items = iterable.toList()
    return (0 until n).map { items.asSequence() }
}

/**
 * Zips iterables of different lengths, filling shorter ones with fillvalue.
 * 
 * @param iterables Variable number of iterables to zip
 * @param fillvalue Value to use for missing elements
 * @return Sequence of lists with elements from all iterables
 */
fun zipLongest(vararg iterables: Iterable<*>, fillvalue: Any? = null): Sequence<List<Any?>> = sequence {
    val iterators = iterables.map { it.iterator() }
    
    while (iterators.any { it.hasNext() }) {
        val result = mutableListOf<Any?>()
        for (iterator in iterators) {
            if (iterator.hasNext()) {
                result.add(iterator.next())
            } else {
                result.add(fillvalue)
            }
        }
        yield(result)
    }
}

/**
 * Extension function to make chaining easier.
 */
fun <T> Iterable<T>.chain(vararg others: Iterable<T>): Sequence<T> = chain(this, *others)