package org.watsi.uhp.helpers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.watsi.domain.entities.IdentificationEvent

class QueryHelperTest {

    @Test
    fun searchMethod_empty_returnsNull() {
        val query = ""

        assertNull(QueryHelper.searchMethod(query))
    }

    @Test
    fun searchMethod_includesNumber_returnsSEARCH_CARD_ID() {
        val query = "RWI096138"

        assertEquals(IdentificationEvent.SearchMethod.SEARCH_CARD_ID, QueryHelper.searchMethod(query))
    }

    @Test
    fun searchMethod_doesNotIncludeNumber_returnsSEARCH_NAME() {
        val query = "Jim"

        assertEquals(IdentificationEvent.SearchMethod.SEARCH_NAME, QueryHelper.searchMethod(query))
    }
}
