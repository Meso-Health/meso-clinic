package org.watsi.uhp.presenters;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.R;
import org.watsi.uhp.adapters.MemberAdapter;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.helpers.PhotoLoaderHelper;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ MemberDao.class, MemberDetailPresenter.class, ExceptionManager.class, PhotoLoaderHelper.class })
public class MemberDetailPresenterTest {
    private MemberDetailPresenter memberDetailPresenter;

    @Mock
    View mockView;

    @Mock
    Context mockContext;

    @Mock
    Member mockMember;

    @Mock
    NavigationManager mockNavigationManager;

    @Mock
    ContentResolver mockContentResolver;

    @Mock
    ImageView mockPatientCardImageView;

    @Mock
    Bitmap mockPatientPhotoBitmap;

    @Mock
    List<Member> mockMemberList;

    @Before
    public void setup() {
        initMocks(this);
        mockStatic(MemberDao.class);
        mockStatic(ExceptionManager.class);
        mockStatic(PhotoLoaderHelper.class);
        memberDetailPresenter = new MemberDetailPresenter(mockView, mockContext, mockMember, mockNavigationManager) {
            @Override
            protected void setMemberActionButton() {
                // no-op
            }

            @Override
            protected void setMemberSecondaryActionButton() {
                // no-op
            }

            @Override
            protected void setMemberIndicator() {
                // no-op
            }

            @Override
            protected void setMemberActionLink() {
                // no-op
            }
        };
    }

    @Test
    public void setUp() {
        MemberDetailPresenter memberDetailPresenterSpy = spy(memberDetailPresenter);
        doNothing().when(memberDetailPresenterSpy).setPatientCardTextFields();
        doNothing().when(memberDetailPresenterSpy).setPatientCardPhoto();
        doNothing().when(memberDetailPresenterSpy).setPatientCardNotifications();
        doNothing().when(memberDetailPresenterSpy).setMemberActionLink();
        doNothing().when(memberDetailPresenterSpy).setMemberActionButton();
        doNothing().when(memberDetailPresenterSpy).setBottomListView();
        doNothing().when(memberDetailPresenterSpy).setMemberSecondaryActionButton();
        doNothing().when(memberDetailPresenterSpy).setMemberIndicator();

        memberDetailPresenterSpy.setUp();

        verify(memberDetailPresenterSpy, times(1)).setPatientCardTextFields();
        verify(memberDetailPresenterSpy, times(1)).setPatientCardPhoto();
        verify(memberDetailPresenterSpy, times(1)).setPatientCardNotifications();
        verify(memberDetailPresenterSpy, times(1)).setMemberActionLink();
        verify(memberDetailPresenterSpy, times(1)).setMemberActionButton();
        verify(memberDetailPresenterSpy, times(1)).setBottomListView();
        verify(memberDetailPresenterSpy, times(1)).setMemberSecondaryActionButton();
        verify(memberDetailPresenterSpy, times(1)).setMemberIndicator();
    }

    @Test
    public void setPatientCardNotifications_absentee() {
        MemberDetailPresenter memberDetailPresenterSpy = spy(memberDetailPresenter);

        doNothing().when(memberDetailPresenterSpy).showAbsenteeNotification();
        when(mockMember.isAbsentee()).thenReturn(true);

        memberDetailPresenterSpy.setPatientCardNotifications();

        verify(memberDetailPresenterSpy, times(1)).showAbsenteeNotification();
    }

    @Test
    public void setPatientCardNotifications_notAbsentee() {
        MemberDetailPresenter memberDetailPresenterSpy = spy(memberDetailPresenter);

        doNothing().when(memberDetailPresenterSpy).showReplaceCardNotification();
        when(mockMember.isAbsentee()).thenReturn(false);

        memberDetailPresenterSpy.setPatientCardNotifications();

        verify(memberDetailPresenterSpy, times(1)).showReplaceCardNotification();
    }

    @Test
    public void setPatientCardPhoto() {
        MemberDetailPresenter memberDetailPresenterSpy = spy(memberDetailPresenter);

        when(mockContext.getContentResolver()).thenReturn(mockContentResolver);
        when(memberDetailPresenter.getMemberPhotoImageView()).thenReturn(mockPatientCardImageView);

        memberDetailPresenterSpy.setPatientCardPhoto();

        verifyStatic();
        PhotoLoaderHelper.loadMemberPhoto(mockContext, mockMember, mockPatientCardImageView, R.dimen.detail_fragment_photo_width, R.dimen.detail_fragment_photo_height);
    }

