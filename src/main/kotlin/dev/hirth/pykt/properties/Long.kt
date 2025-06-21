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
class LongRO(map: Map<String, String>) : MapPropertyRO<Long>(map, String::toLong) {
    constructor(map: Map<String, String>, default: () -> Long) :
            this(map) { this.default = default }

    constructor(map: Map<String, String>, default: Long) :
            this(map) { this.default = { default } }
}

/**
 * Property delegate for nullable [Long] values, which are read from a [Map].
 *
 * @param map The map to read the value from.
 * @param default The default value to use if the key is not present in the map.
 */
class NullableLongRO(map: Map<String, String>) : NullableMapPropertyRO<Long>(map, String::toLong) {
    constructor(map: Map<String, String>, default: () -> Long) :
            this(map) { this.default = default }

    constructor(map: Map<String, String>, default: Long) :
            this(map) { this.default = { default } }
}

/**
 * Property delegate for [Long] values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param default The default value to use if the key is not present in the map.
 */
class LongRW(map: MutableMap<String, String>) : MapPropertyRW<Long>(map, String::toLong) {
    constructor(map: MutableMap<String, String>, default: () -> Long) :
            this(map) { this.default = default }

    constructor(map: MutableMap<String, String>, default: Long) :
            this(map) { this.default = { default } }
}

/**
 * Property delegate for nullable [Long] values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param default The default value to use if the key is not present in the map.
 */
class NullableLongRW(map: MutableMap<String, String>) : NullableMapPropertyRW<Long>(map, String::toLong) {
    constructor(map: MutableMap<String, String>, default: () -> Long) :
            this(map) { this.default = default }

    constructor(map: MutableMap<String, String>, default: Long) :
            this(map) { this.default = { default } }
}
