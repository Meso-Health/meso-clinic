package org.watsi.uhp.view_models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.View;

import org.watsi.domain.entities.Encounter;
import org.watsi.uhp.BR;

public class PresentingConditionsViewModel extends BaseObservable {
    private Encounter mEncounter;
    private View mView;

    public PresentingConditionsViewModel(View view, Encounter encounter) {
        mEncounter = encounter;
        mView = view;
        // Reason we don't add a default to the database model right now is because for unsynced
        // encounters, we cannot assume hasFever = false.
        if (mEncounter.getHasFever() == null) {
            mEncounter.setHasFever(false);
        }
    }

    @Bindable
    public boolean getHasFever() {
        return mEncounter.getHasFever();
    }

    public void onCheckedFever(View view) {
        mEncounter.setHasFever(!mEncounter.getHasFever());
        notifyPropertyChanged(BR.hasFever);
    }
}
