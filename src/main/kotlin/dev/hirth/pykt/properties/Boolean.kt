package dev.hirth.pykt.properties

import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Property delegate for [Boolean] values, which are read from a [Map].
 *
 * @param map The map to read the value from.
 * @param default The default value to return if the key is not present in the map.
 */
class BooleanRO(val map: Map<String, String>) : ReadOnlyProperty<Any?, Boolean> {

    private var default: ((String) -> Boolean)? = null

    constructor(map: Map<String, String>, default: (String) -> Boolean) : this(map) {
        this.default = default
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        return when (default) {
            null -> map.getValue(property.name).toBoolean()
            else -> map[property.name]?.toBoolean() ?: default!!(property.name)
        }
    }
}

/**
 * Property delegate for nullable [Boolean] values, which are read from a [Map].
 *
 * @param map The map to read the value from.
 * @param default The default value to use if the key is not present in the map.
 */
class NullableBooleanRO(val map: Map<String, String>) : ReadOnlyProperty<Any?, Boolean?> {

    private var default: ((String) -> Boolean)? = null

    constructor(map: Map<String, String>, default: (String) -> Boolean) : this(map) {
        this.default = default
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean? {
        val value = map[property.name]?.toBoolean()
        return when (default) {
            null -> value
            else -> value ?: default!!(property.name)
        }
    }
}

/**
 * Property delegate for [Boolean] values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param default The default value to use if the key is not present in the map.
 */
class BooleanRW(val map: MutableMap<String, String>) : ReadWriteProperty<Any?, Boolean> {

    private var default: ((String) -> Boolean)? = null

    constructor(map: MutableMap<String, String>, default: (String) -> Boolean) : this(map) {
        this.default = default
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        return when (default) {
            null -> map.getValue(property.name).toBoolean()
            else -> map[property.name]?.toBoolean() ?: default!!(property.name)
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        map[property.name] = value.toString()
    }
}

/**
 * Property delegate for nullable [Boolean] values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param default The default value to use if the key is not present in the map.
 */
class NullableBooleanRW(val map: MutableMap<String, String>) : ReadWriteProperty<Any?, Boolean?> {

    private var default: ((String) -> Boolean)? = null

    constructor(map: MutableMap<String, String>, default: (String) -> Boolean) : this(map) {
        this.default = default
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean? {
        val value = map[property.name]?.toBoolean()
        return when (default) {
            null -> value
            else -> value ?: default!!(property.name)
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean?) {
        if (value != null) {
            map[property.name] = value.toString()
        }
    }
}