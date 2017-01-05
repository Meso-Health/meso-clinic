package org.watsi.uhp.activities;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import com.j256.ormlite.dao.Dao;

import org.watsi.uhp.adapters.FilterableAdapter;
import org.watsi.uhp.R;
import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReceptionActivity extends Activity implements SearchView.OnQueryTextListener {

    private SearchView searchView;
    private MenuItem searchMenuItem;
    private ArrayAdapter<String> listAdapter;
    private FilterableAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reception);
    }

    private void seedDb() throws SQLException {
        DatabaseHelper helper = new DatabaseHelper(this);
        Dao<Member, Integer> memberDao = null;
        memberDao = helper.getMemberDao();

        int numOfStoredMembers = memberDao.queryForAll().size();

        if (numOfStoredMembers == 0) {
            List<Member> newMembers = new ArrayList<Member>();
            for (int i = 0; i < 10; i++) {
                Member member = new Member();
                member.setName("Member " + i);
                newMembers.add(member);
            }
            memberDao.create(newMembers);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.member_search_menu, menu);

        try {
            // initial DB code based on this guide: https://blog.jayway.com/2016/03/15/android-ormlite/
            try {
                seedDb();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            adapter = new FilterableAdapter(this);

            ListView lv = (ListView) findViewById(R.id.list_view);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Member member = (Member) parent.getItemAtPosition(position);
                    Intent i = new Intent(parent.getContext(), MemberDetailActivity.class);
                    i.putExtra("memberId", String.valueOf(member.getId()));
                    startActivity(i);
                }
            });

            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            searchMenuItem = menu.findItem(R.id.search);
            searchView = (SearchView) searchMenuItem.getActionView();

            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setSubmitButtonEnabled(true);
            searchView.setOnQueryTextListener(this);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);

        return true;
    }
}
