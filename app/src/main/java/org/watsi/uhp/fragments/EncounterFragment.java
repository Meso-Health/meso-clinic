package org.watsi.uhp.fragments;

import android.app.SearchManager;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.adapters.EncounterItemAdapter;
import org.watsi.uhp.database.BillableDao;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.KeyboardManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.presenters.EncounterPresenter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class EncounterFragment extends BaseFragment {

    private Spinner categorySpinner;
    private Spinner billableSpinner;
    private SearchView billableSearch;
    private SimpleCursorAdapter billableCursorAdapter;
    private ListView lineItemsListView;
    private EncounterItemAdapter encounterItemAdapter;
    private Encounter encounter;
    private TextView backdateEncounterLink;
    private EncounterPresenter encounterPresenter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        encounter = (Encounter) getArguments().getSerializable(NavigationManager.ENCOUNTER_BUNDLE_FIELD);
        getActivity().setTitle(encounter.getMember().getFullName());

        encounterItemAdapter = new EncounterItemAdapter(getContext(), new ArrayList<>(encounter.getEncounterItems()));
        encounterPresenter = new EncounterPresenter(encounter, encounterItemAdapter);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_encounter, container, false);

        categorySpinner = (Spinner) view.findViewById(R.id.category_spinner);
        billableSpinner = (Spinner) view.findViewById(R.id.billable_spinner);
        billableSearch = (SearchView) view.findViewById(R.id.drug_search);
        lineItemsListView = (ListView) view.findViewById(R.id.line_items_list);
        backdateEncounterLink = (TextView) view.findViewById(R.id.backdate_encounter);
        Button continueToReceiptButton = (Button) view.findViewById(R.id.save_encounter);

        setCategorySpinner();
        setBillableSearch();
        setLineItemList();
        setContinueToReceiptButton(continueToReceiptButton);
        setAddBillableLink(view);
        setBackdateEncounterListener();

        return view;
    }

    protected Encounter getEncounter() {
        return encounter;
    }

    private void setCategorySpinner() {
        ArrayList<Object> categories = new ArrayList<>();
        categories.add(getContext().getString(R.string.prompt_category));
        categories.addAll(Arrays.asList(Billable.TypeEnum.values()));
        categories.remove(Billable.TypeEnum.UNSPECIFIED);

        ArrayAdapter categoryAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                categories
        );

        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setTag("category");
        categorySpinner.setOnItemSelectedListener(new CategoryListener());
    }
    
    private void setBillableSearch() {
        billableSearch.setOnQueryTextListener(new BillableSearchListener());
        billableSearch.setOnSuggestionListener(new SuggestionClickListener());
        billableSearch.setQueryHint(getString(R.string.search_drug_hint));
    }
    
    private void setBillableSpinner(Billable.TypeEnum category) {
        ArrayAdapter<Billable> adapter = getEncounterItemAdapter(category);

        billableSpinner.setAdapter(adapter);
        billableSpinner.setOnItemSelectedListener(new BillableListener(adapter));
    }

    private void setLineItemList() {
        lineItemsListView.setAdapter(encounterItemAdapter);
    }

    private void scrollToBottom() {
        lineItemsListView.post(new Runnable() {
            @Override
            public void run() {
                lineItemsListView.setSelection(encounterItemAdapter.getCount() - 1);
            }
        });
    }

    private void setAddBillableLink(View view) {
        view.findViewById(R.id.add_billable_prompt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationManager().setAddNewBillableFragment(encounter);
            }
        });
    }

    private void setBackdateEncounterListener() {
        final Fragment fragment = this;
        backdateEncounterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackdateEncounterDialogFragment dialog = new BackdateEncounterDialogFragment();
                dialog.setTargetFragment(fragment, 0);
                dialog.show(getActivity().getSupportFragmentManager(), "BackdateEncounterDialogFragment");
            }
        });
    }

    private void setContinueToReceiptButton(Button continueToReceiptButton) {
        continueToReceiptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationManager().setEncounterFormFragment(encounter);
            }
        });
    }

    private ArrayAdapter<Billable> getEncounterItemAdapter(Billable.TypeEnum category) {
        // TODO: check that creation of new adapter each time does not have memory implications
        List<Billable> billables = new ArrayList<>();
        Billable placeholderBillable = new Billable();
        placeholderBillable.setName(getContext().getString(R.string.prompt_billable) + " " +
                category.toString().toLowerCase() + "...");
        billables.add(placeholderBillable);
        try {
            billables.addAll(BillableDao.getBillablesByCategory(category));
        } catch (SQLException e) {
            ExceptionManager.reportException(e);
        }

        return new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                billables
        );
    }

    public void clearDrugSearch() {
        billableSearch.clearFocus();
        billableSearch.setQuery("", false);
    }

    private class CategoryListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            billableSearch.setVisibility(View.GONE);
            billableSpinner.setVisibility(View.GONE);

            if (position != 0) {
                Billable.TypeEnum selectedCategory = (Billable.TypeEnum) parent
                        .getItemAtPosition(position);
                if (selectedCategory.equals(Billable.TypeEnum.DRUG)) {
                    billableSearch.setVisibility(View.VISIBLE);
                    KeyboardManager.focusAndForceShowKeyboard(billableSearch, getContext());
                } else {
                    setBillableSpinner(selectedCategory);
                    billableSpinner.setVisibility(View.VISIBLE);
                    billableSpinner.performClick();
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // no-op
        }
    }

    private class BillableListener implements AdapterView.OnItemSelectedListener {

        private ArrayAdapter adapter;

        BillableListener(ArrayAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (position != 0) {
                Billable billable = ((Billable) adapter.getItem(position));
                try {
                    encounterPresenter.addToEncounterItemList(billable);
                    scrollToBottom();
                } catch (Encounter.DuplicateBillableException e) {
                    // TODO: make toast message more descriptive
                    Toast.makeText(getContext(), R.string.already_in_list_items, Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            //no-op
        }
    }

    private SimpleCursorAdapter getBillableCursorAdapter(String query) {
        if (query.length() < 3) {
            return null;
        } else {
            String[] cursorColumns = new String[] {
                    "_id",
                    SearchManager.SUGGEST_COLUMN_TEXT_1,
                    SearchManager.SUGGEST_COLUMN_TEXT_2,
                    Billable.FIELD_NAME_ID
            };
            MatrixCursor cursor = new MatrixCursor(cursorColumns);
            try {
                for (Billable billable: BillableDao.fuzzySearchDrugs(query)) {
                    cursor.addRow(new Object[] {
                            billable.getId().getMostSignificantBits(),
                            billable.getName(),
                            billable.dosageDetails(),
                            billable.getId().toString()
                    });
                }
            } catch (SQLException e) {
                ExceptionManager.reportException(e);
            }

            return new SimpleCursorAdapter(
                    getContext(),
                    R.layout.item_billable_search_suggestion,
                    cursor,
                    new String[] { SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2 },
                    new int[] { R.id.text1, R.id.text2 },
                    0
            );
        }
    }

    private class BillableSearchListener implements SearchView.OnQueryTextListener {

        @Override
        public boolean onQueryTextChange(String newText) {
            if (!newText.isEmpty()) {
                billableCursorAdapter = getBillableCursorAdapter(newText);
                billableSearch.setSuggestionsAdapter(billableCursorAdapter);
            }
            return true;
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            //no-op
            return true;
        }
    }

    private class SuggestionClickListener implements SearchView.OnSuggestionListener {

        @Override
        public boolean onSuggestionSelect(int position) {
            //no-op
            return true;
        }

        @Override
        public boolean onSuggestionClick(int position) {
            MatrixCursor cursor = (MatrixCursor) billableCursorAdapter.getItem(position);
            String uuidString = cursor.getString(cursor.getColumnIndex(Billable.FIELD_NAME_ID));
            try {
                Billable billable = BillableDao.findById(UUID.fromString(uuidString));
                encounterPresenter.addToEncounterItemList(billable);
                scrollToBottom();
                clearDrugSearch();
            } catch (Encounter.DuplicateBillableException e) {
                // TODO: make toast message more descriptive
                Toast.makeText(getContext(), R.string.already_in_list_items, Toast.LENGTH_SHORT).show();
            } catch (SQLException e) {
                Toast.makeText(getContext(), "Call Katrina", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
    }

    public void updateBackdateLinkText() {
        SpannableString newText = new SpannableString(encounterPresenter.newDateLinkText(encounter));
        newText.setSpan(new UnderlineSpan(), 0, newText.length(), 0);
        backdateEncounterLink.setText(newText);
    }
}
