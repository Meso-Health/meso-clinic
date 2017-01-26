package org.watsi.uhp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.rollbar.android.Rollbar;

import org.watsi.uhp.R;

import org.watsi.uhp.database.BillableDao;
import org.watsi.uhp.models.Billable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;


public class EncounterFragment extends Fragment {

    private Billable.DepartmentEnum selectedDepartment = null;
    private Billable.CategoryEnum selectedCategory = null;
    private Spinner servicesSpinner;
    private Spinner labsSpinner;
    private AutoCompleteTextView productSearch;
    private View currentProductView;
    private final Map<String, List<Billable>> filteredBillablesMap = new HashMap<>();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String idMethod = getArguments().getString("idMethod");
        Log.d("UHP", "intention memberId: " + idMethod);
        final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_encounter, container, false);

        Spinner departmentSpinner = (Spinner) view.findViewById(R.id.department_spinner);
        final ArrayAdapter departmentAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_item,
                Billable.DepartmentEnum.values()
        );
        departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        departmentSpinner.setAdapter(departmentAdapter);
        departmentSpinner.setTag("department");

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

        CategoryDepartmentListener listener = new CategoryDepartmentListener();
        departmentSpinner.setOnItemSelectedListener(listener);
        categorySpinner.setOnItemSelectedListener(listener);

        final Button selectBillableButton = (Button) view.findViewById(R.id.enter_billable);
        selectBillableButton.setOnClickListener(new CreateBillableListener(view));

        return view;
    }

    private Map<String,List<Billable>> getFilteredBillableMap(Billable.DepartmentEnum department, Billable.CategoryEnum category) {
        filteredBillablesMap.clear();
        try {
            List<Billable> filteredBillables =
                    BillableDao.findByDepartmentAndCategory(department, category);
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
        Log.d("UHP", "billables loaded: " + filteredBillablesMap.size());
        return filteredBillablesMap;
    }

    private ArrayAdapter getProductAdapter(Map<String, List<Billable>> billableMap) {
        SortedSet<String> filteredBillableDisplayStrings = new TreeSet<String>(billableMap.keySet());
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
            // TODO: find billable with matching service
            return matches.get(0);
        }
    }

    private class CategoryDepartmentListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (parent.getTag().equals("category")) {
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
            } else {
                selectedDepartment = (Billable.DepartmentEnum) parent.getItemAtPosition(position);
            }

            if (selectedCategory != null && selectedDepartment != null) {
                Map<String, List<Billable>> filteredBillableMap = getFilteredBillableMap(selectedDepartment, selectedCategory);
                ArrayAdapter adapter = getProductAdapter(filteredBillableMap);
                // AutoCompleteTextView & Spinner are not descendants of same adapter interface, so forced to cast
                if (Billable.CategoryEnum.DRUGS_AND_SUPPLIES.equals(selectedCategory)) {
                    ((AutoCompleteTextView) currentProductView).setAdapter(adapter);
                } else {
                    ((Spinner) currentProductView).setAdapter(adapter);
                }
                currentProductView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // no-op
        }
    }

    private class CreateBillableListener implements View.OnClickListener {

        private LinearLayout parentLayout;

        private CreateBillableListener(LinearLayout view) {
            parentLayout = view;
        }

        @Override
        public void onClick(View v) {
            Billable selectedBillable = getSelectedBillable(currentProductView);
            if (selectedBillable == null) {
                selectedBillable = new Billable();
                selectedBillable.setName(productSearch.getEditableText().toString());
                selectedBillable.setDepartment(selectedDepartment);
                selectedBillable.setCategory(selectedCategory);
            }
            TextView newBillableView = new TextView(getContext());
            newBillableView.setText(selectedBillable.getDisplayName());
            parentLayout.addView(newBillableView);
        }
    }
}
