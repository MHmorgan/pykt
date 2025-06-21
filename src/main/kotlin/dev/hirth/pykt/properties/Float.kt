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
class FloatRO(map: Map<String, String>) : MapPropertyRO<Float>(map, String::toFloat) {
    constructor(map: Map<String, String>, default: () -> Float) :
            this(map) { this.default = default }

    constructor(map: Map<String, String>, default: Float) :
            this(map) { this.default = { default } }
}

/**
 * Property delegate for nullable [Float] values, which are read from a [Map].
 *
 * @param map The map to read the value from.
 * @param default The default value to use if the key is not present in the map.
 */
class NullableFloatRO(map: Map<String, String>) : NullableMapPropertyRO<Float>(map, String::toFloat) {
    constructor(map: Map<String, String>, default: () -> Float) :
            this(map) { this.default = default }

    constructor(map: Map<String, String>, default: Float) :
            this(map) { this.default = { default } }
}

/**
 * Property delegate for [Float] values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param default The default value to use if the key is not present in the map.
 */
class FloatRW(map: MutableMap<String, String>) : MapPropertyRW<Float>(map, String::toFloat) {
    constructor(map: MutableMap<String, String>, default: () -> Float) :
            this(map) { this.default = default }

    constructor(map: MutableMap<String, String>, default: Float) :
            this(map) { this.default = { default } }
}

/**
 * Property delegate for nullable [Float] values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param default The default value to use if the key is not present in the map.
 */
class NullableFloatRW(map: MutableMap<String, String>) : NullableMapPropertyRW<Float>(map, String::toFloat) {
    constructor(map: MutableMap<String, String>, default: () -> Float) :
            this(map) { this.default = default }

    constructor(map: MutableMap<String, String>, default: Float) :
            this(map) { this.default = { default } }
}
