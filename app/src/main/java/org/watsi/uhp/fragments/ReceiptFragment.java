package org.watsi.uhp.fragments;

import android.view.View;
import android.widget.Adapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.adapters.ReceiptItemAdapter;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterItem;

import java.sql.SQLException;
import java.util.List;

public class ReceiptFragment extends FormFragment<Encounter> {

    @Override
    int getTitleLabelId() {
        return R.string.receipt_fragment_label;
    }

    @Override
    int getFragmentLayoutId() {
        return R.layout.fragment_receipt;
    }

    @Override
    public boolean isFirstStep() {
        return false;
    }

    @Override
    public void nextStep() {
        String toastMessage;
        try {
            mSyncableModel.saveChanges(getAuthenticationToken());
            toastMessage = mSyncableModel.getMember()
                    .getFullName() + getString(R.string.encounter_submitted);
            Toast.makeText(getContext(), toastMessage, Toast.LENGTH_LONG).show();
            getNavigationManager().setCurrentPatientsFragment();
        } catch (SQLException | AbstractModel.ValidationException e) {
            toastMessage = "Failed to save data, contact support.";
            Toast.makeText(getContext(), toastMessage, Toast.LENGTH_LONG).show();
            ExceptionManager.reportException(e);
        }
    }

    @Override
    void setUpFragment(View view) {
        List<EncounterItem> encounterItems = (List<EncounterItem>) mSyncableModel.getEncounterItems();

        ListView listView = (ListView) view.findViewById(R.id.receipt_items);
        Adapter mAdapter = new ReceiptItemAdapter(getActivity(), encounterItems);
        listView.setAdapter((ListAdapter) mAdapter);

        TextView priceTextView = (TextView) view.findViewById(R.id.total_price);

        String formattedPrice = Encounter.PRICE_FORMAT.format(mSyncableModel.price());
        priceTextView.setText(getString(R.string.price_with_currency, formattedPrice));

        int numFormsAttached = mSyncableModel.getEncounterForms().size();
        ((TextView) view.findViewById(R.id.forms_attached)).setText(getActivity().getResources()
                .getQuantityString(R.plurals.forms_attached_label, numFormsAttached, numFormsAttached));
    }
}
