package dev.hirth.pykt.toml

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime

class TomlWriterTest {

    @Test
    fun testBasicKeyValuePairs() {
        val toml = buildTomlString {
            string("title", "TOML Example")
            integer("number", 42)
            float("float_val", 3.14)
            boolean("boolean_val", true)
        }

        val expected = """
            title = "TOML Example"
            number = 42
            float_val = 3.14
            boolean_val = true
            
        """.trimIndent()

        assertEquals(expected, toml)
    }

    @Test
    fun testArrays() {
        val toml = buildTomlString {
            array("numbers", 1, 2, 3)
            array("strings", "a", "b", "c")
            array("mixed", listOf(1, "hello", true))
        }

        // Parse the generated TOML back to verify it's valid
        val document = toml.parseToml()
        assertEquals(listOf(1L, 2L, 3L), document.getIntegerArray("numbers"))
        assertEquals(listOf("a", "b", "c"), document.getStringArray("strings"))
        
        val mixed = document.getArray("mixed")!!
        assertEquals(TomlValue.Integer(1), mixed[0])
        assertEquals(TomlValue.String("hello"), mixed[1])
        assertEquals(TomlValue.Boolean(true), mixed[2])
    }

    @Test
    fun testInlineTable() {
        val toml = buildTomlString {
            inlineTable("point") {
                integer("x", 1)
                integer("y", 2)
            }
        }

        val document = toml.parseToml()
        val point = document.getInlineTable("point")!!

        assertEquals(TomlValue.Integer(1), point["x"])
        assertEquals(TomlValue.Integer(2), point["y"])
    }

    @Test
    fun testTable() {
        val toml = buildTomlString {
            table("server") {
                string("host", "localhost")
                integer("port", 8080)
            }
        }

        val document = toml.parseToml()
        val serverTable = document.getTable("server")!!

        assertEquals("localhost", (serverTable.values["host"] as TomlValue.String).value)
        assertEquals(8080L, (serverTable.values["port"] as TomlValue.Integer).value)
    }

    @Test
    fun testNestedTables() {
        val toml = buildTomlString {
            table("database.connection") {
                string("host", "localhost")
                integer("port", 5432)
            }
            table("database.auth") {
                string("username", "admin")
                string("password", "secret")
            }
        }

        val document = toml.parseToml()
        val dbTable = document.getTable("database")!!
        val connectionTable = dbTable.tables["connection"]!!
        val authTable = dbTable.tables["auth"]!!

        assertEquals("localhost", (connectionTable.values["host"] as TomlValue.String).value)
        assertEquals(5432L, (connectionTable.values["port"] as TomlValue.Integer).value)
        assertEquals("admin", (authTable.values["username"] as TomlValue.String).value)
        assertEquals("secret", (authTable.values["password"] as TomlValue.String).value)
    }

    @Test
    fun testArrayOfTables() {
        val toml = buildTomlString {
            arrayTable("products") {
                table {
                    string("name", "Hammer")
                    integer("sku", 738594937)
                }
                table {
                    string("name", "Nail")
                    integer("sku", 284758393)
                }
            }
        }

        val document = toml.parseToml()
        val products = document.rootTable.arrayTables["products"]!!

        assertEquals(2, products.size)
        assertEquals("Hammer", (products[0].values["name"] as TomlValue.String).value)
        assertEquals(738594937L, (products[0].values["sku"] as TomlValue.Integer).value)
        assertEquals("Nail", (products[1].values["name"] as TomlValue.String).value)
        assertEquals(284758393L, (products[1].values["sku"] as TomlValue.Integer).value)
    }

    @Test
    fun testStringEscaping() {
        val toml = buildTomlString {
            string("basic", "I'm a string. \"You can quote me\". Name\tJosé\nLocation\tSF.")
            string("path", "C:\\Users\\nodejs\\templates")
        }

        val document = toml.parseToml()
        assertEquals("I'm a string. \"You can quote me\". Name\tJosé\nLocation\tSF.", document.getString("basic"))
        assertEquals("C:\\Users\\nodejs\\templates", document.getString("path"))
    }

    @Test
    fun testNumbers() {
        val toml = buildTomlString {
            integer("integer1", 99)
            integer("integer2", 42)
            integer("integer3", 0)
            integer("integer4", -17)
            
            float("float1", 1.0)
            float("float2", 3.1415926)
            float("float3", -0.01)
            float("float4", 5e+22)
            
            float("float7", Double.POSITIVE_INFINITY)
            float("float8", Double.NEGATIVE_INFINITY)
            float("float9", Double.NaN)
        }

        val document = toml.parseToml()

        assertEquals(99L, document.getInteger("integer1"))
        assertEquals(42L, document.getInteger("integer2"))
        assertEquals(0L, document.getInteger("integer3"))
        assertEquals(-17L, document.getInteger("integer4"))

        assertEquals(1.0, document.getFloat("float1"))
        assertEquals(3.1415926, document.getFloat("float2"))
        assertEquals(-0.01, document.getFloat("float3")!!, 0.00001)
        assertEquals(5e+22, document.getFloat("float4"))

        assertTrue(document.getFloat("float7")!!.isInfinite())
        assertTrue(document.getFloat("float8")!!.isInfinite())
        assertTrue(document.getFloat("float9")!!.isNaN())
    }

