package org.watsi.uhp.helpers

import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse

class QueryHelperTest {

    @Test
    fun validId() {
        val validId = "RWI096138"

        assertTrue(QueryHelper.isSearchById(validId))
    }

    @Test
    fun nonId() {
        val name = "Jim"

        assertFalse(QueryHelper.isSearchById(name))
    }
}
