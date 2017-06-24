package org.watsi.uhp.presenters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.simprints.libsimprints.SimHelper;

import org.watsi.uhp.BuildConfig;
import org.watsi.uhp.R;
import org.watsi.uhp.fragments.CheckInMemberDetailFragment;
import org.watsi.uhp.fragments.IdentifyMemberDetailFragment;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.managers.SessionManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.models.SyncableModel;

/**
 * Created by michaelliang on 6/24/17.
 */

public class CheckInMemberDetailPresenter extends MemberDetailPresenter {
    private final SessionManager mSessionManager;
    private final CheckInMemberDetailFragment mCheckInMemberDeailFragment;
    private IdentificationEvent mIdEvent;
    private NavigationManager mNavigationManager;
    private  SessionManager sessionManager;

    public CheckInMemberDetailPresenter(NavigationManager navigationManager, SessionManager sessionManager, CheckInMemberDetailFragment checkInMemberDetailFragment, View view, Context context, Member member, IdentificationEvent idEvent) {
        super(view, context, member, navigationManager);

        mCheckInMemberDeailFragment = checkInMemberDetailFragment;
        mSessionManager = sessionManager;
        mIdEvent = idEvent;
        mNavigationManager = navigationManager;
    }

    public void setUp() {
        super.setUp();
    }

    public void setMemberActionButton() {
        Button memberActionButton = getMemberActionButton();
        memberActionButton.setText(R.string.check_in);
        memberActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openClinicOpdDialog();
            }
        });
    }

    public void setMemberActionLink() {
        getMemberActionLink().setVisibility(View.VISIBLE);
        getMemberActionLink().setClickable(false);
        String fingerprintsVerificationTier = mIdEvent.getFingerprintsVerificationTier();
        if (fingerprintsVerificationTier == null) {
            getMemberActionLink().setText(R.string.no_scan_indicator);
        } else {
            if (fingerprintsVerificationTier == "TIER_5") {
                getMemberActionLink().setText(R.string.bad_scan_indicator);
            } else {
                getMemberActionLink().setText(R.string.good_scan_indicator);
            }
        }
    }

    public void openClinicOpdDialog() {
        mNavigationManager.setClinicNumberFormFragment(mIdEvent);
    }
}
