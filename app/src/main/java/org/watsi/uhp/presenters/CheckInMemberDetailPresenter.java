package org.watsi.uhp.presenters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.fragments.CheckInMemberDetailFragment;
import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.managers.SessionManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.models.SyncableModel;

import java.sql.SQLException;


/**
 * Created by michaelliang on 6/13/17.
 */

public class CheckInMemberDetailPresenter extends MemberDetailPresenter {
    private final SessionManager mSessionManager; // Need this later on for fingerprints
    private IdentificationEvent mUnsavedIdentificationEvent;
    private final CheckInMemberDetailFragment mCheckInMemberDetailPresenterFragment;
    private Member mThroughMember;
    private IdentificationEvent.SearchMethodEnum mIdMethod;

    public CheckInMemberDetailPresenter(NavigationManager navigationManager, SessionManager sessionManager, CheckInMemberDetailFragment checkInMemberDetailFragment, View view, Context context, Member member, IdentificationEvent.SearchMethodEnum idMethod, Member throughMember) {
        super(view, context, member, navigationManager);

        mCheckInMemberDetailPresenterFragment = checkInMemberDetailFragment;
        mSessionManager = sessionManager;
        mIdMethod = idMethod;
        mThroughMember = throughMember;
    }

    public void setUp() {
        super.setUp();
        preFillIdentificationEventFields();
    }

    protected void preFillIdentificationEventFields() {
        mUnsavedIdentificationEvent = new IdentificationEvent();
        mUnsavedIdentificationEvent.setMember(getMember());
        mUnsavedIdentificationEvent.setSearchMethod(mIdMethod);
        mUnsavedIdentificationEvent.setThroughMember(mThroughMember);
        if (getMember().getPhoto() == null) {
            mUnsavedIdentificationEvent.setPhotoVerified(false);
        }
    }

    protected void setMemberActionButton() {
        Button memberActionButton = getMemberActionButton();

        memberActionButton.setText(R.string.check_in);
        memberActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completeIdentification();
            }
        });
    }

    //// Tested above
    //// Below TBD because of fingerprints

    protected Button getMemberActionButton() {
         return (Button) getView().findViewById(R.id.member_action_button);
    }

    protected void setMemberActionLink() {
        getMemberActionLink().setVisibility(View.VISIBLE);
        getMemberActionLink().setText(R.string.reject_identity);
        getMemberActionLink().setOnClickListener(new View.OnClickListener() {
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

    protected void completeIdentificationOnReport() throws SyncableModel.UnauthenticatedException {
        mUnsavedIdentificationEvent.setClinicNumberType(null);
        mUnsavedIdentificationEvent.setClinicNumber(null);
        mUnsavedIdentificationEvent.setAccepted(false);
        mUnsavedIdentificationEvent.setOccurredAt(Clock.getCurrentTime());
        try {
            mUnsavedIdentificationEvent.saveChanges(((ClinicActivity) getContext()).getAuthenticationToken());
        } catch (SQLException e) {
            ExceptionManager.reportException(e);
        }

        getNavigationManager().setCurrentPatientsFragment();
        Toast.makeText(getContext(),
                getMember().getFullName() + " " + R.string.identification_rejected,
                Toast.LENGTH_LONG).
                show();
    }

    public IdentificationEvent getUnsavedIdentificationEvent() {
        return mUnsavedIdentificationEvent;
    }

    public void completeIdentification() {
        getNavigationManager().setClinicNumberFormFragment(mUnsavedIdentificationEvent);
    }
}
