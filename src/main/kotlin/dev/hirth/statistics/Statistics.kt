package dev.hirth.statistics

import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Exception thrown when statistics operations fail due to invalid data.
 */
class StatisticsError(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Calculate the arithmetic mean (average) of numeric data.
 */
fun <T : Number> mean(data: Iterable<T>): Double {
    val values = data.toList()
    if (values.isEmpty()) {
        throw StatisticsError("mean requires at least one data point")
    }
    return values.sumOf { it.toDouble() } / values.size
}

/**
 * Calculate the arithmetic mean using a key function.
 */
fun <T> mean(data: Iterable<T>, key: (T) -> Number): Double {
    return mean(data.map { key(it) })
}

/**
 * Calculate the median (middle value) of numeric data.
 */
fun <T : Number> median(data: Iterable<T>): Double {
    val values = data.map { it.toDouble() }.sorted()
    if (values.isEmpty()) {
        throw StatisticsError("median requires at least one data point")
    }
    
    val size = values.size
    return if (size % 2 == 0) {
        (values[size / 2 - 1] + values[size / 2]) / 2.0
    } else {
        values[size / 2]
    }
}

/**
 * Calculate the median using a key function.
 */
fun <T> median(data: Iterable<T>, key: (T) -> Number): Double {
    return median(data.map { key(it) })
}

/**
 * Calculate the mode (most common value) of data.
 * Returns the first mode found if there are multiple modes.
 */
fun <T> mode(data: Iterable<T>): T {
    val values = data.toList()
    if (values.isEmpty()) {
        throw StatisticsError("mode requires at least one data point")
    }
    
    val counts = values.groupingBy { it }.eachCount()
    val maxCount = counts.values.maxOrNull() ?: 0
    
    return counts.entries.first { it.value == maxCount }.key
}

/**
 * Calculate all modes (most common values) of data.
 */
fun <T> multimode(data: Iterable<T>): List<T> {
    val values = data.toList()
    if (values.isEmpty()) {
        throw StatisticsError("multimode requires at least one data point")
    }
    
    val counts = values.groupingBy { it }.eachCount()
    val maxCount = counts.values.maxOrNull() ?: 0
    
    return counts.entries.filter { it.value == maxCount }.map { it.key }
}

/**
 * Calculate the population variance.
 */
fun <T : Number> pvariance(data: Iterable<T>, mu: Double? = null): Double {
    val values = data.map { it.toDouble() }
    if (values.isEmpty()) {
        throw StatisticsError("pvariance requires at least one data point")
    }
    
    val mean = mu ?: mean(values)
    return values.sumOf { (it - mean).pow(2) } / values.size
}

/**
 * Calculate the sample variance.
 */
fun <T : Number> variance(data: Iterable<T>, xbar: Double? = null): Double {
    val values = data.map { it.toDouble() }
    if (values.size < 2) {
        throw StatisticsError("variance requires at least two data points")
    }
    
    val mean = xbar ?: mean(values)
    return values.sumOf { (it - mean).pow(2) } / (values.size - 1)
}

/**
 * Calculate the population standard deviation.
 */
fun <T : Number> pstdev(data: Iterable<T>, mu: Double? = null): Double {
    return sqrt(pvariance(data, mu))
}

/**
 * Calculate the sample standard deviation.
 */
fun <T : Number> stdev(data: Iterable<T>, xbar: Double? = null): Double {
    return sqrt(variance(data, xbar))
}

/**
 * Calculate the harmonic mean.
 */
fun <T : Number> harmonicMean(data: Iterable<T>): Double {
    val values = data.map { it.toDouble() }
    if (values.isEmpty()) {
        throw StatisticsError("harmonic_mean requires at least one data point")
    }
    
    if (values.any { it <= 0 }) {
        throw StatisticsError("harmonic_mean does not support zero or negative values")
    }
    
    return values.size / values.sumOf { 1.0 / it }
}

/**
 * Calculate the geometric mean.
 */
fun <T : Number> geometricMean(data: Iterable<T>): Double {
    val values = data.map { it.toDouble() }
    if (values.isEmpty()) {
        throw StatisticsError("geometric_mean requires at least one data point")
    }
    
    if (values.any { it <= 0 }) {
        throw StatisticsError("geometric_mean does not support zero or negative values")
    }
    
    val product = values.fold(1.0) { acc, value -> acc * value }
    return product.pow(1.0 / values.size)
}

/**
 * Divide data into n continuous intervals with equal probability.
 */
fun <T : Number> quantiles(data: Iterable<T>, n: Int = 4, method: QuantileMethod = QuantileMethod.EXCLUSIVE): List<Double> {
    val values = data.map { it.toDouble() }.sorted()
    if (values.isEmpty()) {
        throw StatisticsError("quantiles requires at least one data point")
    }
    
    if (n < 1) {
        throw StatisticsError("n must be at least 1")
    }
    
    if (n == 1) return emptyList()
    
    val result = mutableListOf<Double>()
    
    for (i in 1 until n) {
        val p = i.toDouble() / n
        val quantile = when (method) {
            QuantileMethod.INCLUSIVE -> quantileInclusive(values, p)
            QuantileMethod.EXCLUSIVE -> quantileExclusive(values, p)
        }
        result.add(quantile)
    }
    
    return result
}

/**
 * Method for calculating quantiles.
 */
enum class QuantileMethod {
    INCLUSIVE,
    EXCLUSIVE
}

private fun quantileInclusive(sortedValues: List<Double>, p: Double): Double {
    val n = sortedValues.size
    val m = n - 1
    val j = (p * m).toInt()
    val g = p * m - j
    
    return if (j < 0) {
        sortedValues[0]
    } else if (j >= m) {
        sortedValues[n - 1]
    } else {
        sortedValues[j] + g * (sortedValues[j + 1] - sortedValues[j])
    }
}

private fun quantileExclusive(sortedValues: List<Double>, p: Double): Double {
    val n = sortedValues.size
    val m = n + 1
    val j = (p * m).toInt()
    val g = p * m - j
    
    return if (j <= 0) {
        sortedValues[0]
    } else if (j >= n) {
        sortedValues[n - 1]
    } else {
        sortedValues[j - 1] + g * (sortedValues[j] - sortedValues[j - 1])
    }
}

/**
 * Extension functions for collections
 */

/**
 * Calculate mean of a numeric collection.
 */
fun <T : Number> Collection<T>.mean(): Double = mean(this)

/**
 * Calculate median of a numeric collection.
 */
fun <T : Number> Collection<T>.median(): Double = median(this)

/**
 * Calculate mode of a collection.
 */
fun <T> Collection<T>.mode(): T = mode(this)

/**
 * Calculate all modes of a collection.
 */
fun <T> Collection<T>.multimode(): List<T> = multimode(this)

/**
 * Calculate sample variance of a numeric collection.
 */
fun <T : Number> Collection<T>.variance(): Double = variance(this)

/**
 * Calculate population variance of a numeric collection.
 */
fun <T : Number> Collection<T>.pvariance(): Double = pvariance(this)

/**
 * Calculate sample standard deviation of a numeric collection.
 */
fun <T : Number> Collection<T>.stdev(): Double = stdev(this)

/**
 * Calculate population standard deviation of a numeric collection.
 */
fun <T : Number> Collection<T>.pstdev(): Double = pstdev(this)

/**
 * Calculate harmonic mean of a numeric collection.
 */
fun <T : Number> Collection<T>.harmonicMean(): Double = harmonicMean(this)

/**
 * Calculate geometric mean of a numeric collection.
 */
fun <T : Number> Collection<T>.geometricMean(): Double = geometricMean(this)

/**
 * Calculate quantiles of a numeric collection.
 */
fun <T : Number> Collection<T>.quantiles(n: Int = 4, method: QuantileMethod = QuantileMethod.EXCLUSIVE): List<Double> = 
    quantiles(this, n, method)