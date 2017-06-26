package org.watsi.uhp.presenters;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.R;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.fragments.CheckInMemberDetailFragment;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.managers.SessionManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.util.List;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by michaelliang on 6/13/17.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({ MemberDao.class, CheckInMemberDetailPresenter.class, ExceptionManager.class })
public class CheckInMemberDetailPresenterTest {

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

    @Mock
    IdentificationEvent mockIdentificationEvent;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockStatic(MemberDao.class);
        mockStatic(ExceptionManager.class);
        checkInMemberDetailPresenter = new CheckInMemberDetailPresenter(
                mockNavigationManager, mockSessionManager, mockCheckInMemberDetailFragment,
                mockView, mockContext, mockMember, mockIdentificationEvent);
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
    public void setMemberSecondaryActionButton_absentee() {
        CheckInMemberDetailPresenter checkInMemberDetailPresenterSpy = spy(checkInMemberDetailPresenter);
        when(mockMember.isAbsentee()).thenReturn(true);
        doNothing().when(checkInMemberDetailPresenterSpy).setMemberSecondaryButtonProperties(
                any(String.class), any(Boolean.class), any(View.OnClickListener.class));

        checkInMemberDetailPresenterSpy.setMemberSecondaryActionButton();

        verify(checkInMemberDetailPresenterSpy, times(1)).setMemberSecondaryButtonProperties(
                eq("Complete Enrollment"), eq(false), any(View.OnClickListener.class));
        verify(checkInMemberDetailPresenterSpy, never()).setMemberSecondaryButtonProperties(
                eq("Scan"), eq(true), any(View.OnClickListener.class));
    }

    @Test
    public void setMemberSecondaryActionButton_needToScanFingerprint() {
        CheckInMemberDetailPresenter checkInMemberDetailPresenterSpy = spy(checkInMemberDetailPresenter);
        when(mockMember.isAbsentee()).thenReturn(false);
        when(mockMember.getFingerprintsGuid()).thenReturn(UUID.randomUUID());
        when(mockIdentificationEvent.getFingerprintsVerificationTier()).thenReturn(null);

        doNothing().when(checkInMemberDetailPresenterSpy).setMemberSecondaryButtonProperties(
                any(String.class), any(Boolean.class), any(View.OnClickListener.class));

        checkInMemberDetailPresenterSpy.setMemberSecondaryActionButton();

        verify(checkInMemberDetailPresenterSpy, never()).setMemberSecondaryButtonProperties(
                eq("Complete Enrollment"), eq(false), any(View.OnClickListener.class));
        verify(checkInMemberDetailPresenterSpy, times(1)).setMemberSecondaryButtonProperties(
                eq("Scan"), eq(true), any(View.OnClickListener.class));
    }

    @Test
    public void setMemberSecondaryActionButton_underSix() {
        CheckInMemberDetailPresenter checkInMemberDetailPresenterSpy = spy(checkInMemberDetailPresenter);
        when(mockMember.isAbsentee()).thenReturn(false);
        when(mockMember.getFingerprintsGuid()).thenReturn(null);
        when(mockIdentificationEvent.getFingerprintsVerificationTier()).thenReturn(null);

        doNothing().when(checkInMemberDetailPresenterSpy).setMemberSecondaryButtonProperties(
                any(String.class), any(Boolean.class), any(View.OnClickListener.class));

        checkInMemberDetailPresenterSpy.setMemberSecondaryActionButton();

        verify(checkInMemberDetailPresenterSpy, never()).setMemberSecondaryButtonProperties(
                eq("Complete Enrollment"), eq(false), any(View.OnClickListener.class));
        verify(checkInMemberDetailPresenterSpy, never()).setMemberSecondaryButtonProperties(
                eq("Scan"), eq(true), any(View.OnClickListener.class));
    }

    @Test
    public void setMemberSecondaryActionButton_alreadyScanned() {
        CheckInMemberDetailPresenter checkInMemberDetailPresenterSpy = spy(checkInMemberDetailPresenter);
        when(mockMember.isAbsentee()).thenReturn(false);
        when(mockMember.getFingerprintsGuid()).thenReturn(UUID.randomUUID());
        when(mockIdentificationEvent.getFingerprintsVerificationTier()).thenReturn("TIER_5");

        doNothing().when(checkInMemberDetailPresenterSpy).setMemberSecondaryButtonProperties(
                any(String.class), any(Boolean.class), any(View.OnClickListener.class));

        checkInMemberDetailPresenterSpy.setMemberSecondaryActionButton();

        verify(checkInMemberDetailPresenterSpy, never()).setMemberSecondaryButtonProperties(
                eq("Complete Enrollment"), eq(false), any(View.OnClickListener.class));
        verify(checkInMemberDetailPresenterSpy, never()).setMemberSecondaryButtonProperties(
                eq("Scan"), eq(true), any(View.OnClickListener.class));
    }
}

