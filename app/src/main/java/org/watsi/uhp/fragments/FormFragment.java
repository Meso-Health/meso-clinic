package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.watsi.uhp.R;
import org.watsi.uhp.managers.NavigationManager;

public abstract class FormFragment<T> extends BaseFragment {

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
                    nextStep();
                }
            });
        }

        setUpFragment(view);

        return view;
    }

    @StringRes
    abstract int getTitleLabelId();

    @LayoutRes
    abstract int getFragmentLayoutId();

    public abstract boolean isFirstStep();

    public abstract void nextStep();

    abstract void setUpFragment(View view);
}
