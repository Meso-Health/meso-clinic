package org.watsi.domain.entities

import org.junit.Assert.assertEquals
import org.junit.Test
import org.watsi.domain.factories.IdentificationEventFactory

class IdentificationEventTest {
    @Test
    fun formatClinicNumber() {
        val delivery = IdentificationEventFactory.build(clinicNumber = 0,
                clinicNumberType = IdentificationEvent.ClinicNumberType.DELIVERY)
        val outpatient = IdentificationEventFactory.build(clinicNumber = 2334,
                clinicNumberType = IdentificationEvent.ClinicNumberType.OPD)
        assertEquals(delivery.formatClinicNumber(), "D0")
        assertEquals(outpatient.formatClinicNumber(), "2334")
    }
}
