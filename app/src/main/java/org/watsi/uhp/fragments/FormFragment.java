package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.watsi.uhp.R;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.SyncableModel;

public abstract class FormFragment<T extends SyncableModel> extends BaseFragment {

    protected T mSyncableModel;
    protected Button mSaveBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(getTitleLabelId());

        mSyncableModel = (T) getArguments().getSerializable(NavigationManager.SYNCABLE_MODEL_BUNDLE_FIELD);

        final View view = inflater.inflate(getFragmentLayoutId(), container, false);

        mSaveBtn = (Button) view.findViewById(R.id.save_button);

        if (mSaveBtn != null) {
            mSaveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nextStep(view);
                }
            });
        }

        setUpFragment(view);

        return view;
    }

    abstract int getTitleLabelId();

    abstract int getFragmentLayoutId();

    public abstract boolean isFirstStep();

    public abstract void nextStep(View view);

    abstract void setUpFragment(View view);
}
