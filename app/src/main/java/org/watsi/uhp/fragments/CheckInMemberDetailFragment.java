package org.watsi.uhp.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.models.SyncableModel;

import java.sql.SQLException;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public class CheckInMemberDetailFragment extends MemberDetailFragment {
    private IdentificationEvent mUnsavedIdentificationEvent;

    @Override
    protected void setUpFragment(View view) {
        String searchMethodString = getArguments().getString(NavigationManager.ID_METHOD_BUNDLE_FIELD);
        IdentificationEvent.SearchMethodEnum idMethod = null;
        if (searchMethodString != null) {
            idMethod = IdentificationEvent.SearchMethodEnum.valueOf(searchMethodString);
        }
        Member throughMember = (Member) getArguments()
                .getSerializable(NavigationManager.THROUGH_MEMBER_BUNDLE_FIELD);

        // Do the IdentificationEvent stuff
        mUnsavedIdentificationEvent = new IdentificationEvent();
        mUnsavedIdentificationEvent.setMember(getMember() );
        mUnsavedIdentificationEvent.setSearchMethod(idMethod);
        mUnsavedIdentificationEvent.setThroughMember(throughMember);
        if (getMember() .getPhoto() == null) {
            mUnsavedIdentificationEvent.setPhotoVerified(false);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.menu_check_in_without_fingerprints).setVisible(true);
    }

    @Override
    public void setMemberActionButton(View view) {
        Button confirmButton = (Button) view.findViewById(R.id.member_action_button);
        if (getMember() .getFingerprintsGuid() != null) {
            confirmButton.setText(R.string.approve_identity);
            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SimHelper simHelper = new SimHelper(BuildConfig.SIMPRINTS_API_KEY, getSessionManager().getCurrentLoggedInUsername());
                    Intent fingerprintIdentificationIntent = simHelper.verify(BuildConfig.PROVIDER_ID.toString(), getMember() .getFingerprintsGuid().toString());
                    startActivityForResult(
                            fingerprintIdentificationIntent,
                            1 // TODO make this a constant
                    );
                }
            });
        } else {
            confirmButton.setText(R.string.approve_identity_without_fingerprints);
            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    completeIdentificationWithoutFingerprints();
                }
            });
        }
    }

    @Override
    protected void setBottomListView(View view) {
        setHouseholdList(view);
    }

    @Override
    protected void setMemberActionLink(View view) {
        memberDetailPresenter.getMemberActionLink().setVisibility(View.VISIBLE);
        memberDetailPresenter.getMemberActionLink().setText(getContext().getString(R.string.reject_identity));
        memberDetailPresenter.getMemberActionLink().setOnClickListener(new View.OnClickListener() {
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

    public void completeIdentificationOnReport() throws SyncableModel.UnauthenticatedException {
        mUnsavedIdentificationEvent.setClinicNumberType(null);
        mUnsavedIdentificationEvent.setClinicNumber(null);
        mUnsavedIdentificationEvent.setAccepted(false);
        mUnsavedIdentificationEvent.setOccurredAt(Clock.getCurrentTime());
        try {
            mUnsavedIdentificationEvent.saveChanges(getAuthenticationToken());
        } catch (SQLException e) {
            ExceptionManager.reportException(e);
        }

        getNavigationManager().setCurrentPatientsFragment();
        Toast.makeText(getContext(),
                getMember() .getFullName() + " " + R.string.identification_rejected,
                Toast.LENGTH_LONG).
                show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            Toast.makeText(
                    getContext(),
                    "result not ok. Error code: " + resultCode,
                    Toast.LENGTH_LONG).show();
            mUnsavedIdentificationEvent.setFingerprintsVerificationResultCode(resultCode);
        } else {
            Verification verification = data.getParcelableExtra(Constants.SIMPRINTS_VERIFICATION);
            String fingerprintTier = verification.getTier().toString();
            float fingerprintConfidence = verification.getConfidence();

            Toast.makeText(getContext(), "Fingerprint Scan Successful!", Toast.LENGTH_LONG);

            mUnsavedIdentificationEvent.setFingerprintsVerificationConfidence(fingerprintConfidence);
            mUnsavedIdentificationEvent.setFingerprintsVerificationResultCode(resultCode);
            mUnsavedIdentificationEvent.setFingerprintsVerificationTier(fingerprintTier);

            getNavigationManager().setClinicNumberFormFragment(mUnsavedIdentificationEvent);
        }
    }

    public void completeIdentificationWithoutFingerprints() {
        // TODO, figure out how to deal with non-nullable integers and floats.
        getNavigationManager().setClinicNumberFormFragment(mUnsavedIdentificationEvent);
    }

    private void setHouseholdList(View view) {
        TextView householdListLabel = (TextView) view.findViewById(R.id.household_members_label);
        ListView householdListView = (ListView) view.findViewById(R.id.household_members);

        try {
            List<Member> householdMembers = MemberDao.getRemainingHouseholdMembers(
                    getMember().getHouseholdId(), getMember() .getId());
            ListAdapter adapter = new MemberAdapter(getContext(), householdMembers, false);
            int householdSize = householdMembers.size() + 1;

            householdListLabel.setText(getResources().getQuantityString(
                    R.plurals.household_label, householdSize, householdSize));
            householdListView.setAdapter(adapter);
            householdListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Member member = (Member) parent.getItemAtPosition(position);
                    if (member.currentCheckIn() == null) {
                        getNavigationManager().setCheckInMemberDetailFragment(
                                member,
                                IdentificationEvent.SearchMethodEnum.THROUGH_HOUSEHOLD,
                                getMember()
                        );
                    } else {
                        getNavigationManager().setCurrentMemberDetailFragment(member);
                    }
                }
            });
        } catch (SQLException e) {
            ExceptionManager.reportException(e);
        }
    }

    public IdentificationEvent getIdEvent() {
        return mUnsavedIdentificationEvent;
    }
}

