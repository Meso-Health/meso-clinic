package org.watsi.uhp.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import org.watsi.uhp.R;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.presenters.MemberDetailPresenter;

/**
 * Created by michaelliang on 6/12/17.
 */

public abstract class MemberDetailFragment extends BaseFragment {

    private Member mMember;
    MemberDetailPresenter memberDetailPresenter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_member_detail, container, false);

        memberDetailPresenter = new MemberDetailPresenter(view);
        mMember = (Member) getArguments().getSerializable(NavigationManager.MEMBER_BUNDLE_FIELD);

        // Prepare fragment stuff
        setUpMenuAndWindow();

        setPatientCard(view);
        setMemberActionLink(view);
        setMemberActionButton(view);
        setBottomListView(view);
        setUpFragment(view);

        return view;
    }

    protected void setUpMenuAndWindow() {
        setHasOptionsMenu(true);
        getActivity().invalidateOptionsMenu();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        getActivity().setTitle(R.string.detail_fragment_label);
    }

    protected abstract void setUpFragment(View view);

    protected abstract void setMemberActionButton(View view);

    protected abstract void setBottomListView(View view);

    protected abstract void setMemberActionLink(View view);

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // These should appear whenever you're in the detail view.
        menu.findItem(R.id.menu_member_edit).setVisible(true);
        menu.findItem(R.id.menu_enroll_newborn).setVisible(true);

        // This should only appear if member is an absentee.
        if (mMember.isAbsentee()) {
            menu.findItem(R.id.menu_complete_enrollment).setVisible(true);
        }
    }

    public Member getMember() {
        return mMember;
    }

    // This should stay and be moved to the presenters.
    protected void setPatientCard(View view) {
        ((TextView) view.findViewById(R.id.member_name_detail_fragment)).setText(mMember.getFullName());
        ((TextView) view.findViewById(R.id.member_age_and_gender))
                .setText(mMember.getFormattedAgeAndGender());
        ((TextView) view.findViewById(R.id.member_card_id_detail_fragment)).setText(mMember.getFormattedCardId());
        ((TextView) view.findViewById(R.id.member_phone_number)).setText(mMember.getFormattedPhoneNumber());

        Bitmap photoBitmap = mMember.getPhotoBitmap(getContext().getContentResolver());
        ImageView memberPhoto = (ImageView) view.findViewById(R.id.member_photo);
        if (photoBitmap != null) {
            memberPhoto.setImageBitmap(photoBitmap);
        } else {
            memberPhoto.setImageResource(R.drawable.portrait_placeholder);
        }

        TextView memberNotification = (TextView) view.findViewById(R.id.member_notification);
        if (mMember.isAbsentee()) {
            memberNotification.setVisibility(View.VISIBLE);
            memberNotification.setText(R.string.absentee_notification);
        } else if (mMember.getCardId() == null) {
            memberNotification.setVisibility(View.VISIBLE);
            memberNotification.setText(R.string.replace_card_notification);
        }
    }
}
