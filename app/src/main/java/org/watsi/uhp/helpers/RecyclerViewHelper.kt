package org.watsi.uhp.helpers

import android.content.Context
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import org.watsi.uhp.R

object RecyclerViewHelper {

    /**
     * Sets up a standard vertically-oriented RecyclerView with dividers.
     * @param [nestedScrollEnabled] set to false if the RecyclerView is in a NestedScrollView and
     * you don't want the RecyclerView to have its own nested scrolling
     */
    fun <VH : RecyclerView.ViewHolder> setRecyclerView(
            recyclerView: RecyclerView,
            adapter: RecyclerView.Adapter<VH>,
            context: Context,
            nestedScrollingEnabled: Boolean = true
    ) {
        val layoutManager = LinearLayoutManager(context)
        val listItemDivider = DividerItemDecoration(context, layoutManager.orientation)
        listItemDivider.setDrawable(context.getDrawable(R.drawable.list_divider))
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(listItemDivider)
        recyclerView.isNestedScrollingEnabled = nestedScrollingEnabled
    }

    fun scrollToBottom(recyclerView: RecyclerView) {
        recyclerView.scrollToPosition(recyclerView.adapter.itemCount - 1)
    }
}
