package dev.hirth.pykt.toml

import java.io.Reader
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Exception thrown when parsing TOML content fails.
 */
class TomlParseException(message: String, line: Int = -1, column: Int = -1, cause: Throwable? = null) :
    Exception("${if (line > 0) "Line $line, column $column: " else ""}$message", cause)

/**
 * TOML parser that reads TOML content and produces a [TomlDocument].
 */
class TomlReader {

    private var input: String = ""
    private var position: Int = 0
    private var line: Int = 1
    private var column: Int = 1

    /**
     * Parses TOML content from a string.
     */
    fun parseToml(content: String): TomlDocument {
        input = content
        position = 0
        line = 1
        column = 1

        val document = TomlDocument()
        var currentTable = document.rootTable
        var currentTablePath = ""

        while (!isAtEnd()) {
            skipWhitespaceAndComments()
            if (isAtEnd()) break

            when (peek()) {
                '[' -> {
                    val (tablePath, isArrayTable) = parseTableHeader()
                    currentTablePath = tablePath

                    if (isArrayTable) {
                        // Array of tables
                        val arrayTables = document.rootTable.arrayTables.getOrPut(tablePath) { mutableListOf() }
                        val newTable = TomlTable()
                        arrayTables.add(newTable)
                        currentTable = newTable
                    } else {
                        // Regular table
                        currentTable = document.getOrCreateTable(tablePath)
                    }
                }

                else -> {
                    // Key-value pair
                    val (key, value) = parseKeyValue()

                    if (currentTablePath.isEmpty()) {
                        document.rootTable.values[key] = value
                    } else {
                        currentTable.values[key] = value
                    }
                }
            }

            skipWhitespaceAndComments()
        }

        return document
    }

    /**
     * Parses TOML content from a Reader.
     */
    fun parseToml(reader: Reader): TomlDocument {
        return parseToml(reader.readText())
    }

    private fun parseTableHeader(): Pair<String, Boolean> {
        consume('[')
        val isArrayTable = peek() == '['
        if (isArrayTable) {
            consume('[')
        }

        val tableName = parseDottedKey()

        if (isArrayTable) {
            consume(']')
        }
        consume(']')

        return tableName to isArrayTable
    }

    private fun parseKeyValue(): Pair<String, TomlValue> {
        val key = parseKey()
        skipWhitespace()
        consume('=')
        skipWhitespace()
        val value = parseValue()
        return key to value
    }

    private fun parseKey(): String {
        skipWhitespace()

        return when (peek()) {
            '"' -> parseQuotedKey()
            '\'' -> parseLiteralString()
            else -> parseBareKey()
        }
    }

    private fun parseDottedKey(): String {
        val parts = mutableListOf<String>()
        parts.add(parseKey())

        while (peek() == '.') {
            advance() // consume '.'
            parts.add(parseKey())
        }

        return parts.joinToString(".")
    }

    private fun parseBareKey(): String {
        val start = position
        while (!isAtEnd() && isBareKeyChar(peek())) {
            advance()
        }
        if (start == position) {
            throw TomlParseException("Expected key", line, column)
        }
        return input.substring(start, position)
    }

    private fun parseQuotedKey(): String {
        return parseBasicString()
    }

    private fun parseValue(): TomlValue {
        skipWhitespace()

        return when (peek()) {
            '"' -> TomlValue.String(parseBasicString())
            '\'' -> TomlValue.String(parseLiteralString())
            '[' -> parseArray()
            '{' -> parseInlineTable()
            't', 'f' -> parseBoolean()
            '+', '-' -> parseNumberOrDateTime()
            in '0'..'9' -> parseNumberOrDateTime()
            'i', 'n' -> parseNumberOrDateTime() // Handle inf and nan
            else -> throw TomlParseException("Unexpected character '${peek()}'", line, column)
        }
    }

    private fun parseBasicString(): String {
        consume('"')
        val result = StringBuilder()

        while (!isAtEnd() && peek() != '"') {
            when (val char = peek()) {
                '\\' -> {
                    advance()
                    when (val escaped = peek()) {
                        '"' -> result.append('"')
                        '\\' -> result.append('\\')
                        'b' -> result.append('\b')
                        'f' -> result.append('\u000C')
                        'n' -> result.append('\n')
                        'r' -> result.append('\r')
                        't' -> result.append('\t')
                        'u' -> {
                            advance()
                            val unicode = parseUnicodeEscape(4)
                            result.append(unicode.toChar())
                        }

                        'U' -> {
                            advance()
                            val unicode = parseUnicodeEscape(8)
                            if (unicode <= 0xFFFF) {
                                result.append(unicode.toChar())
                            } else {
                                result.append(Character.toChars(unicode))
                            }
                        }

                        else -> throw TomlParseException("Invalid escape sequence '\\$escaped'", line, column)
                    }
                    advance()
                }

                else -> {
                    result.append(char)
                    advance()
                }
            }
        }

        consume('"')
        return result.toString()
    }

