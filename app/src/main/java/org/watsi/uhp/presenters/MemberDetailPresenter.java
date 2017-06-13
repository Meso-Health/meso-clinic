package org.watsi.uhp.presenters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.watsi.uhp.R;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.Member;

/**
 * Created by michaelliang on 6/1/17.
 */

public class MemberDetailPresenter {
    private final NavigationManager mNavigationManager;
    private final View mView;
    private final Context mContext;
    private final Member mMember;

    public MemberDetailPresenter(View view, Context context, Member member, NavigationManager navigationManager) {
        mView = view;
        mContext = context;
        mMember = member;
        mNavigationManager = navigationManager;
    }

    public TextView getMemberActionLink() {
        return (TextView) mView.findViewById(R.id.member_action_link);
    }

    public void setUp() {
        setPatientCard();
        setMemberActionLink();
        setMemberActionButton();
        setBottomListView();
    }

    protected void setMemberActionButton() {
        // no-op
    }

    protected void setBottomListView() {
        // no-op
    }

    protected void setMemberActionLink() {
        // no-op
    }

    protected void setPatientCard() {
        ((TextView) mView.findViewById(R.id.member_name_detail_fragment)).setText(mMember.getFullName());
        ((TextView) mView.findViewById(R.id.member_age_and_gender))
                .setText(mMember.getFormattedAgeAndGender());
        ((TextView) mView.findViewById(R.id.member_card_id_detail_fragment)).setText(mMember.getFormattedCardId());
        ((TextView) mView.findViewById(R.id.member_phone_number)).setText(mMember.getFormattedPhoneNumber());

        Bitmap photoBitmap = mMember.getPhotoBitmap(mContext.getContentResolver());
        ImageView memberPhoto = (ImageView) mView.findViewById(R.id.member_photo);
        if (photoBitmap != null) {
            memberPhoto.setImageBitmap(photoBitmap);
        } else {
            memberPhoto.setImageResource(R.drawable.portrait_placeholder);
        }

        TextView memberNotification = (TextView) mView.findViewById(R.id.member_notification);
        if (mMember.isAbsentee()) {
            memberNotification.setVisibility(View.VISIBLE);
            memberNotification.setText(R.string.absentee_notification);
        } else if (mMember.getCardId() == null) {
            memberNotification.setVisibility(View.VISIBLE);
            memberNotification.setText(R.string.replace_card_notification);
        }
    }

    public Member getMember() {
        return mMember;
    }

    public Context getContext() {
        return mContext;
    }

    public View getView() {
        return mView;
    }

    public NavigationManager getNavigationManager() {
        return mNavigationManager;
    }
}
