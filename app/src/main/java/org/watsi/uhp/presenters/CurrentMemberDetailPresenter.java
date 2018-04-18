package org.watsi.uhp.presenters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterItem;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.models.SyncableModel;
import org.watsi.uhp.repositories.BillableRepository;
import org.watsi.uhp.repositories.IdentificationEventRepository;
import org.watsi.uhp.repositories.MemberRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CurrentMemberDetailPresenter extends MemberDetailPresenter {

    private final BillableRepository billableRepository;

    public CurrentMemberDetailPresenter(NavigationManager navigationManager,
                                        View view,
                                        Context context,
                                        Member member,
                                        IdentificationEventRepository identificationEventRepository,
                                        BillableRepository billableRepository,
                                        MemberRepository memberRepository) {
        super(view, context, member, navigationManager, memberRepository, identificationEventRepository);
        this.billableRepository = billableRepository;
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
                    getNavigationManager().setAdditionalInformationFragment(encounter);
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
        IdentificationEvent checkIn = identificationEventRepository.openCheckIn(getMember().getId());
        if (checkIn != null) {
            encounter.setCopaymentPaid(true);
            encounter.setOccurredAt(Clock.getCurrentTime());
            encounter.setMember(getMember());
            encounter.setIdentificationEvent(checkIn);
            encounter.setEncounterItems(getDefaultEncounterItems(checkIn.getClinicNumberType()));
            return encounter;
        } else {
            throw new IllegalStateException("Current member does not have a current IdentificationEvent. Member id is: " + getMember().getId());
        }
    }

    private void dismissIdentification(IdentificationEvent.DismissalReasonEnum dismissReason)
            throws SyncableModel.UnauthenticatedException, SQLException {
        IdentificationEvent checkIn = identificationEventRepository.openCheckIn(getMember().getId());
        checkIn.setDismissalReason(dismissReason);

        identificationEventRepository.update(checkIn);
        getNavigationManager().setCurrentPatientsFragment();
        showCheckedOutSuccessfulToast();
    }

    private void showGenericFailedToast() {
        Toast.makeText(getContext(),
                getContext().getString(R.string.generic_enter_treatment_info_failure),
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

    private List<EncounterItem> getDefaultEncounterItems(IdentificationEvent.ClinicNumberTypeEnum type) {
        List<EncounterItem> defaultLineItems = new ArrayList<>();

        if (type == IdentificationEvent.ClinicNumberTypeEnum.OPD) {
            defaultLineItems.add(new EncounterItem(billableRepository.findByName("Consultation"), 1));
            defaultLineItems.add(new EncounterItem(billableRepository.findByName("Medical Form"), 1));
        }
        return defaultLineItems;
    }
}
