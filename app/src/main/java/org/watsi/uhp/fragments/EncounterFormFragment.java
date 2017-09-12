package org.watsi.uhp.fragments;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterForm;
import org.watsi.uhp.models.Photo;

import java.io.IOException;

public class EncounterFormFragment extends PhotoFragment<Encounter> {

    private EncounterForm mEncounterForm;
    private Button mAddAnotherBtn;

    @Override
    int getTitleLabelId() {
        return R.string.encounter_form_fragment_label;
    }

    @Override
    int getFragmentLayoutId() {
        return R.layout.fragment_encounter_form;
    }

    @Override
    public boolean isFirstStep() {
        return false;
    }

    @Override
    public void nextStep() {
        // no-op
    }

    @Override
    void handleSetupFailure() {
        getNavigationManager().setReceiptFragment(mSyncableModel);
        Toast.makeText(getContext(), R.string.generic_error_message, Toast.LENGTH_LONG).show();
    }

    void additionalSetup(View view) {
        mEncounterForm = new EncounterForm();
        mAddAnotherBtn = (Button) view.findViewById(R.id.add_another_button);
        mAddAnotherBtn.setOnClickListener(new AddPhotoClickListener(false));
        view.findViewById(R.id.finish_button).setOnClickListener(new AddPhotoClickListener(true));
    }

    @Override
    void onPhotoCaptured(Photo photo) throws IOException {
        mEncounterForm.setPhoto(photo);
        mAddAnotherBtn.setEnabled(true);
    }

    private class AddPhotoClickListener implements View.OnClickListener {

        private boolean finished;

        AddPhotoClickListener(boolean finished) {
            this.finished = finished;
        }

        @Override
        public void onClick(View v) {
            if (mEncounterForm.getPhoto() != null) {
                mSyncableModel.addEncounterForm(mEncounterForm);
            }
            if (finished) {
                getNavigationManager().setReceiptFragment(mSyncableModel);
            } else {
                getNavigationManager().setEncounterFormFragment(mSyncableModel);
            }
        }
    }
}
