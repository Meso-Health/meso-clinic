package org.watsi.uhp.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.rollbar.android.Rollbar;
import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.Registration;

import org.watsi.uhp.R;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.ConfigManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class EnrollmentFingerprintFragment extends Fragment {

    private static int SIMPRINTS_ENROLLMENT_INTENT = 3;

    private Member mMember;
    private View mSuccessMessageView;
    private View mFailedMessageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        getActivity().setTitle(R.string.enrollment_fingerprint_label);
        View view = inflater.inflate(R.layout.fragment_enrollment_fingerprint, container, false);

        try {
            mMember = MemberDao.findById(UUID.fromString(getArguments().getString("memberId")));
        } catch (SQLException e) {
            Rollbar.reportException(e);
        }

        mSuccessMessageView = view.findViewById(R.id.enrollment_fingerprint_success_message);
        mFailedMessageView = view.findViewById(R.id.enrollment_fingerprint_failed_message);

        Button continueBtn = (Button) view.findViewById(R.id.enrollment_fingerprint_save_btn);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new NavigationManager(getActivity()).setCurrentPatientsFragment();
            }
        });
        Button fingerprintBtn = (Button) view.findViewById(R.id.enrollment_fingerprint_capture_btn);
        fingerprintBtn.setOnClickListener(new CaptureThumbprintClickListener(mMember.getId(), this));
        return view;
    }

    private static class CaptureThumbprintClickListener implements View.OnClickListener {

        private UUID mMemberId;
        private EnrollmentFingerprintFragment mFragment;

        public CaptureThumbprintClickListener(UUID memberId, EnrollmentFingerprintFragment fragment) {
            this.mMemberId = memberId;
            this.mFragment = fragment;
        }

        @Override
        public void onClick(View v) {
            mFragment.hideFingerprintMessage();
            Intent captureFingerprintIntent = new Intent(Constants.SIMPRINTS_REGISTER_INTENT);
            captureFingerprintIntent.putExtra(
                    Constants.SIMPRINTS_API_KEY,
                    ConfigManager.getSimprintsApiKey(mFragment.getContext())
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
        Rollbar.reportMessage("Simprints scan failed");
    }

    private void hideFingerprintMessage() {
        mSuccessMessageView.setVisibility(View.GONE);
        mFailedMessageView.setVisibility(View.GONE);
    }
}
