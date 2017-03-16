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

import org.watsi.uhp.R;
import org.watsi.uhp.activities.MainActivity;

import java.util.Calendar;

public class BackdateEncounterDialogFragment extends DialogFragment {

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_backdate_encounter, null);

        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);
        datePicker.setMaxDate(Calendar.getInstance().getTimeInMillis());

        final TimePicker timePicker = (TimePicker) view.findViewById(R.id.time_picker);

        view.findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                        timePicker.getCurrentHour(), timePicker.getCurrentMinute());
                ((MainActivity) getActivity()).getCurrentEncounter().setOccurredAt(cal.getTime());
                ((MainActivity) getActivity()).getCurrentEncounter().setBackdatedOccurredAt(true);
                dismissAllowingStateLoss();
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
