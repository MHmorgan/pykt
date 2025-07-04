@file:OptIn(ExperimentalUuidApi::class)

package dev.hirth.pykt

import dev.hirth.pykt.hash.Sha1Hashing
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Extracts and returns the version number of the [Uuid],
 * found in the higher nibble of the 7th byte of the UUID.
 *
 * @return The version number of the UUID as an integer.
 */
fun Uuid.version(): Int {
    val byte = toByteArray()[6]
    return (byte.toInt() ushr 4) and 0x0F
}

/**
 * Determines the variant of the UUID. The variant indicates the layout of the UUID,
 * which can aid in understanding its structure and usage.
 *
 * @return an integer value representing the variant of the UUID.
 *         Common values include:
 *         - 0: Reserved for NCS backward compatibility
 *         - 2: RFC 4122 (Leach-Salz) variant
 *         - Other values may indicate custom or reserved variants
 */
fun Uuid.variant(): Int {
    val byte = toByteArray()[8]
    return (byte.toInt() ushr 6) and 0x03
}

/**
 * Return a short string with the 7 first hex characters of the UUID.
 *
 * @see Uuid.startsWith
 */
fun Uuid.shortString(): String {
    return toHexString().take(7)
}

/**
 * Return `true` if the hex string of this [Uuid] starts
 * with the given [shortString].
 *
 * @see Uuid.shortString
 */
fun Uuid.startsWith(shortString: String): Boolean {
    return toHexString().startsWith(shortString, ignoreCase = true)
}

/**
 * Generates a list of short, unique string representations for each UUID in the iterable.
 * Each string is derived as the shortest prefix of the hexadecimal representation of the UUID
 * that is not already present in the output list.
 *
 * @return A list of short unique strings corresponding to the UUIDs.
 */
fun Iterable<Uuid>.shortStrings(): List<String> {
    val seen = mutableSetOf<String>()
    return mapNotNull {
        val uuid = it.toHexString()
        for (i in 7..<uuid.length) {
            val s = uuid.take(i)
            if (seen.add(s)) return@mapNotNull s
        }
        null
    }
}

private fun ByteArray.setVersion(version: Int) {
    val mask = version shl 4
    val byte = this[6].toInt()
    this[6] = ((byte and 0x0F) or mask).toByte()
}

private fun ByteArray.setVariant() {
    val byte = this[8].toInt()
    this[8] = ((byte and 0x3F) or 0x80).toByte()
}

/**
 * Generates a random UUID (Universally Unique Identifier)
 * based on the RFC 4122, version 4 standard.
 *
 * This is just a wrapper of [Uuid.random] to provide a similar
 * factory function as provided for the other UUID versions.
 *
 * @return A random UUID as defined by the version 4 specification.
 */
fun uuidV4() = Uuid.random()

/**
 * Creates a UUID version 5 (name-based using SHA-1).
 * 
 * UUID v5 is generated by hashing a namespace UUID and a name using SHA-1,
 * then setting the version and variant bits appropriately.
 *
 * @param namespace The namespace UUID to use as a base
 * @param name The name to hash within the namespace
 * @return A new UUID version 5
 */
fun uuidV5(namespace: Uuid, name: String): Uuid {
    val digest = MessageDigest.getInstance("SHA-1")
    digest.update(namespace.toByteArray())
    digest.update(name.toByteArray(Charsets.UTF_8))
    val hash = digest.digest()

    // Take first 16 bytes of the 20-byte SHA-1 hash
    val bytes = hash.copyOf(16)
    bytes.setVersion(5)
    bytes.setVariant()

    return Uuid.fromByteArray(bytes)
}

/**
 * Creates a UUID version 7 (time-ordered with Unix timestamp).
 * 
 * UUID v7 contains a 48-bit Unix timestamp in milliseconds followed by
 * 12 bits of sub-millisecond precision and 62 bits of random data.
 *
 * @param timestamp Optional Unix timestamp in milliseconds. If null, uses current time.
 * @return A new UUID version 7
 */
fun uuidV7(timestamp: Long? = null): Uuid {
    val ts = timestamp ?: System.currentTimeMillis()
    val random = SecureRandom()

    val bytes = ByteArray(16)
    random.nextBytes(bytes)

    // First 48 bits: Unix timestamp in milliseconds
    val buf = ByteBuffer.wrap(bytes)
    buf.putShort(((ts ushr 32) and 0xFFFF).toShort()) // Upper 16 bits
    buf.putInt((ts and 0xFFFFFFFF).toInt()) // Lower 32 bits

    bytes.setVersion(7)
    bytes.setVariant()

    return Uuid.fromByteArray(bytes)
}

/**
 * Creates a UUID version 8 (custom/vendor-specific format).
 * 
 * UUID v8 allows for custom formats while maintaining the standard
 * version and variant bit positions. This implementation accepts
 * custom data for the first 62 bits, with version and variant set appropriately.
 *
 * @param customData The custom data bytes (must be exactly 16 bytes)
 * @return A new UUID version 8
 * @throws IllegalArgumentException if customData is not exactly 16 bytes
 */
fun uuidV8(customData: ByteArray): Uuid {
    require(customData.size == 16) { "Custom data must be exactly 16 bytes" }

    val bytes = customData.copyOf()
    bytes.setVersion(8)
    bytes.setVariant()

    return Uuid.fromByteArray(bytes)
}

fun uuidV8(block: UuidV8Builder.() -> Unit): Uuid {
    val builder = UuidV8Builder()
    builder.block()
    return Uuid.fromByteArray(builder.build())
}

@DslMarker
annotation class UuidBuilder

@UuidBuilder
class UuidV8Builder {
    private val bytes = ByteArray(16)

    val secureRandom by lazy { SecureRandom() }

    val random by lazy { Random(System.currentTimeMillis()) }

    /**
     * Fill all the bytes with random values, using [SecureRandom].
     */
    fun randomSecure() {
        secureRandom.nextBytes(bytes)
    }

    /**
     * Fill all the bytes with random values, using [Random].
     */
    fun random() {
        random.nextBytes(bytes)
    }

    /**
     * Fill all the bytes with the
     */
    fun sha1(block: Sha1Hashing.() -> Unit) {
        val hash = Sha1Hashing.hashToBytes(block)
        hash.copyInto(bytes, endIndex = bytes.size)
    }

    /**
     * Use a [ByteBuffer] to manipulate the UUID bytes.
     */
    @UuidBuilder
    fun buffer(block: ByteBuffer.() -> Unit) {
        ByteBuffer.wrap(bytes).block()
    }

    internal fun build(): ByteArray {
        bytes.setVersion(8)
        bytes.setVariant()
        return bytes.copyOf()
    }
}
