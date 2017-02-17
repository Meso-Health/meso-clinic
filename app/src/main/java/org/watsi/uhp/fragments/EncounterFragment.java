package org.watsi.uhp.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;

import com.rollbar.android.Rollbar;

import org.watsi.uhp.R;
import org.watsi.uhp.adapters.EncounterItemAdapter;
import org.watsi.uhp.database.BillableDao;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.LineItem;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class EncounterFragment extends Fragment {

    private Spinner categorySpinner;
    private Spinner billableSpinner;
    private SearchView billableSearch;
    private ListView lineItemsListView;
    private EncounterItemAdapter encounterItemAdapter;
    private List<LineItem> lineItems;
    private Button createEncounterButton;
    private Encounter.IdMethodEnum idMethod;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_encounter, container, false);

        categorySpinner = (Spinner) view.findViewById(R.id.category_spinner);
        billableSpinner = (Spinner) view.findViewById(R.id.billable_spinner);
        billableSearch = (SearchView) view.findViewById(R.id.drug_search);
        lineItemsListView = (ListView) view.findViewById(R.id.line_items_list);
        createEncounterButton = (Button) view.findViewById(R.id.save_encounter);
        idMethod = Encounter.IdMethodEnum.valueOf(getArguments().getString("idMethod"));

        setCategorySpinner();
        setBillableSearch();
        setLineItemList();
        setCreateEncounterButton();

        return view;
    }

    private void setCategorySpinner() {
        ArrayList<Object> categories = new ArrayList<>();
        categories.add(getContext().getString(R.string.prompt_category));
        categories.addAll(Arrays.asList(Billable.CategoryEnum.values()));
        
        final ArrayAdapter categoryAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                categories
        );
        
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setTag("category");
        categorySpinner.setOnItemSelectedListener(new CategoryListener());
    }
    
    private void setBillableSearch() {
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        billableSearch.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
    }
    
    private void setBillableSpinner(Billable.CategoryEnum category) {
        SimpleCursorAdapter adapter = getlineItemAdapter(category);

        billableSpinner.setAdapter(adapter);
        billableSpinner.setOnItemSelectedListener(new BillableListener());
    }

    private void setLineItemList() {
        lineItems = new ArrayList<>();
        encounterItemAdapter = new EncounterItemAdapter(getContext(), lineItems, createEncounterButton);
        lineItemsListView.setAdapter(encounterItemAdapter);
    }

    private void setCreateEncounterButton() {
        createEncounterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                ReceiptFragment receiptFragment = new ReceiptFragment();
                Bundle bundle = new Bundle();

                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, receiptFragment);
                transaction.addToBackStack(null);
                transaction.commit();

//                // TODO: this should be in a transaction
//                Encounter encounter = new Encounter();
//                encounter.setIdMethod(Encounter.IdMethodEnum.BARCODE);
//                encounter.setDate(Calendar.getInstance().getTime());
//                encounter.setIdMethod(idMethod);
//                try {
//                    // TODO: get actual member instead of arbitrarily selecting first
//                    encounter.setMember(MemberDao.all().get(0));
//                    EncounterDao.create(encounter);
//                    BillableDao.create(billables);
//                    for (Billable billable : billables) {
//                        LineItemEncounter billableEncounter = new LineItemEncounter(billable, encounter);
//                        BillableEncounterDao.create(billableEncounter);
//                    }
//                } catch (SQLException e) {
//                    Rollbar.reportException(e);
//                }
//                MainActivity activity = (MainActivity) getActivity();
//                RecentEncountersFragment fragment = new RecentEncountersFragment();
//                FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
//                transaction.replace(R.id.fragment_container, fragment);
//                transaction.addToBackStack(null);
//                transaction.commit();
            }
        });
    }

    private SimpleCursorAdapter getlineItemAdapter(Billable.CategoryEnum category) {
        // TODO: check that creation of new adapter each time does not have memory implications
        try {
            //Create prompt
            MatrixCursor extras = new MatrixCursor(new String[] { "_id", "name" });
            extras.addRow(new String[] { "0", getContext().getString(R.string.prompt_billable) });

            //Merge prompt with billable results
            Cursor cursor = BillableDao.getBillablesByCategoryCursor(category);
            Cursor[] cursors = { extras, cursor };
            Cursor extendedCursor = new MergeCursor(cursors);

            //Create cursor adapter with merged cursor
            String[] from = { Billable.FIELD_NAME_NAME };
            int[] to = new int[] { android.R.id.text1 };

            return new SimpleCursorAdapter(getContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    extendedCursor, from, to, 0
            );
        } catch (SQLException e) {
            Rollbar.reportException(e);
        }
        return null;
    }

    public void addToLineItemList (String billableId) {
        try {
            Billable billable = BillableDao.findById(billableId);
            LineItem lineItem = new LineItem();
            lineItem.setBillable(billable);
            
            encounterItemAdapter.add(lineItem);
            createEncounterButton.setVisibility(View.VISIBLE);
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
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (position != 0) {
                addToLineItemList(Long.toString(id));
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            //no-op
        }
    }
}
