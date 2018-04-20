package org.watsi.uhp.presenters;

import android.app.SearchManager;
import android.content.Context;
import android.database.MatrixCursor;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.watsi.domain.entities.Billable;
import org.watsi.domain.entities.Encounter;
import org.watsi.domain.entities.EncounterItem;
import org.watsi.domain.repositories.BillableRepository;
import org.watsi.uhp.R;
import org.watsi.uhp.adapters.EncounterItemAdapter;
import org.watsi.uhp.fragments.BackdateEncounterDialogFragment;
import org.watsi.uhp.fragments.EncounterFragment;
import org.watsi.uhp.listeners.BillableSearchEncounterFragmentListener;
import org.watsi.uhp.listeners.BillableSelectedEncounterFragmentListener;
import org.watsi.uhp.listeners.CategorySelectedEncounterFragmentListener;
import org.watsi.uhp.listeners.SuggestionClickEncounterFragmentListener;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.LegacyNavigationManager;
import org.watsi.uhp.runnables.ScrollToBottomRunnable;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class EncounterPresenter {

    protected final Encounter mEncounter;
    private final View mView;
    private final Context mContext;
    private final EncounterItemAdapter mEncounterItemAdapter;
    private final LegacyNavigationManager mNavigationManager;
    private final EncounterFragment mEncounterFragment;
    public String mFormattedBackDate;

    private SimpleCursorAdapter billableCursorAdapter;

    private final BillableRepository billableRepository;

    public EncounterPresenter(Encounter encounter,
                              View view,
                              Context context,
                              EncounterItemAdapter encounterItemAdapter,
                              LegacyNavigationManager navigationManager,
                              EncounterFragment encounterFragment,
                              BillableRepository billableRepository) {
        mEncounter = encounter;
        mView = view;
        mContext = context;
        mEncounterItemAdapter = encounterItemAdapter;
        mNavigationManager = navigationManager;
        mEncounterFragment = encounterFragment;
        this.billableRepository = billableRepository;
    }

    public void setUp() {
        getLineItemsListView().setAdapter(mEncounterItemAdapter);

        setCategorySpinner();
        setBillableSearch();
        setAddBillableLink();
        setBackdateEncounterListener();

        if (mEncounter.getBackdatedOccurredAt()) {
            mEncounterFragment.updateBackdateLinkText();
        }
    }

    protected Spinner getCategorySpinner() {
        return (Spinner) mView.findViewById(R.id.category_spinner);
    }

    public Spinner getBillableSpinner() {
        return (Spinner) mView.findViewById(R.id.billable_spinner);
    }

    public SearchView getDrugSearchView() {
        return (SearchView) mView.findViewById(R.id.drug_search);
    }

    protected ListView getLineItemsListView() {
        return (ListView) mView.findViewById(R.id.line_items_list);
    }

    public TextView getBackdateEncounterLink() {
        return (TextView) mView.findViewById(R.id.backdate_encounter);
    }

    protected TextView getAddBillablePrompt() {
        return (TextView) mView.findViewById(R.id.add_billable_prompt);
    }

    private void setCategorySpinner() {
        String prompt = mContext.getString(R.string.prompt_category);

        getCategorySpinner().setAdapter(getCategoriesAdapter(prompt));
        getCategorySpinner().setTag("category");
        getCategorySpinner().setOnItemSelectedListener(new CategorySelectedEncounterFragmentListener(this, mContext));
    }

    public void setBillableSpinner(Billable.Type category) {
        try {
            ArrayAdapter<Billable> adapter = getEncounterItemAdapter(category);
            getBillableSpinner().setAdapter(adapter);
            getBillableSpinner().setOnItemSelectedListener(new BillableSelectedEncounterFragmentListener(this, adapter, mContext));
        } catch (SQLException e) {
            ExceptionManager.reportException(e);
            Toast.makeText(mContext, R.string.generic_error_message, Toast.LENGTH_LONG).show();
        }
    }

    private void setBillableSearch() {
        getDrugSearchView().setOnQueryTextListener(new BillableSearchEncounterFragmentListener(this));
        getDrugSearchView().setOnSuggestionListener(new SuggestionClickEncounterFragmentListener(this, mContext));
        getDrugSearchView().setQueryHint(mContext.getString(R.string.search_drug_hint));
    }

    public void setBillableCursorAdapter(String query) {
        if (query.length() < 3) {
            billableCursorAdapter = null;
        } else {
            billableCursorAdapter = createBillableCursorAdapter(query);
        }
    }

    private SimpleCursorAdapter createBillableCursorAdapter(String query) {
        String[] cursorColumns = new String[] {
                "_id",
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2,
                Billable.FIELD_NAME_ID
        };
        MatrixCursor cursor = new MatrixCursor(cursorColumns);

        for (Billable billable: billableRepository.fuzzySearchDrugsByName(query)) {
            cursor.addRow(new Object[] {
                    billable.getId().getMostSignificantBits(),
                    billable.getName(),
                    billable.dosageDetails(),
                    billable.getId().toString()
            });
        }

        return new SimpleCursorAdapter(
                mContext,
                R.layout.item_billable_search_suggestion,
                cursor,
                new String[] { SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2 },
                new int[] { R.id.text1, R.id.text2 },
                0
        );
    }

    public void updateBillableSearchSuggestions(String newText) {
        setBillableCursorAdapter(newText);
        getDrugSearchView().setSuggestionsAdapter(billableCursorAdapter);
    }

    public void updateEncounterFromOnSuggestionClick(int position) {
        MatrixCursor cursor = (MatrixCursor) billableCursorAdapter.getItem(position);
        String uuidString = cursor.getString(cursor.getColumnIndex(Billable.FIELD_NAME_ID));
        try {
            Billable billable = billableRepository.find(UUID.fromString(uuidString));
            addToEncounterItemList(billable);
            clearDrugSearch();
            scrollToBottom();
        } catch (Encounter.DuplicateBillableException e) {
            Toast.makeText(mContext, R.string.already_in_list_items, Toast.LENGTH_SHORT).show();
        }
    }

    private void setAddBillableLink() {
        getAddBillablePrompt().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mNavigationManager.setAddNewBillableFragment(mEncounter);
            }
        });
    }

    private void setBackdateEncounterListener() {
        getBackdateEncounterLink().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                BackdateEncounterDialogFragment dialog = new BackdateEncounterDialogFragment();
                dialog.setTargetFragment(mEncounterFragment, 0);
                dialog.show(mEncounterFragment.getActivity().getSupportFragmentManager(), "BackdateEncounterDialogFragment");
            }
        });
    }

    public void scrollToBottom() {
        getLineItemsListView().post(new ScrollToBottomRunnable(getLineItemsListView()));
    }

    public void clearDrugSearch() {
        getDrugSearchView().clearFocus();
        getDrugSearchView().setQuery("", false);
    }

    public void addToEncounterItemList(Billable billable) throws Encounter.DuplicateBillableException {
        EncounterItem encounterItem = new EncounterItem();
        encounterItem.setBillable(billable);

        mEncounter.addEncounterItem(encounterItem);
        mEncounterItemAdapter.add(encounterItem);
    }

    public void addToEncounterItemList(Billable billable) throws Encounter.DuplicateBillableException {
        EncounterItem encounterItem = new EncounterItem();
        encounterItem.setBillable(billable);

        mEncounter.addEncounterItem(encounterItem);
        mEncounterItemAdapter.add(encounterItem);
    }

    public void setFormattedBackDate() {
        Date backdate = mEncounter.getOccurredAt();
        mFormattedBackDate = new SimpleDateFormat("MMM d, H:mma").format(backdate);
    }

    protected Billable promptBillable(String category) {
        Billable placeholderBillable = new Billable();
        String promptText = "Select a " + category.toLowerCase() + "...";
        placeholderBillable.setName(promptText);

        return placeholderBillable;
    }

    protected List<Billable> getBillablesList(Billable.Type type) throws SQLException {
        List<Billable> billables = new ArrayList<>();
        billables.add(promptBillable(type.toString()));

        billables.addAll(billableRepository.findByType(type));
        return billables;
    }

    protected ArrayAdapter<Billable> getEncounterItemAdapter(Billable.Type category) throws SQLException {
        return new ArrayAdapter<>(
                mContext,
                android.R.layout.simple_spinner_dropdown_item,
                getBillablesList(category)
        );
    }

    protected List<String> getCategoriesList(String prompt) {
        List<String> categories = Billable.getBillableTypes();
        categories.add(0, prompt);
        return categories;
    }

    protected ArrayAdapter<String> getCategoriesAdapter(String prompt) {
        return new ArrayAdapter<>(
                mContext,
                android.R.layout.simple_spinner_dropdown_item,
                getCategoriesList(prompt)
        );
    }
}
