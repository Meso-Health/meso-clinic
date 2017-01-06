package org.watsi.uhp.activities;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.table.TableUtils;
import com.rollbar.android.Rollbar;

import org.watsi.uhp.R;
import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReceptionActivity extends Activity {

    private Dao<Member, Integer> mMemberDao;
    private MenuItem mMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO: move this key to a secure Keystore
        Rollbar.init(this, "f5fe49118ae44f6397da61934e8f0a2c", "development");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reception);

        // initial DB code based on this guide: https://blog.jayway.com/2016/03/15/android-ormlite/
        try {
            DatabaseHelper helper = new DatabaseHelper(this);
            mMemberDao = helper.getMemberDao();
            seedDb(helper);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void seedDb(DatabaseHelper helper) throws SQLException {
        TableUtils.clearTable(helper.getConnectionSource(), Member.class);

        List<Member> newMembers = new ArrayList<Member>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -24);
        for (int i = 0; i < 10; i++) {
            Member member = new Member();
            member.setName("Member " + i);
            member.setBirthdate(new Date(cal.getTimeInMillis()));
            newMembers.add(member);
        }
        mMemberDao.create(newMembers);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.member_search_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mMenuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) mMenuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String memberId = intent.getDataString();

            if (memberId != null) {
                try {
                    Member member = mMemberDao.queryForId(Integer.parseInt(memberId));
                    fillOutMemberDetails(member);
                    mMenuItem.collapseActionView();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void fillOutMemberDetails(Member member) {
        TextView nameView = (TextView) findViewById(R.id.member_name);
        nameView.setText(member.getName());
        TextView birthdateView = (TextView) findViewById(R.id.member_birthdate);
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy/MM/dd");
        birthdateView.setText(simpleDate.format(member.getBirthdate()));
        TextView idView = (TextView) findViewById(R.id.member_id);
        idView.setText(String.valueOf(member.getId()));
        ImageView imageView = (ImageView) findViewById(R.id.member_photo);
        imageView.setImageResource(R.drawable.sample_portrait);
    }
}
