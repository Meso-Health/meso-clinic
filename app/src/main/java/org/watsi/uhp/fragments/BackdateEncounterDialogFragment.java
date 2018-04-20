package org.watsi.uhp.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import org.watsi.domain.entities.Encounter;
import org.watsi.uhp.R;

import java.util.Calendar;

public class BackdateEncounterDialogFragment extends DialogFragment {

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_backdate_encounter, null);

        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        datePicker.setMaxDate(yesterday.getTimeInMillis());

        final TimePicker timePicker = (TimePicker) view.findViewById(R.id.time_picker);

        final EncounterFragment fragment = (EncounterFragment) getTargetFragment();
        final Encounter encounter = fragment.getEncounter();

        if (encounter.getBackdatedOccurredAt()) {
            Calendar backdate = Calendar.getInstance();
            backdate.setTime(encounter.getOccurredAt());
            datePicker.updateDate(
                    backdate.get(Calendar.YEAR),
                    backdate.get(Calendar.MONTH),
                    backdate.get(Calendar.DAY_OF_MONTH)
            );
            timePicker.setCurrentHour(backdate.get(Calendar.HOUR));
            timePicker.setCurrentMinute(backdate.get(Calendar.MINUTE));
        }

        view.findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                        timePicker.getCurrentHour(), timePicker.getCurrentMinute());
                encounter.setOccurredAt(cal.getTime());
                encounter.setBackdatedOccurredAt(true);
                fragment.updateBackdateLinkText();
                dismiss();
            }
        });

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissAllowingStateLoss();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(view);

        return builder.create();
    }
}
