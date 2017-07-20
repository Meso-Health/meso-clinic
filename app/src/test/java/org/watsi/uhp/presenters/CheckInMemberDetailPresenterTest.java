package org.watsi.uhp.presenters;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.Verification;

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
import org.watsi.uhp.helpers.SimprintsHelper;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.managers.SessionManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.util.List;

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
@PrepareForTest({ MemberDao.class, CheckInMemberDetailPresenter.class, ExceptionManager.class, ContextCompat.class})
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

    @Mock
    Verification mockVerification;

    @Mock
    SimprintsHelper mockSimprintsHelper;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockStatic(MemberDao.class);
        mockStatic(ExceptionManager.class);
        mockStatic(ContextCompat.class);
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
    public void setScanResult_noScan() {
        CheckInMemberDetailPresenter checkInMemberDetailPresenterSpy = spy(checkInMemberDetailPresenter);
        when(mockIdentificationEvent.getFingerprintsVerificationTier()).thenReturn(null);

        doNothing().when(checkInMemberDetailPresenterSpy).setScanResultProperties(
                any(int.class), any(int.class));

        checkInMemberDetailPresenterSpy.setScanResult();

        verify(checkInMemberDetailPresenterSpy, never()).setScanResultProperties(
                eq(R.color.indicatorRed), eq(R.string.bad_scan_indicator));
        verify(checkInMemberDetailPresenterSpy, never()).setScanResultProperties(
                eq(R.color.indicatorGreen), eq(R.string.good_scan_indicator));
    }

    @Test
    public void setScanResult_tierFiveScan() {
        CheckInMemberDetailPresenter checkInMemberDetailPresenterSpy = spy(checkInMemberDetailPresenter);
        when(mockIdentificationEvent.getFingerprintsVerificationTier()).thenReturn("TIER_5");
        when(ContextCompat.getColor(mockContext, R.color.indicatorRed)).thenReturn(R.color.indicatorRed);
        doNothing().when(checkInMemberDetailPresenterSpy).setScanResultProperties(
                any(int.class), any(int.class));

        checkInMemberDetailPresenterSpy.setScanResult();

        verify(checkInMemberDetailPresenterSpy, times(1)).setScanResultProperties(
                eq(R.color.indicatorRed), eq(R.string.bad_scan_indicator));
        verify(checkInMemberDetailPresenterSpy, never()).setScanResultProperties(
                eq(R.color.indicatorGreen), eq(R.string.good_scan_indicator));
        verify(checkInMemberDetailPresenterSpy, never()).setScanResultProperties(
                eq(R.color.indicatorNeutral), eq(R.string.no_scan_indicator));
    }

    @Test
    public void setScanResult_tierOneScan() {
        CheckInMemberDetailPresenter checkInMemberDetailPresenterSpy = spy(checkInMemberDetailPresenter);
        when(mockIdentificationEvent.getFingerprintsVerificationTier()).thenReturn("TIER_1");
        when(ContextCompat.getColor(mockContext, R.color.indicatorGreen)).thenReturn(R.color.indicatorGreen);
        doNothing().when(checkInMemberDetailPresenterSpy).setScanResultProperties(
                any(int.class), any(int.class));

        checkInMemberDetailPresenterSpy.setScanResult();

        verify(checkInMemberDetailPresenterSpy, never()).setScanResultProperties(
                eq(R.color.indicatorRed), eq(R.string.bad_scan_indicator));
        verify(checkInMemberDetailPresenterSpy, times(1)).setScanResultProperties(
                eq(R.color.indicatorGreen), eq(R.string.good_scan_indicator));
        verify(checkInMemberDetailPresenterSpy, never()).setScanResultProperties(
                eq(R.color.indicatorNeutral), eq(R.string.no_scan_indicator));
    }

    @Test
    public void setScanResult_simprintsErrorResultCode() {
        CheckInMemberDetailPresenter checkInMemberDetailPresenterSpy = spy(checkInMemberDetailPresenter);
        when(mockIdentificationEvent.getFingerprintsVerificationTier()).thenReturn(null);
        when(mockIdentificationEvent.getFingerprintsVerificationResultCode()).thenReturn(Constants.SIMPRINTS_MISSING_VERIFY_GUID);
        when(ContextCompat.getColor(mockContext, R.color.indicatorNeutral)).thenReturn(R.color.indicatorNeutral);
        doNothing().when(checkInMemberDetailPresenterSpy).setScanResultProperties(
                any(int.class), any(int.class));

        checkInMemberDetailPresenterSpy.setScanResult();

        verify(checkInMemberDetailPresenterSpy, never()).setScanResultProperties(
                eq(R.color.indicatorRed), eq(R.string.bad_scan_indicator));
        verify(checkInMemberDetailPresenterSpy, never()).setScanResultProperties(
                eq(R.color.indicatorGreen), eq(R.string.good_scan_indicator));
        verify(checkInMemberDetailPresenterSpy, times(1)).setScanResultProperties(
                eq(R.color.indicatorNeutral), eq(R.string.no_scan_indicator));
    }

    @Test
    public void handleOnActivityResult_badIntent() {
        CheckInMemberDetailPresenter checkInMemberDetailPresenterSpy = spy(checkInMemberDetailPresenter);
        doNothing().when(checkInMemberDetailPresenterSpy).showScanFailedToast();

        checkInMemberDetailPresenterSpy.handleOnActivityResult(
                SimprintsHelper.SIMPRINTS_VERIFICATION_INTENT + 1,
                Constants.SIMPRINTS_OK,
                mockFingerprintIntentData);

        verify(checkInMemberDetailPresenterSpy, times(1)).showScanFailedToast();
        verifyStatic();
        ExceptionManager.reportException(any(SimprintsHelper.SimprintsHelperException.class));
    }

    @Test
    public void handleOnActivityResult_nullVerification() throws Exception {
        CheckInMemberDetailPresenter checkInMemberDetailPresenterSpy = spy(checkInMemberDetailPresenter);
        when(mockSimprintsHelper.onActivityResultFromVerify(any(int.class), any(int.class), eq(mockFingerprintIntentData))).thenReturn(null);
        doNothing().when(checkInMemberDetailPresenterSpy).showScanFailedToast();

        checkInMemberDetailPresenterSpy.handleOnActivityResult(
                SimprintsHelper.SIMPRINTS_VERIFICATION_INTENT,
                Constants.SIMPRINTS_CANCELLED,
                mockFingerprintIntentData);

        verify(checkInMemberDetailPresenterSpy, times(1)).showScanFailedToast();
        verify(mockIdentificationEvent, times(1)).setFingerprintsVerificationResultCode(Constants.SIMPRINTS_CANCELLED);
    }

    @Test
    public void handleOnActivityResult_nonNullVerification() throws Exception {
        CheckInMemberDetailPresenter checkInMemberDetailPresenterSpy = spy(checkInMemberDetailPresenter);
        when(mockSimprintsHelper.onActivityResultFromVerify(any(int.class), any(int.class), any(Intent.class))).thenReturn(mockVerification);

        doNothing().when(checkInMemberDetailPresenterSpy).showScanSuccessfulToast();
        doNothing().when(checkInMemberDetailPresenterSpy).saveIdentificationEventWithVerificationData(mockVerification);

        checkInMemberDetailPresenterSpy.handleOnActivityResult(
                SimprintsHelper.SIMPRINTS_VERIFICATION_INTENT,
                Constants.SIMPRINTS_OK,
                mockFingerprintIntentData);

        verify(mockSimprintsHelper, times(1)).onActivityResultFromVerify(SimprintsHelper.SIMPRINTS_VERIFICATION_INTENT, Constants.SIMPRINTS_OK, mockFingerprintIntentData);
        verify(checkInMemberDetailPresenterSpy, times(1)).showScanSuccessfulToast();
        verify(checkInMemberDetailPresenterSpy, times(1)).saveIdentificationEventWithVerificationData(mockVerification);
        verify(mockIdentificationEvent, times(1)).setFingerprintsVerificationResultCode(Constants.SIMPRINTS_OK);
    }
}
