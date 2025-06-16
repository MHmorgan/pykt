package dev.hirth.pykt.toml

/**
 * Represents a TOML value according to TOML v1.0.0 specification.
 */
sealed interface TomlValue {

    /**
     * String value
     */
    data class String(val value: kotlin.String) : TomlValue

    /**
     * Integer value (64-bit signed)
     */
    data class Integer(val value: Long) : TomlValue

    /**
     * Float value (64-bit IEEE 754 binary floating-point)
     */
    data class Float(val value: Double) : TomlValue

    /**
     * Boolean value
     */
    data class Boolean(val value: kotlin.Boolean) : TomlValue

    /**
     * Offset Date-Time value (RFC 3339)
     */
    data class OffsetDateTime(val value: java.time.OffsetDateTime) : TomlValue

    /**
     * Local Date-Time value
     */
    data class LocalDateTime(val value: java.time.LocalDateTime) : TomlValue

    /**
     * Local Date value
     */
    data class LocalDate(val value: java.time.LocalDate) : TomlValue

    /**
     * Local Time value
     */
    data class LocalTime(val value: java.time.LocalTime) : TomlValue

    /**
     * Array value
     */
    data class Array(val value: List<TomlValue>) : TomlValue

    /**
     * Inline Table value
     */
    data class InlineTable(val value: Map<kotlin.String, TomlValue>) : TomlValue
}

/**
 * Represents a TOML table (section).
 */
data class TomlTable(
    val values: MutableMap<String, TomlValue> = mutableMapOf(),
    val tables: MutableMap<String, TomlTable> = mutableMapOf(),
    val arrayTables: MutableMap<String, MutableList<TomlTable>> = mutableMapOf()
) {

    /**
     * Gets a value by key path (e.g., "a.b.c")
     */
    fun getValue(key: String): TomlValue? {
        val parts = key.split('.')
        if (parts.size == 1) {
            return values[key]
        }

        var currentTable = this
        for (i in 0 until parts.size - 1) {
            currentTable = currentTable.tables[parts[i]] ?: return null
        }
        return currentTable.values[parts.last()]
    }

    /**
     * Gets a table by key path
     */
    fun getTable(key: String): TomlTable? {
        val parts = key.split('.')
        if (parts.size == 1) {
            return tables[key]
        }

        var currentTable = this
        for (part in parts) {
            currentTable = currentTable.tables[part] ?: return null
        }
        return currentTable
    }

    /**
     * Sets a value by key path
     */
    fun setValue(key: String, value: TomlValue) {
        val parts = key.split('.')
        if (parts.size == 1) {
            values[key] = value
            return
        }

        var currentTable = this
        for (i in 0 until parts.size - 1) {
            currentTable = currentTable.tables.getOrPut(parts[i]) { TomlTable() }
        }
        currentTable.values[parts.last()] = value
    }

    /**
     * Creates or gets a nested table by key path
     */
    fun getOrCreateTable(key: String): TomlTable {
        val parts = key.split('.')
        var currentTable = this

        for (part in parts) {
            currentTable = currentTable.tables.getOrPut(part) { TomlTable() }
        }
        return currentTable
    }
}

/**
 * Represents a TOML document.
 */
data class TomlDocument(val rootTable: TomlTable = TomlTable()) {

    /**
     * Gets a value by key path
     */
    fun getValue(key: String): TomlValue? = rootTable.getValue(key)

    /**
     * Gets a table by key path
     */
    fun getTable(key: String): TomlTable? = rootTable.getTable(key)

    /**
     * Sets a value by key path
     */
    fun setValue(key: String, value: TomlValue) = rootTable.setValue(key, value)

    /**
     * Creates or gets a nested table by key path
     */
    fun getOrCreateTable(key: String): TomlTable = rootTable.getOrCreateTable(key)
}