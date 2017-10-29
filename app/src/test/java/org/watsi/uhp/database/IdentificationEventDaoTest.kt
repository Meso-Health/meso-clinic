package org.watsi.uhp.database

import org.junit.Assert.assertEquals
import org.junit.Test
import org.watsi.uhp.managers.Clock
import org.watsi.uhp.models.Encounter
import org.watsi.uhp.models.IdentificationEvent
import org.watsi.uhp.models.Member
import java.util.*

class IdentificationEventDaoTest: DaoTest() {

    @Test
    fun openCheckIn() {
        val checkedInMember = Member()
        checkedInMember.id = UUID.randomUUID()
        val otherMember = Member()
        otherMember.id = UUID.randomUUID()

        createIdentificationEvent(checkedInMember, false, false) // unaccepted id event
        createIdentificationEvent(checkedInMember, true, true) // dismissed id event
        createIdentificationEvent(otherMember, true, false) // other member open check in
        createEncounter(checkedInMember, createIdentificationEvent(checkedInMember, true, false)) // check in with encounter
        val openCheckIn = createIdentificationEvent(checkedInMember, true, false)

        assertEquals(openCheckIn.id, IdentificationEventDao.openCheckIn(checkedInMember.id).id)
    }

    private fun createIdentificationEvent(member: Member,
                                          accepted: Boolean,
                                          dismissed: Boolean): IdentificationEvent {
        val idEvent = IdentificationEvent(member, IdentificationEvent.SearchMethodEnum.SEARCH_ID, null)
        idEvent.id = UUID.randomUUID()
        idEvent.accepted = accepted
        idEvent.dismissed = dismissed
        idEvent.occurredAt = Clock.getCurrentTime()
        idEvent.create()
        return idEvent
    }

    private fun createEncounter(member: Member, idEvent: IdentificationEvent) {
        val encounter = Encounter()
        encounter.occurredAt = Clock.getCurrentTime()
        encounter.member = member
        encounter.identificationEvent = idEvent
        encounter.saveChanges(null)
    }
}