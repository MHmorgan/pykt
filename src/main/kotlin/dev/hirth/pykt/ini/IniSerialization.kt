package dev.hirth.pykt.ini

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.serializer

/**
 * Main INI serialization object providing kotlinx.serialization support.
 */
object Ini {
    
    /**
     * Deserialize an INI string to a data class.
     */
    inline fun <reified T> decodeFromString(string: String): T {
        return decodeFromString(serializer(), string)
    }
    
    /**
     * Deserialize an INI string to a data class using an explicit deserializer.
     */
    fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        val iniFile = string.parseIni()
        val decoder = IniDecoder(iniFile)
        return deserializer.deserialize(decoder)
    }
}

/**
 * Decoder implementation for INI format.
 */
@OptIn(ExperimentalSerializationApi::class)
private class IniDecoder(
    private val iniFile: IniFile
) : Decoder {
    
    override val serializersModule = kotlinx.serialization.modules.EmptySerializersModule()
    
    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        return IniCompositeDecoder(iniFile, descriptor)
    }
    
    override fun decodeBoolean(): Boolean = throw SerializationException("Primitive decoding not supported at root level")
    override fun decodeByte(): Byte = throw SerializationException("Primitive decoding not supported at root level")
    override fun decodeChar(): Char = throw SerializationException("Primitive decoding not supported at root level")
    override fun decodeDouble(): Double = throw SerializationException("Primitive decoding not supported at root level")
    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = throw SerializationException("Primitive decoding not supported at root level")
    override fun decodeFloat(): Float = throw SerializationException("Primitive decoding not supported at root level")
    override fun decodeInline(inlineDescriptor: SerialDescriptor): Decoder = this
    override fun decodeInt(): Int = throw SerializationException("Primitive decoding not supported at root level")
    override fun decodeLong(): Long = throw SerializationException("Primitive decoding not supported at root level")
    override fun decodeNotNullMark(): Boolean = true
    override fun decodeNull(): Nothing? = null
    override fun decodeShort(): Short = throw SerializationException("Primitive decoding not supported at root level")
    override fun decodeString(): String = throw SerializationException("Primitive decoding not supported at root level")
}

/**
 * Composite decoder for INI structures.
 */
@OptIn(ExperimentalSerializationApi::class)
private class IniCompositeDecoder(
    private val iniFile: IniFile,
    private val descriptor: SerialDescriptor
) : CompositeDecoder {
    
    private var elementIndex = 0
    
    override val serializersModule = kotlinx.serialization.modules.EmptySerializersModule()
    
    override fun decodeSequentially(): Boolean = true
    
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        return if (elementIndex < descriptor.elementsCount) {
            elementIndex++
        } else {
            CompositeDecoder.DECODE_DONE
        }
    }
    
    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean {
        val value = getElementValue(descriptor, index)
        return when (value.lowercase()) {
            "true", "1", "yes", "on" -> true
            "false", "0", "no", "off", "" -> false
            else -> throw SerializationException("Cannot parse boolean from '$value'")
        }
    }
    
    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte {
        return getElementValue(descriptor, index).toByte()
    }
    
    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char {
        val value = getElementValue(descriptor, index)
        return if (value.length == 1) value[0] else throw SerializationException("Cannot parse char from '$value'")
    }
    
    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double {
        return getElementValue(descriptor, index).toDouble()
    }
    
    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float {
        return getElementValue(descriptor, index).toFloat()
    }
    
    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int {
        return getElementValue(descriptor, index).toInt()
    }
    
    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long {
        return getElementValue(descriptor, index).toLong()
    }
    
    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short {
        return getElementValue(descriptor, index).toShort()
    }
    
    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String {
        return getElementValue(descriptor, index)
    }
    
    override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T?>,
        previousValue: T?
    ): T? {
        val value = getElementValue(descriptor, index)
        return if (value.isEmpty()) null else decodeSerializableElement(descriptor, index, deserializer, previousValue)
    }
    
    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T {
        // For nested objects, we would need to handle section-based deserialization
        // For now, just handle primitive types through the element decoders above
        throw SerializationException("Nested objects not yet supported")
    }
    
    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
        return IniDecoder(iniFile)
    }
    
    private fun getElementValue(descriptor: SerialDescriptor, index: Int): String {
        val fieldName = descriptor.getElementName(index)
        
        // First try to get from default section
        val defaultValue = iniFile.get("DEFAULT", fieldName)
        if (defaultValue != null) {
            return defaultValue
        }
        
        // Try to get from the main section (using the class name as section)
        val sectionName = descriptor.serialName.substringAfterLast('.')
        val sectionValue = iniFile.get(sectionName, fieldName)
        if (sectionValue != null) {
            return sectionValue
        }
        
        // Look through all sections for this field
        for (section in iniFile.sections.values) {
            val value = section.get(fieldName)
            if (value != null) {
                return value
            }
        }
        
        // Return empty string if not found (will be handled by nullable decoders)
        return ""
    }
    
    override fun endStructure(descriptor: SerialDescriptor) {
        // Nothing to do
    }
}