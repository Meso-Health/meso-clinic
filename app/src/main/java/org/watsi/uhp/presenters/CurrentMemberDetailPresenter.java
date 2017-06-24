package org.watsi.uhp.presenters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.database.EncounterItemDao;
import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.models.SyncableModel;

import java.sql.SQLException;

/**
 * Created by michaelliang on 6/13/17.
 */

public class CurrentMemberDetailPresenter extends MemberDetailPresenter {

    public CurrentMemberDetailPresenter(NavigationManager navigationManager, View view, Context context, Member member) {
        super(view, context, member, navigationManager);
    }

    protected void setMemberActionButton() {
        Button confirmButton = getMemberActionButton();

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

    protected void setMemberActionLink() {
        getMemberActionLink().setVisibility(View.VISIBLE);
        getMemberActionLink().setText(R.string.dismiss_patient);
        getMemberActionLink().setOnClickListener(new View.OnClickListener() {
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
            checkIn.saveChanges(((ClinicActivity) getContext()).getAuthenticationToken());
            getNavigationManager().setCurrentPatientsFragment();
            showCheckedOutSuccessfulToast();
        } catch (SQLException e) {
            ExceptionManager.reportException(e);
            showFailedToCheckOutToast();
        }
    }

    protected void showFailedToCheckOutToast() {
        Toast.makeText(getContext(),
                getContext().getString(R.string.identification_dismissed_failure),
                Toast.LENGTH_LONG).
                show();
    }

    protected void showCheckedOutSuccessfulToast() {
        Toast.makeText(getContext(),
                getMember().getFullName() + " " + getContext().getString(R.string.identification_dismissed),
                Toast.LENGTH_LONG).
                show();
    }
}
