package org.watsi.uhp.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import org.watsi.uhp.R;
import org.watsi.uhp.presenters.SearchMemberPresenter;

public class SearchMemberFragment extends BaseFragment {

    private SearchMemberPresenter mPresenter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(R.string.member_search_fragment_label);

        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_member_search,
                container, false);

        mPresenter = new SearchMemberPresenter(
                new ProgressDialog(getContext(), ProgressDialog.STYLE_SPINNER),
                (ListView) view.findViewById(R.id.member_search_results),
                (TextView) view.findViewById(R.id.member_no_search_results_text),
                (SearchView) view.findViewById(R.id.member_search),
                getContext(), getNavigationManager());

        mPresenter.setupSearchListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mPresenter.focus();
    }
}
