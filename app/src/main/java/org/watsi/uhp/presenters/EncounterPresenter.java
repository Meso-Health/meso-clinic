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

        setBackdateEncounterListener();

        if (mEncounter.getBackdatedOccurredAt()) {
            mEncounterFragment.updateBackdateLinkText();
        }
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



    public void setFormattedBackDate() {
        Date backdate = mEncounter.getOccurredAt();
        mFormattedBackDate = new SimpleDateFormat("MMM d, H:mma").format(backdate);
    }


}
