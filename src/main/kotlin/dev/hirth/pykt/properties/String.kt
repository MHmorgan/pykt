package dev.hirth.pykt.properties

import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Property delegate for [String] values, which are read from a [Map].
 *
 * @param map The map to read the value from.
 * @param default The default value to return if the key is not present in the map.
 */
class StringRO(val map: Map<String, String>) : ReadOnlyProperty<Any?, String> {

    private var default: ((String) -> String)? = null

    constructor(map: Map<String, String>, default: (String) -> String) : this(map) {
        this.default = default
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return when (default) {
            null -> map.getValue(property.name)
            else -> map[property.name] ?: default!!(property.name)
        }
    }
}

/**
 * Property delegate for nullable [String] values, which are read from a [Map].
 *
 * @param map The map to read the value from.
 * @param default The default value to use if the key is not present in the map.
 */
class NullableStringRO(val map: Map<String, String>) : ReadOnlyProperty<Any?, String?> {

    private var default: ((String) -> String)? = null

    constructor(map: Map<String, String>, default: (String) -> String) : this(map) {
        this.default = default
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): String? {
        val value = map[property.name]
        return when (default) {
            null -> value
            else -> value ?: default!!(property.name)
        }
    }
}

/**
 * Property delegate for [String] values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param default The default value to use if the key is not present in the map.
 */
class StringRW(val map: MutableMap<String, String>) : ReadWriteProperty<Any?, String> {

    private var default: ((String) -> String)? = null

    constructor(map: MutableMap<String, String>, default: (String) -> String) : this(map) {
        this.default = default
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return when (default) {
            null -> map.getValue(property.name)
            else -> map[property.name] ?: default!!(property.name)
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        map[property.name] = value
    }
}

/**
 * Property delegate for nullable [String] values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param default The default value to use if the key is not present in the map.
 */
class NullableStringRW(val map: MutableMap<String, String>) : ReadWriteProperty<Any?, String?> {

    private var default: ((String) -> String)? = null

    constructor(map: MutableMap<String, String>, default: (String) -> String) : this(map) {
        this.default = default
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): String? {
        val value = map[property.name]
        return when (default) {
            null -> value
            else -> value ?: default!!(property.name)
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
        if (value != null) {
            map[property.name] = value
        }
    }
}
