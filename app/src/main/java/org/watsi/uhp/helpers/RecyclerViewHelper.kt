package org.watsi.uhp.helpers

import android.support.v7.widget.RecyclerView

object RecyclerViewHelper {

    fun scrollToBottom(recyclerView: RecyclerView) {
        recyclerView.scrollToPosition(recyclerView.adapter.itemCount - 1)
    }
}
