package dev.hirth.pykt.toml

import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TomlSerializationTest {

    @Serializable
    data class User(
        val name: String,
        val age: Int,
        val active: Boolean
    )

    @Serializable
    data class Config(
        val title: String,
        val version: String,
        val database: Database
    )

    @Serializable
    data class Database(
        val host: String,
        val port: Int,
        val enabled: Boolean
    )

    @Serializable
    data class Server(
        val name: String,
        val ip: String
    )

    @Serializable
    data class ServerList(
        val servers: List<Server>
    )

    @Test
    fun testBasicDeserialization() {
        val tomlString = """
            name = "Alice"
            age = 30
            active = true
        """.trimIndent()

        val user = Toml.decodeFromString(User.serializer(), tomlString)
        assertEquals("Alice", user.name)
        assertEquals(30, user.age)
        assertTrue(user.active)
    }

    @Test
    fun testNestedObjectDeserialization() {
        val tomlString = """
            title = "My Application"
            version = "1.0.0"
            
            [database]
            host = "localhost"
            port = 5432
            enabled = true
        """.trimIndent()

        val config = Toml.decodeFromString(Config.serializer(), tomlString)
        assertEquals("My Application", config.title)
        assertEquals("1.0.0", config.version)
        assertEquals("localhost", config.database.host)
        assertEquals(5432, config.database.port)
        assertTrue(config.database.enabled)
    }

    @Test
    fun testInlineTableDeserialization() {
        val tomlString = """
            title = "My Application"
            version = "1.0.0"
            database = { host = "localhost", port = 5432, enabled = true }
        """.trimIndent()

        val config = Toml.decodeFromString(Config.serializer(), tomlString)
        assertEquals("My Application", config.title)
        assertEquals("1.0.0", config.version)
        assertEquals("localhost", config.database.host)
        assertEquals(5432, config.database.port)
        assertTrue(config.database.enabled)
    }

    @Test
    fun testArrayDeserialization() {
        val tomlString = """
            servers = [
                { name = "alpha", ip = "10.0.0.1" },
                { name = "beta", ip = "10.0.0.2" }
            ]
        """.trimIndent()

        val serverList = Toml.decodeFromString(ServerList.serializer(), tomlString)
        assertEquals(2, serverList.servers.size)
        assertEquals("alpha", serverList.servers[0].name)
        assertEquals("10.0.0.1", serverList.servers[0].ip)
        assertEquals("beta", serverList.servers[1].name)
        assertEquals("10.0.0.2", serverList.servers[1].ip)
    }

    @Test
    fun testMissingRequiredField() {
        val tomlString = """
            name = "Alice"
            age = 30
            # missing 'active' field
        """.trimIndent()

        assertThrows<Exception> {
            Toml.decodeFromString(User.serializer(), tomlString)
        }
    }

    @Test
    fun testTypeMismatch() {
        val tomlString = """
            name = "Alice"
            age = "thirty" # should be integer
            active = true
        """.trimIndent()

        assertThrows<Exception> {
            Toml.decodeFromString(User.serializer(), tomlString)
        }
    }

    @Test
    fun testEncodingNotSupported() {
        val user = User("Alice", 30, true)
        
        assertThrows<UnsupportedOperationException> {
            Toml.encodeToString(User.serializer(), user)
        }
    }

    @Serializable
    data class NumericTypes(
        val byteVal: Byte,
        val shortVal: Short,
        val intVal: Int,
        val longVal: Long,
        val floatVal: Float,
        val doubleVal: Double
    )

    @Test
    fun testNumericTypes() {
        val tomlString = """
            byteVal = 1
            shortVal = 2
            intVal = 3
            longVal = 4
            floatVal = 5.5
            doubleVal = 6.6
        """.trimIndent()

        val nums = Toml.decodeFromString(NumericTypes.serializer(), tomlString)
        assertEquals(1.toByte(), nums.byteVal)
        assertEquals(2.toShort(), nums.shortVal)
        assertEquals(3, nums.intVal)
        assertEquals(4L, nums.longVal)
        assertEquals(5.5f, nums.floatVal)
        assertEquals(6.6, nums.doubleVal)
    }

    @Serializable
    data class OptionalFields(
        val required: String,
        val optional: String? = null
    )

    @Test
    fun testOptionalFields() {
        val tomlString = """
            required = "value"
        """.trimIndent()

        val obj = Toml.decodeFromString(OptionalFields.serializer(), tomlString)
        assertEquals("value", obj.required)
        assertNull(obj.optional)
    }
}