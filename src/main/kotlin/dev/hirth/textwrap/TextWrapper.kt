package dev.hirth.textwrap

/**
 * Options for text wrapping operations.
 */
data class TextWrapOptions(
    /** Maximum line width. */
    val width: Int = 70,
    /** Initial indent for first line. */
    val initialIndent: String = "",
    /** Subsequent indent for continuation lines. */
    val subsequentIndent: String = "",
    /** Whether to expand tabs to spaces. */
    val expandTabs: Boolean = true,
    /** Replace whitespace with single spaces. */
    val replaceWhitespace: Boolean = true,
    /** Drop leading/trailing whitespace from lines. */
    val dropWhitespace: Boolean = true,
    /** Break long words if necessary. */
    val breakLongWords: Boolean = true,
    /** Break on hyphens in compound words. */
    val breakOnHyphens: Boolean = true,
    /** Tab size for expansion. */
    val tabSize: Int = 8,
    /** Max number of lines (0 = unlimited). */
    val maxLines: Int = 0,
    /** Placeholder when truncating. */
    val placeholder: String = " [...]"
)

/**
 * A text wrapper that formats text according to specified options.
 */
class TextWrapper(private val options: TextWrapOptions = TextWrapOptions()) {
    
    companion object {
        private val WHITESPACE_REGEX = Regex("\\s+")
        private val WORDSEP_SIMPLE_REGEX = Regex("(\\s+)")
        private val SENTENCE_END_REGEX = Regex("[.!?][\"']?\\s")
    }
    
    /**
     * Wraps text into lines of specified width.
     */
    fun wrap(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        
        var processedText = text
        
        // Handle tabs
        if (options.expandTabs) {
            processedText = processedText.replace("\t", " ".repeat(options.tabSize))
        }
        
        // Handle whitespace normalization
        if (options.replaceWhitespace) {
            processedText = processedText.replace(WHITESPACE_REGEX, " ")
        }
        
        // Split into words (preserving structure based on options)
        val words = if (options.replaceWhitespace) {
            processedText.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        } else {
            // When not replacing whitespace, treat the whole text as units separated by single spaces
            // but preserve the exact whitespace characters
            if (options.expandTabs) {
                // Tab already expanded, so split normally but preserve multiple spaces
                listOf(processedText)
            } else {
                // Keep original text including tabs
                listOf(processedText)
            }
        }
        
        if (words.isEmpty()) return emptyList()
        
        return wrapWords(words)
    }
    
    /**
     * Wraps text and joins with newlines.
     */
    fun fill(text: String): String = wrap(text).joinToString("\n")
    

    private fun wrapWords(words: List<String>): List<String> {
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()
        var currentIndent = options.initialIndent
        
        for (word in words) {
            // Skip empty words
            if (word.isEmpty()) continue
            
            val isWhitespace = word.all { it.isWhitespace() }
            
            if (isWhitespace && !options.replaceWhitespace) {
                // Add whitespace as-is when not replacing whitespace
                currentLine.append(word)
                continue
            }
            
            val lineLength = currentIndent.length + currentLine.length
            val wordLength = word.length
            val spaceNeeded = if (currentLine.isEmpty() || currentLine.endsWith(' ')) 0 else 1
            
            // Check if adding this word would exceed the width
            if (lineLength + spaceNeeded + wordLength > options.width && currentLine.isNotEmpty()) {
                // Finish current line
                val line = buildLine(currentLine.toString(), currentIndent)
                lines.add(line)
                
                // Start new line
                currentLine = StringBuilder()
                currentIndent = options.subsequentIndent
                
                // Check max lines limit
                if (options.maxLines > 0 && lines.size >= options.maxLines) {
                    return handleMaxLines(lines, words, words.indexOf(word))
                }
            }
            
            // Handle long words that exceed width
            if (options.breakLongWords && wordLength > options.width - currentIndent.length) {
                val brokenWords = breakLongWord(word, options.width - currentIndent.length)
                for ((index, brokenWord) in brokenWords.withIndex()) {
                    if (index == 0) {
                        if (currentLine.isNotEmpty() && !currentLine.endsWith(' ')) currentLine.append(" ")
                        currentLine.append(brokenWord)
                    } else {
                        val line = buildLine(currentLine.toString(), currentIndent)
                        lines.add(line)
                        currentLine = StringBuilder(brokenWord)
                        currentIndent = options.subsequentIndent
                    }
                }
            } else {
                if (currentLine.isNotEmpty() && !currentLine.endsWith(' ')) currentLine.append(" ")
                currentLine.append(word)
            }
        }
        
        // Add final line if not empty
        if (currentLine.isNotEmpty()) {
            val line = buildLine(currentLine.toString(), currentIndent)
            lines.add(line)
        }
        
        return lines
    }
    
