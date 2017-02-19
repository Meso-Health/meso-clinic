package org.watsi.uhp.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;

import org.watsi.uhp.R;


public class SearchMemberFragment extends Fragment {

    SearchView memberSearch;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(R.string.member_search_fragment_label);

        final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_member_search,
                container, false);

        memberSearch = (SearchView) view.findViewById(R.id.member_search);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        memberSearch.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        return view;
    }
}
