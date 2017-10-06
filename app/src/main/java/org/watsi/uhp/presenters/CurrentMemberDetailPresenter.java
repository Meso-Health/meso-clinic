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
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.models.SyncableModel;

import java.sql.SQLException;

public class CurrentMemberDetailPresenter extends MemberDetailPresenter {

    public CurrentMemberDetailPresenter(NavigationManager navigationManager, View view, Context context, Member member) {
        super(view, context, member, navigationManager);
    }

    @Override
    public void additionalSetup() {
        // no-op
    }

    @Override
    protected void setMemberActionButton() {
        Button confirmButton = getMemberActionButton();

        confirmButton.setText(R.string.detail_create_encounter);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Encounter encounter = createUnsavedEncounterWithDefaultItems();
                    getNavigationManager().setEncounterFragment(encounter);
                } catch (SQLException e) {
                    ExceptionManager.reportException(e);
                } catch (IllegalStateException e) {
                    showGenericFailedToast();
                    ExceptionManager.reportException(e);
                    getNavigationManager().setCurrentPatientsFragment();
                }
            }
        });
    }

    @Override
    protected void navigateToCompleteEnrollmentFragment() {
        getNavigationManager().startCompleteEnrollmentFlow(getMember(), null);
    }

    public void navigateToMemberEditFragment() {
        getNavigationManager().setMemberEditFragment(getMember(), null);
    }

    private Encounter createUnsavedEncounterWithDefaultItems() throws SQLException {
        Encounter encounter = new Encounter();
        IdentificationEvent checkIn = getMember().currentCheckIn();
        if (checkIn != null) {
            encounter.setCopaymentPaid(true);
            encounter.setOccurredAt(Clock.getCurrentTime());
            encounter.setMember(getMember());
            encounter.setIdentificationEvent(checkIn);
            encounter.setEncounterItems(
                    EncounterItemDao.getDefaultEncounterItems(checkIn.getClinicNumberType()));
            return encounter;
        } else {
            throw new IllegalStateException("Current member does not have a current IdentificationEvent. Member id is: " + getMember().getId());
        }
    }

    private void dismissIdentification(IdentificationEvent.DismissalReasonEnum dismissReason)
            throws SyncableModel.UnauthenticatedException, SQLException {
        IdentificationEvent checkIn = getMember().currentCheckIn();
        checkIn.setDismissalReason(dismissReason);

        try {
            checkIn.saveChanges(((ClinicActivity) getContext()).getAuthenticationToken());
            getNavigationManager().setCurrentPatientsFragment();
            showCheckedOutSuccessfulToast();
        } catch (SQLException | AbstractModel.ValidationException e) {
            ExceptionManager.reportException(e);
            showFailedToCheckOutToast();
        }
    }

    private void showGenericFailedToast() {
        Toast.makeText(getContext(),
                getContext().getString(R.string.generic_enter_treatment_info_failure),
                Toast.LENGTH_LONG).
                show();
    }

    private void showFailedToCheckOutToast() {
        Toast.makeText(getContext(),
                getContext().getString(R.string.identification_dismissed_failure),
                Toast.LENGTH_LONG).
                show();
    }

    private void showCheckedOutSuccessfulToast() {
        Toast.makeText(getContext(),
                getMember().getFullName() + " " + getContext().getString(R.string.identification_dismissed),
                Toast.LENGTH_LONG).
                show();
    }

    public void dismissMember() {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.dismiss_member_alert)
                .setNegativeButton(R.string.cancel, null)
                .setItems(IdentificationEvent.getFormattedDismissalReasons(), new
                        DialogInterface
                                .OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    dismissIdentification(IdentificationEvent
                                            .DismissalReasonEnum.values()[which]);
                                } catch (SQLException | SyncableModel.UnauthenticatedException e) {
                                    ExceptionManager.reportException(e);
                                    Toast.makeText(getContext(),
                                            "Failed to dismiss member, contact support.",
                                            Toast.LENGTH_LONG).
                                            show();
                                }
                            }
                        }).create().show();
    }
}
