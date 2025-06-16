package dev.hirth.pykt.shlex

/**
 * Exception thrown when shlex parsing encounters an error.
 */
class ShlexException(
    message: String,
    val position: Int = -1,
    cause: Throwable? = null
) : Exception(message, cause)