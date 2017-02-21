package org.watsi.uhp.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.rollbar.android.Rollbar;
import com.squareup.leakcanary.LeakCanary;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.watsi.uhp.R;
import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.events.OfflineNotificationEvent;
import org.watsi.uhp.fragments.AddNewBillableFragment;
import org.watsi.uhp.fragments.BarcodeFragment;
import org.watsi.uhp.fragments.CurrentPatientsFragment;
import org.watsi.uhp.fragments.DetailFragment;
import org.watsi.uhp.fragments.EncounterFragment;
import org.watsi.uhp.fragments.ReceiptFragment;
import org.watsi.uhp.fragments.SearchMemberFragment;
import org.watsi.uhp.managers.ConfigManager;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.Identification;
import org.watsi.uhp.models.LineItem;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.services.RefreshMemberListService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final Encounter mCurrentEncounter = new Encounter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupApp();
        startFetchMembersService();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpLeakCanary();
        setupToolbar();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, new CurrentPatientsFragment())
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
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            String memberId = intent.getDataString();
            Identification.IdMethodEnum idMethod = Identification.IdMethodEnum.valueOf(
                    intent.getExtras().getString(SearchManager.EXTRA_DATA_KEY));

            if (memberId != null) {
                setDetailFragment(memberId, idMethod);
            }
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

    private void setUpLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this.getApplication());
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

    public void setNewEncounter(Member member) {
        mCurrentEncounter.setMember(member);
        mCurrentEncounter.setLineItems(new ArrayList<LineItem>());
    }

    public Encounter getCurrentEncounter() {
        return mCurrentEncounter;
    }

    public List<LineItem> getCurrentLineItems() {
        return mCurrentEncounter.getLineItems();
    }

    //TODO: consider moving these to a "NavigationManager" class and/or DRY these up.

    public void setCurrentPatientsFragment() {
        FragmentManager fm = getSupportFragmentManager();

        CurrentPatientsFragment currentPatientsFragment = new CurrentPatientsFragment();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_container, currentPatientsFragment);
        transaction.addToBackStack(null);
        transaction.commit();

        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public void setDetailFragment(String memberId, Identification.IdMethodEnum idMethod) {
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

    public void setSearchMemberFragment() {
        SearchMemberFragment searchMemberFragment = new SearchMemberFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, searchMemberFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void setEncounterFragment() {
        EncounterFragment encounterFragment = new EncounterFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, encounterFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void setReceiptFragment() {
        ReceiptFragment receiptFragment = new ReceiptFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, receiptFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void setAddNewBillableFragment() {
        AddNewBillableFragment addNewBillableFragment = new AddNewBillableFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, addNewBillableFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
