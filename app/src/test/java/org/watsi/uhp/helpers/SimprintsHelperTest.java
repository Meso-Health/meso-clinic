package org.watsi.uhp.helpers;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.simprints.libsimprints.Metadata;
import com.simprints.libsimprints.SimHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.UUID;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertThat;
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
        simPrintsHelperSpy.onActivityResultFromEnroll(SimprintsHelper.SIMPRINTS_ENROLLMENT_INTENT + 5, 0, null);
    }
}
