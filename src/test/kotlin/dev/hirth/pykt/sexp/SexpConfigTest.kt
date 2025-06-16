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

    @Test
    fun testPropertyDelegation() {
        val configText = """
            (server
             (host "localhost")
             (port 8080)
             (ssl
              (enabled true)
              (keystore "/path/to/keystore"))
             (features (auth logging))
             (timeout 30.5))
        """

        val config = SexpConfig(configText)

        // String delegation
        val host: String by config.stringDelegate("server.host")
        val keystorePath: String? by config.nullableStringDelegate("server.ssl.keystore")
        val nonExistent: String? by config.nullableStringDelegate("server.nonexistent")

        assertEquals("localhost", host)
        assertEquals("/path/to/keystore", keystorePath)
        assertNull(nonExistent)

        // Integer delegation
        val port: Int by config.intDelegate("server.port")
        val maxConnections: Int? by config.nullableIntDelegate("server.max-connections")

        assertEquals(8080, port)
        assertNull(maxConnections)

        // Boolean delegation
        val sslEnabled: Boolean by config.booleanDelegate("server.ssl.enabled")
        val debugMode: Boolean? by config.nullableBooleanDelegate("server.debug")

        assertTrue(sslEnabled)
        assertNull(debugMode)

        // Double delegation
        val timeout: Double by config.doubleDelegate("server.timeout")
        val maxSize: Double? by config.nullableDoubleDelegate("server.max-size")

        assertEquals(30.5, timeout, 0.001)
        assertNull(maxSize)

        // List delegation
        val features: List<String> by config.stringListDelegate("server.features")
        val plugins: List<String>? by config.nullableStringListDelegate("server.plugins")

        assertEquals(listOf("auth", "logging"), features)
        assertNull(plugins)
    }

    @Test
    fun testPropertyDelegationExceptions() {
        val config = SexpConfig("(server (host \"localhost\"))")

        // Test exceptions for required delegates
        assertThrows<IllegalArgumentException> {
            val nonExistent: String by config.stringDelegate("server.nonexistent")
            nonExistent // Access the property to trigger the exception
        }

        assertThrows<IllegalArgumentException> {
            val nonExistent: Int by config.intDelegate("server.nonexistent")
            nonExistent
        }

        assertThrows<NumberFormatException> {
            val invalidInt: Int by config.intDelegate("server.host") // "localhost" is not a valid integer
            invalidInt
        }
    }

    @Test
    fun testVariableDefinitions() {
        val configText = """
            (define project-name "Smith")
            (define base-port 8000)
            (define feature-list (auth logging metrics))
            
            (project
             (name project-name)
             (server
              (host "localhost")
              (port base-port)
              (features feature-list)))
        """

        val config = SexpConfig(configText, true)

        assertEquals("Smith", config.getStringValue("project.name"))
        assertEquals(8000, config.getIntValue("project.server.port"))
        assertEquals("localhost", config.getStringValue("project.server.host"))
        assertEquals(listOf("auth", "logging", "metrics"), config.getStringListValue("project.server.features"))
    }

    @Test
    fun testVariableDefinitionsWithNestedValues() {
        val configText = """
            (define db-host "localhost")
            (define db-port 5432)
            (define db-config (host db-host port db-port database "myapp"))
            
            (application
             (database db-config))
        """

        val config = SexpConfig(configText, true)

        assertEquals("localhost", config.getStringValue("application.database.host"))
        assertEquals(5432, config.getIntValue("application.database.port"))
        assertEquals("myapp", config.getStringValue("application.database.database"))
    }

    @Test
    fun testVariableDefinitionsWithoutSupport() {
        val configText = """
            (define project-name "Smith")
            (project
             (name project-name))
        """

        // Without variable support, 'define' should be treated as a regular list
        val config = SexpConfig(configText, false)

        // The define statement should be present as a regular list
        assertTrue(config.has("define"))
        assertEquals("Smith", config.getStringValue("define.project-name"))
        
        // The variable reference should remain as-is
        assertEquals("project-name", config.getStringValue("project.name"))
    }

    @Test
    fun testVariableDefinitionsInExtensions() {
        val configText = """
            (define api-key "secret-key-123")
            (define timeout 30)
            
            (service
             (auth
              (key api-key)
              (timeout timeout)))
        """

        // Test parsing with variables using extension functions
        val config = SexpConfig(configText, true)

        assertEquals("secret-key-123", config.getStringValue("service.auth.key"))
        assertEquals(30, config.getIntValue("service.auth.timeout"))
    }

    @Test
    fun testVariableErrors() {
        // Test undefined variable reference
        val configWithUndefinedVar = """
            (project
             (name undefined-variable))
        """

        val config = SexpConfig(configWithUndefinedVar, true)
        // Undefined variables should remain as atoms
        assertEquals("undefined-variable", config.getStringValue("project.name"))

        // Test malformed define statement
        assertThrows<SexpParseException> {
            val malformedDefine = """
                (define)
                (project (name "test"))
            """
            SexpConfig(malformedDefine, true)
        }

        assertThrows<SexpParseException> {
            val malformedDefine = """
                (define var-name)
                (project (name "test"))
            """
            SexpConfig(malformedDefine, true)
        }
    }
}