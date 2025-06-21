package dev.hirth.pykt.properties

import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Property delegate for [Long] values, which are read from a [Map].
 *
 * @param map The map to read the value from.
 * @param default The default value to return if the key is not present in the map.
 */
class LongRO(val map: Map<String, String>) : ReadOnlyProperty<Any?, Long> {

    private var default: (() -> Long)? = null

    constructor(map: Map<String, String>, default: () -> Long) : this(map) {
        this.default = default
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Long {
        return when (default) {
            null -> map.getValue(property.name).toLong()
            else -> map[property.name]?.toLong() ?: default!!()
        }
    }
}

/**
 * Property delegate for nullable [Long] values, which are read from a [Map].
 *
 * @param map The map to read the value from.
 * @param default The default value to use if the key is not present in the map.
 */
class NullableLongRO(val map: Map<String, String>) : ReadOnlyProperty<Any?, Long?> {

    private var default: (() -> Long)? = null

    constructor(map: Map<String, String>, default: () -> Long) : this(map) {
        this.default = default
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Long? {
        val value = map[property.name]?.toLong()
        return when (default) {
            null -> value
            else -> value ?: default!!()
        }
    }
}

/**
 * Property delegate for [Long] values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param default The default value to use if the key is not present in the map.
 */
class LongRW(val map: MutableMap<String, String>) : ReadWriteProperty<Any?, Long> {

    private var default: (() -> Long)? = null

    constructor(map: MutableMap<String, String>, default: () -> Long) : this(map) {
        this.default = default
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Long {
        return when (default) {
            null -> map.getValue(property.name).toLong()
            else -> map[property.name]?.toLong() ?: default!!()
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) {
        map[property.name] = value.toString()
    }
}

/**
 * Property delegate for nullable [Long] values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param default The default value to use if the key is not present in the map.
 */
class NullableLongRW(val map: MutableMap<String, String>) : ReadWriteProperty<Any?, Long?> {

    private var default: (() -> Long)? = null

    constructor(map: MutableMap<String, String>, default: () -> Long) : this(map) {
        this.default = default
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Long? {
        val value = map[property.name]?.toLong()
        return when (default) {
            null -> value
            else -> value ?: default!!()
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Long?) {
        if (value != null) {
            map[property.name] = value.toString()
        }
    }
}