    @Test
    fun testBooleans() {
        val toml = buildTomlString {
            boolean("bool1", true)
            boolean("bool2", false)
        }

        val document = toml.parseToml()
        assertEquals(true, document.getBoolean("bool1"))
        assertEquals(false, document.getBoolean("bool2"))
    }

    @Test
    fun testRoundTrip() {
        val originalToml = """
            title = "TOML Example"
            
            [owner]
            name = "Tom Preston-Werner"
            
            [database]
            server = "192.168.1.1"
            ports = [8001, 8001, 8002]
            connection_max = 5000
            enabled = true
            
            [[products]]
            name = "Hammer"
            sku = 738594937
            
            [[products]]
            name = "Nail"
            sku = 284758393
        """.trimIndent()

        // Parse the TOML
        val document = originalToml.parseToml()
        
        // Convert back to TOML string
        val regeneratedToml = document.toTomlString()
        
        // Parse the regenerated TOML
        val regeneratedDocument = regeneratedToml.parseToml()
        
        // Verify the content is the same
        assertEquals("TOML Example", regeneratedDocument.getString("title"))
        
        val owner = regeneratedDocument.getTable("owner")!!
        assertEquals("Tom Preston-Werner", (owner.values["name"] as TomlValue.String).value)
        
        val database = regeneratedDocument.getTable("database")!!
        assertEquals("192.168.1.1", (database.values["server"] as TomlValue.String).value)
        assertEquals(listOf(8001L, 8001L, 8002L), 
            (database.values["ports"] as TomlValue.Array).value.map { (it as TomlValue.Integer).value })
        assertEquals(5000L, (database.values["connection_max"] as TomlValue.Integer).value)
        assertEquals(true, (database.values["enabled"] as TomlValue.Boolean).value)
        
        val products = regeneratedDocument.rootTable.arrayTables["products"]!!
        assertEquals(2, products.size)
        assertEquals("Hammer", (products[0].values["name"] as TomlValue.String).value)
        assertEquals(738594937L, (products[0].values["sku"] as TomlValue.Integer).value)
        assertEquals("Nail", (products[1].values["name"] as TomlValue.String).value)
        assertEquals(284758393L, (products[1].values["sku"] as TomlValue.Integer).value)
    }

    @Test
    fun testComplexDocument() {
        val toml = buildTomlString {
            string("title", "TOML Example")
            
            table("owner") {
                string("name", "Tom Preston-Werner")
            }
            
            table("database") {
                string("server", "192.168.1.1")
                array("ports", 8001, 8001, 8002)
                integer("connection_max", 5000)
                boolean("enabled", true)
            }
            
            table("servers.alpha") {
                string("ip", "10.0.0.1")
                string("dc", "eqdc10")
            }
            
            table("servers.beta") {
                string("ip", "10.0.0.2")
                string("dc", "eqdc10")
            }
            
            table("clients") {
                array("hosts", "alpha", "omega")
            }
        }

        val document = toml.parseToml()

        assertEquals("TOML Example", document.getString("title"))
        
        val owner = document.getTable("owner")!!
        assertEquals("Tom Preston-Werner", (owner.values["name"] as TomlValue.String).value)
        
        val database = document.getTable("database")!!
        assertEquals("192.168.1.1", (database.values["server"] as TomlValue.String).value)
        assertEquals(listOf(8001L, 8001L, 8002L), 
            (database.values["ports"] as TomlValue.Array).value.map { (it as TomlValue.Integer).value })
        assertEquals(5000L, (database.values["connection_max"] as TomlValue.Integer).value)
        assertEquals(true, (database.values["enabled"] as TomlValue.Boolean).value)
        
        val servers = document.getTable("servers")!!
        val alpha = servers.tables["alpha"]!!
        assertEquals("10.0.0.1", (alpha.values["ip"] as TomlValue.String).value)
        assertEquals("eqdc10", (alpha.values["dc"] as TomlValue.String).value)
        
        val beta = servers.tables["beta"]!!
        assertEquals("10.0.0.2", (beta.values["ip"] as TomlValue.String).value)
        assertEquals("eqdc10", (beta.values["dc"] as TomlValue.String).value)
        
        val clients = document.getTable("clients")!!
        val hosts = (clients.values["hosts"] as TomlValue.Array).value
        assertEquals("alpha", (hosts[0] as TomlValue.String).value)
        assertEquals("omega", (hosts[1] as TomlValue.String).value)
    }

    @Test
    fun testEmptyDocument() {
        val toml = buildTomlString { }
        assertEquals("", toml.trim())
    }
}