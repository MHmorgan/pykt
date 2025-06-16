package dev.hirth.pykt.ini

/**
 * Example demonstrating INI file functionality.
 */
fun main() {
    // Example INI content for a web application configuration
    val configContent = """
        # Global application settings
        app_name=My Web App
        debug=true
        port=8080
        timeout=30.5
        
        [database]
        host=localhost
        port=5432
        username=admin
        password=secret123
        ssl=true
        max_connections=20
        
        [logging]
        level=INFO
        file=/var/log/app.log
        format=%(asctime)s - %(name)s - %(levelname)s - %(message)s
        
        [cache]
        enabled=true
        size=1000
        ttl=3600
        
        [features]
        feature_a=enabled
        feature_b=disabled
        experimental_feature=
    """.trimIndent()

    println("=== INI File Example ===")
    println()

    // 1. Parse INI content
    val iniFile = configContent.parseIni()

    println("1. Basic INI parsing:")
    println("App name: ${iniFile.get("DEFAULT", "app_name")}")
    println("Debug mode: ${iniFile.get("DEFAULT", "debug")}")
    println("Database host: ${iniFile.get("database", "host")}")
    println("Database port: ${iniFile.get("database", "port")}")
    println()

    // 2. Convert to flat map for easy access
    val configMap = configContent.parseIniMap()
    println("2. Flat map representation:")
    configMap.entries.take(5).forEach { (key, value) ->
        println("$key = $value")
    }
    println("... and ${configMap.size - 5} more entries")
    println()

    // 3. Type-safe configuration using property delegates
    class AppConfig(iniFile: IniFile) {
        // Global settings
        val appName by iniString(iniFile, defaultValue = "Default App")
        val debug by iniBoolean(iniFile, defaultValue = false)
        val port by iniInt(iniFile, defaultValue = 8080)
        val timeout by iniDouble(iniFile, defaultValue = 30.0)

        // Database settings
        val dbHost by iniString(iniFile, "database", "localhost")
        val dbPort by iniInt(iniFile, "database", 5432)
        val dbUsername by iniString(iniFile, "database", "")
        val dbSsl by iniBoolean(iniFile, "database", false)
        val maxConnections by iniInt(iniFile, "database", 10)

        // Cache settings
        val cacheEnabled by iniBoolean(iniFile, "cache", false)
        val cacheSize by iniInt(iniFile, "cache", 100)
        val cacheTtl by iniInt(iniFile, "cache", 1800)

        // Features
        val featureA by iniString(iniFile, "features", "disabled")
        val featureB by iniString(iniFile, "features", "disabled")
    }

    val config = AppConfig(iniFile)

    println("3. Type-safe configuration access:")
    println("App: ${config.appName} (debug: ${config.debug})")
    println("Server: port ${config.port}, timeout ${config.timeout}s")
    println("Database: ${config.dbUsername}@${config.dbHost}:${config.dbPort} (SSL: ${config.dbSsl})")
    println("Cache: enabled=${config.cacheEnabled}, size=${config.cacheSize}, TTL=${config.cacheTtl}s")
    println("Features: A=${config.featureA}, B=${config.featureB}")
    println()

    // 4. Modify configuration
    println("4. Modifying configuration:")
    iniFile.set("DEFAULT", "debug", "false")
    iniFile.set("database", "max_connections", "50")
    iniFile.set("features", "new_feature", "enabled")

    println("Configuration successfully modified.")
    println("Debug mode now: ${iniFile.get("DEFAULT", "debug")}")
    println("Max connections now: ${iniFile.get("database", "max_connections")}")
    println("New feature: ${iniFile.get("features", "new_feature")}")
    println()

    // 5. Demonstrate different dialects
    println("5. Different dialects:")

    // Java Properties style
    val propertiesContent = """
        app.name=Properties App
        app.debug=true
        database.host=localhost
        database.port=5432
    """.trimIndent()

    val propertiesFile = propertiesContent.parseIni(IniDialect.PROPERTIES)
    println("Properties style (flat structure):")
    propertiesFile.toFlatMap().forEach { (key, value) ->
        println("$key = $value")
    }
    println()

    // Case-sensitive dialect
    val caseSensitiveContent = """
        [Section]
        Key=Value1
        
        [section]
        key=Value2
    """.trimIndent()

    val caseSensitiveFile = caseSensitiveContent.parseIni(IniDialect(caseSensitive = true))
    println("Case-sensitive parsing:")
    println("Section.Key = ${caseSensitiveFile.get("Section", "Key")}")
    println("section.key = ${caseSensitiveFile.get("section", "key")}")
    println("Number of sections: ${caseSensitiveFile.sectionNames.size}")
    println()

    println("=== End of INI Example ===")
}