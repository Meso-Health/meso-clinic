package org.watsi.uhp.presenters;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import org.watsi.uhp.R;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

/**
 * Created by michaelliang on 6/24/17.
 */

public class CheckInMemberDetailPresenter extends MemberDetailPresenter {
    private IdentificationEvent mIdEvent;

    public CheckInMemberDetailPresenter(NavigationManager navigationManager, View view, Context context, Member member, IdentificationEvent idEvent) {
        super(view, context, member, navigationManager);
        mIdEvent = idEvent;
    }

    public void setMemberActionButton() {
        getMemberActionButton().setText(R.string.check_in);
        getMemberActionButton().setOnClickListener(new View.OnClickListener() {
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
        getNavigationManager().setClinicNumberFormFragment(mIdEvent);
    }

    public IdentificationEvent getIdEvent() {
        return mIdEvent;
    }
}
