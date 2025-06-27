package dev.hirth.pykt.time

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException


private val localOffset = OffsetDateTime.now().offset
private val isRfc1123 = Regex("""^\w{3}, \d+ \w+ \d+ [0-9:]+ GMT""")::matches

/**
 * Parse this string to a [LocalDateTime], handling local and offset
 * date-times, and HTTP-dates (RFC 1123).
 *
 * Throws [DateTimeParseException] if the string is not a valid date-time.
 */
fun String.parseTime(): LocalDateTime {
    return if (isRfc1123(this)) {
        DateTimeFormatter.RFC_1123_DATE_TIME
            .parse(this, OffsetDateTime::from)
            .toCorrectedLocalDateTime()
    } else {
        val t = DateTimeFormatter.ISO_DATE_TIME
            .parseBest(this, OffsetDateTime::from, LocalDateTime::from)
        when (t) {
            is OffsetDateTime -> t.toCorrectedLocalDateTime()
            is LocalDateTime -> t
            else -> error("Unreachable")
        }
    }
}

/**
 * Convert a [LocalDateTime] to an ISO 8601 formatted string:
 * `2024-07-16T09:38:41.694763+02:00`.
 */
fun LocalDateTime.toIso8601(): String {
    return DateTimeFormatter.ISO_DATE_TIME
        .format(toOffsetDateTime())
}

/**
 * Convert a [LocalDateTime] to an RFC 1123 formatted string:
 * `Tue, 16 Jul 2024 07:38:41 GMT`.
 *
 * The time zone is always GMT.
 */
fun LocalDateTime.toRfc1123(): String {
    val gmt = toOffsetDateTime()
        .atZoneSameInstant(ZoneId.of("GMT"))
    return DateTimeFormatter.RFC_1123_DATE_TIME.format(gmt)
}

/**
 * Convert a [LocalDateTime] to a timestamp with time zone, using the
 * current time zone.
 */
fun LocalDateTime.toOffsetDateTime(): OffsetDateTime = atOffset(localOffset)!!

/**
 * Convert a [OffsetDateTime] to a [LocalDateTime], using the current time
 * zone to correct for the offset.
 */
fun OffsetDateTime.toCorrectedLocalDateTime(): LocalDateTime {
    return withOffsetSameInstant(localOffset)
        .toLocalDateTime()
}
