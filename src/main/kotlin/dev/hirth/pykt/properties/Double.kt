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
class NullableDoubleRO(map: Map<String, String>) : NullableMapPropertyRO<Double>(map, String::toDouble) {
    constructor(map: Map<String, String>, default: () -> Double) :
            this(map) { this.default = default }

    constructor(map: Map<String, String>, default: Double) :
            this(map) { this.default = { default } }
}

/**
 * Property delegate for [Double] values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param default The default value to use if the key is not present in the map.
 */
class DoubleRW(map: MutableMap<String, String>) : MapPropertyRW<Double>(map, String::toDouble) {
    constructor(map: MutableMap<String, String>, default: () -> Double) :
            this(map) { this.default = default }

    constructor(map: MutableMap<String, String>, default: Double) :
            this(map) { this.default = { default } }
}

/**
 * Property delegate for nullable [Double] values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param default The default value to use if the key is not present in the map.
 */
class NullableDoubleRW(map: MutableMap<String, String>) : NullableMapPropertyRW<Double>(map, String::toDouble) {
    constructor(map: MutableMap<String, String>, default: () -> Double) :
            this(map) { this.default = default }

    constructor(map: MutableMap<String, String>, default: Double) :
            this(map) { this.default = { default } }
}
