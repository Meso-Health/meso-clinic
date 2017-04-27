package org.watsi.uhp.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.adapters.MemberAdapter;
import org.watsi.uhp.database.EncounterItemDao;
import org.watsi.uhp.database.IdentificationEventDao;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.models.SyncableModel;

import java.sql.SQLException;
import java.util.List;

public class DetailFragment extends BaseFragment {

    private Member mMember;
    private IdentificationEvent.SearchMethodEnum mIdMethod = null;
    private Member mThroughMember = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(R.string.detail_fragment_label);
        setHasOptionsMenu(true);
        getActivity().invalidateOptionsMenu();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        String searchMethodString = getArguments().getString(NavigationManager.ID_METHOD_BUNDLE_FIELD);
        if (searchMethodString != null) {
            mIdMethod = IdentificationEvent.SearchMethodEnum.valueOf(searchMethodString);
        }
        mMember = (Member) getArguments().getSerializable(NavigationManager.MEMBER_BUNDLE_FIELD);

        mThroughMember = (Member) getArguments()
                .getSerializable(NavigationManager.THROUGH_MEMBER_BUNDLE_FIELD);

        setPatientCard(view);
        setButton(view);
        setHouseholdList(view);
        if (mMember.currentCheckIn() == null) {
            setRejectIdentityLink(view);
        } else {
            setDismissPatientLink(view);
        }

        return view;
    }

    private void setRejectIdentityLink(View view) {
        view.findViewById(R.id.reject_identity).setVisibility(View.VISIBLE);
        view.findViewById(R.id.reject_identity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.reject_identity_alert)
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                try {
                                    completeIdentification(false, null, null);
                                } catch (SyncableModel.UnauthenticatedException e) {
                                    ExceptionManager.reportException(e);
                                    Toast.makeText(getContext(),
                                            "Failed to save identification, contact support.",
                                            Toast.LENGTH_LONG).
                                            show();
                                }
                            }
                        }).create().show();
            }
        });
    }

    private void setDismissPatientLink(View view) {
        view.findViewById(R.id.dismiss_patient).setVisibility(View.VISIBLE);
        view.findViewById(R.id.dismiss_patient).setOnClickListener(new View.OnClickListener() {
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

    private void setPatientCard(View detailView) {
        ((TextView) detailView.findViewById(R.id.member_name)).setText(mMember.getFullName());
        ((TextView) detailView.findViewById(R.id.member_age_and_gender))
                .setText(mMember.getFormattedAgeAndGender());
        ((TextView) detailView.findViewById(R.id.member_card_id)).setText(mMember.getFormattedCardId());
        ((TextView) detailView.findViewById(R.id.member_phone_number)).setText(mMember.getFormattedPhoneNumber());

        Bitmap photoBitmap = mMember.getPhotoBitmap(getContext().getContentResolver());
        ImageView memberPhoto = (ImageView) detailView.findViewById(R.id.member_photo);
        if (photoBitmap != null) {
            memberPhoto.setImageBitmap(photoBitmap);
        } else {
            memberPhoto.setImageResource(R.drawable.portrait_placeholder);
        }

        TextView memberNotification = (TextView) detailView.findViewById(R.id.member_notification);
        if (mMember.getAbsentee()) {
            memberNotification.setVisibility(View.VISIBLE);
            memberNotification.setText(R.string.absentee_notification);
        } else if (mMember.getCardId() == null) {
            memberNotification.setVisibility(View.VISIBLE);
            memberNotification.setText(R.string.replace_card_notification);
        }
    }

    private void setButton(View view) {
        Button confirmButton = (Button) view.findViewById(R.id.approve_identity);
        if (mMember.currentCheckIn() == null) {
            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openClinicNumberDialog();
                }
            });
        } else {
            confirmButton.setText(R.string.detail_create_encounter);
            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Encounter encounter = new Encounter();
                    IdentificationEvent checkIn = mMember.currentCheckIn();
                    encounter.setOccurredAt(Clock.getCurrentTime());
                    encounter.setMember(mMember);
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
    }

    private void openClinicNumberDialog() {
        DialogFragment clinicNumberDialog = new ClinicNumberDialogFragment();
        clinicNumberDialog.show(getActivity().getSupportFragmentManager(),
                "ClinicNumberDialogFragment");
        clinicNumberDialog.setTargetFragment(this, 0);
    }

    private void setHouseholdList(View detailView) {
        TextView householdListLabel = (TextView) detailView.findViewById(R.id.household_members_label);
        ListView householdListView = (ListView) detailView.findViewById(R.id.household_members);

        try {
            List<Member> householdMembers = MemberDao.getRemainingHouseholdMembers(
                    mMember.getHouseholdId(), mMember.getId());
            ListAdapter adapter = new MemberAdapter(getContext(), householdMembers, false);
            int householdSize = householdMembers.size() + 1;

            householdListLabel.setText(getResources().getQuantityString(
                    R.plurals.household_label, householdSize, householdSize));
            householdListView.setAdapter(adapter);
            householdListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Member member = (Member) parent.getItemAtPosition(position);
                    getNavigationManager().setDetailFragment(
                            member,
                            IdentificationEvent.SearchMethodEnum.THROUGH_HOUSEHOLD,
                            mMember
                    );
                }
            });
        } catch (SQLException e) {
            ExceptionManager.reportException(e);
        }
    }

    public void completeIdentification(boolean accepted,
                                       IdentificationEvent.ClinicNumberTypeEnum clinicNumberType,
                                       Integer clinicNumber) throws SyncableModel.UnauthenticatedException {
        IdentificationEvent idEvent = new IdentificationEvent();
        idEvent.setIsNew(true);
        idEvent.setUnsynced(getSessionManager().getToken());
        idEvent.setMember(mMember);
        idEvent.setSearchMethod(mIdMethod);
        idEvent.setThroughMember(mThroughMember);
        idEvent.setClinicNumberType(clinicNumberType);
        idEvent.setClinicNumber(clinicNumber);
        idEvent.setAccepted(accepted);
        idEvent.setOccurredAt(Clock.getCurrentTime());
        if (mMember.getPhoto() == null) {
            idEvent.setPhotoVerified(false);
        }
        try {
            IdentificationEventDao.create(idEvent);
        } catch (SQLException e) {
            ExceptionManager.reportException(e);
        }

        getNavigationManager().setCurrentPatientsFragment();
        int messageStringId = accepted ? R.string.identification_approved : R.string.identification_rejected;
        Toast.makeText(getContext(),
                mMember.getFullName() + " " + getString(messageStringId),
                Toast.LENGTH_LONG).
                show();
    }

    public void dismissIdentification(IdentificationEvent.DismissalReasonEnum dismissReason)
            throws SyncableModel.UnauthenticatedException {
        IdentificationEvent checkIn = mMember.currentCheckIn();
        checkIn.setDismissalReason(dismissReason);
        checkIn.setUnsynced(getSessionManager().getToken());

        try {
            IdentificationEventDao.update(checkIn);
            getNavigationManager().setCurrentPatientsFragment();
            Toast.makeText(getContext(),
                    mMember.getFullName() + " " + getString(R.string.identification_dismissed),
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

    public IdentificationEvent.SearchMethodEnum getIdMethod() {
        return this.mIdMethod;
    }

    public Member getMember() { return this.mMember; }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_member_edit).setVisible(true);
        menu.findItem(R.id.menu_enroll_newborn).setVisible(true);
        if (mMember != null && mMember.getAbsentee()) {
            menu.findItem(R.id.menu_complete_enrollment).setVisible(true);
        }
    }
}
