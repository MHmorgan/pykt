package dev.hirth.pykt.ini

import kotlinx.serialization.Serializable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class IniSerializationTest {

    @Serializable
    data class User(
        val name: String,
        val account: Int,
        val alive: Boolean
    )

    @Test
    fun `should deserialize simple data class from INI string`() {
        val iniString = """
            name=John Doe
            account=12345
            alive=true
        """.trimIndent()

        val user = Ini.decodeFromString<User>(iniString)

        assertThat(user.name).isEqualTo("John Doe")
        assertThat(user.account).isEqualTo(12345)
        assertThat(user.alive).isTrue()
    }

    @Serializable
    data class Config(
        val debug: Boolean,
        val timeout: Double,
        val maxConnections: Int
    )

    @Test
    fun `should deserialize with various data types`() {
        val iniString = """
            debug=false
            timeout=30.5
            maxConnections=100
        """.trimIndent()

        val config = Ini.decodeFromString<Config>(iniString)

        assertThat(config.debug).isFalse()
        assertThat(config.timeout).isEqualTo(30.5)
        assertThat(config.maxConnections).isEqualTo(100)
    }

    @Serializable
    data class Settings(
        val flag1: Boolean,
        val flag2: Boolean,
        val flag3: Boolean,
        val flag4: Boolean
    )

    @Test
    fun `should handle various boolean formats`() {
        val iniString = """
            flag1=true
            flag2=1
            flag3=yes
            flag4=on
        """.trimIndent()

        val settings = Ini.decodeFromString<Settings>(iniString)

        assertThat(settings.flag1).isTrue()
        assertThat(settings.flag2).isTrue()
        assertThat(settings.flag3).isTrue()
        assertThat(settings.flag4).isTrue()
    }

    @Test
    fun `should handle false boolean formats`() {
        val iniString = """
            flag1=false
            flag2=0
            flag3=no
            flag4=off
        """.trimIndent()

        val settings = Ini.decodeFromString<Settings>(iniString)

        assertThat(settings.flag1).isFalse()
        assertThat(settings.flag2).isFalse()
        assertThat(settings.flag3).isFalse()
        assertThat(settings.flag4).isFalse()
    }

    @Serializable
    data class NumberTypes(
        val byteValue: Byte,
        val shortValue: Short,
        val intValue: Int,
        val longValue: Long,
        val floatValue: Float,
        val doubleValue: Double
    )

    @Test
    fun `should deserialize various number types`() {
        val iniString = """
            byteValue=127
            shortValue=32767
            intValue=2147483647
            longValue=9223372036854775807
            floatValue=3.14159
            doubleValue=2.718281828
        """.trimIndent()

        val numbers = Ini.decodeFromString<NumberTypes>(iniString)

        assertThat(numbers.byteValue).isEqualTo(127.toByte())
        assertThat(numbers.shortValue).isEqualTo(32767.toShort())
        assertThat(numbers.intValue).isEqualTo(2147483647)
        assertThat(numbers.longValue).isEqualTo(9223372036854775807L)
        assertThat(numbers.floatValue).isEqualTo(3.14159f)
        assertThat(numbers.doubleValue).isEqualTo(2.718281828)
    }

    @Test
    fun `should work with sections`() {
        val iniString = """
            [User]
            name=Jane Smith
            account=54321
            alive=false
        """.trimIndent()

        val user = Ini.decodeFromString<User>(iniString)

        assertThat(user.name).isEqualTo("Jane Smith")
        assertThat(user.account).isEqualTo(54321)
        assertThat(user.alive).isFalse()
    }

    @Test
    fun `should work with exact example from comment`() {
        @Serializable
        data class User(
            val name: String,
            val account: Int,
            val alive: Boolean,
        )

        val iniString = """
            name=Test User
            account=999
            alive=true
        """.trimIndent()

        val user = Ini.decodeFromString<User>(iniString)

        assertThat(user.name).isEqualTo("Test User")
        assertThat(user.account).isEqualTo(999)
        assertThat(user.alive).isTrue()
    }
}