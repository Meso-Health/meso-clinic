package org.watsi.uhp.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
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

import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.SimHelper;
import com.simprints.libsimprints.Verification;

import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.R;
import org.watsi.uhp.adapters.MemberAdapter;
import org.watsi.uhp.database.EncounterItemDao;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.models.SyncableModel;
import org.watsi.uhp.presenters.DetailPresenter;

import java.sql.SQLException;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class DetailFragment extends BaseFragment {

    private IdentificationEvent mIdentificationEvent;
    private Member mMember;

    DetailPresenter detailPresenter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        detailPresenter = new DetailPresenter(view);

        getActivity().setTitle(R.string.detail_fragment_label);
        setHasOptionsMenu(true);
        getActivity().invalidateOptionsMenu();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        // Getting IdentificationEvent
        String searchMethodString = getArguments().getString(NavigationManager.ID_METHOD_BUNDLE_FIELD);
        IdentificationEvent.SearchMethodEnum idMethod = null;
        if (searchMethodString != null) {
            idMethod = IdentificationEvent.SearchMethodEnum.valueOf(searchMethodString);
        }
        mMember = (Member) getArguments().getSerializable(NavigationManager.MEMBER_BUNDLE_FIELD);
        Member throughMember = (Member) getArguments()
                .getSerializable(NavigationManager.THROUGH_MEMBER_BUNDLE_FIELD);

        mIdentificationEvent = new IdentificationEvent();
        mIdentificationEvent.setMember(mMember);
        mIdentificationEvent.setSearchMethod(idMethod);
        mIdentificationEvent.setThroughMember(throughMember);


        setPatientCard(view);
        setButton(view);
        setHouseholdList(view);
        if (mMember.currentCheckIn() == null) {
            setRejectIdentityLink();
        } else {
            setDismissPatientLink(view);
        }

        return view;
    }

    private void setRejectIdentityLink() {
        detailPresenter.getRejectIdentityLink().setVisibility(View.VISIBLE);
        detailPresenter.getRejectIdentityLink().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.reject_identity_alert)
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                try {
                                    completeIdentificationOnReport();
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
        ((TextView) detailView.findViewById(R.id.member_name_detail_fragment)).setText(mMember.getFullName());
        ((TextView) detailView.findViewById(R.id.member_age_and_gender))
                .setText(mMember.getFormattedAgeAndGender());
        ((TextView) detailView.findViewById(R.id.member_card_id_detail_fragment)).setText(mMember.getFormattedCardId());
        ((TextView) detailView.findViewById(R.id.member_phone_number)).setText(mMember.getFormattedPhoneNumber());

        Bitmap photoBitmap = mMember.getPhotoBitmap(getContext().getContentResolver());
        ImageView memberPhoto = (ImageView) detailView.findViewById(R.id.member_photo);
        if (photoBitmap != null) {
            memberPhoto.setImageBitmap(photoBitmap);
        } else {
            memberPhoto.setImageResource(R.drawable.portrait_placeholder);
        }

        TextView memberNotification = (TextView) detailView.findViewById(R.id.member_notification);
        if (mMember.isAbsentee()) {
            memberNotification.setVisibility(View.VISIBLE);
            memberNotification.setText(R.string.absentee_notification);
        } else if (mMember.getCardId() == null) {
            memberNotification.setVisibility(View.VISIBLE);
            memberNotification.setText(R.string.replace_card_notification);
        }
    }

    private void setButton(View view) {
        Button confirmButton = (Button) view.findViewById(R.id.approve_identity);
        if (mMember.getFingerprintsGuid() != null) {
            confirmButton.setText(R.string.approve_identity);
        } else {
            confirmButton.setText(R.string.approve_identity_without_fingerprints);
        }

        if (mMember.currentCheckIn() == null) {
            // Here is the branching logic

            // If they have fingerprints
            if (mMember.getFingerprintsGuid() != null) {
                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SimHelper simHelper = new SimHelper(BuildConfig.SIMPRINTS_API_KEY, getSessionManager().getCurrentLoggedInUsername());
                        Intent fingerprintIdentificationIntent = simHelper.verify(BuildConfig.PROVIDER_ID.toString(), mMember.getFingerprintsGuid().toString());
                        startActivityForResult(
                                fingerprintIdentificationIntent,
                                1
                        );
                    }
                });
            } else {
                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        completeIdentificationWithoutFingerprints();
                    }
                });
            }
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

    public void completeIdentificationOnReport() throws SyncableModel.UnauthenticatedException {
        IdentificationEvent idEvent = new IdentificationEvent();
        idEvent.setClinicNumberType(null);
        idEvent.setClinicNumber(null);
        idEvent.setAccepted(false);
        idEvent.setOccurredAt(Clock.getCurrentTime());
        if (mMember.getPhoto() == null) {
            idEvent.setPhotoVerified(false);
        }
        try {
            idEvent.saveChanges(getAuthenticationToken());
        } catch (SQLException e) {
            ExceptionManager.reportException(e);
        }

        getNavigationManager().setCurrentPatientsFragment();
        Toast.makeText(getContext(),
                mMember.getFullName() + " " + R.string.identification_rejected,
                Toast.LENGTH_LONG).
                show();
    }

    public void dismissIdentification(IdentificationEvent.DismissalReasonEnum dismissReason)
            throws SyncableModel.UnauthenticatedException {
        IdentificationEvent checkIn = mMember.currentCheckIn();
        checkIn.setDismissalReason(dismissReason);

        try {
            checkIn.saveChanges(getAuthenticationToken());
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
        return mIdentificationEvent.getSearchMethod();
    }

    public Member getMember() { return this.mMember; }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mMember.currentCheckIn() == null) {
            menu.findItem(R.id.menu_check_in_without_fingerprints).setVisible(true);
        }

        menu.findItem(R.id.menu_member_edit).setVisible(true);
        menu.findItem(R.id.menu_enroll_newborn).setVisible(true);

        if (mMember != null && mMember.isAbsentee()) {
            menu.findItem(R.id.menu_complete_enrollment).setVisible(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK){
            Toast.makeText(
                    getContext(),
                    "result not ok. Error code: " + resultCode,
                    Toast.LENGTH_LONG).show();
            mIdentificationEvent.setFingerprintsVerificationResultCode(resultCode);
        } else {
            Verification verification = data.getParcelableExtra(Constants.SIMPRINTS_VERIFICATION);
            String fingerprintTier = verification.getTier().toString();
            float fingerprintConfidence = verification.getConfidence();

            Toast.makeText(getContext(), "Fingerprint Scan Successful!", Toast.LENGTH_LONG);

            mIdentificationEvent.setFingerprintsVerificationConfidence(fingerprintConfidence);
            mIdentificationEvent.setFingerprintsVerificationResultCode(resultCode);
            mIdentificationEvent.setFingerprintsVerificationTier(fingerprintTier);

            if (mMember.getPhoto() == null) {
                mIdentificationEvent.setPhotoVerified(false);
            }
            getNavigationManager().setClinicNumberFormFragment(mIdentificationEvent);
        }
    }

    public void completeIdentificationWithoutFingerprints() {
        // TODO, figure out how to deal with non-nullable integers and floats.
        getNavigationManager().setClinicNumberFormFragment(mIdentificationEvent);
    }
}
