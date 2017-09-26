package org.watsi.uhp.fragments;

import android.databinding.DataBindingUtil;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import org.watsi.uhp.R;
import org.watsi.uhp.databinding.FragmentAddNewBillableBinding;
import org.watsi.uhp.managers.KeyboardManager;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterItem;
import org.watsi.uhp.view_models.BillableViewModel;


public class AddNewBillableFragment extends FormFragment<Encounter> {
    private BillableViewModel mBillableViewModel;
    private AutoCompleteTextView mTextView;

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
        billable.setCreatedDuringEncounter(true);

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

        // TODO: Figure out a better place to put this code.
        ArrayAdapter<String> adapter = new ArrayAdapter(
                getActivity(),
                android.R.layout.simple_list_item_1,
                Billable.getBillableCompositions()
        );
        mTextView = (AutoCompleteTextView) view.findViewById(R.id.list_of_compositions);
        mTextView.setAdapter(adapter);
        // This forces the list of options to be shown even when there's no text entered.
        mTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mTextView.showDropDown();
                }
            }
        });
        // This forces the list of options to be shown even when there's no text entered.
        mTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mTextView.showDropDown();
                return false;
            }
        });
        // This forces keyboard to close after selecting an item.
        mTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                KeyboardManager.hideKeyboard(mTextView, getContext());
            }
        });
    }
}
