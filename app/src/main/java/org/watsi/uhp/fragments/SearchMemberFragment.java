package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import com.rollbar.android.Rollbar;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.MainActivity;
import org.watsi.uhp.adapters.MemberAdapter;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.KeyboardManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.List;

public class SearchMemberFragment extends Fragment {

    private SearchView mMemberSearch;
    private ListView mSearchList;
    private TextView mNoSearchResults;
    private RelativeLayout mLoadingPanel;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(R.string.member_search_fragment_label);

        final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_member_search,
                container, false);

        mMemberSearch = (SearchView) view.findViewById(R.id.member_search);
        mSearchList = (ListView) view.findViewById(R.id.member_search_results);
        mNoSearchResults = (TextView) view.findViewById(R.id.member_no_search_results_text);
        mLoadingPanel = (RelativeLayout) view.findViewById(R.id.loading_panel);

        setMemberSearch();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        KeyboardManager.focusAndForceShowKeyboard(mMemberSearch, getContext());
    }

    private void setMemberSearch() {
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
                        matchingMembers = MemberDao.fuzzySearchMembers(query, 8, 60);
                        idMethod = IdentificationEvent.SearchMethodEnum.SEARCH_NAME;
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (matchingMembers.isEmpty()) {
                                mNoSearchResults.setVisibility(View.VISIBLE);
                            } else {
                                mNoSearchResults.setVisibility(View.GONE);
                            }
                            ListAdapter adapter = new MemberAdapter(getContext(), matchingMembers, false);
                            mSearchList.setAdapter(adapter);
                            mSearchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Member member = (Member) parent.getItemAtPosition(position);
                                    MainActivity activity = (MainActivity) getActivity();
                                    new NavigationManager(activity).setDetailFragment(
                                            member.getId().toString(), idMethod, null);
                                }
                            });
                            mLoadingPanel.setVisibility(View.GONE);
                        }
                    });
                } catch (SQLException e) {
                    Rollbar.reportException(e);
                }
            }
        }).start();
    }
}
