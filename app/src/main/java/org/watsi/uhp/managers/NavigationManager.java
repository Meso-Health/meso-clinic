package org.watsi.uhp.managers;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import org.watsi.uhp.R;
import org.watsi.uhp.fragments.AddNewBillableFragment;
import org.watsi.uhp.fragments.BarcodeFragment;
import org.watsi.uhp.fragments.CheckInMemberDetailFragment;
import org.watsi.uhp.fragments.CurrentMemberDetailFragment;
import org.watsi.uhp.fragments.CurrentPatientsFragment;
import org.watsi.uhp.fragments.EncounterFormFragment;
import org.watsi.uhp.fragments.EncounterFragment;
import org.watsi.uhp.fragments.EnrollNewbornInfoFragment;
import org.watsi.uhp.fragments.EnrollNewbornPhotoFragment;
import org.watsi.uhp.fragments.EnrollmentContactInfoFragment;
import org.watsi.uhp.fragments.EnrollmentFingerprintFragment;
import org.watsi.uhp.fragments.EnrollmentIdPhotoFragment;
import org.watsi.uhp.fragments.EnrollmentMemberPhotoFragment;
import org.watsi.uhp.fragments.MemberEditFragment;
import org.watsi.uhp.fragments.ReceiptFragment;
import org.watsi.uhp.fragments.SearchMemberFragment;
import org.watsi.uhp.fragments.VersionAndSyncFragment;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.util.List;

/**
 * Helper class for managing navigation between fragments
 */
public class NavigationManager {
    public static String IDENTIFICATION_EVENT_BUNDLE_FIELD = "identificationEvent";
    public static String SCAN_PURPOSE_BUNDLE_FIELD = "scanPurpose";
    public static String MEMBER_BUNDLE_FIELD = "member";
    public static String THROUGH_MEMBER_BUNDLE_FIELD = "throughMember";
    public static String SYNCABLE_MODEL_BUNDLE_FIELD = "syncableModel";

    private static String HOME_TAG = "home";
    public static String DETAIL_TAG = "detail";

    private FragmentActivity mActivity;
    private FragmentProvider mFragmentProvider;

    public NavigationManager(FragmentActivity activity, FragmentProvider fragmentProvider) {
        this.mActivity = activity;
        this.mFragmentProvider = fragmentProvider;
    }

    public NavigationManager(FragmentActivity activity) {
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

            // Hacky solution: (michaelliang) I added this extra check since the detail fragment may
            // stick around when returning to the CurrentPatientsFragment.
            if (fm.findFragmentByTag(DETAIL_TAG) != null) {
                fm.beginTransaction().remove(fm.findFragmentByTag(DETAIL_TAG)).commit();
            }
        }

        FragmentTransaction transaction = fm.beginTransaction();
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

    public void setMemberDetailFragment(Member member) {
        setMemberDetailFragment(member, null);
    }

    public void setMemberDetailFragment(Member member, IdentificationEvent idEvent) {
        if (member.currentCheckIn() == null) {
            setCheckInMemberDetailFragment(member, idEvent);
        } else {
            setCurrentMemberDetailFragment(member);
        }
    }

