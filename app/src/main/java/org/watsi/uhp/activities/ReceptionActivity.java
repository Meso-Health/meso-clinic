package org.watsi.uhp.activities;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.SearchView;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.table.TableUtils;
import com.rollbar.android.Rollbar;

import org.watsi.uhp.adapters.FilterableAdapter;
import org.watsi.uhp.R;
import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//public class ReceptionActivity extends Activity implements SearchView.OnQueryTextListener {
public class ReceptionActivity extends Activity {

    private SearchView searchView;
    private MenuItem searchMenuItem;
    private ArrayAdapter<String> listAdapter;
    private FilterableAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO: move this key to a secure Keystore
        Rollbar.init(this, "f5fe49118ae44f6397da61934e8f0a2c", "development");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reception);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
        }

        // initial DB code based on this guide: https://blog.jayway.com/2016/03/15/android-ormlite/
        try {
            seedDb();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void seedDb() throws SQLException {
        DatabaseHelper helper = new DatabaseHelper(this);
        TableUtils.clearTable(helper.getConnectionSource(), Member.class);

        Dao<Member, Integer> memberDao = null;
        memberDao = helper.getMemberDao();

        List<Member> newMembers = new ArrayList<Member>();
        for (int i = 0; i < 10; i++) {
            Member member = new Member();
            member.setName("Member " + i);
            newMembers.add(member);
        }
        memberDao.create(newMembers);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.member_search_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//            searchView.setSubmitButtonEnabled(true);
//            searchView.setOnQueryTextListener(this);

        return true;
    }
}
