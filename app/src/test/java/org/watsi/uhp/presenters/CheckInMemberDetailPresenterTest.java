package org.watsi.uhp.presenters;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.R;
import org.watsi.uhp.adapters.MemberAdapter;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.fragments.CheckInMemberDetailFragment;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.managers.SessionManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
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
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 * Created by michaelliang on 6/13/17.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ MemberDao.class, CheckInMemberDetailPresenter.class, ExceptionManager.class })
public class CheckInMemberDetailPresenterTest {
    // Since unit tests don't have access to database default values, null is the default value.
    private static Boolean DEFAULT_IDENTIFICATION_EVENT_PHOTO_VERIFIED_FIELD = null;

    private CheckInMemberDetailPresenter checkInMemberDetailPresenter;

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
    SessionManager mockSessionManager;

    @Mock
    CheckInMemberDetailFragment mockCheckInMemberDetailFragment;

    @Mock
    Member mockThroughMember;

    @Mock
    ImageView mockPatientCardImageView;

    @Mock
    Bitmap mockPatientPhotoBitmap;

    @Mock
    Button mockButton;

    @Mock
    List<Member> mockMemberList;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockStatic(MemberDao.class);
        mockStatic(ExceptionManager.class);
        checkInMemberDetailPresenter = new CheckInMemberDetailPresenter(
                mockNavigationManager, mockSessionManager, mockCheckInMemberDetailFragment,
                mockView, mockContext, mockMember,
                IdentificationEvent.SearchMethodEnum.SCAN_BARCODE, mockThroughMember);
    }

    @Test
    public void preFillIdentificationEventFields_successfulMemberWithoutPhoto() {
        when(mockMember.getPhoto()).thenReturn(null);
        checkInMemberDetailPresenter = new CheckInMemberDetailPresenter(
                mockNavigationManager, mockSessionManager, mockCheckInMemberDetailFragment,
                mockView, mockContext, mockMember,
                IdentificationEvent.SearchMethodEnum.SCAN_BARCODE, mockThroughMember);
        checkInMemberDetailPresenter.preFillIdentificationEventFields();

        IdentificationEvent idEvent = checkInMemberDetailPresenter.getUnsavedIdentificationEvent();
        assertEquals(idEvent.getMember(), mockMember);
        assertEquals(idEvent.getSearchMethod(), IdentificationEvent.SearchMethodEnum.SCAN_BARCODE);
        assertEquals(idEvent.getThroughMember(), mockThroughMember);
        assertFalse(idEvent.getPhotoVerified());
    }

    @Test
    public void preFillIdentificationEventFields_memberWithPhoto() {
        when(mockMember.getPhoto()).thenReturn(new byte[5]);
        checkInMemberDetailPresenter = new CheckInMemberDetailPresenter(
                mockNavigationManager, mockSessionManager, mockCheckInMemberDetailFragment,
                mockView, mockContext, mockMember, null, null);
        checkInMemberDetailPresenter.preFillIdentificationEventFields();

        IdentificationEvent idEvent = checkInMemberDetailPresenter.getUnsavedIdentificationEvent();
        assertEquals(idEvent.getMember(), mockMember);
        assertEquals(idEvent.getSearchMethod(), null);
        assertEquals(idEvent.getThroughMember(), null);
        assertEquals(idEvent.getPhotoVerified(), DEFAULT_IDENTIFICATION_EVENT_PHOTO_VERIFIED_FIELD);
    }

    @Test
    public void setMemberActionButton() {
        CheckInMemberDetailPresenter checkInMemberDetailPresenterSpy = spy(checkInMemberDetailPresenter);
        when(checkInMemberDetailPresenterSpy.getMemberActionButton()).thenReturn(mockButton);

        checkInMemberDetailPresenterSpy.setMemberActionButton();

        verify(mockButton, times(1)).setText(R.string.check_in);
        verify(mockButton, times(1)).setOnClickListener(any(View.OnClickListener.class));
    }

    @Test
    public void setBottomListView_noOtherHouseholdMembers() {
        CheckInMemberDetailPresenter checkInMemberDetailPresenterSpy = spy(checkInMemberDetailPresenter);

        doReturn(null).when(checkInMemberDetailPresenterSpy).getMembersForBottomListView();

        doNothing().when(checkInMemberDetailPresenterSpy).setBottomListWithMembers(any(List.class));

        checkInMemberDetailPresenterSpy.setBottomListView();

        verify(checkInMemberDetailPresenterSpy, never()).setBottomListWithMembers(null);
    }

    @Test
    public void setBottomListView_hasOtherHouseholdMembers() {
        CheckInMemberDetailPresenter checkInMemberDetailPresenterSpy = spy(checkInMemberDetailPresenter);

        doReturn(mockMemberList).when(checkInMemberDetailPresenterSpy).getMembersForBottomListView();

        doNothing().when(checkInMemberDetailPresenterSpy).setBottomListWithMembers(any(List.class));

        checkInMemberDetailPresenterSpy.setBottomListView();

        verify(checkInMemberDetailPresenterSpy, times(1)).setBottomListWithMembers(mockMemberList);
    }

    @Test
    public void setBottomListWithMembers() throws Exception {
        CheckInMemberDetailPresenter checkInMemberDetailPresenterSpy = spy(checkInMemberDetailPresenter);

        doReturn(5).when(mockMemberList).size();
        TextView mockTextView = mock(TextView.class);
        ListView mockListView = mock(ListView.class);
        MemberAdapter mockMemberAdapter = mock(MemberAdapter.class);

        when(checkInMemberDetailPresenterSpy.getHouseholdMembersLabelTextView()).thenReturn(mockTextView);
        when(checkInMemberDetailPresenterSpy.getHouseholdMembersListView()).thenReturn(mockListView);
        when(checkInMemberDetailPresenterSpy.getContext()).thenReturn(mockContext);
        doReturn("6 Household Members").when(checkInMemberDetailPresenterSpy).formatQuantityStringFromHouseholdSize(6);
        whenNew(MemberAdapter.class).withAnyArguments().thenReturn(mockMemberAdapter);

        checkInMemberDetailPresenterSpy.setBottomListWithMembers(mockMemberList);

        verify(checkInMemberDetailPresenterSpy, times(1)).formatQuantityStringFromHouseholdSize(6);
        verify(mockTextView, times(1)).setText("6 Household Members");
        verify(mockListView, times(1)).setAdapter(any(MemberAdapter.class));
        verify(mockListView, times(1)).setOnItemClickListener(any(AdapterView.OnItemClickListener.class));
    }

    @Test
    public void getMembersForBottomListView_reportsException() throws Exception {
        CheckInMemberDetailPresenter checkInMemberDetailPresenterSpy = spy(checkInMemberDetailPresenter);
        SQLException mockException = mock(SQLException.class);

        when(MemberDao.getRemainingHouseholdMembers(any(UUID.class), any(UUID.class))).thenThrow(mockException);

        List<Member> result = checkInMemberDetailPresenterSpy.getMembersForBottomListView();
        assertNull(result);

        verifyStatic();
        ExceptionManager.reportException(mockException);
    }

    @Test
    public void getMembersForBottomListView_returnsMemberList() throws Exception {
        CheckInMemberDetailPresenter checkInMemberDetailPresenterSpy = spy(checkInMemberDetailPresenter);

        when(MemberDao.getRemainingHouseholdMembers(any(UUID.class), any(UUID.class))).thenReturn(mockMemberList);

        List<Member> result = checkInMemberDetailPresenterSpy.getMembersForBottomListView();
        assertEquals(result, mockMemberList);

        verifyStatic(never());
        ExceptionManager.reportException(any(Exception.class));
    }
}

