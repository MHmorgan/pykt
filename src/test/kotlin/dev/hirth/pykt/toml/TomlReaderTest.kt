package dev.hirth.pykt.toml

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime

class TomlReaderTest {

    @Test
    fun testBasicKeyValuePairs() {
        val toml = """
            title = "TOML Example"
            number = 42
            float_val = 3.14
            boolean_val = true
        """.trimIndent()

        val document = toml.parseToml()

        assertEquals("TOML Example", document.getString("title"))
        assertEquals(42L, document.getInteger("number"))
        assertEquals(3.14, document.getFloat("float_val"))
        assertEquals(true, document.getBoolean("boolean_val"))
    }

    @Test
    fun testArrays() {
        val toml = """
            numbers = [1, 2, 3]
            strings = ["a", "b", "c"]
            mixed = [1, "hello", true]
        """.trimIndent()

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
        val toml = """
            point = { x = 1, y = 2 }
        """.trimIndent()

        val document = toml.parseToml()
        val point = document.getInlineTable("point")!!

        assertEquals(TomlValue.Integer(1), point["x"])
        assertEquals(TomlValue.Integer(2), point["y"])
    }

    @Test
    fun testTable() {
        val toml = """
            [server]
            host = "localhost"
            port = 8080
        """.trimIndent()

        val document = toml.parseToml()
        val serverTable = document.getTable("server")!!

        assertEquals("localhost", (serverTable.values["host"] as TomlValue.String).value)
        assertEquals(8080L, (serverTable.values["port"] as TomlValue.Integer).value)
    }

    @Test
    fun testNestedTables() {
        val toml = """
            [database.connection]
            host = "localhost"
            port = 5432
            
            [database.auth]
            username = "admin"
            password = "secret"
        """.trimIndent()

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
        val toml = """
            [[products]]
            name = "Hammer"
            sku = 738594937
            
            [[products]]
            name = "Nail"
            sku = 284758393
        """.trimIndent()

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
        val toml = """
            basic = "I'm a string. \"You can quote me\". Name\tJosé\nLocation\tSF."
            literal = 'C:\Users\nodejs\templates'
        """.trimIndent()

        val document = toml.parseToml()

        assertEquals("I'm a string. \"You can quote me\". Name\tJosé\nLocation\tSF.", document.getString("basic"))
        assertEquals("C:\\Users\\nodejs\\templates", document.getString("literal"))
    }

    @Test
    fun testNumbers() {
        val toml = """
            integer1 = +99
            integer2 = 42
            integer3 = 0
            integer4 = -17
            
            float1 = +1.0
            float2 = 3.1415926
            float3 = -0.01
            float4 = 5e+22
            float5 = 1e06
            float6 = -2E-2
            
            float7 = inf
            float8 = -inf
            float9 = nan
        """.trimIndent()

        val document = toml.parseToml()

        assertEquals(99L, document.getInteger("integer1"))
        assertEquals(42L, document.getInteger("integer2"))
        assertEquals(0L, document.getInteger("integer3"))
        assertEquals(-17L, document.getInteger("integer4"))

        assertEquals(1.0, document.getFloat("float1"))
        assertEquals(3.1415926, document.getFloat("float2"))
        assertEquals(-0.01, document.getFloat("float3")!!, 0.00001)
        assertEquals(5e+22, document.getFloat("float4"))
        assertEquals(1e06, document.getFloat("float5"))
        assertEquals(-2E-2, document.getFloat("float6"))

        assertTrue(document.getFloat("float7")!!.isInfinite())
        assertTrue(document.getFloat("float8")!!.isInfinite())
        assertTrue(document.getFloat("float9")!!.isNaN())
    }

    @Test
    fun testBooleans() {
        val toml = """
            bool1 = true
            bool2 = false
        """.trimIndent()

        val document = toml.parseToml()

        assertEquals(true, document.getBoolean("bool1"))
        assertEquals(false, document.getBoolean("bool2"))
    }

    @Test
    fun testComments() {
        val toml = """
            # This is a comment
            key1 = "value1" # This is also a comment
            
            # This is another comment
            key2 = "value2"
        """.trimIndent()

        val document = toml.parseToml()

        assertEquals("value1", document.getString("key1"))
        assertEquals("value2", document.getString("key2"))
        assertEquals(2, document.rootTable.values.size)
    }

    @Test
    fun testEmptyDocument() {
        val toml = ""
        val document = toml.parseToml()
        assertTrue(document.rootTable.values.isEmpty())
        assertTrue(document.rootTable.tables.isEmpty())
    }

    @Test
    fun testComplexExample() {
        val toml = """
            # This is a TOML document
            
            title = "TOML Example"
            
            [owner]
            name = "Tom Preston-Werner"
            dob = 1979-05-27T07:32:00-08:00
            
            [database]
            server = "192.168.1.1"
            ports = [ 8001, 8001, 8002 ]
            connection_max = 5000
            enabled = true
            
            [servers]
            
              [servers.alpha]
              ip = "10.0.0.1"
              dc = "eqdc10"
              
              [servers.beta]
              ip = "10.0.0.2"
              dc = "eqdc10"
              
            [clients]
            data = [ ["gamma", "delta"], [1, 2] ]
            
            hosts = [
              "alpha",
              "omega"
            ]
        """.trimIndent()

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
}