package org.watsi.uhp.models;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.watsi.uhp.api.ApiService;
import org.watsi.uhp.api.UhpApi;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ApiService.class, IdentificationEvent.class })
public class IdentificationEventTest {

    @Mock
    Context mockContext;
    @Mock
    UhpApi mockApi;

    private IdentificationEvent identificationEvent;

    @Before
    public void setup() {
        mockStatic(ApiService.class);
        when(ApiService.requestBuilder(mockContext)).thenReturn(mockApi);
        identificationEvent = new IdentificationEvent();
    }

    @Test
    public void getFormattedClinicNumber() throws Exception {
        identificationEvent.setClinicNumber(50);
        identificationEvent.setClinicNumberType(IdentificationEvent.ClinicNumberTypeEnum.OPD);
        assertEquals(identificationEvent.getFormattedClinicNumber(), "50");

        identificationEvent.setClinicNumber(50);
        identificationEvent.setClinicNumberType(IdentificationEvent.ClinicNumberTypeEnum.DELIVERY);
        assertEquals(identificationEvent.getFormattedClinicNumber(), "D50");
    }

    @Test
    public void getFormattedDismissalReasons() throws Exception {
        String[] formattedDismissalReasons = IdentificationEvent.getFormattedDismissalReasons();

        assertEquals(formattedDismissalReasons.length, 4);
        assertEquals(formattedDismissalReasons[0], "Member on other phone");
        assertEquals(formattedDismissalReasons[1], "Accidental identification");
        assertEquals(formattedDismissalReasons[2], "Member left before care");
        assertEquals(formattedDismissalReasons[3], "Member left after care");
    }

    @Test
    public void setDismissalReason() throws Exception {
        IdentificationEvent.DismissalReasonEnum reason =
                IdentificationEvent.DismissalReasonEnum.ACCIDENTAL_IDENTIFICATION;

        identificationEvent.setDismissalReason(reason);

        assertEquals(identificationEvent.getDismissalReason(), reason);
        assertTrue(identificationEvent.getDismissed());
    }
}
