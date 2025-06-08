package dev.hirth.pykt.csv

/**
 * Exception raised for CSV parsing or writing errors.
 */
class CsvError(message: String, cause: Throwable? = null) : Exception(message, cause)