package org.watsi.uhp.presenters;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.watsi.domain.entities.IdentificationEvent;
import org.watsi.domain.entities.Member;
import org.watsi.domain.repositories.IdentificationEventRepository;
import org.watsi.domain.repositories.MemberRepository;
import org.watsi.uhp.R;
import org.watsi.uhp.adapters.MemberAdapter;
import org.watsi.uhp.custom_components.NotificationBar;
import org.watsi.uhp.helpers.PhotoLoaderHelper;
import org.watsi.uhp.managers.NavigationManager;

import java.util.List;

public abstract class MemberDetailPresenter {
    private final NavigationManager mNavigationManager;
    private final View mView;
    private final Context mContext;
    private final Member mMember;

    private final MemberRepository memberRepository;
    protected final IdentificationEventRepository identificationEventRepository;

    MemberDetailPresenter(View view,
                          Context context,
                          Member member,
                          NavigationManager navigationManager,
                          MemberRepository memberRepository,
                          IdentificationEventRepository identificationEventRepository) {
        mView = view;
        mContext = context;
        mMember = memberRepository.find(member.getId());
        mNavigationManager = navigationManager;
        this.memberRepository = memberRepository;
        this.identificationEventRepository = identificationEventRepository;
    }

    public void setUp() {
        setMemberNotifications();
        setPatientCardTextFields();
        setPatientCardPhoto();
        setMemberActionButton();
        setBottomListView();
        additionalSetup();
    }

    protected abstract void additionalSetup();

    protected abstract void setMemberActionButton();

    protected abstract void navigateToCompleteEnrollmentFragment();

    public abstract void navigateToMemberEditFragment();

    void setMemberNotifications() {
        if (mMember.isAbsentee()) {
            setAbsenteeNotification();
        }
        if (mMember.getCardId() == null) {
            setReplaceCardNotification();
        }
    }

    void setBottomListView() {
        List<Member> householdMembers = memberRepository
                .remainingHouseholdMembers(mMember.getHouseholdId(), mMember.getId());
        setBottomListWithMembers(householdMembers);
    }

    void setPatientCardPhoto() {
        PhotoLoaderHelper.loadMemberPhoto(getContext(), getMember(), getMemberPhotoImageView(),
                R.dimen.detail_fragment_photo_width, R.dimen.detail_fragment_photo_height);
    }

    void setPatientCardTextFields() {
        getMemberNameDetailTextView().setText(mMember.getName());
        getMemberAgeAndGenderTextView().setText(mMember.getFormattedAgeAndGender());
        getMemberCardIdDetailTextView().setText(mMember.getFormattedCardId());
        getMemberPhoneNumberTextView().setText(mMember.formattedPhoneNumber());
    }

    void setBottomListWithMembers(List<Member> householdMembers) {
        TextView householdListLabel = getHouseholdMembersLabelTextView();
        ListView householdListView = getHouseholdMembersListView();

        int householdSize = householdMembers.size() + 1;

        householdListLabel.setText(formatQuantityStringFromHouseholdSize(householdSize));
        householdListView.setAdapter(new MemberAdapter(getContext(),
                                                       householdMembers,
                                                       false,
                                                       identificationEventRepository));
        householdListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Member member = (Member) parent.getItemAtPosition(position);
                IdentificationEvent idEvent = new IdentificationEvent(member,
                        IdentificationEvent.SearchMethod.THROUGH_HOUSEHOLD, mMember);
                getNavigationManager().setMemberDetailFragment(member, idEvent);
            }
        });
    }

    List<Member> getMembersForBottomListView() {
        return memberRepository.remainingHouseholdMembers(mMember.getHouseholdId(), mMember.getId());
    }

    TextView getHouseholdMembersLabelTextView() {
        return (TextView) getView().findViewById(R.id.household_members_label);
    }

    ListView getHouseholdMembersListView() {
        return (ListView) getView().findViewById(R.id.household_members);
    }

    String formatQuantityStringFromHouseholdSize(int householdSize) {
        return getContext().getResources().getQuantityString(
                R.plurals.household_label, householdSize, householdSize);
    }

    ImageView getMemberPhotoImageView() {
        return (ImageView) mView.findViewById(R.id.member_photo);
    }

    TextView getMemberNameDetailTextView() {
        return ((TextView) mView.findViewById(R.id.member_name_detail_fragment));
    }

    TextView getMemberAgeAndGenderTextView() {
        return ((TextView) mView.findViewById(R.id.member_age_and_gender));
    }

    TextView getMemberCardIdDetailTextView() {
        return ((TextView) mView.findViewById(R.id.member_card_id_detail_fragment));
    }

    TextView getMemberPhoneNumberTextView() {
        return ((TextView) mView.findViewById(R.id.member_phone_number));
    }

    Button getMemberActionButton() {
        return ((Button) mView.findViewById(R.id.member_action_button));
    }

    private void setAbsenteeNotification() {
        NotificationBar memberNotification =
                (NotificationBar) mView.findViewById(R.id.absentee_notification);
        memberNotification.setVisibility(View.VISIBLE);
        memberNotification.setOnActionClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        navigateToCompleteEnrollmentFragment();
                    }
                }
        );
    }

    private void setReplaceCardNotification() {
        NotificationBar memberNotification =
                (NotificationBar) mView.findViewById(R.id.replace_card_notification);
        memberNotification.setVisibility(View.VISIBLE);
        memberNotification.setOnActionClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        navigateToMemberEditFragment();
                    }
                }
        );
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
