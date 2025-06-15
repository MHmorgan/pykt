package dev.hirth.pykt.toml

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.modules.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime

/**
 * TOML format for kotlinx.serialization.
 * 
 * Usage:
 * ```kotlin
 * @Serializable
 * data class Config(val name: String, val version: String)
 * 
 * val config = Toml.decodeFromString<Config>(tomlString)
 * ```
 */
@OptIn(ExperimentalSerializationApi::class)
object Toml : StringFormat {
    
    override val serializersModule: SerializersModule = EmptySerializersModule()
    
    /**
     * Decodes a TOML string into an object of type [T].
     */
    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        val document = string.parseToml()
        val decoder = TomlDecoder(document.rootTable)
        return decoder.decodeSerializableValue(deserializer)
    }
    
    /**
     * Encoding is not supported in this TOML implementation.
     */
    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String {
        throw UnsupportedOperationException("TOML encoding is not supported. Only decoding is available.")
    }
}

/**
 * TOML decoder that converts TOML values to Kotlin objects using kotlinx.serialization.
 */
@OptIn(ExperimentalSerializationApi::class)
internal class TomlDecoder(
    private val table: TomlTable,
    private val currentPath: String = ""
) : Decoder {
    
    override val serializersModule: SerializersModule get() = Toml.serializersModule
    
    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        return TomlCompositeDecoder(table, currentPath, descriptor)
    }
    
    override fun decodeBoolean(): Boolean = throw SerializationException("Expected composite structure")
    override fun decodeByte(): Byte = throw SerializationException("Expected composite structure")
    override fun decodeChar(): Char = throw SerializationException("Expected composite structure")
    override fun decodeDouble(): Double = throw SerializationException("Expected composite structure")
    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = throw SerializationException("Expected composite structure")
    override fun decodeFloat(): Float = throw SerializationException("Expected composite structure")
    override fun decodeInline(descriptor: SerialDescriptor): Decoder = this
    override fun decodeInt(): Int = throw SerializationException("Expected composite structure")
    override fun decodeLong(): Long = throw SerializationException("Expected composite structure")
    override fun decodeNotNullMark(): Boolean = true
    override fun decodeNull(): Nothing? = null
    override fun decodeShort(): Short = throw SerializationException("Expected composite structure")
    override fun decodeString(): String = throw SerializationException("Expected composite structure")
}

/**
 * Composite decoder for handling TOML structures.
 */
