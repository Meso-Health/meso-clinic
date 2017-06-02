package org.watsi.uhp.runnables;

import android.widget.ListView;

public class ScrollToBottomRunnable implements Runnable {

    private final ListView mListView;

    public ScrollToBottomRunnable(ListView listView) {
        this.mListView = listView;
    }

    @Override
    public void run() {
        mListView.setSelection(mListView.getAdapter().getCount() - 1);
    }
}
