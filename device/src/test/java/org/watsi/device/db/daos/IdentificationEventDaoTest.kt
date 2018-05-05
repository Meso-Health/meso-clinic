package org.watsi.device.db.daos

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

        identificationEventDao.openCheckIn(memberWithOpenCheckIn.id).test().assertValue(openCheckIn)
        identificationEventDao.openCheckIn(memberWithDismissedCheckIn.id).test().assertNoValues()
        identificationEventDao.openCheckIn(memberWithEncounter.id).test().assertNoValues()
        identificationEventDao.openCheckIn(memberWithNoCheckIn.id).test().assertNoValues()
    }
}
