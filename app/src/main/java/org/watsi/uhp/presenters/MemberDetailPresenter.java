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
        setPatientCardTextFields();
        setPatientCardPhoto();
        setPatientCardNotifications();
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

    protected void setPatientCardPhoto() {
        Bitmap photoBitmap = mMember.getPhotoBitmap(mContext.getContentResolver());
        if (photoBitmap != null) {
            setPatientCardPhotoBitmap(photoBitmap);
        } else {
            setPatientCardPhotoAsDefault();
        }
    }

    protected void setPatientCardNotifications() {
        if (mMember.isAbsentee()) {
            showAbsenteeNotification();
        } else if (mMember.getCardId() == null) {
            showReplaceCardNotification();
        }
    }

    protected void setPatientCardTextFields() {
        getMemberNameDetailTextView().setText(mMember.getFullName());
        getMemberAgeAndGenderTextView().setText(mMember.getFormattedAgeAndGender());
        getMemberCardIdDetailTextView().setText(mMember.getFormattedCardId());
        getMemberPhoneNumberTextView().setText(mMember.getFormattedPhoneNumber());
    }

    // Tested above.
    // Untested below.

    protected TextView getMemberNameDetailTextView() {
        return ((TextView) mView.findViewById(R.id.member_name_detail_fragment));
    }

    protected TextView getMemberAgeAndGenderTextView() {
        return ((TextView) mView.findViewById(R.id.member_age_and_gender));
    }

    protected TextView getMemberCardIdDetailTextView() {
        return ((TextView) mView.findViewById(R.id.member_card_id_detail_fragment));
    }

    protected TextView getMemberPhoneNumberTextView() {
        return ((TextView) mView.findViewById(R.id.member_phone_number));
    }

    protected void setPatientCardPhotoBitmap(Bitmap photoBitMap) {
        ImageView memberPhoto = (ImageView) mView.findViewById(R.id.member_photo);
        memberPhoto.setImageBitmap(photoBitMap);
    }

    protected void setPatientCardPhotoAsDefault() {
        ImageView memberPhoto = (ImageView) mView.findViewById(R.id.member_photo);
        memberPhoto.setImageResource(R.drawable.portrait_placeholder);
    }

    protected void showAbsenteeNotification() {
        TextView memberNotification = (TextView) mView.findViewById(R.id.member_notification);
        memberNotification.setVisibility(View.VISIBLE);
        memberNotification.setText(R.string.absentee_notification);
    }

    protected void showReplaceCardNotification() {
        TextView memberNotification = (TextView) mView.findViewById(R.id.member_notification);
        memberNotification.setVisibility(View.VISIBLE);
        memberNotification.setText(R.string.replace_card_notification);
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
