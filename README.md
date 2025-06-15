Pykt
====

`pykt` is contains functionality I am missing from Kotlin's standard library.

Only created for the JVM target.

## Components

### [CSV Support](src/main/kotlin/dev/hirth/pykt/csv)

Read and write CSV files with [`CsvReader`](src/main/kotlin/dev/hirth/pykt/csv/CsvReader.kt) and [`CsvWriter`](src/main/kotlin/dev/hirth/pykt/csv/CsvWriter.kt).

Inspired by python's `csv` module.

### [Properties](src/main/kotlin/dev/hirth/pykt/properties)

Property delegation helpers.

### [Itertools](src/main/kotlin/dev/hirth/pykt/itertools)

Sequence utility functions, inspired by python's `itertools` module.

### [Statistics](src/main/kotlin/dev/hirth/pykt/statistics)

Simple statistics functionality, inspired by python's `statistics` module.

### [Memoization](src/main/kotlin/dev/hirth/pykt/Cache.kt)

Simple function cache (`Cache`) and thread-safe LRU caches (`LruCacheSync`/`LruCacheAsync`).

Inspired by the similar function caches of python's `functools` module.

