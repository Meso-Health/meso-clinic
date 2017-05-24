package org.watsi.uhp.presenters;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import org.watsi.uhp.R;
import org.watsi.uhp.adapters.EncounterItemAdapter;
import org.watsi.uhp.database.BillableDao;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterItem;

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

    public EncounterPresenter(Encounter encounter, View view, Context context, EncounterItemAdapter encounterItemAdapter) {
        mEncounter = encounter;
        mView = view;
        mContext = context;
        mEncounterItemAdapter = encounterItemAdapter;
    }

    public Spinner getCategorySpinner() {
        return (Spinner) mView.findViewById(R.id.category_spinner);
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
