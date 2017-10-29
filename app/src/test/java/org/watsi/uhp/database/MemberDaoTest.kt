package org.watsi.uhp.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.watsi.uhp.managers.Clock
import org.watsi.uhp.models.Encounter
import org.watsi.uhp.models.IdentificationEvent
import org.watsi.uhp.models.Member
import java.sql.SQLException
import java.util.*

class MemberDaoTest: DaoTest() {

    @Test
    fun findByCardId() {
        val member = createMember("foo", "RWI123456")
        createMember("bar", "RWI123457")

        val returnedMember = MemberDao.findByCardId(member.cardId)

        assertEquals(member.id, returnedMember.id)
    }

    @Test
    fun findByCardId_queryIncludesSpaces() {
        val member = createMember("foo", "RWI123456")
        createMember("bar", "RWI123457")

        val returnedMember = MemberDao.findByCardId("RWI 123 456")

        assertEquals(member.id, returnedMember.id)
    }

    @Test(expected = SQLException::class)
    fun findByCardId_noMemberWithCardExists_throwsSQLException() {
        MemberDao.findByCardId("RWI123456")
    }

    @Test
    fun withCardIdLike() {
        val member1 = createMember("foo", "RWI123456")
        val member2 = createMember("bar", "RWI123457")
        createMember("baz", "WTC123457")

        val returnedMemberIds = MemberDao.withCardIdLike("RWI 123").map { it.id }

        assertEquals(returnedMemberIds.size, 2)
        assertTrue(returnedMemberIds.contains(member1.id))
        assertTrue(returnedMemberIds.contains(member2.id))
    }

    @Test
    fun fuzzySearchMembers() {
        val member1 = createMember("Pete Johnson")
        val member2 = createMember("Rich Jackson")
        val member3 = createMember("John Smith")
        createMember("Xavier Wayland")

        val memberIds = MemberDao.fuzzySearchMembers("jon").map { it.id }

        assertEquals(memberIds.size, 3)
        assertTrue(memberIds.contains(member1.id))
        assertTrue(memberIds.contains(member2.id))
        assertTrue(memberIds.contains(member3.id))
    }

    @Test
    fun getCheckedInMembers() {
        createIdentificationEvent(createMember("Unaccepted ID Event"), false, false)
        createIdentificationEvent(createMember("Dismissed ID Event"), true, true)
        val checkedOutMember = createMember("foo")
        createEncounter(checkedOutMember, createIdentificationEvent(checkedOutMember, true, false))
        val checkedInMember = createMember("bar")
        createIdentificationEvent(checkedInMember, true, false)
        val checkedInMemberWithPrevEncounter = createMember("baz")
        createEncounter(checkedInMemberWithPrevEncounter,
                createIdentificationEvent(checkedInMemberWithPrevEncounter, true, false))
        createIdentificationEvent(checkedInMemberWithPrevEncounter, true, false)

        val checkedInMemberIds = MemberDao.getCheckedInMembers().map { it.id }

        assertEquals(checkedInMemberIds.size, 2)
        assertTrue(checkedInMemberIds.contains(checkedInMember.id))
        assertTrue(checkedInMemberIds.contains(checkedInMemberWithPrevEncounter.id))
    }

    @Test
    fun getRemainingHouseholdMembers() {
        val householdId = UUID.randomUUID()
        val member1 = createMember("Mom", "WTC123457", householdId, 60)
        val member2 = createMember("Dad", "WTC123456", householdId, 58)
        val member3 = createMember("Son", "WTC123458", householdId, 29)
        val member4 = createMember("Daughter", "WTC123459", householdId, 27)
        createMember("Different family", "WTC000000", UUID.randomUUID())

        val householdMemberIds = MemberDao.getRemainingHouseholdMembers(householdId, member4.id)

        assertEquals(householdMemberIds.size, 3)
        assertEquals(householdMemberIds[0].id, member1.id)
        assertEquals(householdMemberIds[1].id, member2.id)
        assertEquals(householdMemberIds[2].id, member3.id)
    }

    @Test
    fun membersWithPhotosToFetch() {
        createMemberByPhotoAttributes(null, null)
        createMemberByPhotoAttributes("remote", byteArrayOf(0xe0.toByte()))
        val memberWithPhotoToFetch = createMemberByPhotoAttributes("remote", null)

        val photoFetchMemberIds = MemberDao.membersWithPhotosToFetch().map { it.id }

        assertEquals(photoFetchMemberIds.size, 1)
        assertTrue(photoFetchMemberIds.contains(memberWithPhotoToFetch.id))
    }

    @Test
    fun allMemberIds() {
        val member1 = createMember("foo")
        val member2 = createMember("bar")
        val member3 = createMember("baz")

        val memberIds = MemberDao.allMemberIds()

        assertEquals(memberIds.size, 3)
        assertTrue(memberIds.contains(member1.id))
        assertTrue(memberIds.contains(member2.id))
        assertTrue(memberIds.contains(member3.id))
    }

    private fun createMemberByPhotoAttributes(photoUrl: String?, photoBytes: ByteArray?): Member {
        val member = buildMember("photo")
        member.remoteMemberPhotoUrl = photoUrl
        member.croppedPhotoBytes = photoBytes
        member.create()
        return member
    }

    private fun createMember(name: String,
                             cardId: String = "WTC000000",
                             householdId: UUID? = null,
                             age: Int = 18): Member {
        val member = buildMember(name, cardId, householdId, age)
        member.create()
        return member
    }

    private fun buildMember(name: String,
                            cardId: String = "WTC000000",
                            householdId: UUID? = null,
                            age: Int = 18): Member {
        val member = Member()
        member.id = UUID.randomUUID()
        member.fullName = name
        member.cardId = cardId
        member.householdId = householdId
        member.age = age
        return member
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