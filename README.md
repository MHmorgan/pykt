Pykt
====

`pykt` contains functionality missing from Kotlin's standard library.

Only created for the JVM target.

## Components

### [S-expressions](src/main/kotlin/dev/hirth/pykt/sexp)

Complete S-expression parsing and manipulation library with support for configuration files.

Inspired by OCaml's `Sexp` from `core`.

Key features:
- Kotlin-idiomatic API with extension functions (`String.parseSexp()`, `File.parseSexp()`)
- Callback-based parsing support
- Configuration file support with typed getters (`SexpConfig`)
- Comprehensive error handling and validation

Example usage:
```kotlin
// Parse S-expressions
val sexp = "(config (debug true) (port 8080))".parseSexp()

// Configuration file support with typed getters
val config = SexpConfig("""
    (server
     (host "localhost")
     (port 8080)
     (debug true)
     (features (auth logging)))
""")

val host = config.getStringValue("server.host")      // "localhost"
val port = config.getIntValue("server.port")         // 8080
val debug = config.getBooleanValue("server.debug")   // true
val features = config.getStringListValue("server.features") // ["auth", "logging"]
```

### [INI Support](src/main/kotlin/dev/hirth/pykt/ini)

Read and write INI/configuration files with comprehensive parsing support.

Inspired by Python's `configparser` module.

Key features:
- Parse INI files from strings, readers, or files
- Support for different INI dialects (standard, permissive, properties-style)
- Type-safe configuration access with property delegates
- Case-sensitive and case-insensitive parsing
- Support for multiline values and interpolation
- Comprehensive error handling

Example usage:
```kotlin
// Parse INI content
val iniContent = """
    app_name=My Web App
    debug=true
    port=8080
    
    [database]
    host=localhost
    port=5432
    ssl=true
""".trimIndent()

val iniFile = iniContent.parseIni()

// Basic access
val appName = iniFile.get("DEFAULT", "app_name")  // "My Web App"
val dbHost = iniFile.get("database", "host")      // "localhost"

// Type-safe property delegates
class AppConfig(iniFile: IniFile) {
    val appName by iniString(iniFile, defaultValue = "Default App")
    val debug by iniBoolean(iniFile, defaultValue = false)
    val port by iniInt(iniFile, defaultValue = 8080)
    val dbHost by iniString(iniFile, section = "database", key = "host")
}

val config = AppConfig(iniFile)
println("App: ${config.appName}, Debug: ${config.debug}")
```

### [CSV Support](src/main/kotlin/dev/hirth/pykt/csv)

Read and write CSV files with [`CsvReader`](src/main/kotlin/dev/hirth/pykt/csv/CsvReader.kt) and [`CsvWriter`](src/main/kotlin/dev/hirth/pykt/csv/CsvWriter.kt).

Inspired by python's `csv` module.

Key features:
- Parse CSV from strings, readers, or files
- Write CSV using fluent API
- Support for different CSV dialects (Excel, Unix, custom)
- Configurable delimiters, quoting, and escaping
- Dictionary-based reading and writing
- Proper handling of quoted fields and line breaks

Example usage:
```kotlin
// Reading CSV
val csvData = """
    name,age,city
    John,25,New York
    Jane,30,"Los Angeles"
    Bob,35,Chicago
""".trimIndent()

// Parse CSV rows
val rows = csvData.parseCsv().toList()
val headers = rows[0]  // ["name", "age", "city"]
val people = rows.drop(1)

// Using callback-based parsing
csvData.parseCsv { row ->
    if (row.lineNumber > 1) {  // Skip header
        println("${row.fields[0]} is ${row.fields[1]} years old")
    }
}

// Dictionary-based reading
val dictReader = csvData.parseCsvDict()
dictReader.forEach { record ->
    println("${record["name"]} lives in ${record["city"]}")
}

// Writing CSV
val output = buildCsvString {
    writeRow("Name", "Score", "Grade")
    writeRow("Alice", "95", "A")
    writeRow("Bob", "87", "B")
}

// Custom dialect
val tsvData = "name\tage\tcity\nJohn\t25\tNYC"
val tsvRows = tsvData.parseCsv(CsvDialect(delimiter = '\t')).toList()
```

### [TOML Support](src/main/kotlin/dev/hirth/pykt/toml)

