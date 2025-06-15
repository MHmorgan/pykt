# S-expressions Usage Examples

## Basic Parsing

```kotlin
import dev.hirth.pykt.sexp.*

// Parse a simple S-expression
val sexp = "(hello world)".parseSexp()
println(sexp) // Prints: (hello world)

// Parse multiple S-expressions
val sexps = "atom (list 1 2 3) \"quoted string\"".parseSexps()
// Returns: [atom, (list 1 2 3), "quoted string"]

// Use callback-based parsing
"(a b) (c d)".parseSexp { sexp ->
    println("Parsed: $sexp")
}
```

## Working with S-expressions

```kotlin
// Create S-expressions programmatically
val atom = sexp("hello")
val list = sexp(sexp("config"), sexp("debug"), sexp("true"))

// Check types and extract values
if (sexp.isAtom()) {
    println("Atom value: ${sexp.atomValue()}")
}

if (sexp.isList()) {
    sexp.forEach { element ->
        println("Element: $element")
    }
}
```

## Configuration Files

```kotlin
// Parse configuration from string
val config = SexpConfig("""
    (server
     (host "localhost")
     (port 8080)
     (debug true)
     (features (auth logging)))
""")

// Access configuration values with type conversion
val host = config.getStringValue("server.host")      // "localhost"
val port = config.getIntValue("server.port")         // 8080
val debug = config.getBooleanValue("server.debug")   // true
val features = config.getStringListValue("server.features") // ["auth", "logging"]

// Safe access with null returns
val timeout = config.getInt("server.timeout")        // null (not found)

// Check if configuration keys exist
if (config.has("server.ssl")) {
    // Handle SSL configuration
}
```

## File Parsing

```kotlin
// Parse from file
val configFile = File("config.sexp")
val sexp = configFile.parseSexp()

// Or create config directly from file
val config = SexpConfig(configFile)
val appName = config.getStringValue("app.name")
```

## Error Handling

```kotlin
try {
    val sexp = "invalid ) syntax".parseSexp()
} catch (e: SexpParseException) {
    println("Parse error at position ${e.position}: ${e.message}")
}
```