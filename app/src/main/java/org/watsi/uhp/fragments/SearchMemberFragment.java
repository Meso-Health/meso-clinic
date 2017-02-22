package org.watsi.uhp.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.SearchView;

import org.watsi.uhp.R;

public class SearchMemberFragment extends Fragment {

    private SearchView mMemberSearch;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(R.string.member_search_fragment_label);

        final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_member_search,
                container, false);

        mMemberSearch = (SearchView) view.findViewById(R.id.member_search);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        mMemberSearch.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mMemberSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context
                .INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,0);
    }
}
