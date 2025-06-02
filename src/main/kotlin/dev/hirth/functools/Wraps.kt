package dev.hirth.functools

/**
 * Function metadata wrapper that preserves original function information.
 */
data class FunctionWrapper<T>(
    val function: T,
    val name: String? = null,
    val doc: String? = null,
    val module: String? = null,
    val qualname: String? = null,
    val annotations: Map<String, Any?> = emptyMap()
)

/**
 * Updates a wrapper function to look like the wrapped function.
 * This is similar to Python's functools.wraps decorator.
 * 
 * @param wrapped The original function being wrapped
 * @param wrapper The wrapper function
 * @param assigned List of attributes to copy from wrapped to wrapper
 * @param updated List of attributes to update on wrapper
 */
fun <T> updateWrapper(
    wrapped: T,
    wrapper: T,
    assigned: List<String> = listOf("name", "doc", "module", "qualname"),
    updated: List<String> = listOf("dict")
): T {
    // In Kotlin, this is mainly for documentation purposes
    // as we don't have the same dynamic attribute system as Python
    return wrapper
}

/**
 * Creates a wrapper function that preserves metadata from the original function.
 * This is a simplified version of Python's functools.wraps.
 * 
 * Usage:
 * ```
 * fun myDecorator(func: () -> String): () -> String {
 *     return wraps(func) {
 *         println("Before call")
 *         val result = func()
 *         println("After call")
 *         result
 *     }
 * }
 * ```
 */
fun <T> wraps(wrapped: T, wrapper: T): T {
    return updateWrapper(wrapped, wrapper)
}

/**
 * A decorator factory that creates wrapped functions with metadata preservation.
 */
inline fun <T, R> wraps(noinline wrapped: () -> T, crossinline wrapper: (() -> T) -> R): R {
    return wrapper(wrapped)
}

/**
 * A decorator factory for functions with one parameter.
 */
inline fun <T, U, R> wraps(noinline wrapped: (T) -> U, crossinline wrapper: ((T) -> U) -> R): R {
    return wrapper(wrapped)
}

/**
 * A decorator factory for functions with two parameters.
 */
inline fun <T, U, V, R> wraps(noinline wrapped: (T, U) -> V, crossinline wrapper: ((T, U) -> V) -> R): R {
    return wrapper(wrapped)
}