package org.watsi.uhp.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.Registration;

import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.R;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.ConfigManager;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;

import java.sql.SQLException;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class EnrollmentFingerprintFragment extends EnrollmentFragment {

    private static int SIMPRINTS_ENROLLMENT_INTENT = 3;

    private View mSuccessMessageView;
    private View mFailedMessageView;

    @Override
    int getTitleLabelId() {
        return R.string.enrollment_fingerprint_label;
    }

    @Override
    int getFragmentLayoutId() {
        return R.layout.fragment_enrollment_fingerprint;
    }

    @Override
    boolean isLastStep() {
        return true;
    }

    @Override
    void nextStep() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.enrollment_fingerprint_confirm_completion);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    mMember.setUnsynced(ConfigManager.getLoggedInUserToken(getContext()));
                    MemberDao.update(mMember);
                    new NavigationManager(getActivity()).setCurrentPatientsFragment();
                    Toast.makeText(getContext(), "Enrollment completed", Toast.LENGTH_LONG).show();
                } catch (SQLException e) {
                    ExceptionManager.handleException(e);
                    Toast.makeText(getContext(), "Failed to save fingerprint", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
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

        Button fingerprintBtn = (Button) view.findViewById(R.id.enrollment_fingerprint_capture_btn);
        fingerprintBtn.setOnClickListener(new CaptureThumbprintClickListener(mMember.getId(), this));
    }

    private static class CaptureThumbprintClickListener implements View.OnClickListener {

        private UUID mMemberId;
        private EnrollmentFingerprintFragment mFragment;

        CaptureThumbprintClickListener(UUID memberId, EnrollmentFingerprintFragment fragment) {
            this.mMemberId = memberId;
            this.mFragment = fragment;
        }

        @Override
        public void onClick(View v) {
            mFragment.hideFingerprintMessage();
            Intent captureFingerprintIntent = new Intent(Constants.SIMPRINTS_REGISTER_INTENT);
            captureFingerprintIntent.putExtra(
                    Constants.SIMPRINTS_API_KEY,
                    BuildConfig.SIMPRINTS_API_KEY
            );
            captureFingerprintIntent.putExtra(
                    Constants.SIMPRINTS_USER_ID,
                    mMemberId.toString()
            );
            PackageManager packageManager = mFragment.getActivity().getPackageManager();
            if (captureFingerprintIntent.resolveActivity(packageManager) != null) {
                mFragment.startActivityForResult(
                        captureFingerprintIntent,
                        SIMPRINTS_ENROLLMENT_INTENT
                );
            } else {
                Toast.makeText(
                        mFragment.getContext(),
                        R.string.enrollment_fingerprint_simprints_not_installed,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK || data == null) {
            showFingerprintScanFailedMessage();
            return;
        }

        Registration registration = data.getParcelableExtra(Constants.SIMPRINTS_REGISTRATION);
        if (registration == null || registration.getGuid() == null) {
            showFingerprintScanFailedMessage();
        } else {
            mMember.setFingerprintsGuid(UUID.fromString(registration.getGuid()));
            mSuccessMessageView.setVisibility(View.VISIBLE);
        }
    }

    private void showFingerprintScanFailedMessage() {
        mFailedMessageView.setVisibility(View.VISIBLE);
        ExceptionManager.reportMessage("Simprints scan failed");
    }

    private void hideFingerprintMessage() {
        mSuccessMessageView.setVisibility(View.GONE);
        mFailedMessageView.setVisibility(View.GONE);
    }

}