    private fun parseLiteralString(): String {
        consume('\'')
        val start = position

        while (!isAtEnd() && peek() != '\'') {
            advance()
        }

        val result = input.substring(start, position)
        consume('\'')
        return result
    }

    private fun parseUnicodeEscape(length: Int): Int {
        var result = 0
        repeat(length) {
            val char = peek()
            if (!char.isHexDigit()) {
                throw TomlParseException("Invalid unicode escape", line, column)
            }
            result = result * 16 + char.digitToInt(16)
            advance()
        }
        return result
    }

    private fun parseArray(): TomlValue.Array {
        consume('[')
        val elements = mutableListOf<TomlValue>()

        skipWhitespaceAndComments()

        while (!isAtEnd() && peek() != ']') {
            elements.add(parseValue())
            skipWhitespaceAndComments()

            if (peek() == ',') {
                advance()
                skipWhitespaceAndComments()
            } else if (peek() != ']') {
                throw TomlParseException("Expected ',' or ']' in array", line, column)
            }
        }

        consume(']')
        return TomlValue.Array(elements)
    }

    private fun parseInlineTable(): TomlValue.InlineTable {
        consume('{')
        val table = mutableMapOf<String, TomlValue>()

        skipWhitespace()

        while (!isAtEnd() && peek() != '}') {
            val key = parseKey()
            skipWhitespace()
            consume('=')
            skipWhitespace()
            val value = parseValue()
            table[key] = value

            skipWhitespace()

            if (peek() == ',') {
                advance()
                skipWhitespace()
            } else if (peek() != '}') {
                throw TomlParseException("Expected ',' or '}' in inline table", line, column)
            }
        }

        consume('}')
        return TomlValue.InlineTable(table)
    }

    private fun parseBoolean(): TomlValue.Boolean {
        return when {
            matchKeyword("true") -> TomlValue.Boolean(true)
            matchKeyword("false") -> TomlValue.Boolean(false)
            else -> throw TomlParseException("Expected boolean value", line, column)
        }
    }

    private fun parseNumberOrDateTime(): TomlValue {
        val start = position

        // Handle sign
        if (peek() == '+' || peek() == '-') {
            advance()
        }

        // Check for datetime formats first
        if (peek().isDigit()) {
            val savedPos = position
            val savedLine = line
            val savedCol = column

            try {
                return tryParseDateTime()
            } catch (e: Exception) {
                // Reset position and try parsing as number
                position = savedPos
                line = savedLine
                column = savedCol
            }
        }

        // Parse as number
        position = start
        return parseNumber()
    }

    private fun tryParseDateTime(): TomlValue {
        val start = position - if (peek(-1) == '+' || peek(-1) == '-') 1 else 0

        // Read potential datetime string
        while (!isAtEnd() && (peek().isDigit() || peek() in "T:-+Z.")) {
            advance()
        }

        val dateTimeStr = input.substring(start, position)

        // Try parsing as OffsetDateTime (with timezone)
        if (dateTimeStr.contains('T') && (dateTimeStr.contains('+') || dateTimeStr.contains('-') || dateTimeStr.endsWith(
                'Z'
            ))
        ) {
            try {
                return TomlValue.OffsetDateTime(OffsetDateTime.parse(dateTimeStr))
            } catch (e: DateTimeParseException) {
                // Continue to next format
            }
        }

        // Try parsing as LocalDateTime
        if (dateTimeStr.contains('T')) {
            try {
                return TomlValue.LocalDateTime(LocalDateTime.parse(dateTimeStr))
            } catch (e: DateTimeParseException) {
                // Continue to next format
            }
        }

        // Try parsing as LocalDate
        if (dateTimeStr.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            try {
                return TomlValue.LocalDate(LocalDate.parse(dateTimeStr))
            } catch (e: DateTimeParseException) {
                // Continue to next format
            }
        }

        // Try parsing as LocalTime
        if (dateTimeStr.contains(':')) {
            try {
                return TomlValue.LocalTime(LocalTime.parse(dateTimeStr))
            } catch (e: DateTimeParseException) {
                // Continue to next format
            }
        }

        throw TomlParseException("Invalid datetime format: $dateTimeStr", line, column)
    }

