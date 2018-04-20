package org.watsi.uhp.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.watsi.domain.entities.IdentificationEvent;
import org.watsi.domain.entities.Member;
import org.watsi.domain.entities.Photo;
import org.watsi.domain.repositories.MemberRepository;
import org.watsi.uhp.R;
import org.watsi.uhp.managers.LegacyNavigationManager;
import org.watsi.uhp.presenters.EnrollmentPresenter;

import java.io.IOException;

import javax.inject.Inject;

public class EnrollmentMemberPhotoFragment extends PhotoFragment<Member> {

    private IdentificationEvent mIdEvent;
    private EnrollmentPresenter enrollmentPresenter;

    @Inject MemberRepository memberRepository;

    @Override
    int getTitleLabelId() {
        return R.string.enrollment_member_photo_fragment_label;
    }

    @Override
    int getFragmentLayoutId() {
        return R.layout.fragment_capture_photo;
    }

    @Override
    public boolean isFirstStep() {
        return true;
    }

    @Override
    public void nextStep() {
        if (!mSyncableModel.shouldCaptureFingerprint()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(R.string.enrollment_fingerprint_confirm_completion);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    memberRepository.save(mSyncableModel);
                    getNavigationManager().setMemberDetailFragment(mSyncableModel, mIdEvent);
                    enrollmentPresenter.confirmationToast().show();
                }
            });
            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        } else if (mSyncableModel.shouldCaptureNationalIdPhoto()) {
            getNavigationManager().setEnrollmentIdPhotoFragment(mSyncableModel, mIdEvent);
        } else {
            getNavigationManager().setEnrollmentContactInfoFragment(mSyncableModel, mIdEvent);
        }
    }

    @Override
    void handleSetupFailure() {
        getNavigationManager().setCurrentPatientsFragment();
        Toast.makeText(getContext(), R.string.generic_error_message, Toast.LENGTH_LONG).show();
    }

    @Override
    void additionalSetup(View view) {
        ((Button) view.findViewById(R.id.photo_btn)).setText(R.string.enrollment_member_photo_btn);
        mIdEvent = (IdentificationEvent) getArguments().getSerializable(LegacyNavigationManager.IDENTIFICATION_EVENT_BUNDLE_FIELD);

        if (!mSyncableModel.shouldCaptureFingerprint()) {
            mSaveBtn.setText(R.string.enrollment_complete_btn);
        }

        enrollmentPresenter = new EnrollmentPresenter(mSyncableModel, getContext());
    }

    @Override
    void onPhotoCaptured(Photo photo) throws IOException {
        mSyncableModel.setLocalMemberPhoto(photo);
        mSaveBtn.setEnabled(true);
    }
}
