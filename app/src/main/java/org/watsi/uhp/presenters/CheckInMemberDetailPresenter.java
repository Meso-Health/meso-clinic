package org.watsi.uhp.presenters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.VectorDrawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.Verification;

import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.R;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.fragments.CheckInMemberDetailFragment;
import org.watsi.uhp.fragments.ClinicNumberDialogFragment;
import org.watsi.uhp.helpers.SimprintsHelper;
import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.managers.SessionManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;

public class CheckInMemberDetailPresenter extends MemberDetailPresenter {
    static final int DEFAULT_BORDER_WIDTH = 2;
    static final String SIMPRINTS_VERIFICATION_TIER5 = "TIER_5";

    private final SessionManager mSessionManager;
    private IdentificationEvent mIdEvent;
    private final CheckInMemberDetailFragment mCheckInMemberDetailFragment;
    private SimprintsHelper mSimprintsHelper;

    private final Button mMemberSecondaryButton;
    private final TextView mMemberIndicator;

    public CheckInMemberDetailPresenter(NavigationManager navigationManager, SessionManager sessionManager, CheckInMemberDetailFragment checkInMemberDetailFragment, View view, Context context, Member member, IdentificationEvent idEvent) {
        super(view, context, member, navigationManager);
        mCheckInMemberDetailFragment = checkInMemberDetailFragment;
        mSessionManager = sessionManager;
        mIdEvent = idEvent;
        mSimprintsHelper = new SimprintsHelper(mSessionManager.getCurrentLoggedInUsername(), mCheckInMemberDetailFragment);

        mMemberSecondaryButton = getMemberSecondaryButton();
        mMemberIndicator = getMemberIndicator();
    }

