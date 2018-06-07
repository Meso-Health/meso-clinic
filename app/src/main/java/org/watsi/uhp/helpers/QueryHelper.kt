package org.watsi.uhp.helpers

object QueryHelper {
    fun isSearchById(query: String) = (query.matches(Regex(".*\\d+.*")))
}
