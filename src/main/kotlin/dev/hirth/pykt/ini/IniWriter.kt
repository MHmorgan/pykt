package dev.hirth.pykt.ini

import java.io.Writer

/**
 * A writer that outputs INI data from an IniFile to a Writer.
 */
class IniWriter(
    private val writer: Writer,
    private val dialect: IniDialect = IniDialect.DEFAULT
) {
    
    /**
     * Write an IniFile to the output.
     */
    fun write(iniFile: IniFile) {
        val sections = iniFile.sections
        var firstSection = true
        
        // Write default section first if it exists and has content
        val defaultSection = iniFile.getSection(dialect.defaultSectionName)
        if (defaultSection != null && !defaultSection.isEmpty()) {
            writeSection(defaultSection, isDefault = true)
            firstSection = false
        }
        
        // Write all other sections
        for ((sectionName, section) in sections) {
            if (sectionName == dialect.defaultSectionName) {
                continue // Already written
            }
            
            if (!firstSection) {
                writer.write("\n")
            }
            writeSection(section, isDefault = false)
            firstSection = false
        }
        
        writer.flush()
    }
    
    private fun writeSection(section: IniSection, isDefault: Boolean) {
        // Write section header (unless it's the default section)
        if (!isDefault) {
            writer.write("[${section.name}]\n")
        }
        
        // Write key-value pairs
        for ((key, value) in section.entries) {
            writeKeyValue(key, value)
        }
    }
    
    private fun writeKeyValue(key: String, value: String) {
        // Use the first separator from the dialect
        val separator = dialect.separators.first()
        
        // Handle multiline values
        if (value.contains('\n') && dialect.allowMultilineValues) {
            val lines = value.split('\n')
            writer.write("$key$separator${lines[0]}\n")
            for (i in 1 until lines.size) {
                writer.write("${dialect.continuationPrefix}${lines[i]}\n")
            }
        } else {
            writer.write("$key$separator$value\n")
        }
    }
}

// -----------------------------------------------------------------------------
//
// Convenience functions
//
// -----------------------------------------------------------------------------

/**
 * Write an IniFile to a Writer.
 */
fun writeIni(
    writer: Writer,
    iniFile: IniFile,
    dialect: IniDialect = IniDialect.DEFAULT
) {
    IniWriter(writer, dialect).write(iniFile)
}

/**
 * Write an IniFile to a string.
 */
fun writeIni(
    iniFile: IniFile,
    dialect: IniDialect = IniDialect.DEFAULT
): String {
    val writer = java.io.StringWriter()
    writeIni(writer, iniFile, dialect)
    return writer.toString()
}