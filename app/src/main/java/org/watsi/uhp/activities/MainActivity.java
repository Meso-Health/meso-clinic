package org.watsi.uhp.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.rollbar.android.Rollbar;
import com.squareup.leakcanary.LeakCanary;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.watsi.uhp.R;
import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.events.OfflineNotificationEvent;
import org.watsi.uhp.fragments.BarcodeFragment;
import org.watsi.uhp.fragments.EncounterFragment;
import org.watsi.uhp.fragments.RecentEncountersFragment;
import org.watsi.uhp.fragments.DetailFragment;
import org.watsi.uhp.managers.ConfigManager;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.services.RefreshMemberListService;

import java.io.IOException;
import java.sql.SQLException;

public class MainActivity extends AppCompatActivity {

    private MenuItem mMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this.getApplication());

        Rollbar.init(this, ConfigManager.getRollbarApiKey(this), "development");
        DatabaseHelper.init(getBaseContext());
        try {
            DatabaseHelper.loadBillables(getBaseContext());
        } catch (SQLException | IOException e) {
            Rollbar.reportException(e);
        }

        setContentView(R.layout.activity_main);

        setupToolbar();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, new RecentEncountersFragment())
                .commit();

        Intent fetchMembersService = new Intent(this, RefreshMemberListService.class);
        fetchMembersService.putExtra("apiHost", ConfigManager.getApiHost(this));
        fetchMembersService.putExtra("facilityId", ConfigManager.getFacilityId(this));
        startService(fetchMembersService);
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Recent").setTag("recent"));
        tabLayout.addTab(tabLayout.newTab().setText("Scan ID").setTag("barcode"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String tabName = (tab.getTag() != null) ? (String) tab.getTag() : "";
                switch (tabName) {
                    case "recent":
                        RecentEncountersFragment recentCheckInsFragment = new RecentEncountersFragment();
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, recentCheckInsFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();

                        break;
                    case "barcode":
                        setBarcodeFragment();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // no-op
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // no-op
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessage(OfflineNotificationEvent event) {
        if (getActionBar() != null) {
            if (event.isOffline()) {
                getActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getBaseContext(), R.color.action_bar_offline_color)));
            } else {
                getActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getBaseContext(), R.color.action_bar_online_color)));
            }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mMenuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) mMenuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        mMenuItem.collapseActionView();

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String memberId = intent.getDataString();
            Log.d("UHP", "intention memberId: " + memberId);
            if (memberId != null) {
                setDetailFragment(memberId, Encounter.IdMethodEnum.SEARCH);
            }
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            String billableId = intent.getDataString();
            addBillable(billableId);
            clearDrugSearchView();
        }
    }

    public void setDetailFragment(String memberId, Encounter.IdMethodEnum idMethod) {
        DetailFragment detailFragment = new DetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("memberId", memberId);
        bundle.putString("idMethod", idMethod.toString());
        detailFragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, detailFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void addBillable(String billableId) {
        Fragment fragment =
                getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment instanceof EncounterFragment) {
         ((EncounterFragment) fragment).addSearchSuggestionToBillableList(billableId);
        }
    }

    public void clearDrugSearchView() {
        Fragment fragment =
                getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment instanceof EncounterFragment) {
            ((EncounterFragment) fragment).clearDrugSearch();
        }
    }

    public void setBarcodeFragment() {
        BarcodeFragment barcodeFragment = new BarcodeFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, barcodeFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
