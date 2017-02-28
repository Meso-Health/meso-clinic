package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.rollbar.android.Rollbar;

import org.watsi.uhp.R;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.UUID;

public class EnrollmentQuestionsFragment extends Fragment {

    private Member mMember;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        getActivity().setTitle(R.string.enrollment_questions_label);

        try {
            mMember = MemberDao.findById(UUID.fromString(getArguments().getString("memberId")));
        } catch (SQLException e) {
            Rollbar.reportException(e);
        }

        View view = inflater.inflate(R.layout.fragment_enrollment_questions, container, false);

        Button continueBtn = (Button) view.findViewById(R.id.enrollment_questions_save_button);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new NavigationManager(getActivity()).setEnrollmentPhotosFragment(mMember.getId());
            }
        });
        return view;
    }
}
