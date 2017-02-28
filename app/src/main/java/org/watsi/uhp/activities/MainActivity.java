package org.watsi.uhp.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.rollbar.android.Rollbar;
import com.squareup.leakcanary.LeakCanary;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.watsi.uhp.R;
import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.database.LineItemDao;
import org.watsi.uhp.events.OfflineNotificationEvent;
import org.watsi.uhp.managers.ConfigManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.LineItem;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.services.DownloadMemberPhotosService;
import org.watsi.uhp.services.FetchService;
import org.watsi.uhp.services.SyncService;

import java.sql.SQLException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final Encounter mCurrentEncounter = new Encounter();

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

    private void setupApp() {
        Rollbar.init(this, ConfigManager.getRollbarApiKey(this), "development");
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
        toolbar.setOnMenuItemClickListener(new LogoutListener(this));
    }

    public void setNewEncounter(Member member) {
        try {
            IdentificationEvent lastIdentification = member.getLastIdentification();
            mCurrentEncounter.setMember(member);
            mCurrentEncounter.setIdentification(lastIdentification);
            mCurrentEncounter.setLineItems(
                    LineItemDao.getDefaultLineItems(lastIdentification.getClinicNumberType()));
        } catch (SQLException e) {
            Rollbar.reportException(e);
        }
    }

    public Encounter getCurrentEncounter() {
        return mCurrentEncounter;
    }

    public List<LineItem> getCurrentLineItems() {
        return (List<LineItem>) mCurrentEncounter.getLineItems();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private class LogoutListener implements Toolbar.OnMenuItemClickListener {

        private Activity activity;

        LogoutListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            new NavigationManager(activity).logout();
            return true;
        }
    }
}
