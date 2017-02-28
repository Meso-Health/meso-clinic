package org.watsi.uhp.managers;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.rollbar.android.Rollbar;

import org.watsi.uhp.R;
import org.watsi.uhp.fragments.AddNewBillableFragment;
import org.watsi.uhp.fragments.BarcodeFragment;
import org.watsi.uhp.fragments.CurrentPatientsFragment;
import org.watsi.uhp.fragments.DetailFragment;
import org.watsi.uhp.fragments.EncounterFragment;
import org.watsi.uhp.fragments.EnrollmentContactInfoFragment;
import org.watsi.uhp.fragments.EnrollmentFingerprintFragment;
import org.watsi.uhp.fragments.EnrollmentIdPhotoFragment;
import org.watsi.uhp.fragments.EnrollmentMemberPhotoFragment;
import org.watsi.uhp.fragments.LoginFragment;
import org.watsi.uhp.fragments.ReceiptFragment;
import org.watsi.uhp.fragments.SearchMemberFragment;
import org.watsi.uhp.models.IdentificationEvent;

import java.util.UUID;

/**
 * Helper class for managing navigation between fragments
 */
public class NavigationManager {

    private AppCompatActivity mActivity;
    private FragmentProvider mFragmentProvider;

    public NavigationManager(Activity activity, FragmentProvider fragmentProvider) {
        this.mActivity = (AppCompatActivity) activity;
        this.mFragmentProvider = fragmentProvider;
    }

    public NavigationManager(Activity activity) {
        this(activity, new FragmentProvider());
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

    public void setDetailFragment(UUID memberId,
                                  IdentificationEvent.SearchMethodEnum idMethod,
                                  UUID throughMemberId) {
        Bundle bundle = new Bundle();
        bundle.putString("memberId", memberId.toString());
        bundle.putString("idMethod", idMethod.toString());
        if (throughMemberId != null) {
            bundle.putString("throughMemberId", throughMemberId.toString());
        }

        setFragment(mFragmentProvider.createFragment(DetailFragment.class, bundle));
    }

    public void setBarcodeFragment() {
        setFragment(mFragmentProvider.createFragment(BarcodeFragment.class));
    }

    public void setSearchMemberFragment() {
        setFragment(mFragmentProvider.createFragment(SearchMemberFragment.class));
    }

    public void setEncounterFragment() {
        setFragment(mFragmentProvider.createFragment(EncounterFragment.class));
    }

    public void setReceiptFragment() {
        setFragment(mFragmentProvider.createFragment(ReceiptFragment.class));
    }

    public void setAddNewBillableFragment() {
        setFragment(mFragmentProvider.createFragment(AddNewBillableFragment.class));
    }

    public void setEnrollmentContactInfoFragment(UUID memberId) {
        Bundle bundle = new Bundle();
        bundle.putString("memberId", memberId.toString());
        setFragment(mFragmentProvider.createFragment(EnrollmentContactInfoFragment.class, bundle));
    }

    public void setEnrollmentMemberPhotoFragment(UUID memberId) {
        Bundle bundle = new Bundle();
        bundle.putString("memberId", memberId.toString());
        setFragment(mFragmentProvider.createFragment(EnrollmentMemberPhotoFragment.class, bundle));
    }

    public void setEnrollmentIdPhotoFragment(UUID memberId) {
        Bundle bundle = new Bundle();
        bundle.putString("memberId", memberId.toString());
        setFragment(mFragmentProvider.createFragment(EnrollmentIdPhotoFragment.class, bundle));
    }

    public void setEnrollmentFingerprintFragment(UUID memberId) {
        Bundle bundle = new Bundle();
        bundle.putString("memberId", memberId.toString());
        setFragment(mFragmentProvider.createFragment(EnrollmentFingerprintFragment.class, bundle));
    }

    public void setLoginFragment() {
        setFragment(new LoginFragment(), null, false, false);
    }

    public void logout() {
        ConfigManager.setLoggedInUserToken(null, mActivity.getApplicationContext());
        setLoginFragment();
    }

    public static class FragmentProvider {
        public Fragment createFragment(Class<? extends Fragment> clazz) {
            return createFragment(clazz, null);
        }

        public Fragment createFragment(Class<? extends Fragment> clazz, Bundle bundle) {
            try {
                Fragment fragment = clazz.newInstance();
                if (bundle != null) {
                    fragment.setArguments(bundle);
                }
                return fragment;
            } catch (InstantiationException | IllegalAccessException e) {
                Rollbar.reportException(e);
                return null;
            }
        }
    }
}
