package org.watsi.uhp.helpers

import android.widget.SearchView
import org.watsi.domain.entities.IdentificationEvent

object QueryHelper {
    const val QUERY_DELAY_IN_MS = 500L

    fun searchMethod(query: String): IdentificationEvent.SearchMethod? {
        return when {
            query.isEmpty() -> null
            query.matches(Regex(".*\\d+.*")) -> IdentificationEvent.SearchMethod.SEARCH_ID
            else -> IdentificationEvent.SearchMethod.SEARCH_NAME
        }
    }

    /**
     * Custom OnQueryTextListener that will only update the query after the user has not typed
     * anything for QUERY_DELAY_IN_MS milliseconds, effectively throttling the search.
     *
     * How it works: when the user changes the query, onQueryTextChange will kick off a call to
     * updateQuery that's delayed by QUERY_DELAY_IN_MS. If the user changes the query again
     * at any time before the delay is up, that delayed call will be removed without running,
     * and a new delayed call with the new query will be kicked off.
     *
     * @param searchView The SearchView to attach to.
     * @param updateQuery Callback method that handles the query change.
     */
    class ThrottledQueryListener(val searchView: SearchView, val updateQuery: (query: String) -> Unit) : SearchView.OnQueryTextListener {
        var searchRunnable: Runnable? = null

        override fun onQueryTextSubmit(query: String?): Boolean = true

        override fun onQueryTextChange(newText: String?): Boolean {
            searchRunnable?.let {
                searchView.removeCallbacks(it)
            }
            searchRunnable = Runnable({
                newText?.let { updateQuery(newText) }
            })
            searchView.postDelayed(searchRunnable, QUERY_DELAY_IN_MS)

            return true
        }
    }
}
