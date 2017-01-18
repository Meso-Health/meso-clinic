package org.watsi.uhp.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.table.TableUtils;
import com.rollbar.android.Rollbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.watsi.uhp.R;
import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.events.OfflineNotificationEvent;
import org.watsi.uhp.fragments.DefaultFragment;
import org.watsi.uhp.fragments.DetailFragment;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.services.OfflineNotificationService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReceptionActivity extends FragmentActivity {

    private Dao<Member, Integer> mMemberDao;
    private MenuItem mMenuItem;
    private final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO: move this key to a secure Keystore
        Rollbar.init(this, "f5fe49118ae44f6397da61934e8f0a2c", "development");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.reception_activity);

        // initial DB code based on this guide: https://blog.jayway.com/2016/03/15/android-ormlite/
        try {
            DatabaseHelper helper = new DatabaseHelper(this);
            mMemberDao = helper.getMemberDao();
            seedDb(helper);
        } catch (SQLException e) {
            Rollbar.reportException(e);
        } catch (IOException e) {
            Rollbar.reportException(e);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, new DefaultFragment())
                .commit();

        Intent serviceIntent = new Intent(this, OfflineNotificationService.class);
        startService(serviceIntent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessage(OfflineNotificationEvent event) {
        if (event.isOffline()) {
            getActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getBaseContext(), R.color.action_bar_offline_color)));
        } else {
            getActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getBaseContext(), R.color.action_bar_online_color)));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void seedDb(DatabaseHelper helper) throws SQLException, IOException {
        TableUtils.clearTable(helper.getConnectionSource(), Member.class);

        List<Member> newMembers = new ArrayList<Member>();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -24);
        for (int i = 0; i < 10; i++) {
            Member member = new Member();
            member.setName("Member " + i);
            member.setBirthdate(new Date(cal.getTimeInMillis()));
            member.setPhotoUrl("https://d3w52z135jkm97.cloudfront.net/uploads/profile/photo/11325/profile_638x479_177b9aad-88b7-49c5-860b-52bf97a0e7d9.jpg");
            newMembers.add(member);
        }
        mMemberDao.create(newMembers);
        Member firstMember = newMembers.get(0);
        firstMember.fetchAndSetPhotoFromUrl(getApplicationContext(), mMemberDao);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.member_search_menu, menu);

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
                setDetailFragment(memberId);
            }
        }
    }

    public void setDetailFragment(String memberId) {
        DetailFragment detailFragment = new DetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("memberId", memberId);
        detailFragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, detailFragment);
        transaction.addToBackStack(null);
        transaction.commit();

        mMenuItem.collapseActionView();
    }
}
