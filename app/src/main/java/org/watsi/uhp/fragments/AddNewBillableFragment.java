package org.watsi.uhp.fragments;

import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.managers.KeyboardManager;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterItem;

public class AddNewBillableFragment extends FormFragment<Encounter> {

    private EditText mNameField;
    private EditText mPriceField;
    private View mView;

    @Override
    int getTitleLabelId() {
        return R.string.add_new_billable_fragment_label;
    }

    @Override
    int getFragmentLayoutId() {
        return R.layout.fragment_add_new_billable;
    }

    @Override
    public boolean isFirstStep() {
        return false;
    }

    @Override
    public void nextStep() {
        if (mNameField.getText().toString().length() == 0) {
            Toast.makeText(getActivity(), R.string.empty_billable_name_field,
                    Toast.LENGTH_LONG).show();
        } else if (mPriceField.getText().toString().length() == 0) {
            Toast.makeText(getActivity(), R.string.empty_billable_price_field,
                    Toast.LENGTH_LONG).show();
        } else {
            Billable billable = new Billable();
            billable.setName(mNameField.getText().toString());
            billable.setPrice(Integer.parseInt(mPriceField.getText().toString()));
            billable.setType(Billable.TypeEnum.UNSPECIFIED);
            billable.setCreatedDuringEncounter(true);

            EncounterItem encounterItem = new EncounterItem();
            encounterItem.setBillable(billable);

            KeyboardManager.hideKeyboard(mView, getContext());

            mSyncableModel.getEncounterItems().add(encounterItem);
            getNavigationManager().setEncounterFragment(mSyncableModel);
        }
    }

    @Override
    void setUpFragment(View view) {
        mView = view;
        mNameField = (EditText) view.findViewById(R.id.name_field);
        mPriceField = (EditText) view.findViewById(R.id.price_field);

        KeyboardManager.focusAndForceShowKeyboard(mNameField, getContext());
    }
}
