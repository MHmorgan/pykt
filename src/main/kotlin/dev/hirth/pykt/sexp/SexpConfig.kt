package dev.hirth.pykt.sexp

import java.io.File
import java.io.InputStream

/**
 * Configuration reader for S-expression based config files.
 *
 * This class provides convenient methods for reading configuration from
 * S-expression files, with support for typed value extraction and validation.
 */
class SexpConfig(private val sexp: Sexp) {

    constructor(input: String) : this(input.parseSexp())
    constructor(file: File) : this(file.parseSexp())
    constructor(inputStream: InputStream) : this(inputStream.parseSexp())

    /**
     * Gets a configuration value by path.
     *
     * @param path Dot-separated path to the configuration value
     * @return The S-expression at the specified path, or null if not found
     */
    fun get(path: String): Sexp? {
        val parts = path.split('.')

        // First try to find from current sexp directly
        val result = getByPath(sexp, parts)
        if (result != null) return result

        // If not found and sexp is a list with a root key, try skipping the root key
        if (sexp is Sexp.List && sexp.elements.isNotEmpty() &&
            sexp.elements[0] is Sexp.Atom && parts[0] == (sexp.elements[0] as Sexp.Atom).value
        ) {
            // Skip the root key and search in the rest
            val remainingParts = parts.drop(1)
            if (remainingParts.isNotEmpty()) {
                val subSexp = Sexp.List(sexp.elements.drop(1))
                return getByPath(subSexp, remainingParts)
            }
        }

        return null
    }

    /**
     * Gets a configuration value by path, throwing an exception if not found.
     *
     * @param path Dot-separated path to the configuration value
     * @return The S-expression at the specified path
     * @throws IllegalArgumentException if the path is not found
     */
    fun getValue(path: String): Sexp {
        return get(path) ?: throw IllegalArgumentException("Configuration path not found: $path")
    }

    /**
     * Gets a string value by path.
     */
    fun getString(path: String): String? {
        return get(path)?.asAtom()
    }

    /**
     * Gets a string value by path, throwing an exception if not found.
     */
    fun getStringValue(path: String): String {
        return getValue(path).atomValue()
    }

    /**
     * Gets an integer value by path.
     */
    fun getInt(path: String): Int? {
        return getString(path)?.toIntOrNull()
    }

    /**
     * Gets an integer value by path, throwing an exception if not found.
     */
    fun getIntValue(path: String): Int {
        return getStringValue(path).toInt()
    }

    /**
     * Gets a long value by path.
     */
    fun getLong(path: String): Long? {
        return getString(path)?.toLongOrNull()
    }

    /**
     * Gets a long value by path, throwing an exception if not found.
     */
    fun getLongValue(path: String): Long {
        return getStringValue(path).toLong()
    }

    /**
     * Gets a double value by path.
     */
    fun getDouble(path: String): Double? {
        return getString(path)?.toDoubleOrNull()
    }

    /**
     * Gets a double value by path, throwing an exception if not found.
     */
    fun getDoubleValue(path: String): Double {
        return getStringValue(path).toDouble()
    }

    /**
     * Gets a boolean value by path.
     */
    fun getBoolean(path: String): Boolean? {
        return getString(path)?.let { value ->
            when (value.lowercase()) {
                "true", "t", "yes", "y", "1" -> true
                "false", "f", "no", "n", "0" -> false
                else -> null
            }
        }
    }

    /**
     * Gets a boolean value by path, throwing an exception if not found.
     */
    fun getBooleanValue(path: String): Boolean {
        return getBoolean(path) ?: throw IllegalArgumentException("Invalid boolean value at path: $path")
    }

    /**
     * Gets a list of S-expressions by path.
     */
    fun getList(path: String): List<Sexp>? {
        return get(path)?.asList()
    }

    /**
     * Gets a list of S-expressions by path, throwing an exception if not found.
     */
    fun getListValue(path: String): List<Sexp> {
        return getValue(path).listElements()
    }

    /**
     * Gets a list of strings by path.
     */
    fun getStringList(path: String): List<String>? {
        return getList(path)?.map { it.atomValue() }
    }

    /**
     * Gets a list of strings by path, throwing an exception if not found.
     */
    fun getStringListValue(path: String): List<String> {
        return getListValue(path).map { it.atomValue() }
    }

    /**
     * Checks if a configuration path exists.
     */
    fun has(path: String): Boolean {
        return get(path) != null
    }

    /**
     * Gets all configuration paths as a flattened map.
     */
    fun toMap(): Map<String, Sexp> {
        val result = mutableMapOf<String, Sexp>()

        // Handle the case where the root is a named configuration section
        if (sexp is Sexp.List && sexp.elements.isNotEmpty() && sexp.elements[0] is Sexp.Atom) {
            val rootKey = (sexp.elements[0] as Sexp.Atom).value
            result[rootKey] = Sexp.List(sexp.elements.drop(1))
            flattenSexp(Sexp.List(sexp.elements.drop(1)), rootKey, result)
        } else {
            flattenSexp(sexp, "", result)
        }

        return result
    }

    private fun getByPath(sexp: Sexp, pathParts: List<String>): Sexp? {
        if (pathParts.isEmpty()) return sexp

        return when (sexp) {
            is Sexp.Atom -> null
            is Sexp.List -> {
                val key = pathParts.first()
                val remaining = pathParts.drop(1)

                // Look for direct key-value pairs: (key value)
                for (element in sexp.elements) {
                    if (element is Sexp.List && element.elements.size >= 2) {
                        val firstElement = element.elements.first()
                        if (firstElement is Sexp.Atom && firstElement.value == key) {
                            return if (remaining.isEmpty()) {
                                // If no remaining path, return the value part
                                if (element.elements.size == 2) {
                                    element.elements[1]
                                } else {
                                    // Multiple values, return as a list
                                    Sexp.List(element.elements.drop(1))
                                }
                            } else {
                                // Continue searching in the value part
                                if (element.elements.size == 2) {
                                    getByPath(element.elements[1], remaining)
                                } else {
                                    // Multiple values, treat as a list
                                    getByPath(Sexp.List(element.elements.drop(1)), remaining)
                                }
                            }
                        }
                    }
                }

                null
            }
        }
    }

    private fun flattenSexp(sexp: Sexp, prefix: String, result: MutableMap<String, Sexp>) {
        when (sexp) {
            is Sexp.Atom -> {
                if (prefix.isNotEmpty()) {
                    result[prefix] = sexp
                }
            }

            is Sexp.List -> {
                // Handle key-value pairs: (key value) or (key value1 value2 ...)
                for (element in sexp.elements) {
                    if (element is Sexp.List && element.elements.size >= 2 && element.elements[0] is Sexp.Atom) {
                        val key = (element.elements[0] as Sexp.Atom).value
                        val fullKey = if (prefix.isEmpty()) key else "$prefix.$key"

                        if (element.elements.size == 2) {
                            // Simple key-value pair
                            result[fullKey] = element.elements[1]
                            flattenSexp(element.elements[1], fullKey, result)
                        } else {
                            // Multiple values
                            val valueList = Sexp.List(element.elements.drop(1))
                            result[fullKey] = valueList
                            flattenSexp(valueList, fullKey, result)
                        }
                    }
                }
            }
        }
    }
}