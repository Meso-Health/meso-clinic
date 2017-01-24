package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;

import com.rollbar.android.Rollbar;

import org.watsi.uhp.R;
import org.watsi.uhp.database.BillableDao;
import org.watsi.uhp.models.Billable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EncounterFragment extends Fragment {

    private Billable.DepartmentEnum selectedDepartment = null;
    private Billable.CategoryEnum selectedCategory = null;
    private Map<String,List<Billable>> filteredBillableMap = new HashMap<>();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_encounter, container, false);

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

        final AutoCompleteTextView productSearch = (AutoCompleteTextView) view.findViewById(R.id.encounter_product_search);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getTag().equals("category")) {
                    selectedCategory = (Billable.CategoryEnum) parent.getItemAtPosition(position);
                } else {
                    selectedDepartment = (Billable.DepartmentEnum) parent.getItemAtPosition(position);
                }

                if (selectedCategory != null && selectedDepartment != null) {
                    filteredBillableMap.clear();;
                    try {
                        List<Billable> filteredBillables =
                                BillableDao.findByDepartmentAndCategory(selectedDepartment, selectedCategory);

                        for (Billable billable : filteredBillables) {
                            if (filteredBillableMap.containsKey(billable.getDisplayName())) {
                                filteredBillableMap.get(billable.getDisplayName()).add(billable);
                            } else {
                                List<Billable> list = new ArrayList<>();
                                list.add(billable);
                                filteredBillableMap.put(billable.getDisplayName(), list);
                            }
                        }

                        Log.d("UHP", "billables loaded: " + filteredBillables.size());

                        Set<String> filteredBillableDisplayStrings = filteredBillableMap.keySet();
                        ArrayAdapter filteredBillablesAdapter = new ArrayAdapter<String>(
                                getContext(),
                                android.R.layout.simple_spinner_item,
                                filteredBillableDisplayStrings.toArray(new String[filteredBillableDisplayStrings.size()])
                        );

                        productSearch.setAdapter(filteredBillablesAdapter);
                    } catch (SQLException e) {
                        Rollbar.reportException(e);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // no-op
            }
        };
        departmentSpinner.setOnItemSelectedListener(listener);
        categorySpinner.setOnItemSelectedListener(listener);

        return view;
    }
}
