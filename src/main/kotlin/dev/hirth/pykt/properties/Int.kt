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
class IntRO(map: Map<String, String>) : MapPropertyRO<Int>(map, String::toInt) {
    constructor(map: Map<String, String>, default: () -> Int) :
            this(map) { this.default = default }

    constructor(map: Map<String, String>, default: Int) :
            this(map) { this.default = { default } }
}

/**
 * Property delegate for nullable [Int] values, which are read from a [Map].
 *
 * @param map The map to read the value from.
 * @param default The default value to use if the key is not present in the map.
 */
class NullableIntRO(map: Map<String, String>) : NullableMapPropertyRO<Int>(map, String::toInt) {
    constructor(map: Map<String, String>, default: () -> Int) :
            this(map) { this.default = default }

    constructor(map: Map<String, String>, default: Int) :
            this(map) { this.default = { default } }
}

/**
 * Property delegate for [Int] values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param default The default value to use if the key is not present in the map.
 */
class IntRW(map: MutableMap<String, String>) : MapPropertyRW<Int>(map, String::toInt) {
    constructor(map: MutableMap<String, String>, default: () -> Int) :
            this(map) { this.default = default }

    constructor(map: MutableMap<String, String>, default: Int) :
            this(map) { this.default = { default } }
}

/**
 * Property delegate for nullable [Int] values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param default The default value to use if the key is not present in the map.
 */
class NullableIntRW(map: MutableMap<String, String>) : NullableMapPropertyRW<Int>(map, String::toInt) {
    constructor(map: MutableMap<String, String>, default: () -> Int) :
            this(map) { this.default = default }

    constructor(map: MutableMap<String, String>, default: Int) :
            this(map) { this.default = { default } }
}
