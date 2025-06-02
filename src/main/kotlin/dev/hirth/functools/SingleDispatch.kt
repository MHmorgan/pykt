package dev.hirth.functools

import kotlin.reflect.KClass

/**
 * Single-dispatch generic function registry.
 * This is similar to Python's functools.singledispatch.
 */
class SingleDispatch<T, R>(
    private val defaultImpl: (T) -> R
) : (T) -> R {
    
    private val registry = mutableMapOf<KClass<*>, (Any?) -> R>()
    
    override fun invoke(arg: T): R {
        val argClass = arg?.let { it::class } ?: return defaultImpl(arg)
        val impl = registry[argClass] ?: defaultImpl
        @Suppress("UNCHECKED_CAST")
        return (impl as (T) -> R)(arg)
    }
    
    /**
     * Register a function for a specific type.
     */
    fun <U : Any> register(type: KClass<U>, implementation: (U) -> R) {
        @Suppress("UNCHECKED_CAST")
        registry[type] = implementation as (Any?) -> R
    }
    
    /**
     * Register a function for a specific type using inline reified type parameter.
     */
    inline fun <reified U : Any> register(noinline implementation: (U) -> R) {
        register(U::class, implementation)
    }
    
    /**
     * Get all registered types.
     */
    fun getRegistry(): Map<KClass<*>, (Any?) -> R> = registry.toMap()
    
    /**
     * Clear all registrations.
     */
    fun clearRegistry() {
        registry.clear()
    }
}

/**
 * Creates a single-dispatch generic function.
 * 
 * Usage:
 * ```
 * val process = singledispatch<Any?, String> { obj ->
 *     "Unknown type: ${obj?.let { it::class.simpleName }}"
 * }
 * 
 * process.register<String> { str -> "String: $str" }
 * process.register<Int> { num -> "Number: $num" }
 * 
 * println(process("hello"))  // "String: hello"
 * println(process(42))       // "Number: 42"
 * println(process(3.14))     // "Unknown type: Double"
 * ```
 */
fun <T, R> singledispatch(defaultImpl: (T) -> R): SingleDispatch<T, R> {
    return SingleDispatch(defaultImpl)
}

/**
 * Creates a single-dispatch generic function with a default "not implemented" behavior.
 */
fun <T, R> singledispatch(): SingleDispatch<T, R> {
    return SingleDispatch { arg ->
        throw NotImplementedError("No implementation found for type: ${arg?.let { it::class.simpleName }}")
    }
}