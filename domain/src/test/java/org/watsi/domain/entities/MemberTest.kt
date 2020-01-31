package org.watsi.domain.entities

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.watsi.domain.factories.MemberFactory
import java.util.UUID

class MemberTest {

    val today = LocalDate.now()
    val now = Instant.now()
    val fixedClock = Clock.fixed(now, ZoneId.of("UTC"))
    val tenYearsAgo = today.minusYears(10)

    @Test
    fun isAbsentee_noPhoto_isTrue() {
        val member = MemberFactory.build(thumbnailPhotoId = null, photoUrl = null)

        assertTrue(member.isAbsentee())
    }

    @Test
    fun isAbsentee_hasPhoto_isFalse() {
        val member = MemberFactory.build(thumbnailPhotoId = null, photoUrl = UUID.randomUUID().toString())

        assertFalse(member.isAbsentee())
    }

    @Test
    fun getAgeYears() {
        assertEquals(10, MemberFactory.build(birthdate = tenYearsAgo).getAgeYears(fixedClock))
    }

    @Test
    fun isValidFullName() {
        assertFalse(Member.isValidFullName("", 1))
        assertFalse(Member.isValidFullName("", 2))
        assertFalse(Member.isValidFullName("", 3))

        assertFalse(Member.isValidFullName("   ", 1))
        assertFalse(Member.isValidFullName("   ", 2))
        assertFalse(Member.isValidFullName("   ", 3))

        assertTrue(Member.isValidFullName("Michael", 1))
        assertFalse(Member.isValidFullName("Michael", 2))
        assertFalse(Member.isValidFullName("Michael", 3))

        assertTrue(Member.isValidFullName(" Michael ", 1))
        assertFalse(Member.isValidFullName(" Michael ", 2))
        assertFalse(Member.isValidFullName(" Michael ", 3))

        assertTrue(Member.isValidFullName("   Michael", 1))
        assertFalse(Member.isValidFullName("   Michael", 2))
        assertFalse(Member.isValidFullName("   Michael", 3))

        assertTrue(Member.isValidFullName("Michael   ", 1))
        assertFalse(Member.isValidFullName("Michael   ", 2))
        assertFalse(Member.isValidFullName("Michael   ", 3))

        assertTrue(Member.isValidFullName("Michael Jordan", 1))
        assertTrue(Member.isValidFullName("Michael Jordan", 2))
        assertFalse(Member.isValidFullName("Michael Jordan", 3))

        assertTrue(Member.isValidFullName("  Michael B. ", 1))
        assertTrue(Member.isValidFullName("  Michael B. ", 2))
        assertFalse(Member.isValidFullName("  Michael B. ", 3))

        assertTrue(Member.isValidFullName("Michael Bakari Jordan", 1))
        assertTrue(Member.isValidFullName("Michael Bakari Jordan", 2))
        assertTrue(Member.isValidFullName("Michael Bakari Jordan", 3))

        assertTrue(Member.isValidFullName(" Michael B. J ", 1))
        assertTrue(Member.isValidFullName(" Michael B. J ", 2))
        assertTrue(Member.isValidFullName(" Michael B. J ", 3))

        assertTrue(Member.isValidFullName("Michael B. Jordan Jr", 1))
        assertTrue(Member.isValidFullName("Michael B. Jordan Jr", 2))
        assertTrue(Member.isValidFullName("Michael B. Jordan Jr", 3))
    }

    @Test
    fun isValidMedicalRecordNumber() {
        assertFalse(Member.isValidMedicalRecordNumber("123", 4, 7))
        assertTrue(Member.isValidMedicalRecordNumber("1234", 4, 7))
        assertTrue(Member.isValidMedicalRecordNumber("12345", 4, 7))
        assertTrue(Member.isValidMedicalRecordNumber("123456", 4, 7))
        assertTrue(Member.isValidMedicalRecordNumber("1234567", 4, 7))
        assertFalse(Member.isValidMedicalRecordNumber("12345678", 4, 7))
    }

    @Test
    fun isValidCardId() {
        assertTrue(Member.isValidCardId("RWI123456"))
        assertFalse(Member.isValidCardId("RWI12345X"))
        assertFalse(Member.isValidCardId("RWI1234567"))
        assertFalse(Member.isValidCardId("RWI12345"))
    }

