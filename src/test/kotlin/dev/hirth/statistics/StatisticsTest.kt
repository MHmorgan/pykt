package dev.hirth.statistics

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import kotlin.math.abs

class StatisticsTest {

    private fun assertAlmostEquals(expected: Double, actual: Double, delta: Double = 1e-9) {
        assertTrue(abs(expected - actual) < delta, "Expected $expected but was $actual")
    }

    @Test
    fun testMean() {
        assertEquals(3.0, mean(listOf(1, 2, 3, 4, 5)))
        assertEquals(2.5, mean(listOf(1.0, 2.0, 3.0, 4.0)))
        assertEquals(10.0, mean(listOf(10)))
        
        assertThrows<StatisticsError> {
            mean(emptyList<Int>())
        }
    }

    @Test
    fun testMeanWithKey() {
        val data = listOf("a", "bb", "ccc")
        assertEquals(2.0, mean(data) { it.length })
    }

    @Test
    fun testMeanExtension() {
        assertEquals(3.0, listOf(1, 2, 3, 4, 5).mean())
    }

    @Test
    fun testMedian() {
        assertEquals(3.0, median(listOf(1, 2, 3, 4, 5)))
        assertEquals(2.5, median(listOf(1, 2, 3, 4)))
        assertEquals(10.0, median(listOf(10)))
        assertEquals(2.0, median(listOf(3, 1, 2))) // unsorted input
        
        assertThrows<StatisticsError> {
            median(emptyList<Int>())
        }
    }

    @Test
    fun testMedianWithKey() {
        val data = listOf("a", "bb", "ccc", "dddd")
        assertEquals(2.5, median(data) { it.length })
    }

    @Test
    fun testMedianExtension() {
        assertEquals(3.0, listOf(1, 2, 3, 4, 5).median())
    }

    @Test
    fun testMode() {
        assertEquals(2, mode(listOf(1, 2, 2, 3, 4)))
        assertEquals("a", mode(listOf("a", "b", "a", "c")))
        assertEquals(1, mode(listOf(1))) // single value
        
        assertThrows<StatisticsError> {
            mode(emptyList<Int>())
        }
    }

    @Test
    fun testModeExtension() {
        assertEquals(2, listOf(1, 2, 2, 3, 4).mode())
    }

    @Test
    fun testMultimode() {
        val result1 = multimode(listOf(1, 1, 2, 2, 3))
        assertEquals(setOf(1, 2), result1.toSet())
        
        val result2 = multimode(listOf(1, 2, 3))
        assertEquals(setOf(1, 2, 3), result2.toSet()) // All values have same frequency
        
        assertThrows<StatisticsError> {
            multimode(emptyList<Int>())
        }
    }

    @Test
    fun testMultimodeExtension() {
        assertEquals(setOf(1, 2), listOf(1, 1, 2, 2, 3).multimode().toSet())
    }

    @Test
    fun testVariance() {
        val data = listOf(1.0, 2.0, 3.0, 4.0, 5.0)
        assertAlmostEquals(2.5, variance(data))
        
        assertThrows<StatisticsError> {
            variance(listOf(1.0))
        }
        
        assertThrows<StatisticsError> {
            variance(emptyList<Double>())
        }
    }

    @Test
    fun testVarianceWithMean() {
        val data = listOf(1.0, 2.0, 3.0, 4.0, 5.0)
        assertAlmostEquals(2.5, variance(data, 3.0))
    }

    @Test
    fun testVarianceExtension() {
        assertAlmostEquals(2.5, listOf(1.0, 2.0, 3.0, 4.0, 5.0).variance())
    }

    @Test
    fun testPvariance() {
        val data = listOf(1.0, 2.0, 3.0, 4.0, 5.0)
        assertAlmostEquals(2.0, pvariance(data))
        
        assertThrows<StatisticsError> {
            pvariance(emptyList<Double>())
        }
    }

    @Test
    fun testPvarianceWithMean() {
        val data = listOf(1.0, 2.0, 3.0, 4.0, 5.0)
        assertAlmostEquals(2.0, pvariance(data, 3.0))
    }

    @Test
    fun testPvarianceExtension() {
        assertAlmostEquals(2.0, listOf(1.0, 2.0, 3.0, 4.0, 5.0).pvariance())
    }

    @Test
    fun testStdev() {
        val data = listOf(1.0, 2.0, 3.0, 4.0, 5.0)
        assertAlmostEquals(1.5811388300841898, stdev(data))
        
        assertThrows<StatisticsError> {
            stdev(listOf(1.0))
        }
    }

