package dev.hirth.pykt.hash

import java.math.BigDecimal
import java.math.BigInteger
import java.security.MessageDigest
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * A builder for SHA-1 hashes.
 */
class Sha1Hashing {
    private val digest = MessageDigest.getInstance("SHA-1")

    /**
     * Update the hash with the bytes in [b].
     */
    fun update(b: ByteArray) = digest.update(b)

    /**
     * Update the hash with the byte [n].
     */
    fun update(n: Byte) = digest.update(n)

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
        digest.update(b)
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
        digest.update(b)
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
    fun update(b: Boolean) = digest.update(if (b) 1 else 0)

    @OptIn(ExperimentalUuidApi::class)
    fun update(uuid: Uuid) = digest.update(uuid.toByteArray())

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
         * Builds a hash key which is intended to be used as a database key for an
         * entity.
         */
        fun hashToBytes(f: Sha1Hashing.() -> Unit): ByteArray {
            return Sha1Hashing()
                .apply(f)
                .digest
                .digest()
        }

        /**
         * Builds a hash key which is intended to be used as a database key for an
         * entity, and returns it as a hexadecimal string.
         */
        fun hashToHex(f: Sha1Hashing.() -> Unit): String {
            return hashToBytes(f).joinToString("") { "%02x".format(it) }
        }
    }
}