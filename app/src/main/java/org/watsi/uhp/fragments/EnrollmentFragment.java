package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.watsi.uhp.R;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.Member;

public abstract class EnrollmentFragment extends BaseFragment {

    protected Member mMember;
    protected Button mSaveBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(getTitleLabelId());

        mMember = (Member) getArguments().getSerializable(NavigationManager.MEMBER_BUNDLE_FIELD);

        View view = inflater.inflate(getFragmentLayoutId(), container, false);

        mSaveBtn = (Button) view.findViewById(R.id.save_button);
        if (isLastStep()) {
            mSaveBtn.setText(R.string.enrollment_complete_btn);
        }

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextStep();
            }
        });

        setUpFragment(view);

        return view;
    }

    abstract int getTitleLabelId();
    abstract int getFragmentLayoutId();
    abstract boolean isLastStep();
    abstract void nextStep();
    abstract void setUpFragment(View view);
}
