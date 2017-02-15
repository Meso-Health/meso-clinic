package org.watsi.uhp.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import org.watsi.uhp.activities.MainActivity;
import org.watsi.uhp.adapters.BillableAdapter;
import org.watsi.uhp.database.BillableDao;
import org.watsi.uhp.database.BillableEncounterDao;
import org.watsi.uhp.database.EncounterDao;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.BillableEncounter;
import org.watsi.uhp.models.Encounter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;


public class EncounterFragment extends Fragment {

    private Billable.CategoryEnum selectedCategory = null;
    private Spinner servicesSpinner;
    private Spinner labsSpinner;
    private Spinner suppliesSpinner;
    private Spinner vaccinesSpinner;
    private SearchView drugSearch;
    private View currentProductView;
    private final Map<String, List<Billable>> filteredBillablesMap = new HashMap<>();
    private BillableAdapter billableAdapter;
    private List<Billable> billables;
    private Button createEncounterButton;
    private Encounter.IdMethodEnum idMethod;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        idMethod = Encounter.IdMethodEnum.valueOf(getArguments().getString("idMethod"));

        final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_encounter, container, false);

        Spinner categorySpinner = (Spinner) view.findViewById(R.id.category_spinner);
        final ArrayAdapter categoryAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                Billable.CategoryEnum.values()
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setTag("category");

        servicesSpinner = (Spinner) view.findViewById(R.id.services_spinner);
        labsSpinner = (Spinner) view.findViewById(R.id.labs_spinner);
        suppliesSpinner = (Spinner) view.findViewById(R.id.supplies_spinner);
        vaccinesSpinner = (Spinner) view.findViewById(R.id.vaccines_spinner);
        drugSearch = (SearchView) view.findViewById(R.id.drug_search);

        CategoryListener listener = new CategoryListener();
        categorySpinner.setOnItemSelectedListener(listener);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        drugSearch.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        final Button selectBillableButton = (Button) view.findViewById(R.id.enter_billable);
        selectBillableButton.setOnClickListener(new CreateBillableListener());

        ListView billablesListView = (ListView) view.findViewById(R.id.billables_list);

        createEncounterButton = (Button) view.findViewById(R.id.save_encounter);
        createEncounterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: this should be in a transaction
                Encounter encounter = new Encounter();
                encounter.setIdMethod(Encounter.IdMethodEnum.BARCODE);
                encounter.setDate(Calendar.getInstance().getTime());
                encounter.setIdMethod(idMethod);
                try {
                    // TODO: get actual member instead of arbitrarily selecting first
                    encounter.setMember(MemberDao.all().get(0));
                    EncounterDao.create(encounter);
                    BillableDao.create(billables);
                    for (Billable billable : billables) {
                        BillableEncounter billableEncounter = new BillableEncounter(billable, encounter);
                        BillableEncounterDao.create(billableEncounter);
                    }
                } catch (SQLException e) {
                    Rollbar.reportException(e);
                }
                MainActivity activity = (MainActivity) getActivity();
                RecentEncountersFragment fragment = new RecentEncountersFragment();
                FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        billables = new ArrayList<>();
        billableAdapter = new BillableAdapter(getContext(), billables, createEncounterButton);
        billablesListView.setAdapter(billableAdapter);

        return view;
    }

    private Map<String,List<Billable>> getFilteredBillableMap(Billable.CategoryEnum category) {
        filteredBillablesMap.clear();
        try {
            List<Billable> filteredBillables =
                    BillableDao.findByCategory(category);
            for (Billable billable : filteredBillables) {
                if (filteredBillablesMap.containsKey(billable.getDisplayName())) {
                    filteredBillablesMap.get(billable.getDisplayName()).add(billable);
                } else {
                    List<Billable> list = new ArrayList<>();
                    list.add(billable);
                    filteredBillablesMap.put(billable.getDisplayName(), list);
                }
            }
        } catch (SQLException e) {
            Rollbar.reportException(e);
        }
        return filteredBillablesMap;
    }

    private ArrayAdapter getProductAdapter(Map<String, List<Billable>> billableMap) {
        SortedSet<String> filteredBillableDisplayStrings = new TreeSet<>(billableMap.keySet());
        // TODO: check that creation of new adapter each time does not have memory implications
        return new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                filteredBillableDisplayStrings.toArray(new String[filteredBillableDisplayStrings.size()])
        );
    }

    private Billable getSelectedBillable(View view) {
        String displayString;
        Spinner spinner = (Spinner) view;
        displayString = (String) spinner.getSelectedItem();
        List<Billable> matches = filteredBillablesMap.get(displayString);
        if (matches == null) {
            return null;
        } else {
            // TODO: if multiple matches, find billable based on department
            return matches.get(0);
        }
    }

    public void addSearchSuggestionToBillableList (String billableId) {
        try {
            Billable billable = BillableDao.findById(billableId);
            billableAdapter.add(billable);
        } catch (SQLException e) {
            Rollbar.reportException(e);
        }
    }

    public void clearDrugSearch() {
        drugSearch.clearFocus();
        drugSearch.setQuery("", false);
    }

    private class CategoryListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            selectedCategory = (Billable.CategoryEnum) parent.getItemAtPosition(position);
            drugSearch.setVisibility(View.GONE);
            servicesSpinner.setVisibility(View.GONE);
            labsSpinner.setVisibility(View.GONE);
            suppliesSpinner.setVisibility(View.GONE);
            vaccinesSpinner.setVisibility(View.GONE);
            switch (selectedCategory) {
                case DRUGS:
                    currentProductView = drugSearch;
                    break;
                case SERVICES:
                    currentProductView = servicesSpinner;
                    break;
                case LABS:
                    currentProductView = labsSpinner;
                    break;
                case SUPPLIES:
                    currentProductView = suppliesSpinner;
                    break;
                case VACCINES:
                    currentProductView = vaccinesSpinner;
                    break;
            }

            if (!Billable.CategoryEnum.DRUGS.equals(selectedCategory)) {
                Map<String, List<Billable>> filteredBillableMap = getFilteredBillableMap(selectedCategory);
                ArrayAdapter adapter = getProductAdapter(filteredBillableMap);
                ((Spinner) currentProductView).setAdapter(adapter);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            }
            currentProductView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // no-op
        }
    }

    private class CreateBillableListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Billable selectedBillable = getSelectedBillable(currentProductView);
            billableAdapter.add(selectedBillable);
            if (createEncounterButton.getVisibility() == View.GONE) {
                createEncounterButton.setVisibility(View.VISIBLE);
            }
        }
    }
}
