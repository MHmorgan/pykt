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

### [CSV Support](src/main/kotlin/dev/hirth/pykt/csv)

Read and write CSV files with [`CsvReader`](src/main/kotlin/dev/hirth/pykt/csv/CsvReader.kt) and [`CsvWriter`](src/main/kotlin/dev/hirth/pykt/csv/CsvWriter.kt).

Inspired by python's `csv` module.

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

Property delegation helpers.

### [Itertools](src/main/kotlin/dev/hirth/pykt/itertools)

Sequence utility functions, inspired by python's `itertools` module.

### [Statistics](src/main/kotlin/dev/hirth/pykt/statistics)

Simple statistics functionality, inspired by python's `statistics` module.

### [Memoization](src/main/kotlin/dev/hirth/pykt/Cache.kt)

Simple function cache (`Cache`) and thread-safe LRU caches (`LruCacheSync`/`LruCacheAsync`).

Inspired by the similar function caches of python's `functools` module.

