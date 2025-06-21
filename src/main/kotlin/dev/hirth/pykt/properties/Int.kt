package dev.hirth.pykt.properties

import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Property delegate for [Int] values, which are read from a [Map].
 *
 * @param map The map to read the value from.
 * @param default The default value to return if the key is not present in the map.
 */
class IntRO(val map: Map<String, String>) : ReadOnlyProperty<Any?, Int> {

    private var default: (() -> Int)? = null

    constructor(map: Map<String, String>, default: () -> Int) : this(map) {
        this.default = default
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return when (default) {
            null -> map.getValue(property.name).toInt()
            else -> map[property.name]?.toInt() ?: default!!()
        }
    }
}

/**
 * Property delegate for nullable [Int] values, which are read from a [Map].
 *
 * @param map The map to read the value from.
 * @param default The default value to use if the key is not present in the map.
 */
class NullableIntRO(val map: Map<String, String>) : ReadOnlyProperty<Any?, Int?> {

    private var default: (() -> Int)? = null

    constructor(map: Map<String, String>, default: () -> Int) : this(map) {
        this.default = default
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Int? {
        val value = map[property.name]?.toInt()
        return when (default) {
            null -> value
            else -> value ?: default!!()
        }
    }
}

/**
 * Property delegate for [Int] values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param default The default value to use if the key is not present in the map.
 */
class IntRW(val map: MutableMap<String, String>) : ReadWriteProperty<Any?, Int> {

    private var default: (() -> Int)? = null

    constructor(map: MutableMap<String, String>, default: () -> Int) : this(map) {
        this.default = default
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return when (default) {
            null -> map.getValue(property.name).toInt()
            else -> map[property.name]?.toInt() ?: default!!()
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        map[property.name] = value.toString()
    }
}

/**
 * Property delegate for nullable [Int] values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param default The default value to use if the key is not present in the map.
 */
class NullableIntRW(val map: MutableMap<String, String>) : ReadWriteProperty<Any?, Int?> {

    private var default: (() -> Int)? = null

    constructor(map: MutableMap<String, String>, default: () -> Int) : this(map) {
        this.default = default
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Int? {
        val value = map[property.name]?.toInt()
        return when (default) {
            null -> value
            else -> value ?: default!!()
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int?) {
        if (value != null) {
            map[property.name] = value.toString()
        }
    }
}
