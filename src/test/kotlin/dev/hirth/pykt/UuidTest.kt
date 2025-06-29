package dev.hirth.pykt

import kotlinx.coroutines.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.reflect.Array.setInt
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class UuidTest {

    @Test
    fun `Uuid version`() {
        val versions = mapOf(
            1 to "11111111-0000-1fff-0000-111111111111",
            2 to "22222222-0000-2000-0000-222222222222",
            3 to "33333333-ffff-3000-0000-333333333333",
            4 to "44444444-0000-4000-0000-444444444444",
            5 to "55555555-0000-5fff-0000-555555555555",
        )

        for ((ver, str) in versions) {
            val uuid = Uuid.parse(str)
            assertThat(uuid.version()).isEqualTo(ver)
        }
    }

    @Nested
    inner class UuidV5Tests {

        @Test
        fun `uuidV5 creates version 5 UUID`() {
            val namespace = Uuid.parse("6ba7b810-9dad-11d1-80b4-00c04fd430c8") // DNS namespace
            val name = "example.com"

            val uuid = uuidV5(namespace, name)

            assertThat(uuid.version()).isEqualTo(5)
        }

        @Test
        fun `uuidV5 is deterministic`() {
            val namespace = Uuid.parse("6ba7b810-9dad-11d1-80b4-00c04fd430c8")
            val name = "test.example.com"

            val uuid1 = uuidV5(namespace, name)
            val uuid2 = uuidV5(namespace, name)

            assertThat(uuid1).isEqualTo(uuid2)
            assertThat(uuid1).isEqualTo(uuid2)
        }

        @Test
        fun `uuidV5 produces different UUIDs for different names`() {
            val namespace = Uuid.parse("6ba7b810-9dad-11d1-80b4-00c04fd430c8")

            val uuid1 = uuidV5(namespace, "name1")
            val uuid2 = uuidV5(namespace, "name2")

            assertThat(uuid1).isNotEqualTo(uuid2)
        }

        @Test
        fun `uuidV5 produces different UUIDs for different namespaces`() {
            val namespace1 = Uuid.parse("6ba7b810-9dad-11d1-80b4-00c04fd430c8")
            val namespace2 = Uuid.parse("6ba7b811-9dad-11d1-80b4-00c04fd430c8")
            val name = "same-name"

            val uuid1 = uuidV5(namespace1, name)
            val uuid2 = uuidV5(namespace2, name)

            assertThat(uuid1).isNotEqualTo(uuid2)
        }
    }

    @Nested
    inner class UuidV7Tests {

        @Test
        fun `uuidV7 creates version 7 UUID`() {
            val uuid = uuidV7()

            assertThat(uuid.version()).isEqualTo(7)
        }

        @Test
        fun `uuidV7 with timestamp creates version 7 UUID`() {
            val timestamp = 1640995200000L // 2022-01-01 00:00:00 UTC
            val uuid = uuidV7(timestamp)

            assertThat(uuid.version()).isEqualTo(7)
        }

        @Test
        fun `uuidV7 produces different UUIDs on subsequent calls`() {
            val uuid1 = uuidV7()
            val uuid2 = uuidV7()

            // Should be different due to random components
            assertThat(uuid1).isNotEqualTo(uuid2)
        }

        @Test
        fun `uuidV7 with same timestamp produces different UUIDs`() {
            val timestamp = 1640995200000L
            val uuid1 = uuidV7(timestamp)
            val uuid2 = uuidV7(timestamp)

            // Should be different due to random components
            assertThat(uuid1).isNotEqualTo(uuid2)
        }

        @Test
        fun `uuidV7 contains timestamp information`() {
            val timestamp = 1640995200000L // 2022-01-01 00:00:00 UTC
            val uuid = uuidV7(timestamp)
            val bytes = uuid.toByteArray()

            // Extract timestamp from first 6 bytes
            val extractedTimestamp = ((bytes[0].toLong() and 0xFF) shl 40) or
                                   ((bytes[1].toLong() and 0xFF) shl 32) or
                                   ((bytes[2].toLong() and 0xFF) shl 24) or
                                   ((bytes[3].toLong() and 0xFF) shl 16) or
                                   ((bytes[4].toLong() and 0xFF) shl 8) or
                                   (bytes[5].toLong() and 0xFF)

            assertThat(extractedTimestamp).isEqualTo(timestamp)
        }
    }

    @Nested
    inner class UuidV8Tests {

        @Test
        fun `uuidV8 creates version 8 UUID from byte array`() {
            val customData = ByteArray(16) { it.toByte() }
            val uuid = uuidV8(customData)

            assertThat(uuid.version()).isEqualTo(8)
        }

        @Test
        fun `uuidV8 creates version 8 UUID from builder`() {
            val uuid = uuidV8 {
                buffer {
                    putInt(0x12345678)
                    putShort(0x1234.toShort())
                    putShort(0x5678.toShort())
                    putLong(0x123456789ABCDEF0L)
                }
            }

            assertThat(uuid.version()).isEqualTo(8)
        }

        @Test
        fun `uuidV8 throws exception for invalid byte array size`() {
            run {
                val tooSmall = ByteArray(15)
                assertThrows<IllegalArgumentException> {
                    uuidV8(tooSmall)
                }
            }

            run {
                val tooLarge = ByteArray(17)
                assertThrows<IllegalArgumentException> {
                    uuidV8(tooLarge)
                }
            }
        }

        @Test
        fun `uuidV8 preserves custom data while setting version and variant`() {
            val customData = ByteArray(16) { (it + 1).toByte() }
            val uuid = uuidV8(customData)
            val resultBytes = uuid.toByteArray()

            // Check that version bits are set correctly (upper 4 bits of byte 6)
            assertThat((resultBytes[6].toInt() ushr 4) and 0x0F).isEqualTo(8)

            // Check that variant bits are set correctly (upper 2 bits of byte 8)
            assertThat((resultBytes[8].toInt() ushr 6) and 0x03).isEqualTo(2) // 10 in binary

            // Check that other bytes are preserved (except for the modified bits)
            for (i in 0..15) {
                when (i) {
                    6 -> {
                        // Lower 4 bits should be preserved
                        assertThat(resultBytes[i].toInt() and 0x0F).isEqualTo(customData[i].toInt() and 0x0F)
                    }
                    8 -> {
                        // Lower 6 bits should be preserved
                        assertThat(resultBytes[i].toInt() and 0x3F).isEqualTo(customData[i].toInt() and 0x3F)
                    }
                    else -> {
                        assertThat(resultBytes[i]).isEqualTo(customData[i])
                    }
                }
            }
        }

        @Test
        fun `uuidV8 from builder creates deterministic UUID`() {
            val uuid1 = uuidV8 {
                buffer {
                    putInt(0x12345678)
                    putShort(0x1234.toShort())
                    putShort(0x5678.toShort())
                    putLong(0x123456789ABCDEF0L)
                }
            }
            val uuid2 = uuidV8 {
                buffer {
                    putInt(0x12345678)
                    putShort(0x1234.toShort())
                    putShort(0x5678.toShort())
                    putLong(0x123456789ABCDEF0L)
                }
            }

            assertThat(uuid1).isEqualTo(uuid2)
            assertThat(uuid1.toString()).isEqualTo(uuid2.toString())
        }
    }
}
