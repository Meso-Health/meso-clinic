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
import org.watsi.uhp.fragments.EnrollNewbornInfoFragment;
import org.watsi.uhp.fragments.EnrollNewbornPhotoFragment;
import org.watsi.uhp.fragments.EnrollmentContactInfoFragment;
import org.watsi.uhp.fragments.EnrollmentFingerprintFragment;
import org.watsi.uhp.fragments.EnrollmentIdPhotoFragment;
import org.watsi.uhp.fragments.EnrollmentMemberPhotoFragment;
import org.watsi.uhp.fragments.LoginFragment;
import org.watsi.uhp.fragments.MemberEditFragment;
import org.watsi.uhp.fragments.ReceiptFragment;
import org.watsi.uhp.fragments.SearchMemberFragment;
import org.watsi.uhp.fragments.VersionFragment;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

/**
 * Helper class for managing navigation between fragments
 */
public class NavigationManager {

    public static String ID_METHOD_BUNDLE_FIELD = "idMethod";
    public static String THROUGH_MEMBER_BUNDLE_FIELD = "throughMember";
    public static String SCAN_PURPOSE_BUNDLE_FIELD = "scanPurpose";
    public static String SCANNED_CARD_ID_BUNDLE_FIELD = "scannedCardId";
    public static String MEMBER_BUNDLE_FIELD = "member";

    private static String HOME_TAG = "home";
    public static String DETAIL_TAG = "detail";

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
            if (fm.findFragmentByTag(HOME_TAG) != null) {
                fm.beginTransaction().remove(fm.findFragmentByTag(HOME_TAG)).commit();
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
        setFragment(new CurrentPatientsFragment(), HOME_TAG, false, true);
    }

    public void setDetailFragment(Member member,
                                  IdentificationEvent.SearchMethodEnum idMethod,
                                  Member throughMember) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(MEMBER_BUNDLE_FIELD, member);
        if (idMethod != null) {
            bundle.putString(ID_METHOD_BUNDLE_FIELD, idMethod.toString());
        }
        if (throughMember != null) {
            bundle.putSerializable(THROUGH_MEMBER_BUNDLE_FIELD, throughMember);
        }

        setFragment(mFragmentProvider.createFragment(DetailFragment.class, bundle), DETAIL_TAG, true, false);
    }

    public void setBarcodeFragment(BarcodeFragment.ScanPurposeEnum scanPurpose,
                                   Member member,
                                   IdentificationEvent.SearchMethodEnum idMethod) {
        Bundle bundle = new Bundle();
        if (member != null) bundle.putSerializable(MEMBER_BUNDLE_FIELD, member);
        if (idMethod != null) bundle.putString(ID_METHOD_BUNDLE_FIELD, idMethod.toString());
        bundle.putString(SCAN_PURPOSE_BUNDLE_FIELD, scanPurpose.toString());
        setFragment(mFragmentProvider.createFragment(BarcodeFragment.class, bundle));
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

    public void setEnrollmentContactInfoFragment(Member member) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(MEMBER_BUNDLE_FIELD, member);
        setFragment(mFragmentProvider.createFragment(EnrollmentContactInfoFragment.class, bundle));
    }

    public void setEnrollmentMemberPhotoFragment(Member member) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(MEMBER_BUNDLE_FIELD, member);
        setFragment(mFragmentProvider.createFragment(EnrollmentMemberPhotoFragment.class, bundle));
    }

    public void setEnrollmentIdPhotoFragment(Member member) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(MEMBER_BUNDLE_FIELD, member);
        setFragment(mFragmentProvider.createFragment(EnrollmentIdPhotoFragment.class, bundle));
    }

    public void setEnrollmentFingerprintFragment(Member member) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(MEMBER_BUNDLE_FIELD, member);
        setFragment(mFragmentProvider.createFragment(EnrollmentFingerprintFragment.class, bundle));
    }

    public void setMemberEditFragment(Member member,
                                      IdentificationEvent.SearchMethodEnum searchMethod,
                                      String scannedCardId) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(MEMBER_BUNDLE_FIELD, member);
        if (searchMethod != null) bundle.putString(ID_METHOD_BUNDLE_FIELD , searchMethod.toString());
        bundle.putString(SCANNED_CARD_ID_BUNDLE_FIELD, scannedCardId);
        setFragment(mFragmentProvider.createFragment(MemberEditFragment.class, bundle));
    }

    public void setEnrollNewbornInfoFragment(Member parentMember, String scannedCardId) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(MEMBER_BUNDLE_FIELD, parentMember);
        bundle.putString(SCANNED_CARD_ID_BUNDLE_FIELD, scannedCardId);
        setFragment(mFragmentProvider.createFragment(EnrollNewbornInfoFragment.class, bundle));
    }

    public void setEnrollNewbornPhotoFragment(Member newborn) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(MEMBER_BUNDLE_FIELD, newborn);
        setFragment(mFragmentProvider.createFragment(EnrollNewbornPhotoFragment.class, bundle));
    }

    public void setLoginFragment() {
        setFragment(new LoginFragment(), null, false, false);
    }

    public void setVersionFragment() {
        setFragment(new VersionFragment());
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
