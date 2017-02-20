package org.watsi.uhp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.rollbar.android.Rollbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.watsi.uhp.R;
import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.events.OfflineNotificationEvent;
import org.watsi.uhp.fragments.BarcodeFragment;
import org.watsi.uhp.fragments.CurrentPatientsFragment;
import org.watsi.uhp.fragments.DetailFragment;
import org.watsi.uhp.fragments.EncounterFragment;
import org.watsi.uhp.fragments.MainFragment;
import org.watsi.uhp.managers.ConfigManager;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.LineItem;
import org.watsi.uhp.services.RefreshMemberListService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LineItemInterface {
    private List<LineItem> mCurrentLineItems;

    public void setCurrentLineItems(List<LineItem> lineItems) {
        mCurrentLineItems = lineItems;
    }

    public List<LineItem> getCurrentLineItems() {
        return mCurrentLineItems;
    }

    public List<LineItem> addLineItem(LineItem lineItem) {
        if (mCurrentLineItems == null) {
            mCurrentLineItems = new ArrayList<>();
        }
        mCurrentLineItems.add(lineItem);
        return mCurrentLineItems;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupApp();
        startFetchMembersService();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, new MainFragment())
                .commit();
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
    protected void onNewIntent(Intent intent) {
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

    private void setupApp() {
        Rollbar.init(this, ConfigManager.getRollbarApiKey(this), "development");
        DatabaseHelper.init(getBaseContext());
        try {
            DatabaseHelper.loadBillables(getBaseContext());
        } catch (SQLException | IOException e) {
            Rollbar.reportException(e);
        }
    }

    private void startFetchMembersService() {
        Intent fetchMembersService = new Intent(this, RefreshMemberListService.class);
        fetchMembersService.putExtra("apiHost", ConfigManager.getApiHost(this));
        fetchMembersService.putExtra("facilityId", ConfigManager.getFacilityId(this));
        startService(fetchMembersService);
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
    }

    //TODO: consider moving these to a "NavigationManager" class

    public void setMainFragment() {
        FragmentManager fm = getSupportFragmentManager();

        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_container, new MainFragment());
        transaction.addToBackStack(null);
        transaction.commit();

        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
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

    public void setBarcodeFragment() {
        BarcodeFragment barcodeFragment = new BarcodeFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, barcodeFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void setEncounterFragment(String memberId) {
        if (mCurrentLineItems != null) {
            mCurrentLineItems.clear();
        }

        EncounterFragment encounterFragment = new EncounterFragment();
        Bundle bundle = new Bundle();
        bundle.putString("memberId", memberId);
        encounterFragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, encounterFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void setCurrentPatientsFragment() {
        CurrentPatientsFragment currentPatientsFragment = new CurrentPatientsFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, currentPatientsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void addBillable(String billableId) {
        Fragment fragment =
                getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment instanceof EncounterFragment) {
         ((EncounterFragment) fragment).addToLineItemList(billableId);
        }
    }

    public void clearDrugSearchView() {
        Fragment fragment =
                getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment instanceof EncounterFragment) {
            ((EncounterFragment) fragment).clearDrugSearch();
        }
    }
}
