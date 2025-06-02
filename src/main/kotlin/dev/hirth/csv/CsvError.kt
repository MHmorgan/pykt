package dev.hirth.csv

/**
 * Exception raised for CSV parsing or writing errors.
 */
class CsvError(message: String, cause: Throwable? = null) : Exception(message, cause)