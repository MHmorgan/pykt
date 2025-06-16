package dev.hirth.pykt.ini

import java.io.Reader
import java.io.StringReader

// -----------------------------------------------------------------------------
//
// Reader
//
// -----------------------------------------------------------------------------

/**
 * Reads INI data from a [Reader] and returns an [IniFile].
 */
fun readIni(
    reader: Reader,
    dialect: IniDialect = IniDialect.DEFAULT
): IniFile {
    return IniReader(reader, dialect).read()
}

/**
 * Reads INI data from this [Reader] and returns an [IniFile].
 */
fun Reader.parseIni(
    dialect: IniDialect = IniDialect.DEFAULT
): IniFile {
    return readIni(this, dialect)
}

// -----------------------------------------------------------------------------
//
// String
//
// -----------------------------------------------------------------------------

/**
 * Reads INI data from a string and returns an [IniFile].
 */
fun readIni(
    text: String,
    dialect: IniDialect = IniDialect.DEFAULT
): IniFile {
    return readIni(StringReader(text), dialect)
}

/**
 * Reads INI data from this [String] and returns an [IniFile].
 */
fun String.parseIni(
    dialect: IniDialect = IniDialect.DEFAULT
): IniFile {
    return readIni(this, dialect)
}

// -----------------------------------------------------------------------------
//
// Config-oriented convenience functions
//
// -----------------------------------------------------------------------------

/**
 * Reads INI data from a [Reader] and returns a flat map of all key-value pairs.
 * Keys from sections are prefixed with "section.key".
 */
fun readIniMap(
    reader: Reader,
    dialect: IniDialect = IniDialect.DEFAULT
): Map<String, String> {
    return readIni(reader, dialect).toFlatMap()
}

/**
 * Reads INI data from a string and returns a flat map of all key-value pairs.
 * Keys from sections are prefixed with "section.key".
 */
fun readIniMap(
    text: String,
    dialect: IniDialect = IniDialect.DEFAULT
): Map<String, String> {
    return readIni(text, dialect).toFlatMap()
}

/**
 * Reads INI data from this [Reader] and returns a flat map of all key-value pairs.
 * Keys from sections are prefixed with "section.key".
 */
fun Reader.parseIniMap(
    dialect: IniDialect = IniDialect.DEFAULT
): Map<String, String> {
    return readIniMap(this, dialect)
}

/**
 * Reads INI data from this [String] and returns a flat map of all key-value pairs.
 * Keys from sections are prefixed with "section.key".
 */
fun String.parseIniMap(
    dialect: IniDialect = IniDialect.DEFAULT
): Map<String, String> {
    return readIniMap(this, dialect)
}