@OptIn(ExperimentalSerializationApi::class)
internal class TomlCompositeDecoder(
    private val table: TomlTable,
    private val currentPath: String,
    private val descriptor: SerialDescriptor
) : CompositeDecoder {
    
    private var currentIndex = 0
    
    override val serializersModule: SerializersModule get() = Toml.serializersModule
    
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        return if (currentIndex < descriptor.elementsCount) {
            val index = currentIndex
            currentIndex++
            index
        } else {
            CompositeDecoder.DECODE_DONE
        }
    }
    
    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean {
        val key = descriptor.getElementName(index)
        val value = getTomlValue(key)
        return when (value) {
            is TomlValue.Boolean -> value.value
            else -> throw SerializationException("Expected boolean for key '$key', got ${value?.let { it::class.simpleName }}")
        }
    }
    
    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte {
        val key = descriptor.getElementName(index)
        val value = getTomlValue(key)
        return when (value) {
            is TomlValue.Integer -> value.value.toByte()
            else -> throw SerializationException("Expected integer for key '$key', got ${value?.let { it::class.simpleName }}")
        }
    }
    
    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char {
        val key = descriptor.getElementName(index)
        val value = getTomlValue(key)
        return when (value) {
            is TomlValue.String -> {
                if (value.value.length == 1) value.value[0]
                else throw SerializationException("Expected single character string for key '$key'")
            }
            else -> throw SerializationException("Expected string for key '$key', got ${value?.let { it::class.simpleName }}")
        }
    }
    
    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double {
        val key = descriptor.getElementName(index)
        val value = getTomlValue(key)
        return when (value) {
            is TomlValue.Float -> value.value
            is TomlValue.Integer -> value.value.toDouble()
            else -> throw SerializationException("Expected float or integer for key '$key', got ${value?.let { it::class.simpleName }}")
        }
    }
    
    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float {
        val key = descriptor.getElementName(index)
        val value = getTomlValue(key)
        return when (value) {
            is TomlValue.Float -> value.value.toFloat()
            is TomlValue.Integer -> value.value.toFloat()
            else -> throw SerializationException("Expected float or integer for key '$key', got ${value?.let { it::class.simpleName }}")
        }
    }
    
    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
        val key = descriptor.getElementName(index)
        val value = getTomlValue(key)
        return TomlPrimitiveDecoder(value ?: throw SerializationException("Missing key '$key'"))
    }
    
    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int {
        val key = descriptor.getElementName(index)
        val value = getTomlValue(key)
        return when (value) {
            is TomlValue.Integer -> value.value.toInt()
            else -> throw SerializationException("Expected integer for key '$key', got ${value?.let { it::class.simpleName }}")
        }
    }
    
    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long {
        val key = descriptor.getElementName(index)
        val value = getTomlValue(key)
        return when (value) {
            is TomlValue.Integer -> value.value
            else -> throw SerializationException("Expected integer for key '$key', got ${value?.let { it::class.simpleName }}")
        }
    }
    
    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short {
        val key = descriptor.getElementName(index)
        val value = getTomlValue(key)
        return when (value) {
            is TomlValue.Integer -> value.value.toShort()
            else -> throw SerializationException("Expected integer for key '$key', got ${value?.let { it::class.simpleName }}")
        }
    }
    
    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String {
        val key = descriptor.getElementName(index)
        val value = getTomlValue(key)
        return when (value) {
            is TomlValue.String -> value.value
            else -> throw SerializationException("Expected string for key '$key', got ${value?.let { it::class.simpleName }}")
        }
    }
    
    override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T?>,
        previousValue: T?
    ): T? {
        val key = descriptor.getElementName(index)
        val value = getTomlValue(key)
        return if (value == null) {
            null
        } else {
            decodeSerializableElement(descriptor, index, deserializer as DeserializationStrategy<T>)
        }
    }
    
    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T {
        val key = descriptor.getElementName(index)
        val value = getTomlValue(key)
        
        return when {
            // Handle nested objects
            deserializer.descriptor.kind == StructureKind.CLASS -> {
                val nestedTable = when (value) {
                    is TomlValue.InlineTable -> {
                        // Convert inline table to TomlTable
                        val tomlTable = TomlTable()
                        for ((k, v) in value.value) {
                            tomlTable.setValue(k, v)
                        }
                        tomlTable
                    }
                    null -> {
                        // Look for nested table in table.tables
                        table.tables[key] ?: throw SerializationException("Expected table for key '$key'")
                    }
                    else -> throw SerializationException("Expected table or inline table for key '$key', got ${value::class.simpleName}")
                }
                val nestedDecoder = TomlDecoder(nestedTable, if (currentPath.isEmpty()) key else "$currentPath.$key")
                nestedDecoder.decodeSerializableValue(deserializer)
            }
            
            // Handle lists/arrays
            deserializer.descriptor.kind == StructureKind.LIST -> {
                when (value) {
                    is TomlValue.Array -> {
                        val listDecoder = TomlListDecoder(value.value)
                        listDecoder.decodeSerializableValue(deserializer)
                    }
                    null -> throw SerializationException("Missing required array for key '$key'")
                    else -> throw SerializationException("Expected array for key '$key', got ${value::class.simpleName}")
                }
            }
            
            // Handle primitive values directly
            value == null -> throw SerializationException("Missing required key '$key'")
            else -> {
                val primitiveDecoder = TomlPrimitiveDecoder(value)
                primitiveDecoder.decodeSerializableValue(deserializer)
            }
        }
    }
    
    override fun endStructure(descriptor: SerialDescriptor) {
        // Nothing to do
    }
    
    private fun getTomlValue(key: String): TomlValue? {
        return table.values[key]
    }
}

