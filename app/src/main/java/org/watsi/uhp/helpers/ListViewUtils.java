package org.watsi.uhp.helpers;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

// Code is copied from: https://stackoverflow.com/questions/17693578/android-how-to-display-2-listviews-in-one-activity-one-after-the-other
public class ListViewUtils {
    public static final int SLACK_FOR_MULTILINE_TEXT_VIEWS = 50;
    public static void setDynamicHeight(ListView mListView) {
        ListAdapter mListAdapter = mListView.getAdapter();
        if (mListAdapter == null) {
            return;
        }
        int height = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(mListView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        for (int i = 0; i < mListAdapter.getCount(); i++) {
            View listItem = mListAdapter.getView(i, null, mListView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            height += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = mListView.getLayoutParams();
        params.height = SLACK_FOR_MULTILINE_TEXT_VIEWS + height + (mListView.getDividerHeight() * (mListAdapter.getCount() - 1));
        mListView.setLayoutParams(params);
        mListView.requestLayout();
    }
}