    protected void setCheckInMemberDetailFragment(Member member, IdentificationEvent idEvent) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(MEMBER_BUNDLE_FIELD, member);
        bundle.putSerializable(IDENTIFICATION_EVENT_BUNDLE_FIELD, idEvent);
        setFragment(mFragmentProvider.createFragment(CheckInMemberDetailFragment.class, bundle), DETAIL_TAG, false, true);
    }

    protected void setCurrentMemberDetailFragment(Member member) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(MEMBER_BUNDLE_FIELD, member);
        setFragment(mFragmentProvider.createFragment(CurrentMemberDetailFragment.class, bundle), DETAIL_TAG, false, true);
    }

    public void setBarcodeFragment(BarcodeFragment.ScanPurposeEnum scanPurpose,
                                   Member member,
                                   IdentificationEvent idEvent) {
        Bundle bundle = new Bundle();
        bundle.putString(SCAN_PURPOSE_BUNDLE_FIELD, scanPurpose.toString());
        if (member != null) bundle.putSerializable(MEMBER_BUNDLE_FIELD, member);
        if (idEvent != null) bundle.putSerializable(IDENTIFICATION_EVENT_BUNDLE_FIELD, idEvent);
        setFragment(mFragmentProvider.createFragment(BarcodeFragment.class, bundle));
    }

    public void setSearchMemberFragment() {
        setFragment(mFragmentProvider.createFragment(SearchMemberFragment.class));
    }

    public void setEncounterFragment(Encounter encounter) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(SYNCABLE_MODEL_BUNDLE_FIELD, encounter);
        setFragment(mFragmentProvider.createFragment(EncounterFragment.class, bundle));
    }

    public void setEncounterFormFragment(Encounter encounter) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(SYNCABLE_MODEL_BUNDLE_FIELD, encounter);
        setFragment(mFragmentProvider.createFragment(EncounterFormFragment.class, bundle));
    }

    public void setReceiptFragment(Encounter encounter) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(SYNCABLE_MODEL_BUNDLE_FIELD, encounter);
        setFragment(mFragmentProvider.createFragment(ReceiptFragment.class, bundle));
    }

    public void setAddNewBillableFragment(Encounter encounter) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(SYNCABLE_MODEL_BUNDLE_FIELD, encounter);
        setFragment(mFragmentProvider.createFragment(AddNewBillableFragment.class, bundle));
    }

    public void setEnrollmentContactInfoFragment(Member member, IdentificationEvent idEvent) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(SYNCABLE_MODEL_BUNDLE_FIELD, member);
        bundle.putSerializable(IDENTIFICATION_EVENT_BUNDLE_FIELD, idEvent);
        setFragment(mFragmentProvider.createFragment(EnrollmentContactInfoFragment.class, bundle));
    }

    public void setEnrollmentMemberPhotoFragment(Member member, IdentificationEvent idEvent) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(SYNCABLE_MODEL_BUNDLE_FIELD, member);
        bundle.putSerializable(IDENTIFICATION_EVENT_BUNDLE_FIELD, idEvent);
        if (member.getPhotoUrl() == null && member.getPhoto() == null) {
            setFragment(mFragmentProvider.createFragment(EnrollmentMemberPhotoFragment.class, bundle));
        } else if (member.shouldCaptureNationalIdPhoto()) {
            setEnrollmentIdPhotoFragment(member, idEvent);
        } else if (member.getPhoneNumber() == null) {
            setEnrollmentContactInfoFragment(member, idEvent);
        } else if (member.shouldCaptureFingerprint()) {
            setEnrollmentFingerprintFragment(member, idEvent);
        } else {
            ExceptionManager.reportException(new IllegalStateException("Clinic user clicked complete enrollment for member with photo and fingerprint."));
        }
    }

    public void setEnrollmentIdPhotoFragment(Member member, IdentificationEvent idEvent) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(SYNCABLE_MODEL_BUNDLE_FIELD, member);
        bundle.putSerializable(IDENTIFICATION_EVENT_BUNDLE_FIELD, idEvent);
        setFragment(mFragmentProvider.createFragment(EnrollmentIdPhotoFragment.class, bundle));
    }

    public void setEnrollmentFingerprintFragment(Member member, IdentificationEvent idEvent) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(SYNCABLE_MODEL_BUNDLE_FIELD, member);
        bundle.putSerializable(IDENTIFICATION_EVENT_BUNDLE_FIELD, idEvent);
        setFragment(mFragmentProvider.createFragment(EnrollmentFingerprintFragment.class, bundle));
    }

    public void setMemberEditFragment(Member member, IdentificationEvent idEvent) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(SYNCABLE_MODEL_BUNDLE_FIELD, member);
        bundle.putSerializable(IDENTIFICATION_EVENT_BUNDLE_FIELD, idEvent);
        setFragment(mFragmentProvider.createFragment(MemberEditFragment.class, bundle));
    }

    public void setEnrollNewbornInfoFragment(Member newborn, IdentificationEvent idEvent) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(SYNCABLE_MODEL_BUNDLE_FIELD, newborn);
        bundle.putSerializable(IDENTIFICATION_EVENT_BUNDLE_FIELD, idEvent);
        setFragment(mFragmentProvider.createFragment(EnrollNewbornInfoFragment.class, bundle));
    }

    public void setEnrollNewbornPhotoFragment(Member newborn, IdentificationEvent idEvent) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(SYNCABLE_MODEL_BUNDLE_FIELD, newborn);
        bundle.putSerializable(IDENTIFICATION_EVENT_BUNDLE_FIELD, idEvent);
        setFragment(mFragmentProvider.createFragment(EnrollNewbornPhotoFragment.class, bundle));
    }

    public void setVersionFragment() {
        setFragment(new VersionAndSyncFragment());
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
                ExceptionManager.reportException(e);
                return null;
            }
        }
    }
}
