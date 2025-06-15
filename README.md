Pykt
====

`pykt` is contains functionality I am missing from Kotlin's standard library.

Only created for the JVM target.

## Components

### CSV Support

Read and write CSV files with [`CsvReader`](src/main/kotlin/dev/hirth/pykt/csv/CsvReader.kt) and `CsvWriter`.

Inspired by python's `csv` module.

### Properties

Property delegation helpers.

### Itertools

Sequence utility functions, inspired by python's `itertools` module.

### Statistics

Simple statistics functionality, inspired by python's `statistics` module.

### Memoization

Memoization functionality is implemented in `Cache`, inspired by the
similar function caches of python's `functools` module.