    @Test
    fun diff() {
        val member = MemberFactory.build()
        val updatedMember = member.copy(phoneNumber = "775555555", cardId = "RWI123456")

        val deltas = member.diff(updatedMember)

        assertEquals(2, deltas.size)
        assertTrue(deltas.contains(Delta(action = Delta.Action.EDIT,
                                     modelName = Delta.ModelName.MEMBER,
                                     modelId = member.id,
                                     field = "phoneNumber")))
        assertTrue(deltas.contains(Delta(action = Delta.Action.EDIT,
                                     modelName = Delta.ModelName.MEMBER,
                                     modelId = member.id,
                                     field = "cardId")))
    }

    @Test
    fun memberStatus_nullHousehold_isUnknown() {
        val member = MemberFactory.build(householdId = null)

        assertEquals(Member.MembershipStatus.UNKNOWN, member.memberStatus(fixedClock))
    }

    @Test
    fun memberStatus_nullCoverageEndDate_isUnknown() {
        val member = MemberFactory.build(coverageEndDate = null)

        assertEquals(Member.MembershipStatus.UNKNOWN, member.memberStatus(fixedClock))
    }

    @Test
    fun memberStatus_archivedAndHeadOfHousehold_isDeleted() {
        val member = MemberFactory.build(
            coverageEndDate = LocalDate.now(),
            archivedReason = Member.ArchivedReason.OTHER,
            relationshipToHead = Member.RelationshipToHead.SELF
        )

        assertEquals(Member.MembershipStatus.DELETED, member.memberStatus(fixedClock))
    }

    @Test
    fun memberStatus_coverageEndedBeforeCurrentDate_isExpired() {
        val member = MemberFactory.build(
            coverageEndDate = LocalDate.now().minusDays(10),
            archivedReason = Member.ArchivedReason.OTHER,
            relationshipToHead = Member.RelationshipToHead.CHILD
        )

        assertEquals(Member.MembershipStatus.EXPIRED, member.memberStatus(fixedClock))
    }

    @Test
    fun memberStatus_coverageEndedIsAfterCurrentDate_isActive() {
        val member = MemberFactory.build(
            coverageEndDate = LocalDate.now().plusDays(10),
            archivedReason = Member.ArchivedReason.OTHER,
            relationshipToHead = Member.RelationshipToHead.CHILD
        )

        assertEquals(Member.MembershipStatus.ACTIVE, member.memberStatus(fixedClock))
    }

    @Test
    fun beneficiaryStatus_nullHousehold_isUnknown() {
        val member = MemberFactory.build(householdId = null)

        assertEquals(Member.MembershipStatus.UNKNOWN, member.beneficiaryStatus(fixedClock))
    }

    @Test
    fun beneficiaryStatus_nullCoverageEndDate_isUnknown() {
        val member = MemberFactory.build(coverageEndDate = null)

        assertEquals(Member.MembershipStatus.UNKNOWN, member.beneficiaryStatus(fixedClock))
    }

    @Test
    fun beneficiaryStatus_unpaid_isExpired() {
        val member = MemberFactory.build(
            coverageEndDate = LocalDate.now(),
            archivedReason = Member.ArchivedReason.UNPAID
        )

        assertEquals(Member.MembershipStatus.EXPIRED, member.beneficiaryStatus(fixedClock))
    }

    @Test
    fun beneficiaryStatus_archivedNotUnpaid_isExpired() {
        val member = MemberFactory.build(
            coverageEndDate = LocalDate.now(),
            archivedReason = Member.ArchivedReason.OTHER
        )

        assertEquals(Member.MembershipStatus.DELETED, member.beneficiaryStatus(fixedClock))
    }

    @Test
    fun beneficiaryStatus_coverageEndedBeforeCurrentDate_isExpired() {
        val member = MemberFactory.build(
            coverageEndDate = LocalDate.now().minusDays(10)
        )

        assertEquals(Member.MembershipStatus.EXPIRED, member.beneficiaryStatus(fixedClock))
    }

    @Test
    fun beneficiaryStatus_coverageEndedIsAfterCurrentDate_isActive() {
        val member = MemberFactory.build(
            coverageEndDate = LocalDate.now().plusDays(10)
        )

        assertEquals(Member.MembershipStatus.ACTIVE, member.beneficiaryStatus(fixedClock))
    }
}
