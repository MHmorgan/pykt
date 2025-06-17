package dev.hirth.pykt.shlex

/**
 * A lexical analyzer class for shell-like syntaxes.
 * 
 * This is a Kotlin adaptation of Python's shlex module, providing functionality
 * to parse shell-like command lines and configuration files.
 */
class ShlexLexer(
    private val input: String,
    /** Whether to operate in POSIX mode. */
    val posix: Boolean = true,
    /** Whether to parse comments (lines starting with #). */
    val comments: Boolean = false
) {
    private var position = 0
    private var currentChar: Char? = if (input.isNotEmpty()) input[0] else null
    
    /** Characters that separate words. */
    var wordChars: Set<Char> = ('a'..'z').toSet() + ('A'..'Z').toSet() + ('0'..'9').toSet() + setOf('_')
    
    /** Characters that act as word separators. */
    var whitespace: Set<Char> = setOf(' ', '\t', '\n', '\r')
    
    /** Characters that begin comments. */
    var commenters: Set<Char> = setOf('#')
    
    /** Characters used for quoting. */
    var quotes: Set<Char> = setOf('\'', '"')
    
    /** Escape character for POSIX mode. */
    var escape: Char = '\\'
    
    /** Characters that can be escaped. */
    var escapedQuotes: Set<Char> = setOf('\'', '"')
    
    /** Current token being built. */
    private val token = StringBuilder()
    
    /** Current state of the lexer. */
    private var state = State.START
    
    /** Current quote character being processed. */
    private var quoteChar: Char? = null
    
    private enum class State {
        START, WORD, QUOTE, ESCAPE
    }
    
    /**
     * Get the next token from the input string.
     * 
     * This method advances through the input string and returns the next
     * complete token according to shell parsing rules. It handles quotes,
     * escapes, and word boundaries appropriately based on the lexer configuration.
     * 
     * @return The next token as a string, or null if no more tokens are available
     * @throws ShlexException if parsing encounters an error (e.g., unterminated quote)
     */
    fun nextToken(): String? {
        token.clear()
        state = State.START
        var hadContent = false
        
        while (currentChar != null) {
            when (state) {
                State.START -> {
                    when {
                        currentChar in whitespace -> {
                            advance()
                            continue
                        }
                        comments && currentChar in commenters -> {
                            skipComment()
                            continue
                        }
                        currentChar in quotes -> {
                            if (posix) {
                                quoteChar = currentChar
                                state = State.QUOTE
                                hadContent = true
                                advance()
                            } else {
                                token.append(currentChar!!)
                                hadContent = true
                                advance()
                            }
                        }
                        posix && currentChar == escape -> {
                            state = State.ESCAPE
                            hadContent = true
                            advance()
                        }
                        else -> {
                            state = State.WORD
                            token.append(currentChar!!)
                            hadContent = true
                            advance()
                        }
                    }
                }
                State.WORD -> {
                    when {
                        currentChar in whitespace -> {
                            return token.toString()
                        }
                        currentChar in quotes && posix -> {
                            quoteChar = currentChar
                            state = State.QUOTE
                            advance()
                        }
                        posix && currentChar == escape -> {
                            state = State.ESCAPE
                            advance()
                        }
                        else -> {
                            token.append(currentChar!!)
                            advance()
                        }
                    }
                }
                State.QUOTE -> {
                    when {
                        currentChar == quoteChar -> {
                            state = State.WORD
                            quoteChar = null
                            advance()
                        }
                        posix && currentChar == escape && quoteChar == '"' -> {
                            advance()
                            if (currentChar in escapedQuotes || currentChar == escape) {
                                token.append(currentChar!!)
                                advance()
                            } else {
                                token.append(escape)
                                if (currentChar != null) {
                                    token.append(currentChar!!)
                                    advance()
                                }
                            }
                        }
                        currentChar == null -> {
                            throw ShlexException("Unterminated string", position)
                        }
                        else -> {
                            token.append(currentChar!!)
                            advance()
                        }
                    }
                }
                State.ESCAPE -> {
                    when {
                        currentChar == null -> {
                            throw ShlexException("Escape character at end of string", position)
                        }
                        else -> {
                            token.append(currentChar!!)
                            advance()
                            state = State.WORD
                        }
                    }
                }
            }
        }
        
        return if (hadContent) {
            when (state) {
                State.QUOTE -> throw ShlexException("Unterminated string", position)
                State.ESCAPE -> throw ShlexException("Escape character at end of string", position) 
                else -> token.toString()
            }
        } else {
            null
        }
    }
    
    /**
     * Get all tokens from the input as a list.
     * 
     * This convenience method calls nextToken() repeatedly until all tokens
     * are consumed and returns them as a list. This is equivalent to manually
     * calling nextToken() in a loop until it returns null.
     * 
     * @return List containing all tokens from the input string
     * @throws ShlexException if parsing encounters an error during tokenization
     */
    fun tokenize(): List<String> {
        val tokens = mutableListOf<String>()
        var token = nextToken()
        while (token != null) {
            tokens.add(token)
            token = nextToken()
        }
        return tokens
    }
    
    private fun advance() {
        position++
        currentChar = if (position < input.length) input[position] else null
    }
    
    private fun skipComment() {
        while (currentChar != null && currentChar != '\n') {
            advance()
        }
        if (currentChar == '\n') {
            advance()
        }
    }
}