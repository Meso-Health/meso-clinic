package org.watsi.uhp.activities;

import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.AlertDialog;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import net.hockeyapp.android.UpdateManager;

import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.R;
import org.watsi.uhp.database.DatabaseHelper;
import org.watsi.uhp.fragments.FormFragment;
import org.watsi.uhp.fragments.MemberDetailFragment;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.MenuNavigationManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.managers.PreferencesManager;
import org.watsi.uhp.managers.SessionManager;
import org.watsi.uhp.services.DeleteFetchedPhotoService;
import org.watsi.uhp.services.DownloadMemberPhotosService;
import org.watsi.uhp.services.FetchService;
import org.watsi.uhp.services.SyncService;

import java.io.IOException;

public class ClinicActivity extends AppCompatActivity {

    private static int FETCH_SERVICE_JOB_ID = 0;
    private static int SYNC_SERVICE_JOB_ID = 1;
    private static int DOWNLOAD_MEMBER_PHOTO_SERVICE_JOB_ID = 2;
    private static int DELETE_PHOTOS_SERVICE_JOB_ID = 3;

    private SessionManager mSessionManager;
    private NavigationManager mNavigationManager;
    private String authenticationToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ExceptionManager.init(getApplication());
        super.onCreate(savedInstanceState);

        setupApp();
        startServices();
    }

    /**
     * This method gets called after onResume and will get called after both onCreate and
     * after onActivityResult which will ensure we force setUserAsLoggedIn when necessary
     */
    @Override
    protected void onPostResume() {
        super.onPostResume();

        new LoginTask(this).execute();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (BuildConfig.SHOULD_CHECK_FOR_UPDATES) checkForUpdates();
    }

    private void setupApp() {
        DatabaseHelper.init(this);

        setContentView(R.layout.activity_clinic);
        setupToolbar();
        mSessionManager = new SessionManager(new PreferencesManager(this), AccountManager.get(this));
        mNavigationManager = new NavigationManager(this);
    }

    public SessionManager getSessionManager() {
        return mSessionManager;
    }

    public NavigationManager getNavigationManager() {
        return mNavigationManager;
    }

    private void startServices() {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(FetchService.buildJobInfo(
                FETCH_SERVICE_JOB_ID, new ComponentName(this, FetchService.class)));
        jobScheduler.schedule(SyncService.buildJobInfo(
                SYNC_SERVICE_JOB_ID, new ComponentName(this, SyncService.class)));
        jobScheduler.schedule(DownloadMemberPhotosService.buildJobInfo(
                DOWNLOAD_MEMBER_PHOTO_SERVICE_JOB_ID,
                new ComponentName(this, DownloadMemberPhotosService.class)));
        jobScheduler.schedule(SyncService.buildJobInfo(
                DELETE_PHOTOS_SERVICE_JOB_ID,
                new ComponentName(this, DeleteFetchedPhotoService.class),
                false));
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.showOverflowMenu();
        setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(new MenuItemClickListener(this));
    }

    private void checkForUpdates() {
        UpdateManager.register(this, BuildConfig.HOCKEYAPP_APP_ID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);

        if (currentFragment instanceof FormFragment &&
                ((FormFragment) currentFragment).isFirstStep()) {
            showDialogReturnToPreviousScreen();
        } else if (currentFragment instanceof MemberDetailFragment) {
            getNavigationManager().setCurrentPatientsFragment();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (currentFragment instanceof FormFragment) {
                    onBackPressed();
                } else {
                    getNavigationManager().setCurrentPatientsFragment();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class MenuItemClickListener implements Toolbar.OnMenuItemClickListener {

        private ClinicActivity mActivity;

        MenuItemClickListener(ClinicActivity activity) {
            this.mActivity = activity;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            MenuNavigationManager menuNavigationManager = new MenuNavigationManager(mActivity);
            return menuNavigationManager.nextStep(currentFragment, menuItem);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        UpdateManager.unregister();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UpdateManager.unregister();
    }

    public void setAuthenticationToken(String token) {
        this.authenticationToken = token;
    }

    public String getAuthenticationToken() {
        return authenticationToken;
    }

    private class LoginTask extends AsyncTask<Void, Void, String> {

        private final ClinicActivity mClinicActivity;

        LoginTask(ClinicActivity clinicActivity) {
            this.mClinicActivity = clinicActivity;
        }

        @Override
        protected String doInBackground(Void... params) {
            AccountManagerFuture<Bundle> tokenFuture = mSessionManager.fetchToken();
            if (tokenFuture == null) return null;
            try {
                Bundle bundle = tokenFuture.getResult();
                if (bundle != null) return bundle.getString(AccountManager.KEY_AUTHTOKEN);
            } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                ExceptionManager.reportException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String token) {
            if (token == null) {
                startActivityForResult(new Intent(mClinicActivity, AuthenticationActivity.class), 0);
            } else {
                mClinicActivity.setAuthenticationToken(token);
                Fragment currentFragment = mClinicActivity.getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_container);
                if (currentFragment == null) {
                    getNavigationManager().setCurrentPatientsFragment();
                }
            }
        }
    }

    private void showDialogReturnToPreviousScreen() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.exit_form_alert)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        ClinicActivity.super.onBackPressed();
                    }
                }).create().show();
    }
}
