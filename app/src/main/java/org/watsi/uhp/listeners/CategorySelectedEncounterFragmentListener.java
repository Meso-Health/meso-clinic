package org.watsi.uhp.listeners;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;

import org.watsi.uhp.managers.KeyboardManager;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.presenters.EncounterPresenter;

public class CategorySelectedEncounterFragmentListener implements AdapterView.OnItemSelectedListener {

    private final EncounterPresenter mEncounterPresenter;
    private final Context mContext;

    public CategorySelectedEncounterFragmentListener(EncounterPresenter encounterPresenter, Context context) {
        this.mEncounterPresenter = encounterPresenter;
        this.mContext = context;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mEncounterPresenter.getDrugSearchView().setVisibility(View.GONE);
        mEncounterPresenter.getBillableSpinner().setVisibility(View.GONE);

        if (position != 0) {
            String categoryString = (String) parent.getItemAtPosition(position);
            Billable.TypeEnum selectedCategory = Billable.TypeEnum.valueOf(categoryString);
            if (selectedCategory.equals(Billable.TypeEnum.DRUG)) {
                mEncounterPresenter.getDrugSearchView().setVisibility(View.VISIBLE);
                KeyboardManager.focusAndForceShowKeyboard(mEncounterPresenter.getDrugSearchView(), mContext);
            } else {
                mEncounterPresenter.setBillableSpinner(selectedCategory);
                mEncounterPresenter.getBillableSpinner().setVisibility(View.VISIBLE);
                mEncounterPresenter.getBillableSpinner().performClick();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // no-op
    }
}
