package dev.hirth.pykt.ini

/**
 * Represents a section in an INI file with its name and key-value pairs.
 */
data class IniSection(
    val name: String,
    private val _entries: MutableMap<String, String> = mutableMapOf()
) {
    /** Get all key-value pairs in this section. */
    val entries: Map<String, String> get() = _entries.toMap()

    /** Get all keys in this section. */
    val keys: Set<String> get() = _entries.keys

    /** Check if a key exists in this section. */
    fun containsKey(key: String): Boolean = _entries.containsKey(key)

    /** Get value for a key, or null if not found. */
    fun get(key: String): String? = _entries[key]

    /** Get value for a key, or default if not found. */
    fun get(key: String, defaultValue: String): String = _entries[key] ?: defaultValue

    /** Set a key-value pair in this section. */
    fun set(key: String, value: String) {
        _entries[key] = value
    }

    /** Remove a key from this section. */
    fun remove(key: String): String? = _entries.remove(key)

    /** Check if this section is empty. */
    fun isEmpty(): Boolean = _entries.isEmpty()

    /** Get number of entries in this section. */
    fun size(): Int = _entries.size
}