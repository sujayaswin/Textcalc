package com.example.textcalc.logic

object NumberExtractor {
    /**
     * Extracts all numerical values from a string and returns their sum.
     * It identifies numbers including decimals and negative values.
     * Lines starting with '#' are ignored.
     */
    fun extractAndSum(text: String): Double {
        val numberRegex = """-?\d+(\.\d+)?""".toRegex()
        return text.lineSequence()
            .filter { line -> line.trim().isNotEmpty() && !line.trim().startsWith("#") }
            .flatMap { line -> numberRegex.findAll(line).map { it.value.toDoubleOrNull() ?: 0.0 } }
            .sum()
    }

    /**
     * Extracts a list of all numerical values found in the text, ignoring lines starting with '#'.
     */
    fun extractNumbers(text: String): List<Double> {
        val numberRegex = """-?\d+(\.\d+)?""".toRegex()
        return text.lineSequence()
            .filter { line -> line.trim().isNotEmpty() && !line.trim().startsWith("#") }
            .flatMap { line -> numberRegex.findAll(line).map { it.value.toDoubleOrNull() ?: 0.0 } }
            .toList()
    }
}
