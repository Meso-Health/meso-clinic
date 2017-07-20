package org.watsi.uhp.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.R;
import org.watsi.uhp.helpers.SimprintsHelper;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.presenters.EnrollmentPresenter;

import java.sql.SQLException;
import java.util.UUID;

public class EnrollmentFingerprintFragment extends FormFragment<Member> {
    private IdentificationEvent mIdEvent;
    private View mSuccessMessageView;
    private View mFailedMessageView;
    private EnrollmentPresenter enrollmentPresenter;
    private SimprintsHelper mSimprintsHelper;

    @Override
    int getTitleLabelId() {
        return R.string.enrollment_fingerprint_label;
    }

    @Override
    int getFragmentLayoutId() {
        return R.layout.fragment_enrollment_fingerprint;
    }

    @Override
    public boolean isFirstStep() {
        return false;
    }

    @Override
    void nextStep(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.enrollment_fingerprint_confirm_completion);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    mSyncableModel.saveChanges(getAuthenticationToken());
                    getNavigationManager().setMemberDetailFragment(mSyncableModel, mIdEvent);
                    enrollmentPresenter.confirmationToast().show();
                } catch (SQLException e) {
                    ExceptionManager.reportException(e);
                    Toast.makeText(getContext(), "Failed to save fingerprint", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    void setUpFragment(View view) {
        mSuccessMessageView = view.findViewById(R.id.enrollment_fingerprint_success_message);
        mFailedMessageView = view.findViewById(R.id.enrollment_fingerprint_failed_message);
        mIdEvent = (IdentificationEvent) getArguments().getSerializable(NavigationManager.IDENTIFICATION_EVENT_BUNDLE_FIELD);
        mSimprintsHelper = new SimprintsHelper(getSessionManager().getCurrentLoggedInUsername(), this);

        Button fingerprintBtn = (Button) view.findViewById(R.id.enrollment_fingerprint_capture_btn);
        fingerprintBtn.setOnClickListener(new CaptureThumbprintClickListener(mSyncableModel.getId(), this, mSimprintsHelper));

        enrollmentPresenter = new EnrollmentPresenter(mSyncableModel, getContext());
    }

    private static class CaptureThumbprintClickListener implements View.OnClickListener {

        private UUID mSyncableModelId;
        private EnrollmentFingerprintFragment mFragment;
        private SimprintsHelper mSimprintsHelper;

        CaptureThumbprintClickListener(UUID memberId, EnrollmentFingerprintFragment fragment, SimprintsHelper simprintsHelper) {
            this.mSyncableModelId = memberId;
            this.mFragment = fragment;
            mSimprintsHelper = simprintsHelper;
        }

        @Override
        public void onClick(View v) {
            mFragment.hideFingerprintMessage();

            try {
                mSimprintsHelper.enroll(BuildConfig.PROVIDER_ID.toString(), mSyncableModelId.toString());
            } catch (SimprintsHelper.SimprintsInvalidIntentException e) {
                ExceptionManager.reportException(e);
                Toast.makeText(
                        mFragment.getContext(),
                        R.string.simprints_not_installed,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            UUID fingerprintsGuid = mSimprintsHelper.onActivityResultFromEnroll(requestCode, resultCode, data);
            if (fingerprintsGuid != null) {
                mSyncableModel.setFingerprintsGuid(fingerprintsGuid);
                mSuccessMessageView.setVisibility(View.VISIBLE);
            } else {
                showFingerprintScanFailedMessage();
            }
        } catch (SimprintsHelper.SimprintsHelperException e) {
            ExceptionManager.reportException(e);
            showFingerprintScanFailedMessage();
        }
    }

    private void showFingerprintScanFailedMessage() {
        mFailedMessageView.setVisibility(View.VISIBLE);
    }

    private void hideFingerprintMessage() {
        mSuccessMessageView.setVisibility(View.GONE);
        mFailedMessageView.setVisibility(View.GONE);
    }

}
