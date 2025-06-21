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
class BooleanRO(map: Map<String, String>) : MapPropertyRO<Boolean>(map, String::toBoolean) {
    constructor(map: Map<String, String>, default: () -> Boolean) :
            this(map) { this.default = default }

    constructor(map: Map<String, String>, default: Boolean) :
            this(map) { this.default = { default } }
}

/**
 * Property delegate for nullable [Boolean] values, which are read from a [Map].
 *
 * @param map The map to read the value from.
 * @param default The default value to use if the key is not present in the map.
 */
class NullableBooleanRO(map: Map<String, String>) : NullableMapPropertyRO<Boolean>(map, String::toBoolean) {
    constructor(map: Map<String, String>, default: () -> Boolean) :
            this(map) { this.default = default }

    constructor(map: Map<String, String>, default: Boolean) :
            this(map) { this.default = { default } }
}

/**
 * Property delegate for [Boolean] values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param default The default value to use if the key is not present in the map.
 */
class BooleanRW(map: MutableMap<String, String>) : MapPropertyRW<Boolean>(map, String::toBoolean) {
    constructor(map: MutableMap<String, String>, default: () -> Boolean) :
            this(map) { this.default = default }

    constructor(map: MutableMap<String, String>, default: Boolean) :
            this(map) { this.default = { default } }
}

/**
 * Property delegate for nullable [Boolean] values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param default The default value to use if the key is not present in the map.
 */
class NullableBooleanRW(map: MutableMap<String, String>) : NullableMapPropertyRW<Boolean>(map, String::toBoolean) {
    constructor(map: MutableMap<String, String>, default: () -> Boolean) :
            this(map) { this.default = default }

    constructor(map: MutableMap<String, String>, default: Boolean) :
            this(map) { this.default = { default } }
}
