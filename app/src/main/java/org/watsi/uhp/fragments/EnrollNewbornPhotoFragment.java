package org.watsi.uhp.fragments;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.watsi.domain.entities.IdentificationEvent;
import org.watsi.domain.entities.Member;
import org.watsi.domain.entities.Photo;
import org.watsi.domain.repositories.MemberRepository;
import org.watsi.uhp.R;
import org.watsi.uhp.managers.LegacyNavigationManager;

import java.io.IOException;

import javax.inject.Inject;

public class EnrollNewbornPhotoFragment extends PhotoFragment<Member> {

    @Inject MemberRepository memberRepository;

    @Override
    int getTitleLabelId() {
        return R.string.enroll_newborn_photo_label;
    }

    @Override
    int getFragmentLayoutId() {
        return R.layout.fragment_capture_photo;
    }

    @Override
    public boolean isFirstStep() {
        return false;
    }

    @Override
    public void nextStep() {
        memberRepository.save(mSyncableModel);
        IdentificationEvent idEvent = (IdentificationEvent) getArguments().getSerializable(LegacyNavigationManager.IDENTIFICATION_EVENT_BUNDLE_FIELD);
        getNavigationManager().setCheckInMemberDetailFragmentAfterEnrollNewborn(mSyncableModel, idEvent);
        Toast.makeText(getContext(), "Enrollment completed", Toast.LENGTH_LONG).show();
    }

    @Override
    void handleSetupFailure() {
        getNavigationManager().setMemberDetailFragment(mSyncableModel);
        Toast.makeText(getContext(), R.string.generic_error_message, Toast.LENGTH_LONG).show();
    }

    @Override
    void additionalSetup(View view) {
        Button savePhotoBtn = ((Button) view.findViewById(R.id.save_button));
        savePhotoBtn.setText(R.string.save_btn_label);

        mSaveBtn.setEnabled(false);
    }

    @Override
    void onPhotoCaptured(Photo photo) throws IOException {
        mSyncableModel.setLocalMemberPhoto(photo);
        mSaveBtn.setEnabled(true);
    }
}
