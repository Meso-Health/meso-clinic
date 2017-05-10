package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import org.watsi.uhp.R;
import org.watsi.uhp.adapters.MemberAdapter;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.KeyboardManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.List;

public class SearchMemberFragment extends BaseFragment {

    private SearchView mMemberSearch;
    private ListView mSearchList;
    private TextView mNoSearchResultsMessage;
    private RelativeLayout mLoadingPanel;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(R.string.member_search_fragment_label);

        final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_member_search,
                container, false);

        mMemberSearch = (SearchView) view.findViewById(R.id.member_search);
        mSearchList = (ListView) view.findViewById(R.id.member_search_results);
        mNoSearchResultsMessage = (TextView) view.findViewById(R.id.member_no_search_results_text);
        mLoadingPanel = (RelativeLayout) view.findViewById(R.id.loading_panel);

        setMemberSearch();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMemberSearch.requestFocus();
    }

    private void setMemberSearch() {
        // necessary to automatically show the search keyboard when requestFocus() is called
        mMemberSearch.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    // for SearchViews, in order to properly show the search keyboard, we need to
                    // use findFocus() to grab and pass a view *inside* of the SearchView
                    KeyboardManager.showKeyboard(view.findFocus(), getContext());
                }
            }
        });

        mMemberSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange (String newText){
                //no-op
                return true;
            }

            @Override
            public boolean onQueryTextSubmit (String query){
                performMemberSearch(query);
                KeyboardManager.hideKeyboard(mMemberSearch, getContext());
                return true;
            }
        });
    }

    private boolean containsNumber(String str) {
        return str.matches(".*\\d+.*");
    }

    private void performMemberSearch(String searchQuery) {
        final String query = searchQuery;

        mLoadingPanel.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<Member> matchingMembers;
                    final IdentificationEvent.SearchMethodEnum idMethod;
                    if (containsNumber(query)) {
                        matchingMembers = MemberDao.withCardIdLike(query);
                        idMethod = IdentificationEvent.SearchMethodEnum.SEARCH_ID;
                    } else {
                        matchingMembers = MemberDao.fuzzySearchMembers(query, 20, 60);
                        idMethod = IdentificationEvent.SearchMethodEnum.SEARCH_NAME;
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (matchingMembers.isEmpty()) {
                                mNoSearchResultsMessage.setVisibility(View.VISIBLE);
                            } else {
                                mNoSearchResultsMessage.setVisibility(View.GONE);
                            }
                            ListAdapter adapter = new MemberAdapter(getContext(), matchingMembers, false);
                            mSearchList.setAdapter(adapter);
                            mSearchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Member member = (Member) parent.getItemAtPosition(position);
                                    getNavigationManager().setDetailFragment(member, idMethod, null);
                                }
                            });
                            mLoadingPanel.setVisibility(View.GONE);
                        }
                    });
                } catch (SQLException e) {
                    ExceptionManager.reportException(e);
                }
            }
        }).start();
    }
}
