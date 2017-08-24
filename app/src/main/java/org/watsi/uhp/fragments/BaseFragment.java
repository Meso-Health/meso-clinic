package org.watsi.uhp.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.watsi.uhp.R;
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

    @Override
    public void onResume() {
        super.onResume();
        Log.i("UHP-debug", "resumed " + this.getName());
        FragmentManager fm = getActivity().getSupportFragmentManager();
        int count = fm.getBackStackEntryCount();
        String s = "Back stack currently contains: \n";
        for (int i=count-1; i >= 0; i--) {
            FragmentManager.BackStackEntry entry = fm.getBackStackEntryAt(i);
            s = s + " " + entry.getName() + " \n";
        }
        Log.i("UHP-debug", s);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("UHP-debug", "onPause " + this.getName());
        FragmentManager fm = getActivity().getSupportFragmentManager();
        int count = fm.getBackStackEntryCount();
        String s = "Back stack currently contains: \n";
        for (int i=count-1; i >= 0; i--) {
            FragmentManager.BackStackEntry entry = fm.getBackStackEntryAt(i);
            s = s + " " + entry.getName() + " \n";
        }
        Log.i("UHP-debug", s);
    }

    public String getName() {
        String simpleName = getClass().getSimpleName();
        if (simpleName.contains("MemberDetailFragment")) {
            return "MemberDetailFragment";
        } else {
            return simpleName;
        }
    }
}
