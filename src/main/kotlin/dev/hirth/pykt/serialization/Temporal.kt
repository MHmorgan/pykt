package dev.hirth.pykt.serialization

import dev.hirth.pykt.time.parseTime
import dev.hirth.pykt.time.toIso8601
import dev.hirth.pykt.time.toRfc1123
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Serializer for [LocalDate] which uses ISO-8601 format for serialization.
 *
 * ISO 8601 format: `2024-07-16`
 */
object DateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(
            "java.time.LocalDate",
            PrimitiveKind.STRING
        )

    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.toString())
    }
}

/**
 * Serializer for [LocalDateTime] which uses ISO 8601 format for serialization.
 * When deserializing, it handles both local and offset date-times.
 *
 * ISO 8601 format: `2024-07-16T09:38:41.694763+02:00`
 */
object DateTimeIso8601Serializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(
            "java.time.LocalDateTime",
            PrimitiveKind.STRING
        )

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return decoder.decodeString().parseTime()
    }

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.toIso8601())
    }
}

/**
 * Serializer for [LocalDateTime] which uses RFC 1123 format for serialization.
 *
 * RFC 1123 format: `Tue, 16 Jul 2024 07:38:41 GMT`
 */
object DateTimeRfc1123Serializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(
            "java.time.LocalDateTime",
            PrimitiveKind.STRING
        )

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return decoder.decodeString().parseTime()
    }

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.toRfc1123())
    }
}

/**
 * Serializer for [Instant] which uses default string representation for serialization.
 */
object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(
            "java.time.Instant",
            PrimitiveKind.STRING
        )

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }
}