/**
 * Decoder for primitive TOML values.
 */
@OptIn(ExperimentalSerializationApi::class)
internal class TomlPrimitiveDecoder(private val value: TomlValue) : Decoder {
    
    override val serializersModule: SerializersModule get() = Toml.serializersModule
    
    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        throw SerializationException("Primitive value cannot have structure")
    }
    
    override fun decodeBoolean(): Boolean = when (value) {
        is TomlValue.Boolean -> value.value
        else -> throw SerializationException("Expected boolean, got ${value::class.simpleName}")
    }
    
    override fun decodeByte(): Byte = when (value) {
        is TomlValue.Integer -> value.value.toByte()
        else -> throw SerializationException("Expected integer, got ${value::class.simpleName}")
    }
    
    override fun decodeChar(): Char = when (value) {
        is TomlValue.String -> {
            if (value.value.length == 1) value.value[0]
            else throw SerializationException("Expected single character string")
        }
        else -> throw SerializationException("Expected string, got ${value::class.simpleName}")
    }
    
    override fun decodeDouble(): Double = when (value) {
        is TomlValue.Float -> value.value
        is TomlValue.Integer -> value.value.toDouble()
        else -> throw SerializationException("Expected float or integer, got ${value::class.simpleName}")
    }
    
    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        val stringValue = when (value) {
            is TomlValue.String -> value.value
            else -> throw SerializationException("Expected string for enum, got ${value::class.simpleName}")
        }
        
        val index = (0 until enumDescriptor.elementsCount).find { i ->
            enumDescriptor.getElementName(i) == stringValue
        }
        
        return index ?: throw SerializationException("Unknown enum value: $stringValue")
    }
    
    override fun decodeFloat(): Float = when (value) {
        is TomlValue.Float -> value.value.toFloat()
        is TomlValue.Integer -> value.value.toFloat()
        else -> throw SerializationException("Expected float or integer, got ${value::class.simpleName}")
    }
    
    override fun decodeInline(descriptor: SerialDescriptor): Decoder = this
    
    override fun decodeInt(): Int = when (value) {
        is TomlValue.Integer -> value.value.toInt()
        else -> throw SerializationException("Expected integer, got ${value::class.simpleName}")
    }
    
    override fun decodeLong(): Long = when (value) {
        is TomlValue.Integer -> value.value
        else -> throw SerializationException("Expected integer, got ${value::class.simpleName}")
    }
    
    override fun decodeNotNullMark(): Boolean = true
    
    override fun decodeNull(): Nothing? = null
    
    override fun decodeShort(): Short = when (value) {
        is TomlValue.Integer -> value.value.toShort()
        else -> throw SerializationException("Expected integer, got ${value::class.simpleName}")
    }
    
    override fun decodeString(): String = when (value) {
        is TomlValue.String -> value.value
        else -> throw SerializationException("Expected string, got ${value::class.simpleName}")
    }
}

/**
 * Decoder for TOML arrays/lists.
 */
@OptIn(ExperimentalSerializationApi::class)
internal class TomlListDecoder(private val values: List<TomlValue>) : Decoder {
    
    override val serializersModule: SerializersModule get() = Toml.serializersModule
    
    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        return TomlListCompositeDecoder(values)
    }
    
    override fun decodeBoolean(): Boolean = throw SerializationException("Expected composite structure")
    override fun decodeByte(): Byte = throw SerializationException("Expected composite structure")
    override fun decodeChar(): Char = throw SerializationException("Expected composite structure")
    override fun decodeDouble(): Double = throw SerializationException("Expected composite structure")
    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = throw SerializationException("Expected composite structure")
    override fun decodeFloat(): Float = throw SerializationException("Expected composite structure")
    override fun decodeInline(descriptor: SerialDescriptor): Decoder = this
    override fun decodeInt(): Int = throw SerializationException("Expected composite structure")
    override fun decodeLong(): Long = throw SerializationException("Expected composite structure")
    override fun decodeNotNullMark(): Boolean = true
    override fun decodeNull(): Nothing? = null
    override fun decodeShort(): Short = throw SerializationException("Expected composite structure")
    override fun decodeString(): String = throw SerializationException("Expected composite structure")
}

