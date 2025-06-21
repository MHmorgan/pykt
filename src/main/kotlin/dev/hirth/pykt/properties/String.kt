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
class StringRO(map: Map<String, String>) : MapPropertyRO<String>(map, { it }) {
    constructor(map: Map<String, String>, default: () -> String) :
            this(map) { this.default = default }

    constructor(map: Map<String, String>, default: String) :
            this(map) { this.default = { default } }
}

/**
 * Property delegate for nullable [String] values, which are read from a [Map].
 *
 * @param map The map to read the value from.
 * @param default The default value to use if the key is not present in the map.
 */
class NullableStringRO(map: Map<String, String>) : NullableMapPropertyRO<String>(map, { it }) {
    constructor(map: Map<String, String>, default: () -> String) :
            this(map) { this.default = default }

    constructor(map: Map<String, String>, default: String) :
            this(map) { this.default = { default } }
}

/**
 * Property delegate for [String] values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param default The default value to use if the key is not present in the map.
 */
class StringRW(map: MutableMap<String, String>) : MapPropertyRW<String>(map, { it }) {
    constructor(map: MutableMap<String, String>, default: () -> String) :
            this(map) { this.default = default }

    constructor(map: MutableMap<String, String>, default: String) :
            this(map) { this.default = { default } }
}

/**
 * Property delegate for nullable [String] values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param default The default value to use if the key is not present in the map.
 */
class NullableStringRW(map: MutableMap<String, String>) : NullableMapPropertyRW<String>(map, { it }) {
    constructor(map: MutableMap<String, String>, default: () -> String) :
            this(map) { this.default = default }

    constructor(map: MutableMap<String, String>, default: String) :
            this(map) { this.default = { default } }
}
