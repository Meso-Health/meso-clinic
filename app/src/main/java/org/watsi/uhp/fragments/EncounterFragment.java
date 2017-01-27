package org.watsi.uhp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

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
    private AutoCompleteTextView productSearch;
    private View currentProductView;
    private final Map<String, List<Billable>> filteredBillablesMap = new HashMap<>();
    private BillableAdapter billableAdapter;
    private List<Billable> billables;
    private Button createEncounterButton;
    private Encounter.IdMethodEnum idMethod;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        idMethod = Encounter.IdMethodEnum.valueOf(getArguments().getString("idMethod"));

        final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_encounter, container, false);

        final EditText opdIpdInput = (EditText) view.findViewById(R.id.encounter_opd_ipd);
        opdIpdInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                    opdIpdInput.clearFocus();
                }
                return false;
            }
        });

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

        productSearch = (AutoCompleteTextView) view.findViewById(R.id.drug_search);
        productSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // dismisses keyboard when product is selected from autocomplete suggestions
                InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(arg1.getApplicationWindowToken(), 0);
            }
        });

        CategoryListener listener = new CategoryListener();
        categorySpinner.setOnItemSelectedListener(listener);

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
                if (billable.getName().equals("In-Patient")) {
                    // In-patient is determined by radio button selection
                    continue;
                }
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
        if (Billable.CategoryEnum.DRUGS_AND_SUPPLIES.equals(selectedCategory)) {
            displayString = ((AutoCompleteTextView) view).getEditableText().toString();
        } else {
            Spinner spinner = (Spinner) view;
            displayString = (String) spinner.getSelectedItem();
        }
        List<Billable> matches = filteredBillablesMap.get(displayString);
        if (matches == null) {
            return null;
        } else {
            // TODO: if multiple matches, find billable based on department
            return matches.get(0);
        }
    }

    private class CategoryListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            selectedCategory = (Billable.CategoryEnum) parent.getItemAtPosition(position);
            servicesSpinner.setVisibility(View.GONE);
            labsSpinner.setVisibility(View.GONE);
            productSearch.setVisibility(View.GONE);
            switch (selectedCategory) {
                case SERVICES:
                    currentProductView = servicesSpinner;
                    break;
                case LABS:
                    currentProductView = labsSpinner;
                    break;
                case DRUGS_AND_SUPPLIES:
                    currentProductView = productSearch;
                    break;
            }

            Map<String, List<Billable>> filteredBillableMap = getFilteredBillableMap(selectedCategory);
            ArrayAdapter adapter = getProductAdapter(filteredBillableMap);
            // AutoCompleteTextView & Spinner are not descendants of same adapter interface, so forced to cast
            if (Billable.CategoryEnum.DRUGS_AND_SUPPLIES.equals(selectedCategory)) {
                ((AutoCompleteTextView) currentProductView).setAdapter(adapter);
            } else {
                ((Spinner) currentProductView).setAdapter(adapter);
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
            if (selectedBillable == null) {
                selectedBillable = new Billable();
                selectedBillable.setName(productSearch.getEditableText().toString());
                selectedBillable.setCategory(selectedCategory);
            }
            billableAdapter.add(selectedBillable);
            if (createEncounterButton.getVisibility() == View.GONE) {
                createEncounterButton.setVisibility(View.VISIBLE);
            }
        }
    }
}