/**
 * Composite decoder for TOML arrays/lists.
 */
@OptIn(ExperimentalSerializationApi::class)
internal class TomlListCompositeDecoder(private val values: List<TomlValue>) : CompositeDecoder {
    
    private var currentIndex = 0
    
    override val serializersModule: SerializersModule get() = Toml.serializersModule
    
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        return if (currentIndex < values.size) currentIndex++ else CompositeDecoder.DECODE_DONE
    }
    
    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean {
        val value = values[index]
        return when (value) {
            is TomlValue.Boolean -> value.value
            else -> throw SerializationException("Expected boolean at index $index, got ${value::class.simpleName}")
        }
    }
    
    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte {
        val value = values[index]
        return when (value) {
            is TomlValue.Integer -> value.value.toByte()
            else -> throw SerializationException("Expected integer at index $index, got ${value::class.simpleName}")
        }
    }
    
    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char {
        val value = values[index]
        return when (value) {
            is TomlValue.String -> {
                if (value.value.length == 1) value.value[0]
                else throw SerializationException("Expected single character string at index $index")
            }
            else -> throw SerializationException("Expected string at index $index, got ${value::class.simpleName}")
        }
    }
    
    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double {
        val value = values[index]
        return when (value) {
            is TomlValue.Float -> value.value
            is TomlValue.Integer -> value.value.toDouble()
            else -> throw SerializationException("Expected float or integer at index $index, got ${value::class.simpleName}")
        }
    }
    
    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float {
        val value = values[index]
        return when (value) {
            is TomlValue.Float -> value.value.toFloat()
            is TomlValue.Integer -> value.value.toFloat()
            else -> throw SerializationException("Expected float or integer at index $index, got ${value::class.simpleName}")
        }
    }
    
    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
        return TomlPrimitiveDecoder(values[index])
    }
    
    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int {
        val value = values[index]
        return when (value) {
            is TomlValue.Integer -> value.value.toInt()
            else -> throw SerializationException("Expected integer at index $index, got ${value::class.simpleName}")
        }
    }
    
    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long {
        val value = values[index]
        return when (value) {
            is TomlValue.Integer -> value.value
            else -> throw SerializationException("Expected integer at index $index, got ${value::class.simpleName}")
        }
    }
    
    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short {
        val value = values[index]
        return when (value) {
            is TomlValue.Integer -> value.value.toShort()
            else -> throw SerializationException("Expected integer at index $index, got ${value::class.simpleName}")
        }
    }
    
    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String {
        val value = values[index]
        return when (value) {
            is TomlValue.String -> value.value
            else -> throw SerializationException("Expected string at index $index, got ${value::class.simpleName}")
        }
    }
    
    override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T?>,
        previousValue: T?
    ): T? {
        return decodeSerializableElement(descriptor, index, deserializer as DeserializationStrategy<T>)
    }
    
    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T {
        val value = values[index]
        
        return when {
            // Handle objects in arrays (inline tables)
            deserializer.descriptor.kind == StructureKind.CLASS && value is TomlValue.InlineTable -> {
                // Convert inline table to TomlTable
                val tomlTable = TomlTable()
                for ((k, v) in value.value) {
                    tomlTable.setValue(k, v)
                }
                val objectDecoder = TomlDecoder(tomlTable, "")
                objectDecoder.decodeSerializableValue(deserializer)
            }
            // Handle primitive values
            else -> {
                val primitiveDecoder = TomlPrimitiveDecoder(value)
                primitiveDecoder.decodeSerializableValue(deserializer)
            }
        }
    }
    
    override fun endStructure(descriptor: SerialDescriptor) {
        // Nothing to do
    }
}