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
import java.util.List;


public class EncounterFragment extends Fragment {

    private Billable.CategoryEnum selectedCategory = null;
    private Spinner servicesSpinner;
    private Spinner labsSpinner;
    private Spinner suppliesSpinner;
    private Spinner vaccinesSpinner;
    private SearchView drugSearch;
    private View currentProductView;
    private BillableAdapter billableAdapter;
    private List<Billable> billables;
    private Button createEncounterButton;
    private Encounter.IdMethodEnum idMethod;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        idMethod = Encounter.IdMethodEnum.valueOf(getArguments().getString("idMethod"));

        final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_encounter, container, false);

        Spinner categorySpinner = (Spinner) view.findViewById(R.id.category_spinner);
        ArrayList<Object> categories = new ArrayList<>();
        categories.add(getContext().getString(R.string.prompt_category));
        for (Billable.CategoryEnum c : Billable.CategoryEnum.values()) {
            categories.add(c);
        }
        final ArrayAdapter categoryAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                categories
        );

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

    private SimpleCursorAdapter getProductAdapter(Billable.CategoryEnum category) {
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

    public void addSearchSuggestionToBillableList (String billableId) {
        try {
            Billable billable = BillableDao.findById(billableId);
            billableAdapter.add(billable);
        } catch (SQLException e) {
            Rollbar.reportException(e);
        }
    }

    private class CategoryListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            drugSearch.setVisibility(View.GONE);
            servicesSpinner.setVisibility(View.GONE);
            labsSpinner.setVisibility(View.GONE);
            suppliesSpinner.setVisibility(View.GONE);
            vaccinesSpinner.setVisibility(View.GONE);

            if (position != 0) {
                selectedCategory = (Billable.CategoryEnum) parent.getItemAtPosition(position);
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
                    SimpleCursorAdapter adapter = getProductAdapter(selectedCategory);

                    Spinner currentSpinner = (Spinner) currentProductView;
                    currentSpinner.setAdapter(adapter);
                    currentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            try {
                                if (position != 0) {
                                    Billable billable = BillableDao.findById(Long.toString(id));
                                    billableAdapter.add(billable);
                                }
                            } catch (SQLException e) {
                                Rollbar.reportException(e);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            //no-op
                        }
                    });
                }
                currentProductView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // no-op
        }
    }
}
