package dev.hirth.pykt.sexp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File

class SexpConfigTest {

    @Test
    fun testBasicConfiguration() {
        val configText = """
            (config
             (server
              (host "localhost")
              (port 8080)
              (debug true))
             (database
              (url "jdbc:postgresql://localhost/mydb")
              (user "admin")))
        """

        val config = SexpConfig(configText)

        assertEquals("localhost", config.getString("server.host"))
        assertEquals("8080", config.getString("server.port"))
        assertEquals("true", config.getString("server.debug"))
        assertEquals("jdbc:postgresql://localhost/mydb", config.getString("database.url"))
        assertEquals("admin", config.getString("database.user"))

        assertNull(config.getString("nonexistent.path"))
    }

    @Test
    fun testTypedGetters() {
        val configText = """
            (config
             (numbers
              (int 42)
              (long 9876543210)
              (double 3.14159)
              (negative -123))
             (flags
              (debug true)
              (verbose false)
              (enabled yes)
              (disabled no)))
        """

        val config = SexpConfig(configText)

        // Test integer parsing
        assertEquals(42, config.getInt("numbers.int"))
        assertEquals(9876543210L, config.getLong("numbers.long"))
        assertEquals(3.14159, config.getDouble("numbers.double")!!, 0.0001)
        assertEquals(-123, config.getInt("numbers.negative"))

        // Test boolean parsing
        assertTrue(config.getBoolean("flags.debug")!!)
        assertFalse(config.getBoolean("flags.verbose")!!)
        assertTrue(config.getBoolean("flags.enabled")!!)
        assertFalse(config.getBoolean("flags.disabled")!!)

        // Test required value getters
        assertEquals(42, config.getIntValue("numbers.int"))
        assertEquals(3.14159, config.getDoubleValue("numbers.double"), 0.0001)
        assertTrue(config.getBooleanValue("flags.debug"))
    }

    @Test
    fun testListConfiguration() {
        val configText = """
            (config
             (servers ("web1" "web2" "web3"))
             (ports (8080 8081 8082))
             (nested 
              ((name "service1") (port 9000))
              ((name "service2") (port 9001))))
        """

        val config = SexpConfig(configText)

        val servers = config.getStringList("servers")
        assertEquals(listOf("web1", "web2", "web3"), servers)

        val ports = config.getStringListValue("ports")
        assertEquals(listOf("8080", "8081", "8082"), ports)

        val nested = config.getList("nested")
        assertNotNull(nested)
        assertEquals(2, nested!!.size)
    }

    @Test
    fun testFileConfiguration() {
        val tempFile = File.createTempFile("config-test", ".sexp")
        tempFile.deleteOnExit()
        tempFile.writeText(
            """
            (app
             (name "MyApp")
             (version "1.0.0")
             (features (auth database logging)))
        """.trimIndent()
        )

        val config = SexpConfig(tempFile)

        assertEquals("MyApp", config.getStringValue("app.name"))
        assertEquals("1.0.0", config.getStringValue("app.version"))

        val features = config.getStringListValue("app.features")
        assertEquals(listOf("auth", "database", "logging"), features)
    }

    @Test
    fun testHasMethod() {
        val configText = "(config (existing value))"
        val config = SexpConfig(configText)

        assertTrue(config.has("config.existing"))
        assertFalse(config.has("config.missing"))
        assertFalse(config.has("nonexistent"))
    }

    @Test
    fun testToMap() {
        val configText = """
            (config
             (server
              (host "localhost")
              (port 8080))
             (debug true))
        """

        val config = SexpConfig(configText)
        val map = config.toMap()

        assertTrue(map.containsKey("config"))
        assertTrue(map.containsKey("config.server"))
        assertTrue(map.containsKey("config.server.host"))
        assertTrue(map.containsKey("config.server.port"))
        assertTrue(map.containsKey("config.debug"))

        assertEquals(Sexp.atom("localhost"), map["config.server.host"])
        assertEquals(Sexp.atom("8080"), map["config.server.port"])
        assertEquals(Sexp.atom("true"), map["config.debug"])
    }

    @Test
    fun testErrorCases() {
        val config = SexpConfig("(test (value 42))")

        // Test missing required values
        assertThrows<IllegalArgumentException> {
            config.getValue("missing.path")
        }

        assertThrows<IllegalArgumentException> {
            config.getStringValue("missing.path")
        }

        // Test invalid number format
        val invalidConfig = SexpConfig("(test (invalid abc))")
        assertThrows<NumberFormatException> {
            invalidConfig.getIntValue("test.invalid")
        }

        // Test invalid boolean values
        val invalidBoolConfig = SexpConfig("(test (invalid maybe))")
        assertNull(invalidBoolConfig.getBoolean("test.invalid"))

        assertThrows<IllegalArgumentException> {
            invalidBoolConfig.getBooleanValue("test.invalid")
        }
    }

    @Test
    fun testComplexNesting() {
        val configText = """
            (application
             (server
              (http
               (host "0.0.0.0")
               (port 8080)
               (ssl
                (enabled true)
                (keystore "/path/to/keystore")
                (password "secret")))
              (workers 4))
             (logging
              (level "INFO")
              (outputs ("console" "file"))
              (file
               (path "/var/log/app.log")
               (max-size "10MB"))))
        """

        val config = SexpConfig(configText)

        assertEquals("0.0.0.0", config.getString("application.server.http.host"))
        assertEquals(8080, config.getInt("application.server.http.port"))
        assertTrue(config.getBooleanValue("application.server.http.ssl.enabled"))
        assertEquals("/path/to/keystore", config.getString("application.server.http.ssl.keystore"))
        assertEquals(4, config.getInt("application.server.workers"))

        assertEquals("INFO", config.getString("application.logging.level"))
        val outputs = config.getStringList("application.logging.outputs")
        assertEquals(listOf("console", "file"), outputs)
        assertEquals("/var/log/app.log", config.getString("application.logging.file.path"))
        assertEquals("10MB", config.getString("application.logging.file.max-size"))
    }

    @Test
    fun testEdgeCases() {
        // Empty configuration
        val emptyConfig = SexpConfig("()")
        assertFalse(emptyConfig.has("anything"))

        // Single atom configuration
        val atomConfig = SexpConfig("atom")
        assertFalse(atomConfig.has("anything"))

        // Configuration with empty strings and special values
        val specialConfig = SexpConfig(
            """
            (config
             (empty "")
             (whitespace "   ")
             (zero 0)
             (negative -1))
        """
        )

        assertEquals("", specialConfig.getString("config.empty"))
        assertEquals("   ", specialConfig.getString("config.whitespace"))
        assertEquals(0, specialConfig.getInt("config.zero"))
        assertEquals(-1, specialConfig.getInt("config.negative"))
    }
}