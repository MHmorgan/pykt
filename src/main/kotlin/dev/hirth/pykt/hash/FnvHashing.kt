package dev.hirth.pykt.hash

import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Alias for simple 32-bit hash values.
 */
typealias Hash32 = Int

/**
 * Implementation of the [Fowler–Noll–Vo](https://en.wikipedia.org/wiki/Fowler%E2%80%93Noll%E2%80%93Vo_hash_function)
 * hash function (specifically 32-bit FNV-1a).
 *
 * This is a non-cryptographic hash function, which is used
 * to generate an [Int] hash value.
 */
class FnvHashing {
    private var hash = 0x811c9dc5U
    private val prime = 0x01000193U

    fun update(b: ByteArray) {
        for (i in b.indices) {
            hash = hash xor b[i].toUInt()
            hash *= prime
        }
    }

    fun update(b: Byte) {
        hash = hash xor b.toUInt()
        hash *= prime
    }

    /**
     * Update the hash with the string [s].
     */
    fun update(s: String) = update(s.toByteArray())


    /**
     * Update the hash with the given enum entry [e]. This uses [Enum.name].
     */
    fun update(e: Enum<*>) {
        val clazz = e::class.qualifiedName
        if (clazz != null) {
            // Use the combination of class name and ordinal to ensure uniqueness
            // even if the enum names are not unique.
            update(clazz.toByteArray())
            update(e.ordinal)
        } else {
            // Fallback to just the name if the class name is not available,
            // since the enum name is probably more unique than the ordinal.
            update(e.name.toByteArray())
        }
    }

    /**
     * Update the hash with the given number [n].
     */
    fun update(n: Short) {
        val b = ByteArray(2)
        b[0] = (n.toInt() shr 8).toByte()
        b[1] = n.toByte()
        update(b)
    }

    /**
     * Update the hash with the given number [n].
     */
    fun update(n: Int) {
        val b = ByteArray(4)
        b[0] = (n shr 24).toByte()
        b[1] = (n shr 16).toByte()
        b[2] = (n shr 8).toByte()
        b[3] = n.toByte()
        update(b)
    }

    /**
     * Update the hash with the given number [n].
     */
    fun update(n: Long) {
        val b = ByteArray(8)
        b[0] = (n shr 56).toByte()
        b[1] = (n shr 48).toByte()
        b[2] = (n shr 40).toByte()
        b[3] = (n shr 32).toByte()
        b[4] = (n shr 24).toByte()
        b[5] = (n shr 16).toByte()
        b[6] = (n shr 8).toByte()
        b[7] = n.toByte()
        update(b)
    }

    /**
     * Update the hash with the given character [c].
     */
    fun update(c: Char) = update(c.code)

    /**
     * Update the hash with the given number [n].
     */
    fun update(n: Float) = update(n.toRawBits())

    /**
     * Update the hash with the given number [n].
     */
    fun update(n: Double) = update(n.toRawBits())

    /**
     * Update the hash with the given boolean [b].
     */
    fun update(b: Boolean) = update(if (b) 1 else 0)

    @OptIn(ExperimentalUuidApi::class)
    fun update(uuid: Uuid) = update(uuid.toByteArray())

    /**
     * Update the hash with the given number [n].
     */
    fun update(n: BigDecimal) {
        // Normalize the BigDecimal to a canonical form to avoid different
        // scales causing different hash values.
        val norm = n.stripTrailingZeros()
        update(norm.toString())
    }

    /**
     * Update the hash with the given number [n].
     */
    fun update(n: BigInteger) = update(n.toByteArray())

    /**
     * Update the hash with the given date time.
     */
    fun update(t: LocalDateTime) = update(t.hashCode())

    /**
     * Update the hash with the given date time.
     */
    fun update(t: OffsetDateTime) = update(t.hashCode())

    /**
     * Update the hash with the given date time.
     */
    fun update(t: ZonedDateTime) = update(t.hashCode())

    /**
     * Update the hash with the given date.
     */
    fun update(d: LocalDate) = update(d.hashCode())

    companion object {
        /**
         * Builds a 32-bit FNV-1a hash value.
         */
        fun hash(f: FnvHashing.() -> Unit): Hash32 {
            return FnvHashing()
                .apply(f)
                .hash
                .toInt()
        }
    }
}
