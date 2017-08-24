package org.watsi.uhp.fragments;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.managers.SessionManager;

public abstract class BaseFragment extends Fragment {

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
            } else {
                supportBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    public String getName() {
        return getClass().getSimpleName();
    }
}
