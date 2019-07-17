package org.watsi.domain.entities

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
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
    val fiveYearsAgo = today.minusYears(5)
    val tenYearsAgo = today.minusYears(10)

    @Test
    fun isAbsentee_requiresFingerprints_hasFingerprintAndPhoto_isFalse() {
        val member = MemberFactory.build(birthdate = tenYearsAgo,
                fingerprintsGuid = UUID.randomUUID(),
                thumbnailPhotoId = UUID.randomUUID())

        assertFalse(member.isAbsentee(fixedClock))
    }

    @Test
    fun isAbsentee_doesNotRequireFingerprints_noFingerprintsHasThumbnailPhoto_isFalse() {
        val member = MemberFactory.build(birthdate = fiveYearsAgo,
                                         fingerprintsGuid = null,
                                         thumbnailPhotoId = UUID.randomUUID())

        assertFalse(member.isAbsentee(fixedClock))
    }

    @Test
    fun isAbsentee_doesNotRequireFingerprints_noFingerprintsHasPhotoUrl_isFalse() {
        val member = MemberFactory.build(birthdate = fiveYearsAgo,
                                         fingerprintsGuid = null,
                                         photoUrl = "")

        assertFalse(member.isAbsentee(fixedClock))
    }

    @Test
    fun isAbsentee_requiresFingerprints_noFingerprint_isTrue() {
        val member = MemberFactory.build(birthdate = tenYearsAgo, fingerprintsGuid = null)

        assertTrue(member.isAbsentee(fixedClock))
    }

    @Test
    fun isAbsentee_noPhoto_isTrue() {
        val member = MemberFactory.build(thumbnailPhotoId = null, photoUrl = null)

        assertTrue(member.isAbsentee(fixedClock))
    }

    @Test
    fun requiresFingerprint() {
        assertTrue(MemberFactory.build(birthdate = tenYearsAgo).requiresFingerprint(fixedClock))
        assertFalse(MemberFactory.build(birthdate = fiveYearsAgo).requiresFingerprint(fixedClock))
    }

    @Test
    fun getAgeYears() {
        assertEquals(10, MemberFactory.build(birthdate = tenYearsAgo).getAgeYears(fixedClock))
    }

    @Test
    fun isValidName() {
        assertFalse(Member.isValidName(""))
        assertFalse(Member.isValidName("   "))
        assertFalse(Member.isValidName("Michael"))
        assertFalse(Member.isValidName(" Michael "))
        assertFalse(Member.isValidName("Michael Jordan"))
        assertFalse(Member.isValidName("  Michael B. "))
        assertTrue(Member.isValidName("Michael Bakari Jordan"))
        assertTrue(Member.isValidName(" Michael B. J "))
        assertTrue(Member.isValidName("Michael B. Jordan Jr"))
    }

    @Test
    fun isValidMedicalRecordNumber() {
        assertFalse(Member.isValidMedicalRecordNumber("1234"))
        assertTrue(Member.isValidMedicalRecordNumber("12345"))
        assertTrue(Member.isValidMedicalRecordNumber("123456"))
        assertTrue(Member.isValidMedicalRecordNumber("1234567"))
        assertFalse(Member.isValidMedicalRecordNumber("12345678"))
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
}
