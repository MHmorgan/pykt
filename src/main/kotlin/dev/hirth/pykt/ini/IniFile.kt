package dev.hirth.pykt.ini

/**
 * Represents an INI file with sections and provides a high-level interface
 * for accessing and manipulating INI data.
 */
class IniFile(
    private val dialect: IniDialect = IniDialect.DEFAULT
) {
    private val _sections = mutableMapOf<String, IniSection>()
    private val _originalSectionNames = mutableMapOf<String, String>()

    /** Get all section names. */
    val sectionNames: Set<String>
        get() = _originalSectionNames.values.toSet()

    /** Get all sections. */
    val sections: Map<String, IniSection>
        get() = _sections.values.associateBy {
            _originalSectionNames[getSectionKey(it.name)] ?: it.name
        }

    /** Check if a section exists. */
    fun hasSection(sectionName: String): Boolean {
        val name = getSectionKey(sectionName)
        return _sections.containsKey(name)
    }

    /** Get a section by name, or null if not found. */
    fun getSection(sectionName: String): IniSection? {
        val name = getSectionKey(sectionName)
        return _sections[name]
    }

    /** Get a section by name, creating it if it doesn't exist. */
    fun section(sectionName: String): IniSection {
        val name = getSectionKey(sectionName)
        if (!_sections.containsKey(name)) {
            _sections[name] = IniSection(sectionName)
            _originalSectionNames[name] = sectionName
        }
        return _sections[name]!!
    }

    /** Add or replace a section. */
    fun addSection(section: IniSection) {
        val name = getSectionKey(section.name)
        _sections[name] = section
        _originalSectionNames[name] = section.name
    }

    /** Remove a section. */
    fun removeSection(sectionName: String): IniSection? {
        val name = getSectionKey(sectionName)
        _originalSectionNames.remove(name)
        return _sections.remove(name)
    }

    private fun getSectionKey(sectionName: String): String {
        return if (dialect.caseSensitive) sectionName else sectionName.lowercase()
    }

    /** Get a value from a specific section and key. */
    fun get(sectionName: String, key: String): String? {
        return getSection(sectionName)?.get(key)
    }

    /** Get a value from a specific section and key, with default. */
    fun get(sectionName: String, key: String, defaultValue: String): String {
        return getSection(sectionName)?.get(key, defaultValue) ?: defaultValue
    }

    /** Set a value in a specific section and key. */
    fun set(sectionName: String, key: String, value: String) {
        section(sectionName).set(key, value)
    }

    /** Check if a key exists in a section. */
    fun has(sectionName: String, key: String): Boolean {
        return getSection(sectionName)?.containsKey(key) ?: false
    }

    /** Remove a key from a section. */
    fun remove(sectionName: String, key: String): String? {
        return getSection(sectionName)?.remove(key)
    }

    /** Get all key-value pairs from all sections as a flat map with "section.key" keys. */
    fun toFlatMap(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        for ((sectionKey, section) in _sections) {
            val originalSectionName = _originalSectionNames[sectionKey] ?: section.name
            for ((key, value) in section.entries) {
                val flatKey =
                    if (originalSectionName == dialect.defaultSectionName) key else "$originalSectionName.$key"
                result[flatKey] = value
            }
        }
        return result
    }

    /** Get all key-value pairs from the default section. */
    fun getDefaults(): Map<String, String> {
        return getSection(dialect.defaultSectionName)?.entries ?: emptyMap()
    }
}