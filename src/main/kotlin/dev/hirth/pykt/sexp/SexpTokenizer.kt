package dev.hirth.pykt.sexp

/**
 * Represents a token in an S-expression.
 */
internal sealed class SexpToken {
    object LeftParen : SexpToken()
    object RightParen : SexpToken()
    data class Atom(val value: String) : SexpToken()
    object EOF : SexpToken()
}

/**
 * Tokenizes S-expression input.
 */
internal class SexpTokenizer(private val input: String) {
    private var position = 0

    fun tokenize(): List<SexpToken> {
        val tokens = mutableListOf<SexpToken>()

        while (position < input.length) {
            skipWhitespaceAndComments()

            if (position >= input.length) break

            when (val char = input[position]) {
                '(' -> {
                    tokens.add(SexpToken.LeftParen)
                    position++
                }

                ')' -> {
                    tokens.add(SexpToken.RightParen)
                    position++
                }

                '"' -> {
                    tokens.add(SexpToken.Atom(readQuotedString()))
                }

                else -> {
                    tokens.add(SexpToken.Atom(readUnquotedAtom()))
                }
            }
        }

        tokens.add(SexpToken.EOF)
        return tokens
    }

    private fun skipWhitespaceAndComments() {
        while (position < input.length) {
            when {
                input[position].isWhitespace() -> position++
                input[position] == ';' -> skipLineComment()
                else -> break
            }
        }
    }

    private fun skipLineComment() {
        while (position < input.length && input[position] != '\n') {
            position++
        }
        if (position < input.length && input[position] == '\n') {
            position++
        }
    }

    private fun readQuotedString(): String {
        if (input[position] != '"') {
            throw SexpParseException("Expected '\"' at position $position", position)
        }

        position++ // Skip opening quote
        val start = position
        val result = StringBuilder()

        while (position < input.length) {
            when (val char = input[position]) {
                '"' -> {
                    position++ // Skip closing quote
                    return result.toString()
                }

                '\\' -> {
                    position++
                    if (position >= input.length) {
                        throw SexpParseException("Unexpected end of input in string literal", position)
                    }
                    when (val escaped = input[position]) {
                        '"' -> result.append('"')
                        '\\' -> result.append('\\')
                        'n' -> result.append('\n')
                        'r' -> result.append('\r')
                        't' -> result.append('\t')
                        else -> {
                            result.append('\\')
                            result.append(escaped)
                        }
                    }
                    position++
                }

                else -> {
                    result.append(char)
                    position++
                }
            }
        }

        throw SexpParseException("Unterminated string literal starting at position $start", start)
    }

    private fun readUnquotedAtom(): String {
        val start = position

        while (position < input.length) {
            val char = input[position]
            if (char.isWhitespace() || char in "()\"" || char == ';') {
                break
            }
            position++
        }

        if (position == start) {
            throw SexpParseException("Unexpected character '${input[position]}' at position $position", position)
        }

        return input.substring(start, position)
    }
}