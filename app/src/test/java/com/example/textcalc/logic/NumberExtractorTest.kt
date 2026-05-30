package com.example.textcalc.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class NumberExtractorTest {

    @Test
    fun `extractAndSum returns sum of all numbers in text`() {
        val text = "Apples 5, Oranges 10.5, Discount -2"
        val expected = 13.5
        assertEquals(expected, NumberExtractor.extractAndSum(text), 0.001)
    }

    @Test
    fun `extractAndSum handles text with no numbers`() {
        val text = "No numbers here!"
        val expected = 0.0
        assertEquals(expected, NumberExtractor.extractAndSum(text), 0.001)
    }

    @Test
    fun `extractAndSum handles negative numbers`() {
        val text = "Income 100, Expense -40"
        val expected = 60.0
        assertEquals(expected, NumberExtractor.extractAndSum(text), 0.001)
    }

    @Test
    fun `extractAndSum handles multiple numbers per line`() {
        val text = "10 20 30\n40.5"
        val expected = 100.5
        assertEquals(expected, NumberExtractor.extractAndSum(text), 0.001)
    }

    @Test
    fun `extractNumbers returns list of all numbers`() {
        val text = "Items: 1.5, 2, 3"
        val expected = listOf(1.5, 2.0, 3.0)
        assertEquals(expected, NumberExtractor.extractNumbers(text))
    }

    @Test
    fun `extractAndSum ignores lines starting with hash`() {
        val text = """
            # This is a comment 100
            Apples 5
            # Another comment 50
            Oranges 10
        """.trimIndent()
        val expected = 15.0
        assertEquals(expected, NumberExtractor.extractAndSum(text), 0.001)
    }
}