    @Test
    public void setPatientCardTextFields() {
        MemberDetailPresenter memberDetailPresenterSpy = spy(memberDetailPresenter);

        TextView mockTextView = mock(TextView.class);
        when(memberDetailPresenter.getMemberNameDetailTextView()).thenReturn(mockTextView);
        when(memberDetailPresenter.getMemberAgeAndGenderTextView()).thenReturn(mockTextView);
        when(memberDetailPresenter.getMemberCardIdDetailTextView()).thenReturn(mockTextView);
        when(memberDetailPresenter.getMemberPhoneNumberTextView()).thenReturn(mockTextView);

        when(mockMember.getFullName()).thenReturn("mockName");
        when(mockMember.getFormattedAgeAndGender()).thenReturn("25 F");
        when(mockMember.getFormattedCardId()).thenReturn("MOCK123123");
        when(mockMember.getFormattedPhoneNumber()).thenReturn("123456789");

        memberDetailPresenterSpy.setPatientCardTextFields();

        verify(mockTextView, times(1)).setText("mockName");
        verify(mockTextView, times(1)).setText("25 F");
        verify(mockTextView, times(1)).setText("MOCK123123");
        verify(mockTextView, times(1)).setText("123456789");

        verify(memberDetailPresenterSpy, times(1)).getMemberNameDetailTextView();
        verify(memberDetailPresenterSpy, times(1)).getMemberAgeAndGenderTextView();
        verify(memberDetailPresenterSpy, times(1)).getMemberCardIdDetailTextView();
        verify(memberDetailPresenterSpy, times(1)).getMemberPhoneNumberTextView();
    }

    @Test
    public void setBottomListView_noOtherHouseholdMembers() {
        MemberDetailPresenter memberDetailPresenterSpy = spy(memberDetailPresenter);

        doReturn(null).when(memberDetailPresenterSpy).getMembersForBottomListView();

        doNothing().when(memberDetailPresenterSpy).setBottomListWithMembers(any(List.class));

        memberDetailPresenterSpy.setBottomListView();

        verify(memberDetailPresenterSpy, never()).setBottomListWithMembers(null);
    }

    @Test
    public void setBottomListView_hasOtherHouseholdMembers() {
        MemberDetailPresenter memberDetailPresenterSpy = spy(memberDetailPresenter);

        doReturn(mockMemberList).when(memberDetailPresenterSpy).getMembersForBottomListView();

        doNothing().when(memberDetailPresenterSpy).setBottomListWithMembers(any(List.class));

        memberDetailPresenterSpy.setBottomListView();

        verify(memberDetailPresenterSpy, times(1)).setBottomListWithMembers(mockMemberList);
    }

    @Test
    public void setBottomListWithMembers() throws Exception {
        MemberDetailPresenter memberDetailPresenterSpy = spy(memberDetailPresenter);

        doReturn(5).when(mockMemberList).size();
        TextView mockTextView = mock(TextView.class);
        ListView mockListView = mock(ListView.class);
        MemberAdapter mockMemberAdapter = mock(MemberAdapter.class);

        when(memberDetailPresenterSpy.getHouseholdMembersLabelTextView()).thenReturn(mockTextView);
        when(memberDetailPresenterSpy.getHouseholdMembersListView()).thenReturn(mockListView);
        when(memberDetailPresenterSpy.getContext()).thenReturn(mockContext);
        doReturn("6 Household Members").when(memberDetailPresenterSpy).formatQuantityStringFromHouseholdSize(6);
        whenNew(MemberAdapter.class).withAnyArguments().thenReturn(mockMemberAdapter);

        memberDetailPresenterSpy.setBottomListWithMembers(mockMemberList);

        verify(memberDetailPresenterSpy, times(1)).formatQuantityStringFromHouseholdSize(6);
        verify(mockTextView, times(1)).setText("6 Household Members");
        verify(mockListView, times(1)).setAdapter(any(MemberAdapter.class));
        verify(mockListView, times(1)).setOnItemClickListener(any(AdapterView.OnItemClickListener.class));
    }

    @Test
    public void getMembersForBottomListView_reportsException() throws Exception {
        MemberDetailPresenter memberDetailPresenterSpy = spy(memberDetailPresenter);
        SQLException mockException = mock(SQLException.class);

        when(MemberDao.getRemainingHouseholdMembers(any(UUID.class), any(UUID.class))).thenThrow(mockException);

        List<Member> result = memberDetailPresenterSpy.getMembersForBottomListView();
        assertNull(result);

        verifyStatic();
        ExceptionManager.reportException(mockException);
    }

    @Test
    public void getMembersForBottomListView_returnsMemberList() throws Exception {
        MemberDetailPresenter memberDetailPresenterSpy = spy(memberDetailPresenter);

        when(MemberDao.getRemainingHouseholdMembers(any(UUID.class), any(UUID.class))).thenReturn(mockMemberList);

        List<Member> result = memberDetailPresenterSpy.getMembersForBottomListView();
        assertEquals(result, mockMemberList);

        verifyStatic(never());
        ExceptionManager.reportException(any(Exception.class));
    }
}
