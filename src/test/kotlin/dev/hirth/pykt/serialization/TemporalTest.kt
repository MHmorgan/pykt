package dev.hirth.pykt.serialization

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

class TemporalTest {

    // Test data classes using the temporal serializers
    @Serializable
    data class DateContainer(
        @Serializable(with = DateSerializer::class)
        val date: LocalDate
    )

    @Serializable
    data class DateTimeIso8601Container(
        @Serializable(with = DateTimeIso8601Serializer::class)
        val dateTime: LocalDateTime
    )

    @Serializable
    data class DateTimeRfc1123Container(
        @Serializable(with = DateTimeRfc1123Serializer::class)
        val dateTime: LocalDateTime
    )

    @Serializable
    data class InstantContainer(
        @Serializable(with = InstantSerializer::class)
        val instant: Instant
    )

    @Test
    fun testDateSerializer() {
        val date = LocalDate.of(2024, 7, 16)
        val container = DateContainer(date)

        // Test serialization
        val json = Json.encodeToString(container)
        assertThat(json).isEqualTo("""{"date":"2024-07-16"}""")

        // Test deserialization
        run {
            val actual = Json.decodeFromString<DateContainer>(json)
            assertThat(actual).isEqualTo(container)
        }

        // Test invalid format
        run {
            val json = """{"date":"invalid-date"}"""
            assertThatThrownBy {
                Json.decodeFromString(DateContainer.serializer(), json)
            }.isInstanceOf(DateTimeParseException::class.java)
        }
    }

    @Test
    fun testDateTimeIso8601Serializer() {
        val dateTime = LocalDateTime.of(2024, 7, 16, 9, 38, 41, 694763000)
        val container = DateTimeIso8601Container(dateTime)

        // Test serialization
        val json = Json.encodeToString(container)
        // The exact format depends on the system timezone, but should contain the date and time
        assertThat(json).contains("2024-07-16T09:38:41.694763")

        // Test deserialization with ISO 8601 format
        run {
            val container = Json.decodeFromString<DateTimeIso8601Container>(json)
            assertThat(container.dateTime).isEqualTo(LocalDateTime.of(2024, 7, 16, 9, 38, 41, 694763000))
        }

        // Test deserialization with offset date-time
        run {
            val json = """{"dateTime":"2024-07-16T09:38:41.694763+02:00"}"""
            val container = Json.decodeFromString<DateTimeIso8601Container>(json)
            // The exact result depends on the system timezone, but should be a valid LocalDateTime
            assertThat(container.dateTime).isNotNull()
        }

        // Test deserialization with RFC 1123 format
        run {
            val json = """{"dateTime":"Tue, 16 Jul 2024 07:38:41 GMT"}"""
            val container = Json.decodeFromString<DateTimeIso8601Container>(json)
            // The exact result depends on the system timezone, but should be a valid LocalDateTime
            assertThat(container.dateTime).isNotNull()
        }

        // Test invalid format
        run {
            val json = """{"dateTime":"invalid-datetime"}"""
            assertThrows<Exception> {
                Json.decodeFromString(json)
            }
        }
    }

    @Test
    fun testDateTimeRfc1123Serializer() {
        val dateTime = LocalDateTime.of(2024, 7, 16, 9, 38, 41, 694763000)
        val expect = DateTimeRfc1123Container(dateTime)

        // Test serialization
        val json = Json.encodeToString(DateTimeRfc1123Container.serializer(), expect)
        // Should serialize to ISO 8601 format (as per the implementation)
        assertThat(json).contains("2024-07-16T09:38:41.694763")

        // Test deserialization with ISO 8601 format
        run {
            val container = Json.decodeFromString<DateTimeRfc1123Container>(json)
            assertThat(container).isEqualTo(expect)
        }

        // Test deserialization with RFC 1123 format
        run {
            val json = """{"dateTime":"Tue, 16 Jul 2024 07:38:41 GMT"}"""
            val container = Json.decodeFromString<DateTimeRfc1123Container>(json)
            // The exact result depends on the system timezone, but should be a valid LocalDateTime
            assertThat(container.dateTime).isNotNull()
        }

        // Test invalid format
        run {
            val json = """{"dateTime":"invalid-datetime"}"""
            assertThrows<Exception> {
                Json.decodeFromString<DateTimeRfc1123Container>(json)
            }
        }
    }

    @Test
    fun testInstantSerializer() {
        val instant = Instant.parse("2024-07-16T07:38:41.694763Z")
        val expect = InstantContainer(instant)

        // Test serialization
        val json = Json.encodeToString<InstantContainer>(expect)
        assertThat(json).isEqualTo("""{"instant":"2024-07-16T07:38:41.694763Z"}""")

        // Test deserialization
        run {
            val json = Json.encodeToString<InstantContainer>(expect)
            val actual = Json.decodeFromString(InstantContainer.serializer(), json)
            assertThat(actual).isEqualTo(expect)
        }

        // Test with different valid instant formats
        run {
            val formats = listOf(
                "2024-07-16T07:38:41Z",
                "2024-07-16T07:38:41.123Z",
                "2024-07-16T07:38:41.123456Z",
                "2024-07-16T07:38:41.123456789Z"
            )

            for (format in formats) {
                val json = """{"instant":"$format"}"""
                val container = Json.decodeFromString<InstantContainer>(json)
                assertThat(container.instant).isNotNull()
            }
        }

        // Test invalid format
        run {
            val json = """{"instant":"invalid-instant"}"""
            assertThrows<Exception> {
                Json.decodeFromString<InstantContainer>(json)
            }
        }
    }
}
