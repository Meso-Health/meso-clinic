package org.watsi.uhp.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.rollbar.android.Rollbar;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.MainActivity;
import org.watsi.uhp.database.IdentificationDao;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.models.Identification;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;

public class DetailFragment extends Fragment {

    private Member mMember;
    private TextView mMemberName;
    private TextView mMemberAge;
    private TextView mMemberGender;
    private TextView mMemberId;
    private ImageView mMemberPhoto;
    private Identification.IdMethodEnum mIdMethod;
    private Button mConfirmButton;
    private Button mRejectButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(R.string.detail_fragment_label);

        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        String memberId = getArguments().getString("memberId");
        String idMethod = getArguments().getString("idMethod");
        mIdMethod = Identification.IdMethodEnum.valueOf(idMethod);

        try {
            mMember = MemberDao.findById(memberId);
        } catch (SQLException e) {
            Rollbar.reportException(e);
        }

        mMemberName = (TextView) view.findViewById(R.id.member_name);
        mMemberAge = (TextView) view.findViewById(R.id.member_age);
        mMemberGender = (TextView) view.findViewById(R.id.member_gender);
        mMemberId = (TextView) view.findViewById(R.id.member_id);
        mMemberPhoto = (ImageView) view.findViewById(R.id.member_photo);
        mConfirmButton = (Button) view.findViewById(R.id.confirm_identity);
        mRejectButton = (Button) view.findViewById(R.id.reject_identity);

        setPatientCard();
        setConfirmButton();
        setRejectButton();
        return view;
    }

    private void setPatientCard() {
        mMemberName.setText(mMember.getFullName());
        mMemberAge.setText("Age - " + String.valueOf(mMember.getAge()));
        mMemberGender.setText(String.valueOf(mMember.getGender()));
        mMemberId.setText(String.valueOf(mMember.getCardId()));
        Bitmap photoBitmap = mMember.getPhotoBitmap();
        if (photoBitmap != null) {
            mMemberPhoto.setImageBitmap(photoBitmap);
        } else {
            mMemberPhoto.setImageResource(R.drawable.portrait_placeholder);
        }
    }

    private void setConfirmButton() {
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createIdentification(true);
                ((MainActivity) getActivity()).setCurrentPatientsFragment();
            }
        });
    }

    private void setRejectButton() {
        mRejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createIdentification(false);
                ((MainActivity) getActivity()).setCurrentPatientsFragment();
            }
        });
    }

    private void createIdentification(boolean successful) {
        // TODO: this should be in a transaction
        Identification id = new Identification();
        id.setMember(mMember);
        id.setIdMethod(mIdMethod);
        id.setSuccessful(successful);

        try {
            IdentificationDao.create(id);
        } catch (SQLException e) {
            Rollbar.reportException(e);
        }
    }
}
