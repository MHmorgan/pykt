package dev.hirth.pykt.ini

/**
 * Exception raised for INI parsing or writing errors.
 */
class IniError(message: String, cause: Throwable? = null) : Exception(message, cause)