package org.watsi.uhp.presenters;

import android.content.Context;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.models.Member;

public class EnrollmentPresenter {

    private final Member mMember;
    private final Context mContext;

    public EnrollmentPresenter(Member member, Context context) {
        mMember = member;
        mContext = context;
    }

    public Toast confirmationToast() {
        if (mMember.isAbsentee()) {
            return Toast.makeText(mContext, "Any updates successfully saved", Toast.LENGTH_LONG);
        } else {
            return Toast.makeText(mContext, "Enrollment completed", Toast.LENGTH_LONG);
        }
    }
}
