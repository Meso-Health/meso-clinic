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

import org.watsi.uhp.R;
import org.watsi.uhp.adapters.EncounterItemAdapter;
import org.watsi.uhp.database.BillableDao;
import org.watsi.uhp.listeners.BillableSearchEncounterFragmentListener;
import org.watsi.uhp.listeners.BillableSelectedEncounterFragmentListener;
import org.watsi.uhp.listeners.CategorySelectedEncounterFragmentListener;
import org.watsi.uhp.listeners.SuggestionClickEncounterFragmentListener;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterItem;
import org.watsi.uhp.runnables.ScrollToBottomRunnable;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EncounterPresenter {

    protected final Encounter mEncounter;
    protected final View mView;
    protected final Context mContext;
    protected final EncounterItemAdapter mEncounterItemAdapter;
    public SimpleCursorAdapter billableCursorAdapter;

    public EncounterPresenter(Encounter encounter, View view, Context context, EncounterItemAdapter encounterItemAdapter) {
        mEncounter = encounter;
        mView = view;
        mContext = context;
        mEncounterItemAdapter = encounterItemAdapter;
    }

    public Spinner getCategorySpinner() {
        return (Spinner) mView.findViewById(R.id.category_spinner);
    }

    public Spinner getBillableSpinner() {
        return (Spinner) mView.findViewById(R.id.billable_spinner);
    }

    public SearchView getDrugSearchView() {
        return (SearchView) mView.findViewById(R.id.drug_search);
    }

    public ListView getLineItemsListView() {
        return (ListView) mView.findViewById(R.id.line_items_list);
    }

    public TextView getBackdateEncounterLink() {
        return (TextView) mView.findViewById(R.id.backdate_encounter);
    }

    /////////////////////////////NOT TESTED///////////////////////////////////////
    public void setUpEncounterPresenter(View view, Context context) {
        getLineItemsListView().setAdapter(mEncounterItemAdapter);

        setCategorySpinner(context);
        setBillableSearch();
    }

    /////////////////////////////NOT TESTED///////////////////////////////////////
    public void setCategorySpinner(Context context) {
        String prompt = context.getString(R.string.prompt_category);

        getCategorySpinner().setAdapter(getCategoriesAdapter(prompt));
        getCategorySpinner().setTag("category");
        getCategorySpinner().setOnItemSelectedListener(new CategorySelectedEncounterFragmentListener(this, context));
    }

    /////////////////////////////NOT TESTED///////////////////////////////////////
    public void setBillableSpinner(Billable.TypeEnum category) {
        ArrayAdapter<Billable> adapter = getEncounterItemAdapter(category);

        getBillableSpinner().setAdapter(adapter);
        getBillableSpinner().setOnItemSelectedListener(new BillableSelectedEncounterFragmentListener(this, adapter, mContext));
    }

    /////////////////////////////NOT TESTED///////////////////////////////////////
    public void scrollToBottom() {
        getLineItemsListView().post(new ScrollToBottomRunnable(getLineItemsListView()));
    }

    /////////////////////////////NOT TESTED///////////////////////////////////////
    public void setBillableSearch() {
        getDrugSearchView().setOnQueryTextListener(new BillableSearchEncounterFragmentListener(this));
        getDrugSearchView().setOnSuggestionListener(new SuggestionClickEncounterFragmentListener(this, mContext));
        getDrugSearchView().setQueryHint(mContext.getString(R.string.search_drug_hint));
    }

    /////////////////////////////NOT TESTED///////////////////////////////////////
    public SimpleCursorAdapter getBillableCursorAdapter(String query) {
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
                    mContext,
                    R.layout.item_billable_search_suggestion,
                    cursor,
                    new String[] { SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2 },
                    new int[] { R.id.text1, R.id.text2 },
                    0
            );
        }
    }

    /////////////////////////////NOT TESTED///////////////////////////////////////
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

    public String newDateLinkText(Encounter encounter) {
        Date backdate = encounter.getOccurredAt();
        String newBackdateText = dateFormatter(backdate);

        return "Date: " + newBackdateText;
    }

    public String dateFormatter(Date date) {
        return new SimpleDateFormat("MMM d, H:mma").format(date);
    }

    public Billable promptBillable(String category) {
        Billable placeholderBillable = new Billable();
        String promptText = "Select a " + category.toLowerCase() + "...";
        placeholderBillable.setName(promptText);

        return placeholderBillable;
    }

    public List<Billable> getBillablesList(String category) {
        List<Billable> billables = new ArrayList<>();
        billables.add(promptBillable(category));

        try {
            billables.addAll(BillableDao.getBillablesByCategory(Billable.TypeEnum.valueOf(category)));
        } catch (SQLException e) {
            ExceptionManager.reportException(e);
        }

        return billables;
    }

    public ArrayAdapter<Billable> getEncounterItemAdapter(Billable.TypeEnum category) {
        // TODO: check that creation of new adapter each time does not have memory implications
        return new ArrayAdapter<>(
                mContext,
                android.R.layout.simple_spinner_dropdown_item,
                getBillablesList(category.toString())
        );
    }

    public List<String> getCategoriesList(String prompt) {
        List<String> categories = new ArrayList<>();
        categories.add(prompt);
        for (Billable.TypeEnum billableType : Billable.TypeEnum.values()) {
            if (!billableType.equals(Billable.TypeEnum.UNSPECIFIED)) {
                categories.add(billableType.toString());
            }
        }

        return categories;
    }

    public ArrayAdapter<String> getCategoriesAdapter(String prompt) {
        return new ArrayAdapter<>(
                mContext,
                android.R.layout.simple_spinner_dropdown_item,
                getCategoriesList(prompt)
        );
    }
}
