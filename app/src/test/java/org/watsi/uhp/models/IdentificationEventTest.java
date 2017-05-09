package org.watsi.uhp.models;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class IdentificationEventTest {

    private IdentificationEvent identificationEvent;

    @Before
    public void setup() {
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

        assertEquals(formattedDismissalReasons.length, 3);
        assertEquals(formattedDismissalReasons[0], "Accidental identification");
        assertEquals(formattedDismissalReasons[1], "Patient left before care");
        assertEquals(formattedDismissalReasons[2], "Patient left after care");
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
