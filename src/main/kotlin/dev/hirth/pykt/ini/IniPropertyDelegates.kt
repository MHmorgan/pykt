package dev.hirth.pykt.ini

import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Property delegate for accessing INI configuration values.
 * The property name is used as the key, and the section can be specified.
 */
class IniProperty(
    private val iniFile: IniFile,
    private val section: String = "DEFAULT"
) : ReadOnlyProperty<Any?, String?> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): String? {
        return iniFile.get(section, property.name)
    }
}

/**
 * Property delegate for accessing INI configuration values with a default value.
 */
class IniPropertyWithDefault<T>(
    private val iniFile: IniFile,
    private val section: String = "DEFAULT",
    private val defaultValue: T,
    private val converter: (String) -> T = { @Suppress("UNCHECKED_CAST") it as T }
) : ReadOnlyProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val value = iniFile.get(section, property.name) ?: return defaultValue
        return try {
            converter(value)
        } catch (e: Exception) {
            defaultValue
        }
    }
}

/**
 * Property delegate for read-write access to INI configuration values.
 */
class IniPropertyRW(
    private val iniFile: IniFile,
    private val section: String = "DEFAULT"
) : ReadWriteProperty<Any?, String?> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): String? {
        return iniFile.get(section, property.name)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
        if (value != null) {
            iniFile.set(section, property.name, value)
        } else {
            iniFile.remove(section, property.name)
        }
    }
}

/**
 * Property delegate for read-write access to INI configuration values with type conversion.
 */
class IniPropertyRWWithDefault<T>(
    private val iniFile: IniFile,
    private val section: String = "DEFAULT",
    private val defaultValue: T,
    private val converter: (String) -> T = { @Suppress("UNCHECKED_CAST") it as T },
    private val serializer: (T) -> String = { it.toString() }
) : ReadWriteProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val value = iniFile.get(section, property.name) ?: return defaultValue
        return try {
            converter(value)
        } catch (e: Exception) {
            defaultValue
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        iniFile.set(section, property.name, serializer(value))
    }
}

// Convenience functions for creating property delegates

/**
 * Creates a property delegate for reading string values from INI files.
 */
fun iniString(iniFile: IniFile, section: String = "DEFAULT") = IniProperty(iniFile, section)

/**
 * Creates a property delegate for reading string values from INI files with a default.
 */
fun iniString(iniFile: IniFile, section: String = "DEFAULT", defaultValue: String) =
    IniPropertyWithDefault(iniFile, section, defaultValue)

/**
 * Creates a property delegate for reading integer values from INI files with a default.
 */
fun iniInt(iniFile: IniFile, section: String = "DEFAULT", defaultValue: Int) =
    IniPropertyWithDefault(iniFile, section, defaultValue) { it.toInt() }

/**
 * Creates a property delegate for reading boolean values from INI files with a default.
 */
fun iniBoolean(iniFile: IniFile, section: String = "DEFAULT", defaultValue: Boolean) =
    IniPropertyWithDefault(iniFile, section, defaultValue) { it.toBoolean() }

/**
 * Creates a property delegate for reading double values from INI files with a default.
 */
fun iniDouble(iniFile: IniFile, section: String = "DEFAULT", defaultValue: Double) =
    IniPropertyWithDefault(iniFile, section, defaultValue) { it.toDouble() }

/**
 * Creates a read-write property delegate for string values in INI files.
 */
fun iniStringRW(iniFile: IniFile, section: String = "DEFAULT") = IniPropertyRW(iniFile, section)

/**
 * Creates a read-write property delegate for integer values in INI files.
 */
fun iniIntRW(iniFile: IniFile, section: String = "DEFAULT", defaultValue: Int) =
    IniPropertyRWWithDefault(iniFile, section, defaultValue, { it.toInt() }, { it.toString() })

/**
 * Creates a read-write property delegate for boolean values in INI files.
 */
fun iniBooleanRW(iniFile: IniFile, section: String = "DEFAULT", defaultValue: Boolean) =
    IniPropertyRWWithDefault(iniFile, section, defaultValue, { it.toBoolean() }, { it.toString() })

/**
 * Creates a read-write property delegate for double values in INI files.
 */
fun iniDoubleRW(iniFile: IniFile, section: String = "DEFAULT", defaultValue: Double) =
    IniPropertyRWWithDefault(iniFile, section, defaultValue, { it.toDouble() }, { it.toString() })