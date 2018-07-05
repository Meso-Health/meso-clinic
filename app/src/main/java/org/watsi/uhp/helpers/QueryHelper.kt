package org.watsi.uhp.helpers

import org.watsi.domain.entities.IdentificationEvent

object QueryHelper {
    fun searchMethod(query: String): IdentificationEvent.SearchMethod? {
        return when {
            query.isEmpty() -> null
            query.matches(Regex(".*\\d+.*")) -> IdentificationEvent.SearchMethod.SEARCH_ID
            else -> IdentificationEvent.SearchMethod.SEARCH_NAME
        }
    }
}
