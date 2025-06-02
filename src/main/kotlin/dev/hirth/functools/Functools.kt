/**
 * Kotlin implementation of Python's functools module.
 * 
 * This package provides utility functions for higher-order functions and operations on callable objects.
 * 
 * Core utilities:
 * - [partial] - Partial function application
 * - [cache] and [lruCache] - Function memoization
 * - [reduce] - Cumulative function application
 * - [cachedProperty] - Cached property delegates
 * - [wraps] - Function wrapper metadata preservation
 * - [singledispatch] - Single-dispatch generic functions
 */
@file:JvmName("Functools")

package dev.hirth.functools

// Re-export key functions for easy access
// All implementation details are in their respective files