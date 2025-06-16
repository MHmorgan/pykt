package dev.hirth.pykt.ini

import java.io.Reader

/**
 * A reader that parses INI data from a Reader into an IniFile.
 */
class IniReader(
    private val reader: Reader,
    private val dialect: IniDialect = IniDialect.DEFAULT
) {
    private var lineNumber = 0
    
    /**
     * Parse the INI content and return an IniFile.
     */
    fun read(): IniFile {
        val iniFile = IniFile(dialect)
        var currentSectionName = dialect.defaultSectionName
        var currentKey: String? = null
        var currentValue = StringBuilder()
        
        reader.useLines { lines ->
            for (rawLine in lines) {
                lineNumber++
                val line = if (dialect.trimWhitespace) rawLine.trim() else rawLine
                
                try {
                    when {
                        // Empty line or comment
                        line.isEmpty() || isComment(line) -> {
                            finalizePendingKeyValue(iniFile, currentSectionName, currentKey, currentValue)
                            currentKey = null
                            currentValue = StringBuilder()
                            continue
                        }
                        
                        // Section header
                        isSection(line) -> {
                            finalizePendingKeyValue(iniFile, currentSectionName, currentKey, currentValue)
                            currentKey = null
                            currentValue = StringBuilder()
                            currentSectionName = parseSectionName(line)
                        }
                        
                        // Key-value pair (check this before continuation to handle indented key-value pairs)
                        hasKeyValueSeparator(line) -> {
                            finalizePendingKeyValue(iniFile, currentSectionName, currentKey, currentValue)
                            val (key, value) = parseKeyValue(line)
                            currentKey = key
                            currentValue = StringBuilder(value)
                        }
                        
                        // Continuation line (check against raw line for proper indentation detection)
                        isContinuation(rawLine) && currentKey != null -> {
                            if (dialect.allowMultilineValues) {
                                if (currentValue.isNotEmpty()) {
                                    currentValue.append('\n')
                                }
                                val continuationContent = rawLine.removePrefix(dialect.continuationPrefix)
                                currentValue.append(if (dialect.trimWhitespace) continuationContent.trim() else continuationContent)
                            } else if (dialect.strict) {
                                throw IniError("Multiline values not allowed at line $lineNumber")
                            }
                        }
                        
                        // Key without value (if allowed)
                        else -> {
                            finalizePendingKeyValue(iniFile, currentSectionName, currentKey, currentValue)
                            val (key, value) = parseKeyValue(line)
                            currentKey = key
                            currentValue = StringBuilder(value)
                        }
                    }
                } catch (e: IniError) {
                    if (dialect.strict) {
                        throw e
                    }
                    // In non-strict mode, skip problematic lines
                    currentKey = null
                    currentValue = StringBuilder()
                }
            }
        }
        
        // Finalize any pending key-value pair
        finalizePendingKeyValue(iniFile, currentSectionName, currentKey, currentValue)
        
        return iniFile
    }
    
    private fun finalizePendingKeyValue(iniFile: IniFile, sectionName: String, key: String?, value: StringBuilder) {
        if (key != null) {
            val finalValue = value.toString()
            iniFile.set(sectionName, key, finalValue)
        }
    }
    
    private fun isComment(line: String): Boolean {
        return line.isNotEmpty() && dialect.commentPrefixes.contains(line[0])
    }
    
    private fun isSection(line: String): Boolean {
        return line.startsWith('[') && line.endsWith(']')
    }
    
    private fun isContinuation(rawLine: String): Boolean {
        return dialect.allowMultilineValues && rawLine.startsWith(dialect.continuationPrefix)
    }
    
    private fun hasKeyValueSeparator(line: String): Boolean {
        return dialect.separators.any { line.contains(it) }
    }
    
    private fun parseSectionName(line: String): String {
        if (!isSection(line)) {
            throw IniError("Invalid section header at line $lineNumber: $line")
        }
        
        val sectionName = line.substring(1, line.length - 1)
        
        if (dialect.trimWhitespace) {
            return sectionName.trim()
        }
        
        return sectionName
    }
    
    private fun parseKeyValue(line: String): Pair<String, String> {
        // Find the first separator
        var separatorIndex = -1
        var separatorChar: Char? = null
        
        for (i in line.indices) {
            if (dialect.separators.contains(line[i])) {
                separatorIndex = i
                separatorChar = line[i]
                break
            }
        }
        
        if (separatorIndex == -1) {
            // No separator found
            if (dialect.allowKeysWithoutValues) {
                val key = if (dialect.trimWhitespace) line.trim() else line
                return key to ""
            } else if (dialect.strict) {
                throw IniError("No separator found in key-value pair at line $lineNumber: $line")
            } else {
                // In non-strict mode, treat the whole line as a key with empty value
                val key = if (dialect.trimWhitespace) line.trim() else line
                return key to ""
            }
        }
        
        val keyPart = line.substring(0, separatorIndex)
        val valuePart = line.substring(separatorIndex + 1)
        
        val key = if (dialect.trimWhitespace) keyPart.trim() else keyPart
        val value = if (dialect.trimWhitespace) valuePart.trim() else valuePart
        
        if (key.isEmpty() && dialect.strict) {
            throw IniError("Empty key at line $lineNumber: $line")
        }
        
        if (value.isEmpty() && !dialect.allowEmptyValues && dialect.strict) {
            throw IniError("Empty value not allowed at line $lineNumber: $line")
        }
        
        return key to value
    }
}