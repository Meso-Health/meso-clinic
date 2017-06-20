package org.watsi.uhp.presenters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.adapters.MemberAdapter;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.List;

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
        TextView householdListLabel = (TextView) getView().findViewById(R.id.household_members_label);
        ListView householdListView = (ListView) getView().findViewById(R.id.household_members);

        try {
            List<Member> householdMembers = MemberDao.getRemainingHouseholdMembers(
                    getMember().getHouseholdId(), getMember() .getId());
            ListAdapter adapter = new MemberAdapter(getContext(), householdMembers, false);
            int householdSize = householdMembers.size() + 1;

            householdListLabel.setText(getContext().getResources().getQuantityString(
                    R.plurals.household_label, householdSize, householdSize));
            householdListView.setAdapter(adapter);
            householdListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Member member = (Member) parent.getItemAtPosition(position);
                    getNavigationManager().setMemberDetailFragment(
                            member,
                            IdentificationEvent.SearchMethodEnum.THROUGH_HOUSEHOLD,
                            getMember()
                    );
                }
            });
        } catch (SQLException e) {
            ExceptionManager.reportException(e);
        }
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

    // Untested below.
    protected void setPatientCardTextFields() {
        ((TextView) mView.findViewById(R.id.member_name_detail_fragment)).setText(mMember.getFullName());
        ((TextView) mView.findViewById(R.id.member_age_and_gender)).setText(mMember.getFormattedAgeAndGender());
        ((TextView) mView.findViewById(R.id.member_card_id_detail_fragment)).setText(mMember.getFormattedCardId());
        ((TextView) mView.findViewById(R.id.member_phone_number)).setText(mMember.getFormattedPhoneNumber());
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
