package org.watsi.uhp.fragments;

import android.databinding.DataBindingUtil;
import android.view.View;

import org.watsi.uhp.R;
import org.watsi.uhp.custom_components.BillableCompositionInput;
import org.watsi.uhp.databinding.FragmentAddNewBillableBinding;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.KeyboardManager;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterItem;
import org.watsi.uhp.view_models.BillableViewModel;

import java.sql.SQLException;


public class AddNewBillableFragment extends FormFragment<Encounter> {
    private BillableViewModel mBillableViewModel;
    private BillableCompositionInput mCompositionNameTextView;

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
        Billable billable = mBillableViewModel.getBillable();

        EncounterItem encounterItem = new EncounterItem();
        encounterItem.setBillable(billable);

        mSyncableModel.getEncounterItems().add(encounterItem);
        KeyboardManager.hideKeyboard(getView(), getContext());
        getNavigationManager().setEncounterFragment(mSyncableModel);
    }

    @Override
    void setUpFragment(View view) {
        FragmentAddNewBillableBinding binding = DataBindingUtil.bind(view);
        mBillableViewModel = new BillableViewModel(this);
        binding.setBillable(mBillableViewModel);

        try {
            mCompositionNameTextView = (BillableCompositionInput) view.findViewById(R.id.list_of_compositions);
            mCompositionNameTextView.setCompositionChoices(Billable.getBillableCompositions());
        } catch (SQLException e) {
            ExceptionManager.reportException(e);
        }
    }
}
