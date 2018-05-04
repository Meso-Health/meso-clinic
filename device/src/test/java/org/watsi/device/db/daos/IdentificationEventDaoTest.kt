package org.watsi.device.db.daos

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.watsi.device.factories.EncounterModelFactory
import org.watsi.device.factories.IdentificationEventModelFactory
import org.watsi.device.factories.MemberModelFactory

class IdentificationEventDaoTest : DaoBaseTest() {

    @Test
    fun openCheckIn() {
        val memberWithOpenCheckIn = MemberModelFactory.create(memberDao)
        val memberWithDismissedCheckIn = MemberModelFactory.create(memberDao)
        val memberWithEncounter = MemberModelFactory.create(memberDao)
        val memberWithNoCheckIn = MemberModelFactory.create(memberDao)

        // open identification event
        val openCheckIn = IdentificationEventModelFactory.create(identificationEventDao,
                memberId = memberWithOpenCheckIn.id,
                accepted = true,
                dismissed = false)

        // dismissed identification event
        IdentificationEventModelFactory.create(identificationEventDao,
                memberId = memberWithDismissedCheckIn.id,
                accepted = true,
                dismissed = true)

        // open identification event but with corresponding encounter
        val idEventWithEncounter = IdentificationEventModelFactory.create(identificationEventDao,
                memberId = memberWithEncounter.id,
                accepted = true,
                dismissed = false)
        EncounterModelFactory.create(encounterDao, identificationEventId = idEventWithEncounter.id)

        assertEquals(openCheckIn, identificationEventDao.openCheckIn(memberWithOpenCheckIn.id).blockingGet())
        assertNull(identificationEventDao.openCheckIn(memberWithDismissedCheckIn.id).blockingGet())
        assertNull(identificationEventDao.openCheckIn(memberWithEncounter.id).blockingGet())
        assertNull(identificationEventDao.openCheckIn(memberWithNoCheckIn.id).blockingGet())
    }
}
