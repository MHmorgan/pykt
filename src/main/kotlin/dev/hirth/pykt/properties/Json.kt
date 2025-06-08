package dev.hirth.pykt.properties

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Property delegate for JSON serializable values, which are read from a [Map].
 *
 * @param map The map to read the value from.
 * @param type The Kotlin class of the value.
 * @param json The Json instance to use for serialization/deserialization.
 */
class JsonRO<T : Any>(
    val map: Map<String, String>,
    val type: KClass<T>,
    val json: Json = Json,
) : ReadOnlyProperty<Any?, T> {

    private var default: ((String) -> T)? = null

    constructor(
        map: Map<String, String>,
        type: KClass<T>,
        default: (String) -> T,
        json: Json = Json
    ) : this(map, type, json) {
        this.default = default
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (default != null && property.name !in map) {
            return default!!(property.name)
        }

        val value = map.getValue(property.name)
        @Suppress("UNCHECKED_CAST")
        return json.decodeFromString(serializer(type.java), value) as T
    }
}

/**
 * Property delegate for nullable JSON serializable values, which are read from a [Map].
 *
 * @param map The map to read the value from.
 * @param type The Kotlin class of the value.
 * @param json The Json instance to use for serialization/deserialization.
 */
class NullableJsonRO<T : Any>(
    val map: Map<String, String>,
    val type: KClass<T>,
    val json: Json = Json
) : ReadOnlyProperty<Any?, T?> {

    private var default: ((String) -> T)? = null

    constructor(
        map: Map<String, String>,
        type: KClass<T>,
        default: (String) -> T,
        json: Json = Json
    ) : this(map, type, json) {
        this.default = default
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        if (default != null && property.name !in map) {
            return default!!(property.name)
        }

        return map[property.name]?.let {
            @Suppress("UNCHECKED_CAST")
            json.decodeFromString(serializer(type.java), it) as T
        }
    }
}

/**
 * Property delegate for JSON serializable values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param type The Kotlin class of the value.
 * @param json The Json instance to use for serialization/deserialization.
 */
class JsonRW<T : Any>(
    val map: MutableMap<String, String>,
    val type: KClass<T>,
    val json: Json = Json
) : ReadWriteProperty<Any?, T> {

    private var default: ((String) -> T)? = null

    constructor(
        map: MutableMap<String, String>,
        type: KClass<T>,
        default: (String) -> T,
        json: Json = Json
    ) : this(map, type, json) {
        this.default = default
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (default != null && property.name !in map) {
            return default!!(property.name)
        }

        val value = map.getValue(property.name)
        @Suppress("UNCHECKED_CAST")
        return json.decodeFromString(serializer(type.java), value) as T
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        map[property.name] = json.encodeToString(serializer(type.java), value)
    }
}

/**
 * Property delegate for nullable JSON serializable values, which are written to a [MutableMap].
 *
 * @param map The map to write the value to.
 * @param type The Kotlin class of the value.
 * @param json The Json instance to use for serialization/deserialization.
 */
class NullableJsonRW<T : Any>(
    val map: MutableMap<String, String>,
    val type: KClass<T>,
    val json: Json = Json
) : ReadWriteProperty<Any?, T?> {

    private var default: ((String) -> T)? = null

    constructor(
        map: MutableMap<String, String>,
        type: KClass<T>,
        default: (String) -> T,
        json: Json = Json
    ) : this(map, type, json) {
        this.default = default
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        if (default != null && property.name !in map) {
            return default!!(property.name)
        }

        return map[property.name]?.let {
            @Suppress("UNCHECKED_CAST")
            json.decodeFromString(serializer(type.java), it) as T
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        if (value != null) {
            map[property.name] = json.encodeToString(serializer(type.java), value)
        }
    }
}