    @Override
    protected void setMemberActionButton() {
        Button memberActionButton = getMemberActionButton();
        memberActionButton.setText(R.string.check_in);
        memberActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openClinicNumberDialog();
            }
        });
    }

    @Override
    protected void setMemberSecondaryActionButton() {
        if (getMember().isAbsentee()) {
            setMemberSecondaryButtonProperties("Complete Enrollment", false,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getNavigationManager().setEnrollmentMemberPhotoFragment(getMember(), mIdEvent);
                        }
                    }
            );
        } else if (getMember().getFingerprintsGuid() != null && mIdEvent.getFingerprintsVerificationTier() == null) {
            setMemberSecondaryButtonProperties("Scan", true,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                mSimprintsHelper.verify(BuildConfig.PROVIDER_ID.toString(), getMember().getFingerprintsGuid());
                            } catch (SimprintsHelper.SimprintsInvalidIntentException e) {
                                Toast.makeText(
                                        getContext(),
                                        R.string.simprints_not_installed,
                                        Toast.LENGTH_LONG).show();
                            } catch (SimprintsHelper.SimprintsHelperException e) {
                                ExceptionManager.reportException(e);
                                showProceedToCheckAnywayToastAndReport();
                            }
                        }
                    }
            );
        }
    }

    @Override
    protected void setMemberIndicator() {
        if (SIMPRINTS_VERIFICATION_TIER5.equals(mIdEvent.getFingerprintsVerificationTier())) {
            setMemberIndicatorProperties(ContextCompat.getColor(getContext(), R.color.indicatorRed), R.string.bad_scan_indicator);
        } else if (mIdEvent.getFingerprintsVerificationTier() != null) {
            setMemberIndicatorProperties(ContextCompat.getColor(getContext(), R.color.indicatorGreen), R.string.good_scan_indicator);
        } else if (mIdEvent.getFingerprintsVerificationResultCode() != null && mIdEvent.getFingerprintsVerificationResultCode() == Constants.SIMPRINTS_CANCELLED) {
            setMemberIndicatorProperties(ContextCompat.getColor(getContext(), R.color.indicatorNeutral), R.string.no_scan_indicator);
        }
    }

    @Override
    protected void setMemberActionLink() {
        // no-op
    }

    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
        // how do you want to shift the tasks over to the helper?
        try {
            Verification verification = getSimprintsHelper().onActivityResultFromVerify(requestCode, resultCode, data);
            mIdEvent.setFingerprintsVerificationResultCode(resultCode);
            if (verification != null) {
                saveIdentificationEventWithVerificationData(verification);
                showScanSuccessfulToast();
                showFingerprintsResults();
            } else {
                // When an event is cancelled, we want it to be a non 0 number because 0 is default resultCode if
                // no fingerprints call was made.
                showScanFailedToast();
            }
        } catch (SimprintsHelper.SimprintsHelperException e) {
            ExceptionManager.reportException(e);
            showProceedToCheckAnywayToastAndReport();
        }
    }

    protected SimprintsHelper getSimprintsHelper() {
        return mSimprintsHelper;
    }

    protected void showProceedToCheckAnywayToastAndReport() {
        Toast.makeText(getContext(),
                "Some issues with the scanner, please check in anyway.",
                Toast.LENGTH_LONG).
                show();
    }

    protected void showFingerprintsResults() {
        getMemberSecondaryButton().setVisibility(View.INVISIBLE);
        setMemberIndicator();
    }

    protected void setMemberSecondaryButtonProperties(String text, boolean showFingerprintsIcon, View.OnClickListener onClickListener) {
        mMemberSecondaryButton.setVisibility(View.VISIBLE);
        mMemberSecondaryButton.setText(text);
        if (showFingerprintsIcon) {
            addFingerprintsIconToSecondaryButton();
        }
        mMemberSecondaryButton.setOnClickListener(onClickListener);
    }

    protected void addFingerprintsIconToSecondaryButton() {
        Drawable fingerprintIcon = getContext().getResources().getDrawable(R.drawable.fingerprints, null);
        fingerprintIcon.setTint(ContextCompat.getColor(getContext(), R.color.title));
        mMemberSecondaryButton.setCompoundDrawablesWithIntrinsicBounds(fingerprintIcon, null, null, null);
    }

    protected void setMemberIndicatorProperties(int color, int textId) {
        mMemberIndicator.setVisibility(View.VISIBLE);
        mMemberIndicator.setText(textId);
        mMemberIndicator.setTextColor(color);
        mMemberIndicator.invalidate();
        GradientDrawable border = (GradientDrawable) mMemberIndicator.getBackground();
        border.setStroke(DEFAULT_BORDER_WIDTH, color);
        VectorDrawable fingerprintIcon = (VectorDrawable) mMemberIndicator.getCompoundDrawables()[0];
        fingerprintIcon.setTint(color);
    }

    protected void saveIdentificationEventWithVerificationData(Verification verification) {
        String fingerprintTier = verification.getTier().toString();
        float fingerprintConfidence = verification.getConfidence();

        mIdEvent.setFingerprintsVerificationConfidence(fingerprintConfidence);
        mIdEvent.setFingerprintsVerificationTier(fingerprintTier);
    }

    public IdentificationEvent getIdEvent() {
        return mIdEvent;
    }

    protected void showScanFailedToast() {
        Toast.makeText(getContext(), R.string.fingerprint_scan_failed, Toast.LENGTH_LONG).show();
    }

    protected void showScanSuccessfulToast() {
        Toast.makeText(getContext(), R.string.fingerprint_scan_successful, Toast.LENGTH_LONG).show();
    }

    private void openClinicNumberDialog() {
        ClinicNumberDialogFragment clinicNumberDialog = new ClinicNumberDialogFragment();
        clinicNumberDialog.show(((ClinicActivity) getContext()).getSupportFragmentManager(),
                "ClinicNumberDialogFragment");
        clinicNumberDialog.setTargetFragment(mCheckInMemberDetailFragment, 0);
    }

    public void saveIdentificationEventAndCheckIn(IdentificationEvent.ClinicNumberTypeEnum clinicNumberType, int clinicNumber) throws SQLException {
        mIdEvent.setClinicNumber(clinicNumber);
        mIdEvent.setClinicNumberType(clinicNumberType);
        mIdEvent.setAccepted(true);
        mIdEvent.setOccurredAt(Clock.getCurrentTime());
        mIdEvent.saveChanges(((ClinicActivity) getContext()).getAuthenticationToken());
        displayIdentificationSuccessfulToast();
        navigateToCurrentPatientsFragment();
    }

    protected void navigateToCurrentPatientsFragment() {
        getNavigationManager().setCurrentPatientsFragment();
    }

    protected void displayIdentificationSuccessfulToast() {
        Toast.makeText(getContext(),
                mIdEvent.getMember().getFullName() + " " + getContext().getString(R.string.identification_approved),
                Toast.LENGTH_LONG).
                show();
    }

    public void reportMember() {
        mIdEvent.setAccepted(false);
        if (mIdEvent.getOccurredAt() == null) {
            mIdEvent.setOccurredAt(Clock.getCurrentTime());
        }
        confirmBeforeReporting();
    }

    protected void confirmBeforeReporting() {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.reject_identity_alert)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        try {
                            mIdEvent.saveChanges(((ClinicActivity) getContext()).getAuthenticationToken());
                            getNavigationManager().setCurrentPatientsFragment();
                            Toast.makeText(getContext(),
                                    getMember().getFullName() + " " + getContext().getString(R.string.identification_rejected),
                                    Toast.LENGTH_LONG).
                                    show();
                        } catch (SQLException e) {
                            ExceptionManager.reportException(e);
                        }
                    }
                }).create().show();
    }
}