    @Test
    fun testStdevExtension() {
        assertAlmostEquals(1.5811388300841898, listOf(1.0, 2.0, 3.0, 4.0, 5.0).stdev())
    }

    @Test
    fun testPstdev() {
        val data = listOf(1.0, 2.0, 3.0, 4.0, 5.0)
        assertAlmostEquals(1.4142135623730951, pstdev(data))
    }

    @Test
    fun testPstdevExtension() {
        assertAlmostEquals(1.4142135623730951, listOf(1.0, 2.0, 3.0, 4.0, 5.0).pstdev())
    }

    @Test
    fun testHarmonicMean() {
        val data = listOf(1.0, 2.0, 4.0)
        assertAlmostEquals(1.7142857142857142, harmonicMean(data))
        
        assertThrows<StatisticsError> {
            harmonicMean(emptyList<Double>())
        }
        
        assertThrows<StatisticsError> {
            harmonicMean(listOf(1.0, 0.0, 2.0))
        }
        
        assertThrows<StatisticsError> {
            harmonicMean(listOf(1.0, -1.0, 2.0))
        }
    }

    @Test
    fun testHarmonicMeanExtension() {
        assertAlmostEquals(1.7142857142857142, listOf(1.0, 2.0, 4.0).harmonicMean())
    }

    @Test
    fun testGeometricMean() {
        val data = listOf(1.0, 2.0, 4.0, 8.0)
        assertAlmostEquals(2.8284271247461903, geometricMean(data))
        
        assertThrows<StatisticsError> {
            geometricMean(emptyList<Double>())
        }
        
        assertThrows<StatisticsError> {
            geometricMean(listOf(1.0, 0.0, 2.0))
        }
        
        assertThrows<StatisticsError> {
            geometricMean(listOf(1.0, -1.0, 2.0))
        }
    }

    @Test
    fun testGeometricMeanExtension() {
        assertAlmostEquals(2.8284271247461903, listOf(1.0, 2.0, 4.0, 8.0).geometricMean())
    }

    @Test
    fun testQuantiles() {
        val data = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val quartiles = quantiles(data, 4)
        
        assertEquals(3, quartiles.size)
        // These values depend on the exact quantile algorithm
        assertTrue(quartiles[0] > 2.0 && quartiles[0] < 4.0) // Q1
        assertTrue(quartiles[1] > 4.0 && quartiles[1] < 7.0) // Q2 (median)
        assertTrue(quartiles[2] > 7.0 && quartiles[2] < 9.0) // Q3
    }

    @Test
    fun testQuantilesInclusive() {
        val data = listOf(1, 2, 3, 4, 5)
        val quartiles = quantiles(data, 4, QuantileMethod.INCLUSIVE)
        
        assertEquals(3, quartiles.size)
    }

    @Test
    fun testQuantilesEdgeCases() {
        assertThrows<StatisticsError> {
            quantiles(emptyList<Int>(), 4)
        }
        
        assertThrows<StatisticsError> {
            quantiles(listOf(1, 2, 3), 0)
        }
        
        // n=1 should return empty list
        assertEquals(emptyList<Double>(), quantiles(listOf(1, 2, 3), 1))
    }

    @Test
    fun testQuantilesExtension() {
        val data = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val quartiles = data.quantiles(4)
        
        assertEquals(3, quartiles.size)
    }

    @Test
    fun testMixedNumericTypes() {
        val data = listOf(1, 2.5, 3L, 4.0f)
        assertAlmostEquals(2.625, mean(data))
        assertAlmostEquals(2.75, median(data))
    }

    @Test
    fun testLargeDataset() {
        val data = (1..1000).toList()
        assertEquals(500.5, mean(data))
        assertEquals(500.5, median(data))
    }

    @Test
    fun testStatisticsErrorMessages() {
        val exception = assertThrows<StatisticsError> {
            mean(emptyList<Int>())
        }
        assertTrue(exception.message!!.contains("mean requires at least one data point"))
    }

    @Test
    fun testIdenticalValues() {
        val data = listOf(5, 5, 5, 5, 5)
        assertEquals(5.0, mean(data))
        assertEquals(5.0, median(data))
        assertEquals(5, mode(data))
        assertEquals(0.0, variance(data))
        assertEquals(0.0, pvariance(data))
        assertEquals(0.0, stdev(data))
        assertEquals(0.0, pstdev(data))
    }

    @Test
    fun testNegativeNumbers() {
        val data = listOf(-5, -3, -1, 1, 3, 5)
        assertEquals(0.0, mean(data))
        assertEquals(0.0, median(data))
    }
}