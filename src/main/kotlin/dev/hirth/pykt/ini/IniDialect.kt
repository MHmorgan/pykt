package dev.hirth.pykt.ini

/**
 * Defines the properties of an INI dialect - how INI files should be parsed.
 *
 * This allows configuring various aspects of INI file parsing such as
 * case sensitivity, whitespace handling, comment characters, etc.
 */
data class IniDialect(
    /** Characters that can be used as key-value separators. */
    val separators: Set<Char> = setOf('=', ':'),

    /** Characters that start comment lines. */
    val commentPrefixes: Set<Char> = setOf('#', ';'),

    /** Whether section and key names are case sensitive. */
    val caseSensitive: Boolean = false,

    /** Whether to trim whitespace from keys and values. */
    val trimWhitespace: Boolean = true,

    /** Whether to allow empty values (key with no value after separator). */
    val allowEmptyValues: Boolean = true,

    /** Whether to allow keys without values (no separator). */
    val allowKeysWithoutValues: Boolean = false,

    /** Whether to allow multiline values using line continuation. */
    val allowMultilineValues: Boolean = true,

    /** String used for line continuation (usually leading whitespace). */
    val continuationPrefix: String = " ",

    /** Whether to allow interpolation of values using ${var} syntax. */
    val allowInterpolation: Boolean = false,

    /** Default section name for key-value pairs not under any section. */
    val defaultSectionName: String = "DEFAULT",

    /** Whether to have strict parsing (raise exceptions on malformed INI). */
    val strict: Boolean = true
) {
    companion object {
        /** Standard INI dialect similar to Python's configparser. */
        val DEFAULT = IniDialect()

        /** Permissive dialect that's more forgiving with parsing errors. */
        val PERMISSIVE = IniDialect(
            allowKeysWithoutValues = true,
            strict = false
        )

        /** Java Properties-style dialect. */
        val PROPERTIES = IniDialect(
            separators = setOf('=', ':'),
            commentPrefixes = setOf('#', '!'),
            allowKeysWithoutValues = false,
            allowMultilineValues = true,
            continuationPrefix = " ",
            defaultSectionName = "",
            strict = true
        )
    }
}