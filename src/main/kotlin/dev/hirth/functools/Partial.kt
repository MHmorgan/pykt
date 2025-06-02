package dev.hirth.functools

/**
 * A partial function that wraps a function with some arguments pre-filled.
 * Note: This is a simplified implementation that works with the type-safe partial functions below.
 */
class Partial<out R>(
    private val func: () -> R
) : () -> R {
    
    override fun invoke(): R = func()
}

/**
 * Creates a partial function with some arguments pre-filled.
 * 
 * @param func The function to create a partial for
 * @param args The arguments to pre-fill
 * @return A new partial function
 */
fun <R> partial(func: () -> R, vararg args: Any?): () -> R {
    require(args.isEmpty()) { "Cannot provide args for zero-parameter function" }
    return func
}

/**
 * Creates a partial function with some arguments pre-filled.
 */
fun <T1, R> partial(func: (T1) -> R, arg1: T1): () -> R {
    return { func(arg1) }
}

/**
 * Creates a partial function with some arguments pre-filled.
 */
fun <T1, T2, R> partial(func: (T1, T2) -> R, arg1: T1): (T2) -> R {
    return { arg2 -> func(arg1, arg2) }
}

/**
 * Creates a partial function with some arguments pre-filled.
 */
fun <T1, T2, R> partial(func: (T1, T2) -> R, arg1: T1, arg2: T2): () -> R {
    return { func(arg1, arg2) }
}

/**
 * Creates a partial function with some arguments pre-filled.
 */
fun <T1, T2, T3, R> partial(func: (T1, T2, T3) -> R, arg1: T1): (T2, T3) -> R {
    return { arg2, arg3 -> func(arg1, arg2, arg3) }
}

/**
 * Creates a partial function with some arguments pre-filled.
 */
fun <T1, T2, T3, R> partial(func: (T1, T2, T3) -> R, arg1: T1, arg2: T2): (T3) -> R {
    return { arg3 -> func(arg1, arg2, arg3) }
}

/**
 * Creates a partial function with some arguments pre-filled.
 */
fun <T1, T2, T3, R> partial(func: (T1, T2, T3) -> R, arg1: T1, arg2: T2, arg3: T3): () -> R {
    return { func(arg1, arg2, arg3) }
}