    private fun parseNumber(): TomlValue {
        val start = position

        // Handle sign
        var hasSign = false
        if (peek() == '+' || peek() == '-') {
            hasSign = true
            advance()
        }

        // Handle special float values
        val savedPos = position
        if (matchKeyword("inf")) {
            val sign = if (hasSign && input[start] == '-') -1.0 else 1.0
            return TomlValue.Float(sign * Double.POSITIVE_INFINITY)
        }

        // Reset position if inf didn't match
        position = savedPos

        if (matchKeyword("nan")) {
            return TomlValue.Float(Double.NaN)
        }

        // Reset position if nan didn't match
        position = savedPos

        var isFloat = false

        // Parse digits
        if (!peek().isDigit()) {
            throw TomlParseException("Expected digit", line, column)
        }

        while (!isAtEnd() && (peek().isDigit() || peek() == '_')) {
            if (peek() == '_') {
                advance()
                if (!peek().isDigit()) {
                    throw TomlParseException("Underscore must be surrounded by digits", line, column)
                }
            } else {
                advance()
            }
        }

        // Check for decimal point
        if (!isAtEnd() && peek() == '.') {
            isFloat = true
            advance()

            if (!peek().isDigit()) {
                throw TomlParseException("Expected digit after decimal point", line, column)
            }

            while (!isAtEnd() && (peek().isDigit() || peek() == '_')) {
                if (peek() == '_') {
                    advance()
                    if (!peek().isDigit()) {
                        throw TomlParseException("Underscore must be surrounded by digits", line, column)
                    }
                } else {
                    advance()
                }
            }
        }

        // Check for exponent
        if (!isAtEnd() && (peek() == 'e' || peek() == 'E')) {
            isFloat = true
            advance()

            if (peek() == '+' || peek() == '-') {
                advance()
            }

            if (!peek().isDigit()) {
                throw TomlParseException("Expected digit in exponent", line, column)
            }

            while (!isAtEnd() && (peek().isDigit() || peek() == '_')) {
                if (peek() == '_') {
                    advance()
                    if (!peek().isDigit()) {
                        throw TomlParseException("Underscore must be surrounded by digits", line, column)
                    }
                } else {
                    advance()
                }
            }
        }

        val numberStr = input.substring(start, position).replace("_", "")

        return if (isFloat) {
            TomlValue.Float(numberStr.toDouble())
        } else {
            TomlValue.Integer(numberStr.toLong())
        }
    }

    private fun matchKeyword(keyword: String): Boolean {
        if (position + keyword.length > input.length) return false

        for (i in keyword.indices) {
            if (input[position + i] != keyword[i]) return false
        }

        // Only advance if we have a match
        position += keyword.length
        return true
    }

    private fun skipWhitespace() {
        while (!isAtEnd() && (peek() == ' ' || peek() == '\t')) {
            advance()
        }
    }

    private fun skipWhitespaceAndComments() {
        while (!isAtEnd()) {
            when (peek()) {
                ' ', '\t' -> advance()
                '\n', '\r' -> {
                    if (peek() == '\r' && peek(1) == '\n') {
                        advance()
                    }
                    advance()
                    line++
                    column = 1
                }

                '#' -> {
                    // Skip comment line
                    while (!isAtEnd() && peek() != '\n' && peek() != '\r') {
                        advance()
                    }
                }

                else -> break
            }
        }
    }

    private fun isBareKeyChar(char: Char): Boolean {
        return char.isLetterOrDigit() || char == '_' || char == '-'
    }

    private fun isAtEnd(): Boolean = position >= input.length

    private fun peek(offset: Int = 0): Char {
        val pos = position + offset
        return if (pos >= input.length) '\u0000' else input[pos]
    }

    private fun advance(): Char {
        if (isAtEnd()) return '\u0000'
        val char = input[position++]
        if (char != '\n' && char != '\r') {
            column++
        }
        return char
    }

    private fun consume(expected: Char) {
        if (peek() != expected) {
            throw TomlParseException("Expected '$expected' but got '${peek()}'", line, column)
        }
        advance()
    }

    private fun Char.isHexDigit(): Boolean = this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'
}