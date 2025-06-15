package dev.hirth.pykt.ini

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class IniPropertyDelegatesTest {
    
    private val sampleIni = """
        globalKey=global_value
        debug=true
        port=8080
        timeout=30.5
        
        [database]
        host=localhost
        dbPort=5432
        ssl=false
        dbTimeout=60.0
        
        [cache]
        cacheEnabled=true
        cacheSize=1000
    """.trimIndent()
    
    @Test
    fun testReadOnlyStringProperty() {
        val iniFile = readIni(sampleIni)
        
        class Config {
            val globalKey by iniString(iniFile)
            val host by iniString(iniFile, "database")
            val nonExistent by iniString(iniFile)
        }
        
        val config = Config()
        assertEquals("global_value", config.globalKey)
        assertEquals("localhost", config.host)
        assertNull(config.nonExistent)
    }
    
    @Test
    fun testReadOnlyStringPropertyWithDefault() {
        val iniFile = readIni(sampleIni)
        
        class Config {
            val globalKey by iniString(iniFile, defaultValue = "default_global")
            val host by iniString(iniFile, "database", "default_host")
            val nonExistent by iniString(iniFile, defaultValue = "default_value")
        }
        
        val config = Config()
        assertEquals("global_value", config.globalKey)
        assertEquals("localhost", config.host)
        assertEquals("default_value", config.nonExistent)
    }
    
    @Test
    fun testReadOnlyIntProperty() {
        val iniFile = readIni(sampleIni)
        
        class Config {
            val port by iniInt(iniFile, defaultValue = 0)
            val dbPort by iniInt(iniFile, "database", 0)
            val cacheSize by iniInt(iniFile, "cache", 0)
            val nonExistent by iniInt(iniFile, defaultValue = 999)
        }
        
        val config = Config()
        assertEquals(8080, config.port)
        assertEquals(5432, config.dbPort)
        assertEquals(1000, config.cacheSize)
        assertEquals(999, config.nonExistent)
    }
    
    @Test
    fun testReadOnlyBooleanProperty() {
        val iniFile = readIni(sampleIni)
        
        class Config {
            val debug by iniBoolean(iniFile, defaultValue = false)
            val ssl by iniBoolean(iniFile, "database", true)
            val cacheEnabled by iniBoolean(iniFile, "cache", false)
            val nonExistent by iniBoolean(iniFile, defaultValue = true)
        }
        
        val config = Config()
        assertEquals(true, config.debug)
        assertEquals(false, config.ssl)
        assertEquals(true, config.cacheEnabled)
        assertEquals(true, config.nonExistent)
    }
    
    @Test
    fun testReadOnlyDoubleProperty() {
        val iniFile = readIni(sampleIni)
        
        class Config {
            val timeout by iniDouble(iniFile, defaultValue = 0.0)
            val dbTimeout by iniDouble(iniFile, "database", 0.0)
            val nonExistent by iniDouble(iniFile, defaultValue = 99.9)
        }
        
        val config = Config()
        assertEquals(30.5, config.timeout, 0.001)
        assertEquals(60.0, config.dbTimeout, 0.001)
        assertEquals(99.9, config.nonExistent, 0.001)
    }
    
    @Test
    fun testReadWriteStringProperty() {
        val iniFile = readIni(sampleIni)
        
        class Config {
            var globalKey by iniStringRW(iniFile)
            var host by iniStringRW(iniFile, "database")
            var newKey by iniStringRW(iniFile)
        }
        
        val config = Config()
        
        // Read existing values
        assertEquals("global_value", config.globalKey)
        assertEquals("localhost", config.host)
        assertNull(config.newKey)
        
        // Write new values
        config.globalKey = "new_global_value"
        config.host = "remote_host"
        config.newKey = "new_value"
        
        assertEquals("new_global_value", config.globalKey)
        assertEquals("remote_host", config.host)
        assertEquals("new_value", config.newKey)
        
        // Verify they're actually stored in the INI file
        assertEquals("new_global_value", iniFile.get("DEFAULT", "globalKey"))
        assertEquals("remote_host", iniFile.get("database", "host"))
        assertEquals("new_value", iniFile.get("DEFAULT", "newKey"))
        
        // Set to null should remove the key
        config.newKey = null
        assertNull(config.newKey)
        assertFalse(iniFile.has("DEFAULT", "newKey"))
    }
    
    @Test
    fun testReadWriteIntProperty() {
        val iniFile = readIni(sampleIni)
        
        class Config {
            var port by iniIntRW(iniFile, defaultValue = 0)
            var dbPort by iniIntRW(iniFile, "database", 0)
            var newPort by iniIntRW(iniFile, defaultValue = 9999)
        }
        
        val config = Config()
        
        // Read existing values
        assertEquals(8080, config.port)
        assertEquals(5432, config.dbPort)
        assertEquals(9999, config.newPort) // Default for non-existent key
        
        // Write new values
        config.port = 9090
        config.dbPort = 3306
        config.newPort = 8888
        
        assertEquals(9090, config.port)
        assertEquals(3306, config.dbPort)
        assertEquals(8888, config.newPort)
        
        // Verify they're actually stored in the INI file
        assertEquals("9090", iniFile.get("DEFAULT", "port"))
        assertEquals("3306", iniFile.get("database", "dbPort"))
        assertEquals("8888", iniFile.get("DEFAULT", "newPort"))
    }
    
    @Test
    fun testReadWriteBooleanProperty() {
        val iniFile = readIni(sampleIni)
        
        class Config {
            var debug by iniBooleanRW(iniFile, defaultValue = false)
            var ssl by iniBooleanRW(iniFile, "database", true)
            var newFlag by iniBooleanRW(iniFile, defaultValue = false)
        }
        
        val config = Config()
        
        // Read existing values
        assertEquals(true, config.debug)
        assertEquals(false, config.ssl)
        assertEquals(false, config.newFlag) // Default for non-existent key
        
        // Write new values
        config.debug = false
        config.ssl = true
        config.newFlag = true
        
        assertEquals(false, config.debug)
        assertEquals(true, config.ssl)
        assertEquals(true, config.newFlag)
        
        // Verify they're actually stored in the INI file
        assertEquals("false", iniFile.get("DEFAULT", "debug"))
        assertEquals("true", iniFile.get("database", "ssl"))
        assertEquals("true", iniFile.get("DEFAULT", "newFlag"))
    }
    
    @Test
    fun testReadWriteDoubleProperty() {
        val iniFile = readIni(sampleIni)
        
        class Config {
            var timeout by iniDoubleRW(iniFile, defaultValue = 0.0)
            var dbTimeout by iniDoubleRW(iniFile, "database", 0.0)
            var newTimeout by iniDoubleRW(iniFile, defaultValue = 123.45)
        }
        
        val config = Config()
        
        // Read existing values
        assertEquals(30.5, config.timeout, 0.001)
        assertEquals(60.0, config.dbTimeout, 0.001)
        assertEquals(123.45, config.newTimeout, 0.001) // Default for non-existent key
        
        // Write new values
        config.timeout = 45.6
        config.dbTimeout = 90.0
        config.newTimeout = 678.9
        
        assertEquals(45.6, config.timeout, 0.001)
        assertEquals(90.0, config.dbTimeout, 0.001)
        assertEquals(678.9, config.newTimeout, 0.001)
        
        // Verify they're actually stored in the INI file
        assertEquals("45.6", iniFile.get("DEFAULT", "timeout"))
        assertEquals("90.0", iniFile.get("database", "dbTimeout"))
        assertEquals("678.9", iniFile.get("DEFAULT", "newTimeout"))
    }
    
    @Test
    fun testTypeConversionErrors() {
        val iniFile = readIni("invalidNumber=not_a_number\ninvalidDouble=not_double")
        
        class Config {
            val invalidNumber by iniInt(iniFile, defaultValue = 999)
            val invalidDouble by iniDouble(iniFile, defaultValue = 99.9)
        }
        
        val config = Config()
        
        // Should return default values when conversion fails
        assertEquals(999, config.invalidNumber)
        assertEquals(99.9, config.invalidDouble, 0.001)
    }
}