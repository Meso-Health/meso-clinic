package org.watsi.uhp.helpers;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.simprints.libsimprints.Constants;
import com.simprints.libsimprints.Metadata;
import com.simprints.libsimprints.Registration;
import com.simprints.libsimprints.SimHelper;
import com.simprints.libsimprints.Verification;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.UUID;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ SimHelper.class, Metadata.class })
public class SimprintsHelperTest {
    private SimprintsHelper simprintsHelper;

    @Mock
    SimHelper mockSimHelper;

    @Mock
    Fragment mockFragment;

    @Mock
    Intent mockIntent;

    @Mock
    Metadata mockMetadata;

    @Mock
    Registration mockRegistration;

    @Mock
    Verification mockVerification;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockStatic(SimHelper.class);
        mockStatic(Metadata.class);
        simprintsHelper = new SimprintsHelper("FakeUserName", mockFragment);
    }

    @Test
    public void enroll_validIntent() throws Exception {
        SimprintsHelper simPrintsHelperSpy = spy(simprintsHelper);

        when(simPrintsHelperSpy.getSimHelper()).thenReturn(mockSimHelper);
        when(mockSimHelper.register(any(String.class), any(Metadata.class))).thenReturn(mockIntent);
        doReturn(true).when(simPrintsHelperSpy).validIntent(mockIntent);
        doReturn(mockMetadata).when(simPrintsHelperSpy).createMetadataWithMemberId(any(String.class));
        whenNew(Metadata.class).withAnyArguments().thenReturn(mockMetadata);

        simPrintsHelperSpy.enroll("providerId", "memberId");

        verify(mockFragment, times(1)).startActivityForResult(mockIntent, SimprintsHelper.SIMPRINTS_ENROLLMENT_INTENT);
    }

    @Test(expected=SimprintsHelper.SimprintsInvalidIntentException.class)
    public void enroll_inValidIntent() throws Exception {
        SimprintsHelper simPrintsHelperSpy = spy(simprintsHelper);

        when(simPrintsHelperSpy.getSimHelper()).thenReturn(mockSimHelper);
        when(mockSimHelper.register(any(String.class), any(Metadata.class))).thenReturn(mockIntent);
        doReturn(false).when(simPrintsHelperSpy).validIntent(mockIntent);
        doReturn(mockMetadata).when(simPrintsHelperSpy).createMetadataWithMemberId(any(String.class));
        whenNew(Metadata.class).withAnyArguments().thenReturn(mockMetadata);

        simPrintsHelperSpy.enroll("providerId", "memberId");
    }

    @Test
    public void verify_validIntent() throws Exception {
        SimprintsHelper simPrintsHelperSpy = spy(simprintsHelper);

        when(simPrintsHelperSpy.getSimHelper()).thenReturn(mockSimHelper);
        when(mockSimHelper.verify(any(String.class), any(String.class))).thenReturn(mockIntent);
        doReturn(true).when(simPrintsHelperSpy).validIntent(mockIntent);

        simPrintsHelperSpy.verify("providerId", UUID.randomUUID());

        verify(mockFragment, times(1)).startActivityForResult(mockIntent, SimprintsHelper.SIMPRINTS_VERIFICATION_INTENT);
    }

    @Test(expected=SimprintsHelper.SimprintsInvalidIntentException.class)
    public void verify_invalidIntent() throws Exception {
        SimprintsHelper simPrintsHelperSpy = spy(simprintsHelper);

        when(simPrintsHelperSpy.getSimHelper()).thenReturn(mockSimHelper);
        when(mockSimHelper.verify(any(String.class), any(String.class))).thenReturn(mockIntent);
        doReturn(false).when(simPrintsHelperSpy).validIntent(mockIntent);

        simPrintsHelperSpy.verify("providerId", UUID.randomUUID());

        verify(mockFragment, times(1)).startActivityForResult(mockIntent, SimprintsHelper.SIMPRINTS_VERIFICATION_INTENT);
    }

    @Test(expected=SimprintsHelper.SimprintsInvalidIntentException.class)
    public void onActivityResultFromEnroll_invalidIntent() throws Exception {
        SimprintsHelper simPrintsHelperSpy = spy(simprintsHelper);
        simPrintsHelperSpy.onActivityResultFromEnroll(SimprintsHelper.SIMPRINTS_VERIFICATION_INTENT, 0, null);
    }

    @Test(expected=SimprintsHelper.SimprintsRegistrationError.class)
    public void onActivityResultFromEnroll_noRegistration() throws Exception {
        SimprintsHelper simPrintsHelperSpy = spy(simprintsHelper);

        when(mockIntent.getParcelableExtra(Constants.SIMPRINTS_REGISTRATION)).thenReturn(null);
        simPrintsHelperSpy.onActivityResultFromEnroll(SimprintsHelper.SIMPRINTS_ENROLLMENT_INTENT, Constants.SIMPRINTS_OK, mockIntent);
    }

    @Test
    public void onActivityResultFromEnroll_validRegistration() throws Exception {
        SimprintsHelper simPrintsHelperSpy = spy(simprintsHelper);
        UUID expectedUUID = UUID.randomUUID();

        when(mockIntent.getParcelableExtra(Constants.SIMPRINTS_REGISTRATION)).thenReturn(mockRegistration);
        when(mockRegistration.getGuid()).thenReturn(expectedUUID.toString());
        UUID result = simPrintsHelperSpy.onActivityResultFromEnroll(SimprintsHelper.SIMPRINTS_ENROLLMENT_INTENT, Constants.SIMPRINTS_OK, mockIntent);
        assertEquals(result, expectedUUID);
    }

    @Test
    public void onActivityResultFromEnroll_scanCancelled() throws Exception {
        SimprintsHelper simPrintsHelperSpy = spy(simprintsHelper);

        when(mockIntent.getParcelableExtra(Constants.SIMPRINTS_REGISTRATION)).thenReturn(mockRegistration);
        UUID result = simPrintsHelperSpy.onActivityResultFromEnroll(SimprintsHelper.SIMPRINTS_ENROLLMENT_INTENT, Constants.SIMPRINTS_CANCELLED, mockIntent);
        assertNull(result);
    }

    @Test(expected=SimprintsHelper.SimprintsErrorResultCodeException.class)
    public void onActivityResultFromEnroll_otherResultCode() throws Exception {
        SimprintsHelper simPrintsHelperSpy = spy(simprintsHelper);

        when(mockIntent.getParcelableExtra(Constants.SIMPRINTS_REGISTRATION)).thenReturn(mockRegistration);
        UUID result = simPrintsHelperSpy.onActivityResultFromEnroll(SimprintsHelper.SIMPRINTS_ENROLLMENT_INTENT, Constants.SIMPRINTS_MISSING_VERIFY_GUID, mockIntent);
        assertNull(result);
    }

    @Test(expected=SimprintsHelper.SimprintsInvalidIntentException.class)
    public void onActivityResultFromVerify_invalidIntent() throws Exception {
        SimprintsHelper simPrintsHelperSpy = spy(simprintsHelper);
        simPrintsHelperSpy.onActivityResultFromVerify(SimprintsHelper.SIMPRINTS_ENROLLMENT_INTENT, 0, null);
    }

    @Test
    public void onActivityResultFromVerify_simprintsOK() throws Exception {
        SimprintsHelper simPrintsHelperSpy = spy(simprintsHelper);
        when(mockIntent.getParcelableExtra(Constants.SIMPRINTS_VERIFICATION)).thenReturn(mockVerification);
        Verification verification = simPrintsHelperSpy.onActivityResultFromVerify(SimprintsHelper.SIMPRINTS_VERIFICATION_INTENT, Constants.SIMPRINTS_OK, mockIntent);
        assertEquals(verification, mockVerification);
    }

    @Test
    public void onActivityResultFromVerify_scanCancelled() throws Exception {
        SimprintsHelper simPrintsHelperSpy = spy(simprintsHelper);
        Verification verification = simPrintsHelperSpy.onActivityResultFromVerify(SimprintsHelper.SIMPRINTS_VERIFICATION_INTENT, Constants.SIMPRINTS_CANCELLED, mockIntent);
        assertNull(verification);
    }

    @Test(expected=SimprintsHelper.SimprintsErrorResultCodeException.class)
    public void onActivityResultFromVerify_otherResultCode() throws Exception {
        SimprintsHelper simPrintsHelperSpy = spy(simprintsHelper);
        Verification verification = simPrintsHelperSpy.onActivityResultFromVerify(SimprintsHelper.SIMPRINTS_VERIFICATION_INTENT, Constants.SIMPRINTS_VERIFY_GUID_NOT_FOUND_OFFLINE, mockIntent);
        assertNull(verification);
    }
}
