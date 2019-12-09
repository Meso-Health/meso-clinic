package org.watsi.uhp.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class FuzzySearchUtilTest {
    private val englishChoiceList = listOf<String>(
        "Kitone Justin",
        "Alinda Aod",
        "Njeffrey",
        "Alindon Jeffry",
        "Kahwa James",
        "Twikirize Cissy",
        "Tumusime Agnes",
        "Rugumayo Tadeo",
        "Mujuni Brian",
        "Tumusiime Edger",
        "MURUNGI PETRA",
        "Kyosa Silver",
        "Businge Patrick",
        "Mugisa Julius",
        "Atuyambe James",
        "Ninsiima Gorret",
        "Akampulira Andrew",
        "Amanyire Erias",
        "kebirungi",
        "iratukunda emerine",
        "Rukidi James",
        "Businge Simon",
        "kabarungi queen",
        "Katuhaise Goretti",
        "Akampa Brighton",
        "Twesige Rymond",
        "Tukole Edwin",
        "Amutuhiire Bonitah",
        "Tumwebaze Ambrose",
        "Atugarukiremu Benard",
        "Namatovu Bridgette",
        "Gafabusa Yohan",
        "Byaruhanga Tomas",
        "Tugume John",
        "Mwesige John",
        "Kataiki Grace",
        "Akanyijuka Praise",
        "Akankwasa Eria",
        "Birungi Scovia",
        "Nabireba God"
    )

    private val duplicateNameList = listOf<String>(
        "Alinda Alinda",
        "Alinda Alinda",
        "Alinda Alinda",
        "Alinda Alinda",
        "Alinda Alinda",
        "Alinda Alinda",
        "Alinda Alinda",
        "Alinda Alinda",
        "Alinda Alinda",
        "Alinda Alinda",
        "Alinda Alinda"
    )

    private val symbolsList = listOf<String>(
        "$100 dollahs",
        "maïs",
        "30% of the time",
        "chanel #5",
        "^_^",
        "+/-twelve",
        "simon & garfunkel"
    )

    private val nonEnglishChoiceList = listOf<String>(
        "አበባ",
        "አበራሽ",
        "አለማየሁ",
        "ብርሃኔ",
        "እስክንድር",
        "ሓረገ ወይን",
        "ሉሊት",
        "ቴዎድሮስ",
        "ዮሐንስ"
    )

    @Test
    fun searchEmptyList_EmptyQuery() {
        val topMatches = FuzzySearchUtil.topMatches("", emptyList(), 5);
        assertEquals(topMatches, emptyList<String>())
    }

    @Test
    fun searchEmptyList_SomeQuery() {
        val topMatches = FuzzySearchUtil.topMatches("some", emptyList(), 5);
        assertEquals(topMatches, emptyList<String>())
    }

    @Test
    fun searchList_emptyQuery() {
        val topMatches = FuzzySearchUtil.topMatches("", englishChoiceList, 5);
        assertEquals(topMatches, emptyList<String>())
    }

    @Test
    fun searchListWithDuplicates() {
        val choices = englishChoiceList + duplicateNameList
        val topMatches = FuzzySearchUtil.topMatches("alind", choices, 5);
        assertEquals(topMatches.size, 5)
    }

    @Test
    fun searchSymbolsList() {
        val choices = englishChoiceList + symbolsList
        val topMatches = FuzzySearchUtil.topMatches("alind", choices, 5);
        assertEquals(topMatches.size, 5)
    }

    @Test
    fun searchNonEnglishList_englishQuery() {
        val topMatches = FuzzySearchUtil.topMatches("alind", nonEnglishChoiceList, 5);
        assertEquals(topMatches.size, 5)
    }

    @Test
    fun searchNonEnglishList_nonEnglishQuery() {
        val topMatches = FuzzySearchUtil.topMatches("አበባ", nonEnglishChoiceList, 5);
        assertEquals(topMatches.size, 5)
    }

    @Test
    fun searchEnglishList_nonEnglishQuery() {
        val topMatches = FuzzySearchUtil.topMatches("አበባ", nonEnglishChoiceList, 5);
        assertEquals(topMatches.size, 5)
    }

    @Test
    fun queryWithMultipleSpaces() {
        val topMatches = FuzzySearchUtil.topMatches("alind    a", englishChoiceList, 5);
        assertEquals(topMatches.size, 1)
    }

    @Test
    fun queryOnlyWhiteSpace() {
        val topMatches = FuzzySearchUtil.topMatches("    ", englishChoiceList, 5);
        assertEquals(topMatches.size, 0)
    }

    @Test
    fun searchLimit() {
        val choices = englishChoiceList + duplicateNameList
        val topMatches = FuzzySearchUtil.topMatches("Alinda Alinda", choices, 5);
        assert(topMatches.size <= 5)
    }

    @Test
    fun searchThreshold() {
        val query = "Jeffry"
        val threshold = 9.0
        val topMatches = FuzzySearchUtil.topMatches(query, englishChoiceList, 5, threshold);
        topMatches.forEach { match ->
            val full = FuzzySearchUtil.fullStringValue(query, match)
            val word = FuzzySearchUtil.wordByWordValue(query, match)
            val sub = if (match.contains(query, true)) -1 else 1
            val matchValue = FuzzySearchUtil.matchValue(query, match, full, word, sub)
            assert(matchValue <= threshold)
        }
    }
}
