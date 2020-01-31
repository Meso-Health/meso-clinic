package org.watsi.uhp.utils

import org.apache.commons.text.similarity.LevenshteinDistance

// This util is based on the answer in the following stackoverflow answer
// https://stackoverflow.com/questions/5859561/getting-the-closest-string-match
object FuzzySearchUtil {
    private val distanceAlg = LevenshteinDistance()
    // this compares the two strings together splitting them by words to check for full word transposition
    // or partial string match (not beginning at the first word)
    // could split on multiple delims / have a max # splits
    fun wordByWordValue(query: String, choice: String): Int {
        val queryWords = query.toLowerCase().split(" ")
        val choiceWords = choice.toLowerCase().split(" ")
        var wordsTotal = 0
        outerLoop@ for (qWord in queryWords) {
            var wordBest = choice.length
            innerLoop@ for (cWord in choiceWords) {
                val currDistance = distanceAlg.apply(qWord, cWord)
                if (currDistance < wordBest) {
                    wordBest = currDistance
                }
                if (currDistance == 0) {
                    break@innerLoop
                }
            }
            wordsTotal = wordsTotal + wordBest
        }
        return wordsTotal
    }

    // This compares the two strings together ignoring any delimiters that would indicate words
    fun fullStringValue(query: String, choice: String): Int {
        return distanceAlg.apply(query.toLowerCase(), choice.toLowerCase())
    }

    fun matchValue(query: String, choice: String, fullStringValue: Int, wordByWordValue: Int, substringValue: Int): Double {
        // These two weight determine if you want to give preference to a string that matches
        // the un-split choice vs. any one of the delimited words
        val fullStringWeight = 0.5
        val wordByWordWeight = 0.8

        // This is penalizing long strings less for differences than short strings
        val lengthWeight = -0.1

        // This is giving preference to queries that are an exact substring of a choice
        val substringWeight = 2.5

        // These are set such that you don't have to have a good match for both full / words
        // Whichever is a better score will be a larger portion of the final score
        val minWeight = 0.8
        val maxWeight = 0.2
        return (Math.min(fullStringWeight * fullStringValue, wordByWordWeight * wordByWordValue) * minWeight
                + Math.max(fullStringWeight * fullStringValue, wordByWordWeight * wordByWordValue) * maxWeight
                + lengthWeight * Math.abs(query.length - choice.length)
                + substringValue * substringWeight)
    }

    fun topMatches(query: String, choices: List<String>?, limit: Int, threshold: Double = 6.0): List<String> {
        if (choices == null || query.isBlank()) {
            return emptyList()
        }

        val processedQuery = query.replace("\\s+".toRegex()," ")

        val best = choices.map { choice ->
            val fullStringValue = fullStringValue(processedQuery, choice)
            val wordByWordValue = wordByWordValue(processedQuery, choice)
            val substringValue = if (choice.contains(processedQuery, true)) -1 else 1
            val value = matchValue(processedQuery, choice, fullStringValue, wordByWordValue, substringValue)

            Pair(choice, value)
        }
        return best.filter { it.second < threshold }.sortedBy { it.second }.map{ it.first }.take(limit)
    }
}
