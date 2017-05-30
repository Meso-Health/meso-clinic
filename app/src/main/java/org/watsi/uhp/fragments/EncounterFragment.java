package org.watsi.uhp.fragments;

import android.app.SearchManager;
import android.database.MatrixCursor;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.adapters.EncounterItemAdapter;
import org.watsi.uhp.database.BillableDao;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.KeyboardManager;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.presenters.EncounterPresenter;
import org.watsi.uhp.runnables.ScrollToBottomRunnable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class EncounterFragment extends FormFragment<Encounter> {

    private SimpleCursorAdapter billableCursorAdapter;
    private EncounterItemAdapter encounterItemAdapter;
    private EncounterPresenter encounterPresenter;

    @Override
    int getTitleLabelId() {
        return R.string.encounter_fragment_label;
    }

    @Override
    int getFragmentLayoutId() {
        return R.layout.fragment_encounter;
    }

    @Override
    public boolean isFirstStep() {
        return true;
    }

    @Override
    void nextStep(View view) {
        getNavigationManager().setEncounterFormFragment(mSyncableModel);
    }

    @Override
    void setUpFragment(View view) {
        encounterItemAdapter = new EncounterItemAdapter(getContext(), new ArrayList<>(mSyncableModel.getEncounterItems()));
        encounterPresenter = new EncounterPresenter(mSyncableModel, view, getContext(), encounterItemAdapter);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        encounterPresenter.setUpEncounterPresenter(view, getContext());

        setAddBillableLink(view);
        setBackdateEncounterListener();
    }

    protected Encounter getEncounter() {
        return mSyncableModel;
    }

    private void setAddBillableLink(View view) {
        view.findViewById(R.id.add_billable_prompt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationManager().setAddNewBillableFragment(mSyncableModel);
            }
        });
    }

    private void setBackdateEncounterListener() {
        final Fragment fragment = this;
        encounterPresenter.getBackdateEncounterLink().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackdateEncounterDialogFragment dialog = new BackdateEncounterDialogFragment();
                dialog.setTargetFragment(fragment, 0);
                dialog.show(getActivity().getSupportFragmentManager(), "BackdateEncounterDialogFragment");
            }
        });
    }

    public void updateBackdateLinkText() {
        SpannableString newText = new SpannableString(encounterPresenter.newDateLinkText(mSyncableModel));
        newText.setSpan(new UnderlineSpan(), 0, newText.length(), 0);
        encounterPresenter.getBackdateEncounterLink().setText(newText);
    }
}
