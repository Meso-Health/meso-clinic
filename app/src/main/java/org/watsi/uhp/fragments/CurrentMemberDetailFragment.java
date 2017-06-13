package org.watsi.uhp.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.database.EncounterItemDao;
import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.SyncableModel;

import java.sql.SQLException;

/**
 * Created by michaelliang on 6/12/17.
 */

public class CurrentMemberDetailFragment extends MemberDetailFragment {

    @Override
    protected void setUpFragment(View view) {
        // no-op
    }

    @Override
    protected void setMemberActionButton(View view) {
        Button confirmButton = (Button) view.findViewById(R.id.member_action_button);

        confirmButton.setText(R.string.detail_create_encounter);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Encounter encounter = new Encounter();
                IdentificationEvent checkIn = getMember().currentCheckIn();
                encounter.setOccurredAt(Clock.getCurrentTime());
                encounter.setMember(getMember());
                encounter.setIdentificationEvent(checkIn);
                try {
                    encounter.setEncounterItems(
                            EncounterItemDao.getDefaultEncounterItems(checkIn.getClinicNumberType()));
                } catch (SQLException e) {
                    ExceptionManager.reportException(e);
                }
                getNavigationManager().setEncounterFragment(encounter);
            }
        });
    }

    @Override
    protected void setBottomListView(View view) {

    }

    @Override
    protected void setMemberActionLink(View view) {
        setDismissPatientLink(view);
    }

    private void setDismissPatientLink(View view) {
        memberDetailPresenter.getMemberActionLink().setVisibility(View.VISIBLE);
        memberDetailPresenter.getMemberActionLink().setText(R.string.dismiss_patient);

        memberDetailPresenter.getMemberActionLink().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.dismiss_patient_alert)
                        .setNegativeButton(R.string.cancel, null)
                        .setItems(IdentificationEvent.getFormattedDismissalReasons(), new
                                DialogInterface
                                        .OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            dismissIdentification(IdentificationEvent
                                                    .DismissalReasonEnum.values()[which]);
                                        } catch (SyncableModel.UnauthenticatedException e) {
                                            ExceptionManager.reportException(e);
                                            Toast.makeText(getContext(),
                                                    "Failed to dismiss member, contact support.",
                                                    Toast.LENGTH_LONG).
                                                    show();
                                        }
                                    }
                                }).create().show();
            }
        });
    }

    public void dismissIdentification(IdentificationEvent.DismissalReasonEnum dismissReason)
            throws SyncableModel.UnauthenticatedException {
        IdentificationEvent checkIn = getMember().currentCheckIn();
        checkIn.setDismissalReason(dismissReason);

        try {
            checkIn.saveChanges(getAuthenticationToken());
            getNavigationManager().setCurrentPatientsFragment();
            Toast.makeText(getContext(),
                    getMember().getFullName() + " " + getString(R.string.identification_dismissed),
                    Toast.LENGTH_LONG).
                    show();
        } catch (SQLException e) {
            ExceptionManager.reportException(e);
            Toast.makeText(getContext(),
                    getString(R.string.identification_dismissed_failure),
                    Toast.LENGTH_LONG).
                    show();
        }
    }
}
