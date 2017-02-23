package org.watsi.uhp.managers;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import org.watsi.uhp.R;
import org.watsi.uhp.api.TokenAuthenticator;
import org.watsi.uhp.fragments.AddNewBillableFragment;
import org.watsi.uhp.fragments.BarcodeFragment;
import org.watsi.uhp.fragments.ClinicNumberFragment;
import org.watsi.uhp.fragments.CurrentPatientsFragment;
import org.watsi.uhp.fragments.DetailFragment;
import org.watsi.uhp.fragments.EncounterFragment;
import org.watsi.uhp.fragments.LoginFragment;
import org.watsi.uhp.fragments.ReceiptFragment;
import org.watsi.uhp.fragments.SearchMemberFragment;
import org.watsi.uhp.models.IdentificationEvent;

/**
 * Helper class for managing navigation between fragments
 */
public class NavigationManager {

    private AppCompatActivity mActivity;

    public NavigationManager(Activity activity) {
        this.mActivity = (AppCompatActivity) activity;
    }

    private void setFragment(Fragment fragment, String tag, boolean addToBackstack, boolean
                             popBackStack) {

        FragmentManager fm = mActivity.getSupportFragmentManager();
        if (popBackStack) {
            fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            // manually remove the previous "home" fragment from the fragment container
            if (fm.findFragmentByTag("home") != null) {
                fm.beginTransaction().remove(fm.findFragmentByTag("home")).commit();
            }
        }

        FragmentTransaction transaction = mActivity.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, tag);
        if (addToBackstack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    private void setFragment(Fragment fragment) {
        setFragment(fragment, null, true, false);
    }

    public void setCurrentPatientsFragment() {
        setFragment(new CurrentPatientsFragment(), "home", false, true);
    }

    public void setDetailFragment(String memberId,
                                  IdentificationEvent.SearchMethodEnum idMethod,
                                  String throughMemberId) {
        DetailFragment detailFragment = new DetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("memberId", memberId);
        bundle.putString("throughMemberId", throughMemberId);
        bundle.putString("idMethod", idMethod.toString());
        detailFragment.setArguments(bundle);

        setFragment(detailFragment);
    }

    public void setBarcodeFragment() {
        setFragment(new BarcodeFragment());
    }

    public void setSearchMemberFragment() {
        setFragment(new SearchMemberFragment());
    }

    public void setClinicNumberFragment() {
        setFragment(new ClinicNumberFragment());
    }

    public void setEncounterFragment() {
        setFragment(new EncounterFragment());
    }

    public void setReceiptFragment() {
        setFragment(new ReceiptFragment());
    }

    public void setAddNewBillableFragment() {
        setFragment(new AddNewBillableFragment());
    }

    public void setLoginFragment() {
        setFragment(new LoginFragment(), null, false, false);
    }

    public void logout() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(TokenAuthenticator.TOKEN_PREFERENCES_KEY);
        editor.apply();

        setLoginFragment();
    }
}
