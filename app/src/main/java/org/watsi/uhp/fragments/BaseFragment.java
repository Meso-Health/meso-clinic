package org.watsi.uhp.fragments;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.managers.SessionManager;

import dagger.android.support.DaggerFragment;

public abstract class BaseFragment extends DaggerFragment {

    protected SessionManager getSessionManager() {
        return ((ClinicActivity) getActivity()).getSessionManager();
    }

    protected NavigationManager getNavigationManager() {
        return ((ClinicActivity) getActivity()).getNavigationManager();
    }

    protected String getAuthenticationToken() {
        return ((ClinicActivity) getActivity()).getAuthenticationToken();
    }

    public void onStart() {
        super.onStart();

        ActionBar supportBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (supportBar != null) {
            if (this instanceof CurrentPatientsFragment) {
                supportBar.setDisplayHomeAsUpEnabled(false);
            } else if (this instanceof FormFragment) {
                supportBar.setDisplayHomeAsUpEnabled(true);
                supportBar.setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
            } else {
                supportBar.setDisplayHomeAsUpEnabled(true);
                // 0 uses the default drawable from the theme.
                supportBar.setHomeAsUpIndicator(0);
            }
        }
    }

    public String getName() {
        return getClass().getSimpleName();
    }
}
