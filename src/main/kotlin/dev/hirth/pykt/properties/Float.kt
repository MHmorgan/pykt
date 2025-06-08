package dev.hirth.pykt.properties

import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Property delegate for [Float] values, which are read from a [Map].
 *
 * @param map The map to read the value from.
 * @param default The default value to return if the key is not present in the map.
 */
class FloatRO(val map: Map<String, String>) : ReadOnlyProperty<Any?, Float> {

    private var default: ((String) -> Float)? = null

    constructor(map: Map<String, String>, default: (String) -> Float) : this(map) {
        this.default = default
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Float {
        return when (default) {
            null -> map.getValue(property.name).toFloat()
            else -> map[property.name]?.toFloat() ?: default!!(property.name)
        }
    }
}

/**
 * Property delegate for nullable [Float] values, which are read from a [Map].
 *
 * @param map The map to read the value from.
 * @param default The default value to use if the key is not present in the map.
 */
class NullableFloatRO(val map: Map<String, String>) : ReadOnlyProperty<Any?, Float?> {

    private var default: ((String) -> Float)? = null

    constructor(map: Map<String, String>, default: (String) -> Float) : this(map) {
        this.default = default
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Float? {
        val value = map[property.name]?.toFloat()
        return when (default) {
            null -> value
            else -> value ?: default!!(property.name)
        }
    }
}

/**
 * Property delegate for [Float] values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param default The default value to use if the key is not present in the map.
 */
class FloatRW(val map: MutableMap<String, String>) : ReadWriteProperty<Any?, Float> {

    private var default: ((String) -> Float)? = null

    constructor(map: MutableMap<String, String>, default: (String) -> Float) : this(map) {
        this.default = default
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Float {
        return when (default) {
            null -> map.getValue(property.name).toFloat()
            else -> map[property.name]?.toFloat() ?: default!!(property.name)
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
        map[property.name] = value.toString()
    }
}

/**
 * Property delegate for nullable [Float] values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param default The default value to use if the key is not present in the map.
 */
class NullableFloatRW(val map: MutableMap<String, String>) : ReadWriteProperty<Any?, Float?> {

    private var default: ((String) -> Float)? = null

    constructor(map: MutableMap<String, String>, default: (String) -> Float) : this(map) {
        this.default = default
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Float? {
        val value = map[property.name]?.toFloat()
        return when (default) {
            null -> value
            else -> value ?: default!!(property.name)
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Float?) {
        if (value != null) {
            map[property.name] = value.toString()
        }
    }
}
