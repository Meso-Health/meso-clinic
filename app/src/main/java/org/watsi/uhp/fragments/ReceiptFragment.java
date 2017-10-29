package org.watsi.uhp.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Adapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.adapters.ReceiptItemAdapter;
import org.watsi.uhp.databinding.FragmentReceiptBinding;
import org.watsi.uhp.databinding.FragmentReceiptListFooterBinding;
import org.watsi.uhp.databinding.FragmentReceiptListHeaderBinding;
import org.watsi.uhp.helpers.ListViewUtils;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.view_models.DiagnosesListViewModel;
import org.watsi.uhp.view_models.EncounterViewModel;

import java.sql.SQLException;

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
            getNavigationManager().setCurrentPatientsFragment();

            toastMessage = mSyncableModel.getMember()
                    .getFullName() + getString(R.string.encounter_submitted);
        } catch (SQLException | AbstractModel.ValidationException e) {
            toastMessage = "Failed to save data, contact support.";
            ExceptionManager.reportException(e);
        }

        Toast.makeText(getContext(), toastMessage, Toast.LENGTH_LONG).show();
    }

    public Encounter getEncounter() {
        return mSyncableModel;
    }

    @Override
    void setUpFragment(View view) {
        setHasOptionsMenu(true);
        getActivity().invalidateOptionsMenu();

        FragmentReceiptBinding binding = DataBindingUtil.bind(view);
        EncounterViewModel viewModel = new EncounterViewModel(mSyncableModel, getContext());
        binding.setEncounter(viewModel);

        DiagnosesListViewModel diagnosisListViewModel = new DiagnosesListViewModel(getContext(), mSyncableModel, false);
        binding.setDiagnosesItemListView(diagnosisListViewModel);

        setUpEncounterItemListView(view, viewModel);

        ListViewUtils.setDynamicHeight((ListView) view.findViewById(R.id.receipt_items));
    }

    // Android does not let you nest views that scroll and by default ListView is a scrollable view,
    // so in order to have the entire receipt content by scrollable, we have to include it within
    // the ListView by adding the top label and bottom total information as a header/footer
    private void setUpEncounterItemListView(View view, EncounterViewModel viewModel) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService (Context.LAYOUT_INFLATER_SERVICE);

        ListView listView = (ListView) view.findViewById(R.id.receipt_items);

        View listHeaderView = inflater.inflate(R.layout.fragment_receipt_list_header, null);
        FragmentReceiptListHeaderBinding headerBinding = DataBindingUtil.bind(listHeaderView);
        headerBinding.setEncounter(viewModel);
        listView.addHeaderView(listHeaderView);

        View listFooterView = inflater.inflate(R.layout.fragment_receipt_list_footer, null);
        FragmentReceiptListFooterBinding footerBinding = DataBindingUtil.bind(listFooterView);
        footerBinding.setEncounter(viewModel);
        listView.addFooterView(listFooterView);

        Adapter mAdapter = new ReceiptItemAdapter(getActivity(), mSyncableModel.getEncounterItems());
        listView.setAdapter((ListAdapter) mAdapter);

        listView.setFooterDividersEnabled(false);
        listView.setHeaderDividersEnabled(false);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_submit_without_copayment).setVisible(true);
    }
}
