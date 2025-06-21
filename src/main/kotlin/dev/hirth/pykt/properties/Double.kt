package dev.hirth.pykt.properties

import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Property delegate for [Double] values, which are read from a [Map].
 *
 * @param map The map to read the value from.
 * @param default The default value to return if the key is not present in the map.
 */
class DoubleRO(map: Map<String, String>) : MapPropertyRO<Double>(map, String::toDouble) {
    constructor(map: Map<String, String>, default: () -> Double) :
            this(map) { this.default = default }

    constructor(map: Map<String, String>, default: Double) :
            this(map) { this.default = { default } }
}

/**
 * Property delegate for nullable [Double] values, which are read from a [Map].
 *
 * @param map The map to read the value from.
 * @param default The default value to use if the key is not present in the map.
 */
class NullableDoubleRO(val map: Map<String, String>) : ReadOnlyProperty<Any?, Double?> {

    private var default: (() -> Double)? = null

    constructor(map: Map<String, String>, default: () -> Double) : this(map) {
        this.default = default
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Double? {
        val value = map[property.name]?.toDouble()
        return when (default) {
            null -> value
            else -> value ?: default!!()
        }
    }
}

/**
 * Property delegate for [Double] values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param default The default value to use if the key is not present in the map.
 */
class DoubleRW(val map: MutableMap<String, String>) : ReadWriteProperty<Any?, Double> {

    private var default: (() -> Double)? = null

    constructor(map: MutableMap<String, String>, default: () -> Double) : this(map) {
        this.default = default
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Double {
        return when (default) {
            null -> map.getValue(property.name).toDouble()
            else -> map[property.name]?.toDouble() ?: default!!()
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Double) {
        map[property.name] = value.toString()
    }
}

/**
 * Property delegate for nullable [Double] values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param default The default value to use if the key is not present in the map.
 */
class NullableDoubleRW(val map: MutableMap<String, String>) : ReadWriteProperty<Any?, Double?> {

    private var default: (() -> Double)? = null

    constructor(map: MutableMap<String, String>, default: () -> Double) : this(map) {
        this.default = default
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Double? {
        val value = map[property.name]?.toDouble()
        return when (default) {
            null -> value
            else -> value ?: default!!()
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Double?) {
        if (value != null) {
            map[property.name] = value.toString()
        }
    }
}
