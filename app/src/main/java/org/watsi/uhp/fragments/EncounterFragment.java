package org.watsi.uhp.fragments;

import android.app.SearchManager;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import com.rollbar.android.Rollbar;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.MainActivity;
import org.watsi.uhp.adapters.EncounterItemAdapter;
import org.watsi.uhp.database.BillableDao;
import org.watsi.uhp.managers.KeyboardManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.LineItem;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class EncounterFragment extends Fragment {

    private Spinner categorySpinner;
    private Spinner billableSpinner;
    private SearchView billableSearch;
    private SimpleCursorAdapter billableCursorAdapter;
    private ListView lineItemsListView;
    private EncounterItemAdapter encounterItemAdapter;
    private Button continueToReceiptButton;
    private TextView addBillableLink;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(R.string.encounter_fragment_label);

        final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_encounter, container, false);

        categorySpinner = (Spinner) view.findViewById(R.id.category_spinner);
        billableSpinner = (Spinner) view.findViewById(R.id.billable_spinner);
        billableSearch = (SearchView) view.findViewById(R.id.drug_search);
        addBillableLink = (TextView) view.findViewById(R.id.add_billable_prompt);
        lineItemsListView = (ListView) view.findViewById(R.id.line_items_list);
        continueToReceiptButton = (Button) view.findViewById(R.id.save_encounter);

        setCategorySpinner();
        setBillableSearch();
        setLineItemList();
        setContinueToReceiptButton();
        setAddBillableLink();

        return view;
    }

    private void setCategorySpinner() {
        ArrayList<Object> categories = new ArrayList<>();
        categories.add(getContext().getString(R.string.prompt_category));
        categories.addAll(Arrays.asList(Billable.CategoryEnum.values()));
        categories.remove(Billable.CategoryEnum.UNSPECIFIED);

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
        billableSearch.setQueryHint(getActivity().getString(R.string.search_drug_hint));
    }
    
    private void setBillableSpinner(Billable.CategoryEnum category) {
        ArrayAdapter<Billable> adapter = getEncounterItemAdapter(category);

        billableSpinner.setAdapter(adapter);
        billableSpinner.setOnItemSelectedListener(new BillableListener(adapter));
    }

    private void setLineItemList() {
        List<LineItem> lineItems = ((MainActivity) getActivity()).getCurrentLineItems();
        continueToReceiptButton.setVisibility(View.VISIBLE);

        encounterItemAdapter = new EncounterItemAdapter(getContext(), lineItems, continueToReceiptButton);
        lineItemsListView.setAdapter(encounterItemAdapter);
    }

    private void setAddBillableLink() {
        addBillableLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new NavigationManager(getActivity()).setAddNewBillableFragment();
            }
        });
    }

    private void setContinueToReceiptButton() {
        continueToReceiptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new NavigationManager(getActivity()).setReceiptFragment();
            }
        });
    }

    private ArrayAdapter<Billable> getEncounterItemAdapter(Billable.CategoryEnum category) {
        // TODO: check that creation of new adapter each time does not have memory implications
        List<Billable> billables = new ArrayList<>();
        Billable placeholderBillable = new Billable();
        placeholderBillable.setName(getContext().getString(R.string.prompt_billable));
        billables.add(placeholderBillable);
        try {
            billables.addAll(BillableDao.getBillablesByCategory(category));
        } catch (SQLException e) {
            Rollbar.reportException(e);
        }

        return new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                billables
        );
    }

    public static boolean containsId(List<LineItem> list, UUID id) {
        for (LineItem item : list) {
            UUID itemId = item.getBillable().getId();
            if (itemId != null && itemId.equals(id)) {
                return true;
            }
        }
        return false;
    }

    public void addToLineItemList(UUID billableId) {
        try {
            Billable billable = BillableDao.findById(billableId);
            List<LineItem> lineItems = ((MainActivity) getActivity()).getCurrentLineItems();

            if (containsId(lineItems, billableId)) {
                Toast.makeText(getActivity().getApplicationContext(), R.string.already_in_list_items,
                        Toast.LENGTH_SHORT).show();
            } else {
                LineItem lineItem = new LineItem();
                lineItem.setBillable(billable);

                encounterItemAdapter.add(lineItem);

                continueToReceiptButton.setVisibility(View.VISIBLE);
            }
        } catch (SQLException e) {
            Rollbar.reportException(e);
        }
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
                Billable.CategoryEnum selectedCategory = (Billable.CategoryEnum) parent
                        .getItemAtPosition(position);
                if (selectedCategory.equals(Billable.CategoryEnum.DRUGS)) {
                    billableSearch.setVisibility(View.VISIBLE);
                    billableSearch.requestFocus();
                    KeyboardManager.hideKeyboard(getContext());
                } else {
                    setBillableSpinner(selectedCategory);
                    billableSpinner.setVisibility(View.VISIBLE);
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
                UUID billableId = ((Billable) adapter.getItem(position)).getId();
                addToLineItemList(billableId);
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
            if (query.length() > 2) {
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
                    Rollbar.reportException(e);
                }
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
            addToLineItemList(UUID.fromString(uuidString));
            clearDrugSearch();
            return true;
        }
    }
}
