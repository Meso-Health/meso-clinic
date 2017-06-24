package org.watsi.uhp.presenters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.SimHelper;
import com.simprints.libsimprints.Verification;

import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.R;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.fragments.IdentifyMemberDetailFragment;
import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.managers.SessionManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.models.SyncableModel;

import java.sql.SQLException;

/**
 * Created by michaelliang on 6/13/17.
 */

public class IdentifyMemberDetailPresenter extends MemberDetailPresenter {
    static final int SIMPRINTS_VERIFICATION_INTENT = 1;
    // static final int SIMPRINTS_IDENTIFICATION_INTENT = 1;

    private final SessionManager mSessionManager;
    private IdentificationEvent mUnsavedIdentificationEvent;
    private final IdentifyMemberDetailFragment mIdentifyMemberDetailPresenterFragment;
    private Member mThroughMember;
    private IdentificationEvent.SearchMethodEnum mIdMethod;

    public IdentifyMemberDetailPresenter(NavigationManager navigationManager, SessionManager sessionManager, IdentifyMemberDetailFragment identifyMemberDetailFragment, View view, Context context, Member member, IdentificationEvent.SearchMethodEnum idMethod, Member throughMember) {
        super(view, context, member, navigationManager);

        mIdentifyMemberDetailPresenterFragment = identifyMemberDetailFragment;
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

    protected void setMemberActionButton() {
        Button memberActionButton = getMemberActionButton();
        memberActionButton.setText(R.string.scan_fingerprints);
        memberActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimHelper simHelper = new SimHelper(BuildConfig.SIMPRINTS_API_KEY, mSessionManager.getCurrentLoggedInUsername());
                Intent fingerprintIdentificationIntent = simHelper.verify(BuildConfig.PROVIDER_ID.toString(), getMember().getFingerprintsGuid().toString());
                mIdentifyMemberDetailPresenterFragment.startActivityForResult(
                        fingerprintIdentificationIntent,
                        SIMPRINTS_VERIFICATION_INTENT
                );
            }
        });
    }

    //// Tested above
    //// Below TBD because of fingerprints

    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
        mUnsavedIdentificationEvent.setFingerprintsVerificationResultCode(resultCode);

        // Report any errors if necessary.
        if (requestCode != SIMPRINTS_VERIFICATION_INTENT) {
            ExceptionManager.reportException(new IllegalStateException("Request code in simprints call was from a different intent: " + requestCode));
        }

        if (resultCode != Constants.SIMPRINTS_OK) {
            // TODO make this message more useful
            ExceptionManager.reportException(new IllegalStateException("ResultCode in simprints call is not OK. resultCode: " + resultCode));
        }

        if (resultCode == Constants.SIMPRINTS_CANCELLED) {
            showScanFailedToast();
        } else if (resultCode == Constants.SIMPRINTS_OK) {
            Verification verification = data.getParcelableExtra(Constants.SIMPRINTS_VERIFICATION);
            String fingerprintTier = verification.getTier().toString();
            float fingerprintConfidence = verification.getConfidence();

            mUnsavedIdentificationEvent.setFingerprintsVerificationConfidence(fingerprintConfidence);
            mUnsavedIdentificationEvent.setFingerprintsVerificationTier(fingerprintTier);

            showScanSuccessfulToast();
            navigateToCheckInMemberDetailFragment();
        } else {
            // TODO No toast here?
            // Reasons that it would reach here is more of a simprints issue.
            // So we want this to advance to the clinic form.
            navigateToCheckInMemberDetailFragment();
        }
    }

    protected void setMemberActionLink() {
        getMemberActionLink().setVisibility(View.VISIBLE);
        getMemberActionLink().setText(R.string.reject_identity);
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
            mUnsavedIdentificationEvent.saveChanges(((ClinicActivity) getContext()).getAuthenticationToken());
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

    protected void navigateToCheckInMemberDetailFragment() {
        getNavigationManager().setCheckInMemberDetailFragment(getMember(), mUnsavedIdentificationEvent);
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
