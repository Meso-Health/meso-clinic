package org.watsi.device.db.daos

import org.junit.Assert.assertEquals
import org.junit.Test
import org.watsi.device.factories.EncounterModelFactory
import org.watsi.device.factories.IdentificationEventModelFactory
import org.watsi.device.factories.MemberModelFactory
import java.util.UUID

class MemberDaoTest : DaoBaseTest() {

    @Test
    fun checkedInMembers() {
        val memberWithOpenCheckIn = MemberModelFactory.create(memberDao)
        val memberWithDismissedCheckIn = MemberModelFactory.create(memberDao)
        val memberWithEncounter = MemberModelFactory.create(memberDao)
        MemberModelFactory.create(memberDao)

        // open identification event
        IdentificationEventModelFactory.create(identificationEventDao,
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

        val checkedInMembers = memberDao.checkedInMembers().blockingFirst()

        assertEquals(listOf(memberWithOpenCheckIn), checkedInMembers)
    }

    @Test
    fun remainingHouseholdMembers() {
        val householdId = UUID.randomUUID()
        val householdMember1 = MemberModelFactory.create(memberDao, householdId = householdId)
        val householdMember2 = MemberModelFactory.create(memberDao, householdId = householdId)
        val householdMember3 = MemberModelFactory.create(memberDao, householdId = householdId)
        MemberModelFactory.create(memberDao)

        val remainingHouseholdMembers =
                memberDao.remainingHouseholdMembers(householdId, householdMember1.id).blockingFirst()

        assertEquals(2, remainingHouseholdMembers.size)
        assert(remainingHouseholdMembers.contains(householdMember2))
        assert(remainingHouseholdMembers.contains(householdMember3))
    }

    @Test
    fun needPhotoDownload() {
        val needsPhoto = MemberModelFactory.create(memberDao, photoUrl = "foo", thumbnailPhotoId = null)
        // photo downloaded
        MemberModelFactory.create(memberDao, photoUrl = "foo", thumbnailPhotoId = UUID.randomUUID())
        // does not have photo
        MemberModelFactory.create(memberDao, photoUrl = null)

        val needPhotoDownload = memberDao.needPhotoDownload().blockingGet()

        assertEquals(listOf(needsPhoto), needPhotoDownload)
    }
}
