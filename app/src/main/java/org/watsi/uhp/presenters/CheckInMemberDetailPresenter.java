package org.watsi.uhp.presenters;

import android.content.Context;
import android.content.Intent;
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
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;

public class CheckInMemberDetailPresenter extends MemberDetailPresenter {
    private static final int DEFAULT_BORDER_WIDTH = 2;
    private static final String SIMPRINTS_VERIFICATION_TIER5 = "TIER_5";

    private IdentificationEvent mIdEvent;
    private final CheckInMemberDetailFragment mCheckInMemberDetailFragment;
    private SimprintsHelper mSimprintsHelper;

    private final Button mScanFingerprintsBtn;
    private final TextView mScanResult;

    public CheckInMemberDetailPresenter(NavigationManager navigationManager, SessionManager sessionManager, CheckInMemberDetailFragment checkInMemberDetailFragment, View view, Context context, Member member, IdentificationEvent idEvent) {
        super(view, context, member, navigationManager);
        mCheckInMemberDetailFragment = checkInMemberDetailFragment;
        mIdEvent = idEvent;
        mSimprintsHelper = new SimprintsHelper(sessionManager.getCurrentLoggedInUsername(), mCheckInMemberDetailFragment);

        mScanFingerprintsBtn = (Button) getView().findViewById(R.id.scan_fingerprints_btn);
        mScanResult = (TextView) getView().findViewById(R.id.scan_result);
    }

    @Override
    public void additionalSetup() {
        setScanFingerprintButton();
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

    protected void navigateToCompleteEnrollmentFragment() {
        getNavigationManager().startCompleteEnrollmentFlow(getMember(), mIdEvent);
    }

    @Override
    public void navigateToMemberEditFragment() {
        getNavigationManager().setMemberEditFragment(getMember(), mIdEvent);
    }

    private void setScanFingerprintButton() {
        if (getMember().getFingerprintsGuid() != null) {
            mScanFingerprintsBtn.setVisibility(View.VISIBLE);
            mScanFingerprintsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        mSimprintsHelper.verify(BuildConfig.PROVIDER_ID.toString(), getMember().getFingerprintsGuid());
                    } catch (SimprintsHelper.SimprintsInvalidIntentException e) {
                        Toast.makeText(
                                getContext(),
                                R.string.simprints_not_installed,
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
        mIdEvent.setFingerprintsVerificationResultCode(resultCode);
        try {
            Verification verification = mSimprintsHelper.onActivityResultFromVerify(requestCode, resultCode, data);
            if (verification != null) {
                saveIdentificationEventWithVerificationData(verification);
                showScanSuccessfulToast();
            } else {
                showScanFailedToast();
            }
        } catch (SimprintsHelper.SimprintsHelperException e) {
            ExceptionManager.reportException(e);
            showScanFailedToast();
        }
        setScanResult();
    }

    void setScanResult() {
        if (SIMPRINTS_VERIFICATION_TIER5.equals(mIdEvent.getFingerprintsVerificationTier())) {
            setScanResultProperties(ContextCompat.getColor(getContext(), R.color.indicatorRed), R.string.bad_scan_indicator);
        } else if (mIdEvent.getFingerprintsVerificationTier() != null) {
            setScanResultProperties(ContextCompat.getColor(getContext(), R.color.indicatorGreen), R.string.good_scan_indicator);
        } else if (mIdEvent.getFingerprintsVerificationResultCode() != null && mIdEvent.getFingerprintsVerificationResultCode() != Constants.SIMPRINTS_CANCELLED) {
            setScanResultProperties(ContextCompat.getColor(getContext(), R.color.indicatorNeutral), R.string.no_scan_indicator);
        }
    }

    void setScanResultProperties(int color, int textId) {
        mScanResult.invalidate();
        mScanResult.setText(textId);
        mScanResult.setTextColor(color);
        GradientDrawable border = (GradientDrawable) mScanResult.getBackground();
        border.setStroke(DEFAULT_BORDER_WIDTH, color);
        VectorDrawable fingerprintIcon = (VectorDrawable) mScanResult.getCompoundDrawables()[0];
        //mutate() allows us to modify only this instance of the drawable without affecting others
        fingerprintIcon.mutate().setTint(color);

        mScanFingerprintsBtn.setVisibility(View.GONE);
        mScanResult.setVisibility(View.VISIBLE);
    }

    void saveIdentificationEventWithVerificationData(Verification verification) {
        String fingerprintTier = verification.getTier().toString();
        float fingerprintConfidence = verification.getConfidence();

        mIdEvent.setFingerprintsVerificationConfidence(fingerprintConfidence);
        mIdEvent.setFingerprintsVerificationTier(fingerprintTier);
    }

    void showScanFailedToast() {
        Toast.makeText(getContext(), R.string.fingerprint_scan_failed, Toast.LENGTH_LONG).show();
    }

    void showScanSuccessfulToast() {
        Toast.makeText(getContext(), R.string.fingerprint_scan_successful, Toast.LENGTH_LONG).show();
    }

    private void openClinicNumberDialog() {
        ClinicNumberDialogFragment clinicNumberDialog = new ClinicNumberDialogFragment();
        clinicNumberDialog.show(((ClinicActivity) getContext()).getSupportFragmentManager(),
                "ClinicNumberDialogFragment");
        clinicNumberDialog.setTargetFragment(mCheckInMemberDetailFragment, 0);
    }

    public void saveIdentificationEventAndCheckIn(IdentificationEvent.ClinicNumberTypeEnum clinicNumberType, int clinicNumber) throws SQLException, AbstractModel.ValidationException {
        mIdEvent.setClinicNumber(clinicNumber);
        mIdEvent.setClinicNumberType(clinicNumberType);
        mIdEvent.setAccepted(true);
        mIdEvent.setOccurredAt(Clock.getCurrentTime());
        mIdEvent.saveChanges(((ClinicActivity) getContext()).getAuthenticationToken());
        displayIdentificationSuccessfulToast();
        navigateToCurrentPatientsFragment();
    }

    private void displayIdentificationSuccessfulToast() {
        Toast.makeText(getContext(),
                mIdEvent.getMember().getFullName() + " " + getContext().getString(R.string.identification_approved),
                Toast.LENGTH_LONG).
                show();
    }

    private void navigateToCurrentPatientsFragment() {
        getNavigationManager().setCurrentPatientsFragment();
    }
}