    private fun buildLine(content: String, indent: String): String {
        val trimmedContent = if (options.dropWhitespace) content.trim() else content
        return indent + trimmedContent
    }
    
    private fun breakLongWord(word: String, maxLength: Int): List<String> {
        if (maxLength <= 0) return listOf(word)
        
        val result = mutableListOf<String>()
        var remaining = word
        
        while (remaining.length > maxLength) {
            result.add(remaining.substring(0, maxLength))
            remaining = remaining.substring(maxLength)
        }
        
        if (remaining.isNotEmpty()) {
            result.add(remaining)
        }
        
        return result
    }
    
    private fun handleMaxLines(lines: MutableList<String>, words: List<String>, currentIndex: Int): List<String> {
        if (lines.isEmpty()) return lines
        
        // Replace last line with truncated version
        val lastLine = lines.removeAt(lines.size - 1)
        val availableWidth = options.width - options.placeholder.length
        
        if (lastLine.length <= availableWidth) {
            lines.add(lastLine + options.placeholder)
        } else {
            lines.add(lastLine.substring(0, availableWidth) + options.placeholder)
        }
        
        return lines
    }
}

/**
 * Wraps text to the specified width.
 */
fun wrap(text: String, width: Int = 70, options: TextWrapOptions = TextWrapOptions(width = width)): List<String> {
    return TextWrapper(options).wrap(text)
}

/**
 * Wraps text and returns as a single string with newlines.
 */
fun fill(text: String, width: Int = 70, options: TextWrapOptions = TextWrapOptions(width = width)): String {
    return TextWrapper(options).fill(text)
}

/**
 * Shortens text to fit within the specified width.
 */
fun shorten(text: String, width: Int, placeholder: String = " [...]"): String {
    if (text.length <= width) return text
    
    val availableWidth = width - placeholder.length
    if (availableWidth <= 0) return placeholder.take(width)
    
    val words = text.split(Regex("\\s+"))
    val result = StringBuilder()
    
    for (word in words) {
        val nextLength = result.length + if (result.isEmpty()) 0 else 1 + word.length
        if (nextLength <= availableWidth) {
            if (result.isNotEmpty()) result.append(" ")
            result.append(word)
        } else {
            break
        }
    }
    
    return result.toString() + placeholder
}

/**
 * Removes common leading whitespace from every line in text.
 */
fun dedent(text: String): String {
    val lines = text.split('\n')
    if (lines.isEmpty()) return text
    
    // Find common leading whitespace
    var commonIndent: String? = null
    
    for (line in lines) {
        if (line.isBlank()) continue
        
        val leadingWhitespace = line.takeWhile { it.isWhitespace() }
        
        if (commonIndent == null) {
            commonIndent = leadingWhitespace
        } else {
            // Find common prefix
            var i = 0
            while (i < minOf(commonIndent.length, leadingWhitespace.length) &&
                   commonIndent[i] == leadingWhitespace[i]) {
                i++
            }
            commonIndent = commonIndent.substring(0, i)
        }
        
        if (commonIndent.isEmpty()) break
    }
    
    // Remove common indent from all lines
    return if (commonIndent.isNullOrEmpty()) {
        text
    } else {
        lines.joinToString("\n") { line ->
            if (line.startsWith(commonIndent)) {
                line.substring(commonIndent.length)
            } else {
                line
            }
        }
    }
}

/**
 * Adds prefix to the beginning of selected lines in text.
 */
fun indent(text: String, prefix: String, predicate: ((String) -> Boolean)? = null): String {
    val lines = text.split('\n')
    
    return lines.joinToString("\n") { line ->
        if (predicate?.invoke(line) != false) {
            prefix + line
        } else {
            line
        }
    }
}