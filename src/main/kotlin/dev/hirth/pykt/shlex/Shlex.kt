package dev.hirth.pykt.shlex

/**
 * Split a shell command line string into tokens using shell-like syntax.
 * 
 * @param s The string to split
 * @param comments Whether to parse comments (skip text after #)
 * @param posix Whether to use POSIX mode for parsing
 * @return List of tokens
 */
fun split(s: String, comments: Boolean = false, posix: Boolean = true): List<String> {
    val lexer = ShlexLexer(s, posix, comments)
    return lexer.tokenize()
}

/**
 * Return a shell-escaped version of the string.
 * The returned value is a string that can be safely used as one token
 * in a shell command line.
 * 
 * @param s The string to quote
 * @return Shell-escaped string
 */
fun quote(s: String): String {
    if (s.isEmpty()) {
        return "''"
    }
    
    // Check if quoting is needed
    val unsafeChars = setOf(' ', '\t', '\n', '\r', '\'', '"', '\\', '|', '&', ';', '(', ')', '<', '>', '`', '$', '*', '?', '[', '#')
    val needsQuoting = s.any { it in unsafeChars }
    
    if (!needsQuoting) {
        return s
    }
    
    // Characters that need double quotes with escaping
    val needsDoubleQuotes = setOf('\\', '$', '`')
    val hasSpecialChars = s.any { it in needsDoubleQuotes }
    
    // If string contains single quotes OR special characters that need escaping, use double quotes
    if ('\'' in s || hasSpecialChars) {
        val escaped = s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("$", "\\\$")
            .replace("`", "\\`")
        
        return "\"$escaped\""
    }
    
    // Otherwise use single quotes
    return "'$s'"
}

/**
 * Concatenate a list of tokens into a shell command string.
 * Each token will be quoted if necessary.
 * 
 * @param splitCommand List of tokens to join
 * @return Shell command string
 */
fun join(splitCommand: List<String>): String {
    return splitCommand.joinToString(" ") { quote(it) }
}