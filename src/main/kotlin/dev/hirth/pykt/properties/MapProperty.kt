package dev.hirth.pykt.properties

import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Property delegate for [Map] values, which are read from a [Map].
 *
 * @param map The map to read the value from.
 * @param convert The conversion function to convert the string value to the desired type.
 * @param default The default value to return if the key is not present in the map.
 * @param T The type of the value to read.
 *
 * @see MapPropertyRW
 */
open class MapPropertyRO<T>(
    val map: Map<String, String>,
    val convert: (String) -> T,
): ReadOnlyProperty<Any?, T> {
    protected var default: (() -> T)? = null

    /**
     * Creates a new [MapPropertyRO] with the given [default] value
     * if the key is not present in the map.
     */
    constructor(
        map: Map<String, String>,
        default: () -> T,
        convert: (String) -> T,
    ) : this(map, convert) {
        this.default = default
    }

    /**
     * Creates a new [MapPropertyRO] with the given [default] value
     * if the key is not present in the map.
     */
    constructor(
        map: Map<String, String>,
        default: T,
        convert: (String) -> T,
    ) : this(map, convert) {
        this.default = { default }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return when (default) {
            null -> convert(map.getValue(property.name))
            else -> map[property.name]
                ?.let { convert(it) }
                ?: default!!()
        }
    }
}

/**
 * Property delegate for nullable [Map] values, which are read from a [Map].
 *
 * @param map The map to read the value from.
 * @param convert The conversion function to convert the string value to the desired type.
 * @param default The default value to return if the key is not present in the map.
 * @param T The type of the value to read.
 *
 * @see NullableMapPropertyRW
 */
open class NullableMapPropertyRO<T>(
    val map: Map<String, String>,
    val convert: (String) -> T,
): ReadOnlyProperty<Any?, T?> {
    protected var default: (() -> T)? = null

    /**
     * Creates a new [NullableMapPropertyRO] with the given [default] value
     * if the key is not present in the map.
     */
    constructor(
        map: Map<String, String>,
        default: () -> T,
        convert: (String) -> T,
    ) : this(map, convert) {
        this.default = default
    }

    /**
     * Creates a new [NullableMapPropertyRO] with the given [default] value
     * if the key is not present in the map.
     */
    constructor(
        map: Map<String, String>,
        default: T,
        convert: (String) -> T,
    ) : this(map, convert) {
        this.default = { default }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        val value = map[property.name]?.let { convert(it) }
        return when (default) {
            null -> value
            else -> value ?: default!!()
        }
    }
}

/**
 * Property delegate for [Map] values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param convert The conversion function to convert the string value to the desired type.
 * @param default The default value to return if the key is not present in the map.
 * @param T The type of the value to read/write.
 */
open class MapPropertyRW<T>(
    val map: MutableMap<String, String>,
    val convert: (String) -> T,
): ReadWriteProperty<Any?, T> {
    protected var default: (() -> T)? = null

    /**
     * Creates a new [MapPropertyRW] with the given [default] value
     * if the key is not present in the map.
     */
    constructor(
        map: MutableMap<String, String>,
        default: () -> T,
        convert: (String) -> T,
    ) : this(map, convert) {
        this.default = default
    }

    /**
     * Creates a new [MapPropertyRW] with the given [default] value
     * if the key is not present in the map.
     */
    constructor(
        map: MutableMap<String, String>,
        default: T,
        convert: (String) -> T,
    ) : this(map, convert) {
        this.default = { default }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return when (default) {
            null -> convert(map.getValue(property.name))
            else -> map[property.name]
                ?.let { convert(it) }
                ?: default!!()
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        map[property.name] = value.toString()
    }
}

/**
 * Property delegate for nullable [Map] values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param convert The conversion function to convert the string value to the desired type.
 * @param default The default value to return if the key is not present in the map.
 * @param T The type of the value to read/write.
 */
open class NullableMapPropertyRW<T>(
    val map: MutableMap<String, String>,
    val convert: (String) -> T,
): ReadWriteProperty<Any?, T?> {
    protected var default: (() -> T)? = null

    /**
     * Creates a new [NullableMapPropertyRW] with the given [default] value
     * if the key is not present in the map.
     */
    constructor(
        map: MutableMap<String, String>,
        default: () -> T,
        convert: (String) -> T,
    ) : this(map, convert) {
        this.default = default
    }

    /**
     * Creates a new [NullableMapPropertyRW] with the given [default] value
     * if the key is not present in the map.
     */
    constructor(
        map: MutableMap<String, String>,
        default: T,
        convert: (String) -> T,
    ) : this(map, convert) {
        this.default = { default }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        val value = map[property.name]?.let { convert(it) }
        return when (default) {
            null -> value
            else -> value ?: default!!()
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        if (value != null) {
            map[property.name] = value.toString()
        } else if (property.name in map) {
            map.remove(property.name)
        }
    }
}
