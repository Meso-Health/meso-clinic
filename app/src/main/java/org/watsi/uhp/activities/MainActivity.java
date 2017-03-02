package org.watsi.uhp.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.rollbar.android.Rollbar;
import com.squareup.leakcanary.LeakCanary;

import org.watsi.uhp.R;
import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.database.EncounterItemDao;
import org.watsi.uhp.fragments.EncounterFragment;
import org.watsi.uhp.managers.ConfigManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterItem;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.services.DownloadMemberPhotosService;
import org.watsi.uhp.services.FetchService;
import org.watsi.uhp.services.SyncService;

import net.hockeyapp.android.UpdateManager;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private final Encounter mCurrentEncounter = new Encounter();
    private UUID mMemberId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupApp();
        startServices();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpLeakCanary();
        setupToolbar();

        if (ConfigManager.getLoggedInUserToken(getApplicationContext()) != null) {
            new NavigationManager(this).setCurrentPatientsFragment();
        } else {
            new NavigationManager(this).setLoginFragment();
        }
        checkForUpdates();
    }

    private void setupApp() {
        Rollbar.init(
                this,
                ConfigManager.getRollbarApiKey(this),
                ConfigManager.getRollbarEnv(this)
        );
        DatabaseHelper.init(getApplicationContext());
    }

    private void setUpLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this.getApplication());
    }

    private void startServices() {
        startService(new Intent(this, SyncService.class));
        startService(new Intent(this, FetchService.class));
        startService(new Intent(this, DownloadMemberPhotosService.class));
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.showOverflowMenu();
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(new MenuItemClickListener(this));
        if (!ConfigManager.isProduction(getApplicationContext())) {
            toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.sand));
        }
    }

    private void checkForUpdates() {
        // Remove this for store builds!
        UpdateManager.register(this);
    }

    private void unregisterManagers() {
        UpdateManager.unregister();
    }

    public void setNewEncounter(Member member) {
        try {
            IdentificationEvent lastIdentification = member.getLastIdentification();
            mCurrentEncounter.setMember(member);
            mCurrentEncounter.setIdentificationEvent(lastIdentification);
            mCurrentEncounter.setEncounterItems(
                    EncounterItemDao.getDefaultEncounterItems(lastIdentification.getClinicNumberType()));
        } catch (SQLException e) {
            Rollbar.reportException(e);
        }
    }

    public Encounter getCurrentEncounter() {
        return mCurrentEncounter;
    }

    public List<EncounterItem> getCurrentLineItems() {
        return (List<EncounterItem>) mCurrentEncounter.getEncounterItems();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (currentFragment instanceof EncounterFragment) {
            new AlertDialog.Builder(this)
                    .setTitle("Are you sure you want to exit?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            MainActivity.super.onBackPressed();
                        }
                    }).create().show();
        } else {
            MainActivity.super.onBackPressed();
        }
    }

    public void setMemberId(UUID memberId) {
        this.mMemberId = memberId;
    }

    private class MenuItemClickListener implements Toolbar.OnMenuItemClickListener {

        private Activity mActivity;

        MenuItemClickListener(Activity activity) {
            this.mActivity = activity;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_logout:
                    new NavigationManager(mActivity).logout();
                    break;
                case R.id.menu_complete_enrollment:
                    new NavigationManager(mActivity)
                            .setEnrollmentMemberPhotoFragment(mMemberId);
                    break;
            }
            return true;
        }
    }
}
