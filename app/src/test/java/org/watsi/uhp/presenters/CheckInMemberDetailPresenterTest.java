package org.watsi.uhp.presenters;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.simprints.libsimprints.Constants;

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
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

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

    @Mock
    Intent mockFingerprintIntentData;

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

    @Test
    public void setMemberIndicator_noScan() {
        CheckInMemberDetailPresenter checkInMemberDetailPresenterSpy = spy(checkInMemberDetailPresenter);
        when(mockIdentificationEvent.getFingerprintsVerificationTier()).thenReturn(null);

        doNothing().when(checkInMemberDetailPresenterSpy).setMemberIndicatorProperties(
                any(int.class), any(String.class));

        checkInMemberDetailPresenterSpy.setMemberSecondaryActionButton();

        verify(checkInMemberDetailPresenterSpy, never()).setMemberIndicatorProperties(
                eq(R.color.indicatorRed), eq("Bad Match"));
        verify(checkInMemberDetailPresenterSpy, never()).setMemberIndicatorProperties(
                eq(R.color.indicatorGreen), eq("Good Match"));
    }

    @Test
    public void setMemberIndicator_tierFiveScan() {
        CheckInMemberDetailPresenter checkInMemberDetailPresenterSpy = spy(checkInMemberDetailPresenter);
        when(mockIdentificationEvent.getFingerprintsVerificationTier()).thenReturn("TIER_5");

        doNothing().when(checkInMemberDetailPresenterSpy).setMemberIndicatorProperties(
                any(int.class), any(String.class));

        checkInMemberDetailPresenterSpy.setMemberIndicator();

        verify(checkInMemberDetailPresenterSpy, times(1)).setMemberIndicatorProperties(
                eq(R.color.indicatorRed), eq("Bad Match"));
        verify(checkInMemberDetailPresenterSpy, never()).setMemberIndicatorProperties(
                eq(R.color.indicatorGreen), eq("Good Match"));
    }

    @Test
    public void setMemberIndicator_tierOneScan() {
        CheckInMemberDetailPresenter checkInMemberDetailPresenterSpy = spy(checkInMemberDetailPresenter);
        when(mockIdentificationEvent.getFingerprintsVerificationTier()).thenReturn("TIER_1");

        doNothing().when(checkInMemberDetailPresenterSpy).setMemberIndicatorProperties(
                any(int.class), any(String.class));

        checkInMemberDetailPresenterSpy.setMemberIndicator();

        verify(checkInMemberDetailPresenterSpy, never()).setMemberIndicatorProperties(
                eq(R.color.indicatorRed), eq("Bad Match"));
        verify(checkInMemberDetailPresenterSpy, times(1)).setMemberIndicatorProperties(
                eq(R.color.indicatorGreen), eq("Good Match"));
    }

    @Test
    public void handleOnActivityResult_badIntent() {
        CheckInMemberDetailPresenter checkInMemberDetailPresenterSpy = spy(checkInMemberDetailPresenter);

        checkInMemberDetailPresenterSpy.handleOnActivityResult(
                CheckInMemberDetailPresenter.SIMPRINTS_VERIFICATION_INTENT + 1,
                Constants.SIMPRINTS_OK,
                mockFingerprintIntentData);

        verifyStatic();
        ExceptionManager.reportException(any(IllegalStateException.class));
    }

    @Test
    public void handleOnActivityResult_simprintsCancelled() {
        CheckInMemberDetailPresenter checkInMemberDetailPresenterSpy = spy(checkInMemberDetailPresenter);
        doNothing().when(checkInMemberDetailPresenterSpy).showScanFailedToast();

        checkInMemberDetailPresenterSpy.handleOnActivityResult(
                CheckInMemberDetailPresenter.SIMPRINTS_VERIFICATION_INTENT,
                Constants.SIMPRINTS_CANCELLED,
                mockFingerprintIntentData);

        verify(checkInMemberDetailPresenterSpy, times(1)).showScanFailedToast();
        verify(mockIdentificationEvent, times(1)).setFingerprintsVerificationResultCode(Constants.SIMPRINTS_CANCELLED);
    }

    @Test
    public void handleOnActivityResult_simprintsOK() {
        CheckInMemberDetailPresenter checkInMemberDetailPresenterSpy = spy(checkInMemberDetailPresenter);
        doNothing().when(checkInMemberDetailPresenterSpy).showScanSuccessfulToast();
        doNothing().when(checkInMemberDetailPresenterSpy).saveIdentificationEventWithVerificationData(any(Intent.class));
        doNothing().when(checkInMemberDetailPresenterSpy).navigateToCheckInMemberDetailFragment();

        checkInMemberDetailPresenterSpy.handleOnActivityResult(
                CheckInMemberDetailPresenter.SIMPRINTS_VERIFICATION_INTENT,
                Constants.SIMPRINTS_OK,
                mockFingerprintIntentData);

        verify(checkInMemberDetailPresenterSpy, times(1)).showScanSuccessfulToast();
        verify(checkInMemberDetailPresenterSpy, times(1)).saveIdentificationEventWithVerificationData(mockFingerprintIntentData);
        verify(checkInMemberDetailPresenterSpy, times(1)).navigateToCheckInMemberDetailFragment();
        verify(mockIdentificationEvent, times(1)).setFingerprintsVerificationResultCode(Constants.SIMPRINTS_OK);
    }

    @Test
    public void handleOnActivityResult_otherSimprintsResultCode() {
        CheckInMemberDetailPresenter checkInMemberDetailPresenterSpy = spy(checkInMemberDetailPresenter);
        doNothing().when(checkInMemberDetailPresenterSpy).navigateToCheckInMemberDetailFragment();

        checkInMemberDetailPresenterSpy.handleOnActivityResult(
                CheckInMemberDetailPresenter.SIMPRINTS_VERIFICATION_INTENT,
                Constants.SIMPRINTS_MISSING_VERIFY_GUID,
                mockFingerprintIntentData);

        verify(checkInMemberDetailPresenterSpy, times(1)).navigateToCheckInMemberDetailFragment();
        verify(mockIdentificationEvent, times(1)).setFingerprintsVerificationResultCode(Constants.SIMPRINTS_MISSING_VERIFY_GUID);
    }
}

