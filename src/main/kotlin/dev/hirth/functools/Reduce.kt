package dev.hirth.functools

/**
 * Apply a function of two arguments cumulatively to the items of an iterable, from left to right,
 * so as to reduce the iterable to a single value.
 * 
 * For example, reduce(::add, [1, 2, 3, 4, 5]) calculates ((((1+2)+3)+4)+5).
 * If the optional initializer is present, it is placed before the items of the iterable
 * in the calculation, and serves as a default when the iterable is empty.
 */
fun <T> reduce(function: (T, T) -> T, iterable: Iterable<T>): T {
    val iterator = iterable.iterator()
    if (!iterator.hasNext()) {
        throw IllegalArgumentException("reduce() of empty sequence with no initial value")
    }
    
    var result = iterator.next()
    while (iterator.hasNext()) {
        result = function(result, iterator.next())
    }
    return result
}

/**
 * Apply a function of two arguments cumulatively to the items of an iterable, from left to right,
 * so as to reduce the iterable to a single value.
 * 
 * @param function The binary function to apply cumulatively
 * @param iterable The iterable to reduce
 * @param initialValue The initial value to start with
 */
fun <T, R> reduce(function: (R, T) -> R, iterable: Iterable<T>, initialValue: R): R {
    var result = initialValue
    for (item in iterable) {
        result = function(result, item)
    }
    return result
}

/**
 * Apply a function of two arguments cumulatively to the items of a sequence, from left to right,
 * so as to reduce the sequence to a single value.
 */
fun <T> reduce(function: (T, T) -> T, sequence: Sequence<T>): T {
    return reduce(function, sequence.asIterable())
}

/**
 * Apply a function of two arguments cumulatively to the items of a sequence, from left to right,
 * so as to reduce the sequence to a single value.
 */
fun <T, R> reduce(function: (R, T) -> R, sequence: Sequence<T>, initialValue: R): R {
    return reduce(function, sequence.asIterable(), initialValue)
}

/**
 * Apply a function of two arguments cumulatively to the items of an array, from left to right,
 * so as to reduce the array to a single value.
 */
fun <T> reduce(function: (T, T) -> T, array: Array<T>): T {
    return reduce(function, array.asIterable())
}

/**
 * Apply a function of two arguments cumulatively to the items of an array, from left to right,
 * so as to reduce the array to a single value.
 */
fun <T, R> reduce(function: (R, T) -> R, array: Array<T>, initialValue: R): R {
    return reduce(function, array.asIterable(), initialValue)
}