Read and write TOML files with full support for [TOML v1.0.0](https://toml.io/en/v1.0.0) specification.

Features:
- Parse TOML documents from strings, readers, or files
- Write TOML documents using a Kotlin DSL
- Support for all TOML data types (strings, integers, floats, booleans, dates, arrays, tables)
- Inline tables and array of tables
- Comments and multi-line strings
- Proper escaping and Unicode support
- Idiomatic Kotlin API with extension functions

Example usage:
```kotlin
// Reading TOML
val document = """
    title = "My App"
    [database]
    host = "localhost"
    port = 5432
""".parseToml()

val title = document.getString("title")
val dbTable = document.getTable("database")

// Writing TOML
val toml = buildTomlString {
    string("title", "My App")
    table("database") {
        string("host", "localhost")
        integer("port", 5432)
    }
}
```

### [Properties](src/main/kotlin/dev/hirth/pykt/properties)

Property delegation helpers for reading values from maps with type conversion.

Key features:
- Type-safe property delegates for common types (String, Int, Long, Double, Float, Boolean)
- Support for nullable and non-nullable properties
- Default value handling
- JSON property delegates for complex types

Example usage:
```kotlin
// Configuration from a map
val config = mapOf(
    "app_name" to "My Application",
    "port" to "8080",
    "debug" to "true",
    "timeout" to "30.5"
)

class AppSettings {
    // Non-nullable properties with defaults
    val appName by StringRO(config) { "Default App" }
    val port by IntRO(config) { 3000 }
    val debug by BooleanRO(config) { false }
    val timeout by DoubleRO(config) { 60.0 }
    
    // Nullable properties
    val theme by StringRONull(config)
    val maxConnections by IntRONull(config)
}

val settings = AppSettings()
println("${settings.appName} running on port ${settings.port}")
println("Debug mode: ${settings.debug}, Timeout: ${settings.timeout}s")

// JSON property delegates
val jsonConfig = mapOf("user" to """{"name": "John", "age": 30}""")

class UserConfig {
    val user by JsonRO<User>(jsonConfig)
}
```

### [Itertools](src/main/kotlin/dev/hirth/pykt/itertools)

Sequence utility functions, inspired by python's `itertools` module.

Key features:
- Infinite sequence generators (`count`, `cycle`, `repeat`)
- Sequence manipulation (`islice`, `takeWhile`, `dropWhile`)
- Iterator duplication (`tee`)
- Lazy evaluation with Kotlin sequences

Example usage:
```kotlin
// Infinite sequences
val numbers = count(1, 2).take(5).toList()  // [1, 3, 5, 7, 9]
val cycling = listOf("A", "B", "C").cycle().take(7).toList()  // [A, B, C, A, B, C, A]
val repeated = "hello".repeat(3).toList()  // ["hello", "hello", "hello"]

// Sequence slicing
val data = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
val slice = islice(data, 2, 8, 2).toList()  // [2, 4, 6] (start=2, stop=8, step=2)

// Conditional iteration
val numbers2 = listOf(1, 2, 3, 4, 5, 6, 7)
val lessThanFive = numbers2.asSequence().takeWhile { it < 5 }.toList()  // [1, 2, 3, 4]
val afterThree = numbers2.asSequence().dropWhile { it <= 3 }.toList()  // [4, 5, 6, 7]

// Iterator duplication
val original = listOf(1, 2, 3, 4)
val (iter1, iter2) = original.tee()
val sum1 = iter1.sum()  // 10
val sum2 = iter2.sum()  // 10 (independent iterator)

// Practical example: pagination
fun <T> List<T>.paginate(pageSize: Int) = 
    chunked(pageSize).mapIndexed { page, items ->
        "Page ${page + 1}: $items"
    }

val items = (1..20).toList()
items.paginate(5).forEach { println(it) }
```

### [Statistics](src/main/kotlin/dev/hirth/pykt/statistics)

Simple statistics functionality, inspired by python's `statistics` module.

Key features:
- Basic statistical measures (mean, median, mode, variance, standard deviation)
- Support for different numeric types
- Key function support for complex data types
- Robust error handling for edge cases

Example usage:
```kotlin
// Basic statistics
val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

val average = mean(numbers)           // 5.5
val middle = median(numbers)          // 5.5
val spread = stdev(numbers)           // 3.03 (standard deviation)
val variance = variance(numbers)      // 9.17

// Working with different data types
val scores = listOf(85.5, 92.0, 88.5, 91.0, 87.5)
val avgScore = mean(scores)           // 88.9

// Using key functions for complex types
data class Student(val name: String, val grade: Int)
val students = listOf(
    Student("Alice", 95),
    Student("Bob", 87),
    Student("Charlie", 92)
)

val avgGrade = mean(students) { it.grade }        // 91.33
val medianGrade = median(students) { it.grade }   // 92.0

// Mode (most frequent value)
val grades = listOf(85, 90, 85, 92, 90, 85)
val mostCommon = mode(grades)         // 85

// Robust statistics
val dataWithOutliers = listOf(1, 2, 3, 4, 5, 100)
val robustMedian = median(dataWithOutliers)  // 3.5 (not affected by outlier)
val affectedMean = mean(dataWithOutliers)    // 19.17 (affected by outlier)
```

### [Memoization](src/main/kotlin/dev/hirth/pykt/Cache.kt)

Simple function cache (`Cache`) and thread-safe LRU caches (`LruCacheSync`/`LruCacheAsync`).

Inspired by the similar function caches of python's `functools` module.

Key features:
- Simple unbounded cache wrapper for functions
- Thread-safe LRU caches with configurable size limits
- Async cache for coroutines
- Cache statistics and pre-warming
- Manual cache eviction and clearing

Example usage:
```kotlin
// Simple function caching
fun expensiveCalculation(n: Int): Long = 
    if (n <= 1) 1L else expensiveCalculation(n - 1) + expensiveCalculation(n - 2)

val cachedFib = Cache(::expensiveCalculation)

// First call computes and caches
val fib10 = cachedFib(10)  // Slow first time
val fib10Again = cachedFib(10)  // Fast from cache

println("Cache stats: ${cachedFib.hitRate()}%")

// LRU Cache with size limit
val lruCache = LruCacheSync<String, String>(maxSize = 100) { key ->
    "Processed: $key"
}

val result = lruCache("input")  // Computes and caches
val cached = lruCache("input")  // Retrieved from cache

// Pre-warm cache
lruCache.prewarm(listOf("key1", "key2", "key3"))

// Async cache for coroutines
val asyncCache = LruCacheAsync<Int, String>(maxSize = 50) { key ->
    delay(100)  // Simulate async work
    "Result for $key"
}

// Usage in coroutines
runBlocking {
    val asyncResult = asyncCache(42)
    println(asyncResult)  // "Result for 42"
}

// Manual cache management
cachedFib.evict(10)      // Remove specific entry
cachedFib.clear()        // Clear all entries
lruCache.evictAll()      // Clear LRU cache
```

