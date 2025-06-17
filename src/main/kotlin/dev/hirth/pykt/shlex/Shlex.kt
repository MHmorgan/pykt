package dev.hirth.pykt.shlex

/**
 * Split a shell command line string into tokens using shell-like syntax.
 * 
 * This function parses a string as if it were a shell command line, handling
 * quotes, escapes, and whitespace appropriately. It supports both POSIX and
 * non-POSIX modes for different parsing behaviors.
 * 
 * @param input The string to split
 * @param comments Whether to parse comments (skip text after #)
 * @param posix Whether to use POSIX mode for parsing
 * @return List of tokens extracted from the input string
 */
fun split(input: String, comments: Boolean = false, posix: Boolean = true): List<String> {
    val lexer = ShlexLexer(input, posix, comments)
    return lexer.tokenize()
}

/**
 * Return a shell-escaped version of the string.
 * 
 * The returned value is a string that can be safely used as one token
 * in a shell command line. If the string contains spaces, quotes, or other
 * special shell characters, it will be properly quoted and escaped.
 * Empty strings are returned as ''.
 * 
 * @param input The string to quote
 * @return Shell-escaped string that can be safely used in shell commands
 */
fun quote(input: String): String {
    if (input.isEmpty()) {
        return "''"
    }
    
    // Check if quoting is needed
    val unsafeChars = setOf(' ', '\t', '\n', '\r', '\'', '"', '\\', '|', '&', ';', '(', ')', '<', '>', '`', '$', '*', '?', '[', '#')
    val needsQuoting = input.any { it in unsafeChars }
    
    if (!needsQuoting) {
        return input
    }
    
    // Characters that need double quotes with escaping
    val needsDoubleQuotes = setOf('\\', '$', '`')
    val hasSpecialChars = input.any { it in needsDoubleQuotes }
    
    // If string contains single quotes OR special characters that need escaping, use double quotes
    if ('\'' in input || hasSpecialChars) {
        val escaped = input.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("$", "\\\$")
            .replace("`", "\\`")
        
        return "\"$escaped\""
    }
    
    // Otherwise use single quotes
    return "'$input'"
}

/**
 * Concatenate a list of tokens into a shell command string.
 * 
 * Each token will be quoted if necessary to ensure safe execution
 * in a shell environment. This is the inverse operation of split().
 * 
 * @param splitCommand List of tokens to join
 * @return Shell command string with properly quoted tokens
 */
fun join(splitCommand: List<String>): String {
    return splitCommand.joinToString(" ") { quote(it) }
}