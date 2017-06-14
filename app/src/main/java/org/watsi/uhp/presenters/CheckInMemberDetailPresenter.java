package org.watsi.uhp.presenters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.SimHelper;
import com.simprints.libsimprints.Verification;

import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.R;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.adapters.MemberAdapter;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.fragments.CheckInMemberDetailFragment;
import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.managers.SessionManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.models.SyncableModel;

import java.sql.SQLException;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by michaelliang on 6/13/17.
 */

public class CheckInMemberDetailPresenter extends MemberDetailPresenter {
    static final int SIMPRINTS_VERIFICATION_INTENT = 1;

    private final SessionManager mSessionManager;
    private IdentificationEvent mUnsavedIdentificationEvent;
    private final CheckInMemberDetailFragment mCheckInMemberDetailPresenterFragment;
    private Member mThroughMember;
    private IdentificationEvent.SearchMethodEnum mIdMethod;

    public CheckInMemberDetailPresenter(NavigationManager navigationManager, SessionManager sessionManager, CheckInMemberDetailFragment checkInMemberDetailFragment, View view, Context context, Member member, IdentificationEvent.SearchMethodEnum idMethod, Member throughMember) {
        super(view, context, member, navigationManager);

        mCheckInMemberDetailPresenterFragment = checkInMemberDetailFragment;
        mSessionManager = sessionManager;
        mIdMethod = idMethod;
        mThroughMember = throughMember;
    }

    public void setUp() {
        super.setUp();
        preFillIdentificationEventFields();
    }

    protected void preFillIdentificationEventFields() {
        mUnsavedIdentificationEvent = new IdentificationEvent();
        mUnsavedIdentificationEvent.setMember(getMember());
        mUnsavedIdentificationEvent.setSearchMethod(mIdMethod);
        mUnsavedIdentificationEvent.setThroughMember(mThroughMember);
        if (getMember().getPhoto() == null) {
            mUnsavedIdentificationEvent.setPhotoVerified(false);
        }
    }

    //// Tested above

    protected void setMemberActionButton() {
        Button memberActionButton = getMemberActionButton();
        if (getMember().getFingerprintsGuid() != null) {
            memberActionButton.setText(R.string.check_in);
            memberActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SimHelper simHelper = new SimHelper(BuildConfig.SIMPRINTS_API_KEY, mSessionManager.getCurrentLoggedInUsername());
                    Intent fingerprintIdentificationIntent = simHelper.verify(BuildConfig.PROVIDER_ID.toString(), getMember() .getFingerprintsGuid().toString());
                    mCheckInMemberDetailPresenterFragment.startActivityForResult(
                            fingerprintIdentificationIntent,
                            SIMPRINTS_VERIFICATION_INTENT
                    );
                }
            });
        } else {
            memberActionButton.setText(R.string.check_in_without_fingerprints);
            memberActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    completeIdentificationWithoutFingerprints();
                }
            });
        }
    }

    protected Button getMemberActionButton() {
         return (Button) getView().findViewById(R.id.member_action_button);
    }

    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SIMPRINTS_VERIFICATION_INTENT && resultCode != RESULT_OK) {
            showScanSuccessfulToast();
            Verification verification = data.getParcelableExtra(Constants.SIMPRINTS_VERIFICATION);
            String fingerprintTier = verification.getTier().toString();
            float fingerprintConfidence = verification.getConfidence();

            mUnsavedIdentificationEvent.setFingerprintsVerificationConfidence(fingerprintConfidence);
            mUnsavedIdentificationEvent.setFingerprintsVerificationResultCode(resultCode);
            mUnsavedIdentificationEvent.setFingerprintsVerificationTier(fingerprintTier);

            navigateToClinicNumberForm();
        } else {
            showScanFailedToast();
            mUnsavedIdentificationEvent.setFingerprintsVerificationResultCode(resultCode);
        }
    }

    protected void setBottomListView() {
        TextView householdListLabel = (TextView) getView().findViewById(R.id.household_members_label);
        ListView householdListView = (ListView) getView().findViewById(R.id.household_members);

        try {
            List<Member> householdMembers = MemberDao.getRemainingHouseholdMembers(
                    getMember().getHouseholdId(), getMember() .getId());
            ListAdapter adapter = new MemberAdapter(getContext(), householdMembers, false);
            int householdSize = householdMembers.size() + 1;

            householdListLabel.setText(mCheckInMemberDetailPresenterFragment.getResources().getQuantityString(
                    R.plurals.household_label, householdSize, householdSize));
            householdListView.setAdapter(adapter);
            householdListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Member member = (Member) parent.getItemAtPosition(position);
                    getNavigationManager().setMemberDetailFragment(
                            member,
                            IdentificationEvent.SearchMethodEnum.THROUGH_HOUSEHOLD,
                            getMember()
                    );
                }
            });
        } catch (SQLException e) {
            ExceptionManager.reportException(e);
        }
    }

    protected void setMemberActionLink() {
        getMemberActionLink().setVisibility(View.VISIBLE);
        getMemberActionLink().setText(getContext().getString(R.string.reject_identity));
        getMemberActionLink().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.reject_identity_alert)
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                try {
                                    completeIdentificationOnReport();
                                } catch (SyncableModel.UnauthenticatedException e) {
                                    ExceptionManager.reportException(e);
                                    Toast.makeText(getContext(),
                                            "Failed to save identification, contact support.",
                                            Toast.LENGTH_LONG).
                                            show();
                                }
                            }
                        }).create().show();
            }
        });
    }

    protected void completeIdentificationOnReport() throws SyncableModel.UnauthenticatedException {
        mUnsavedIdentificationEvent.setClinicNumberType(null);
        mUnsavedIdentificationEvent.setClinicNumber(null);
        mUnsavedIdentificationEvent.setAccepted(false);
        mUnsavedIdentificationEvent.setOccurredAt(Clock.getCurrentTime());
        try {
            mUnsavedIdentificationEvent.saveChanges(((ClinicActivity) mCheckInMemberDetailPresenterFragment.getActivity()).getAuthenticationToken());
        } catch (SQLException e) {
            ExceptionManager.reportException(e);
        }

        getNavigationManager().setCurrentPatientsFragment();
        Toast.makeText(getContext(),
                getMember().getFullName() + " " + R.string.identification_rejected,
                Toast.LENGTH_LONG).
                show();
    }

    public IdentificationEvent getUnsavedIdentificationEvent() {
        return mUnsavedIdentificationEvent;
    }

    protected void navigateToClinicNumberForm() {
        getNavigationManager().setClinicNumberFormFragment(mUnsavedIdentificationEvent);
    }

    protected void showScanFailedToast() {
        Toast.makeText(
                getContext(),
                "Fingerprint Scan Failed",
                Toast.LENGTH_LONG).show();
    }

    protected void showScanSuccessfulToast() {
        Toast.makeText(getContext(), "Fingerprint Scan Successful!", Toast.LENGTH_LONG).show();
    }

    public void completeIdentificationWithoutFingerprints() {
        // TODO, figure out how to deal with non-nullable integers and floats.
        getNavigationManager().setClinicNumberFormFragment(mUnsavedIdentificationEvent);
    }
}
