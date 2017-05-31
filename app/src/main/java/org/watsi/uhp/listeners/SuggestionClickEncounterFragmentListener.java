package org.watsi.uhp.listeners;

import android.content.Context;
import android.database.MatrixCursor;
import android.widget.SearchView;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.database.BillableDao;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.presenters.EncounterPresenter;

import java.sql.SQLException;
import java.util.UUID;

public class SuggestionClickEncounterFragmentListener implements SearchView.OnSuggestionListener {

    private final EncounterPresenter encounterPresenter;
    private final Context context;


    public SuggestionClickEncounterFragmentListener(EncounterPresenter encounterPresenter, Context context) {
        this.encounterPresenter = encounterPresenter;
        this.context = context;
    }

    @Override
    public boolean onSuggestionSelect(int position) {
        // no-op
        return true;
    }

    @Override
    public boolean onSuggestionClick(int position) {
        MatrixCursor cursor = (MatrixCursor) encounterPresenter.billableCursorAdapter.getItem(position);
        String uuidString = cursor.getString(cursor.getColumnIndex(Billable.FIELD_NAME_ID));
        try {
            Billable billable = BillableDao.findById(UUID.fromString(uuidString));
            encounterPresenter.addToEncounterItemList(billable);
            encounterPresenter.clearDrugSearch();
        } catch (Encounter.DuplicateBillableException e) {
            // TODO: make toast message more descriptive
            Toast.makeText(context, R.string.already_in_list_items, Toast.LENGTH_SHORT).show();
        } catch (SQLException e) {
            Toast.makeText(context, "Call Katrina", Toast.LENGTH_SHORT).show();
        }

        return true;
    }
}
