package dev.hirth.functools

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A property delegate that caches the result of a computation.
 * The computation is performed only once, when the property is first accessed.
 */
class CachedProperty<T>(private val initializer: () -> T) : ReadOnlyProperty<Any?, T> {
    private var value: T? = null
    private var initialized = false
    
    @Synchronized
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (!initialized) {
            value = initializer()
            initialized = true
        }
        @Suppress("UNCHECKED_CAST")
        return value as T
    }
    
    /**
     * Clears the cached value, forcing recomputation on next access.
     */
    @Synchronized
    fun clearCache() {
        value = null
        initialized = false
    }
    
    /**
     * Returns true if the value has been computed and cached.
     */
    @Synchronized
    fun isCached(): Boolean = initialized
}

/**
 * Creates a cached property delegate.
 * 
 * Usage:
 * ```
 * class MyClass {
 *     val expensiveProperty by cachedProperty { 
 *         // Expensive computation here
 *         computeExpensiveValue()
 *     }
 * }
 * ```
 */
fun <T> cachedProperty(initializer: () -> T): CachedProperty<T> {
    return CachedProperty(initializer)
}

/**
 * Thread-safe cached property delegate using lazy initialization.
 * This is similar to Kotlin's built-in `lazy` but follows functools naming convention.
 */
fun <T> cachedPropertyThreadSafe(initializer: () -> T): Lazy<T> {
    return lazy(LazyThreadSafetyMode.SYNCHRONIZED, initializer)
}

/**
 * Non-thread-safe cached property delegate for better performance when thread safety isn't needed.
 */
fun <T> cachedPropertyUnsafe(initializer: () -> T): Lazy<T> {
    return lazy(LazyThreadSafetyMode.NONE, initializer)
}