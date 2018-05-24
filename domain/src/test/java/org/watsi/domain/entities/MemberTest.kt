package org.watsi.domain.entities

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate
import org.watsi.domain.factories.MemberFactory
import java.util.UUID

class MemberTest {

    val today = LocalDate.now()
    lateinit var fiveYearsAgo: LocalDate
    lateinit var tenYearsAgo: LocalDate

    @Before
    fun setup() {
        fiveYearsAgo = today.minusYears(5)
        tenYearsAgo = today.minusYears(10)
    }

    @Test
    fun isAbsentee_requiresFingerprints_hasFingerprintAndPhoto_isFalse() {
        val member = MemberFactory.build(birthdate = tenYearsAgo,
                fingerprintsGuid = UUID.randomUUID(),
                thumbnailPhotoId = UUID.randomUUID())

        assertFalse(member.isAbsentee())
    }

    @Test
    fun isAbsentee_doesNotRequireFingerprints_noFingerprintsHasThumbnailPhoto_isFalse() {
        val member = MemberFactory.build(birthdate = fiveYearsAgo,
                                         fingerprintsGuid = null,
                                         thumbnailPhotoId = UUID.randomUUID())

        assertFalse(member.isAbsentee())
    }

    @Test
    fun isAbsentee_doesNotRequireFingerprints_noFingerprintsHasPhotoUrl_isFalse() {
        val member = MemberFactory.build(birthdate = fiveYearsAgo,
                                         fingerprintsGuid = null,
                                         photoUrl = "")

        assertFalse(member.isAbsentee())
    }

    @Test
    fun isAbsentee_requiresFingerprints_noFingerprint_isTrue() {
        val member = MemberFactory.build(birthdate = tenYearsAgo, fingerprintsGuid = null)

        assert(member.isAbsentee())
    }

    @Test
    fun isAbsentee_noPhoto_isTrue() {
        val member = MemberFactory.build(thumbnailPhotoId = null, photoUrl = null)

        assert(member.isAbsentee())
    }

    @Test
    fun requiresFingerprint() {
        assert(MemberFactory.build(birthdate = tenYearsAgo).requiresFingerprint())
        assertFalse(MemberFactory.build(birthdate = fiveYearsAgo).requiresFingerprint())
    }

    @Test
    fun getAgeYears() {
        assertEquals(10, MemberFactory.build(birthdate = tenYearsAgo).getAgeYears())
    }

    @Test
    fun formattedPhoneNumber() {
        assertNull(MemberFactory.build(phoneNumber = null).formattedPhoneNumber())
        assertEquals("(0) 775 555 555",
                MemberFactory.build(phoneNumber = "775555555").formattedPhoneNumber())
        assertEquals("(0) 775 555 555",
                MemberFactory.build(phoneNumber = "0775555555").formattedPhoneNumber())
    }

    @Test
    fun validCardId() {
        assert(Member.validCardId("RWI123456"))
        assertFalse(Member.validCardId("RWI12345X"))
        assertFalse(Member.validCardId("RWI1234567"))
        assertFalse(Member.validCardId("RWI12345"))
    }

    @Test
    fun validPhoneNumber() {
        assert(Member.validPhoneNumber("775555555"))
        assert(Member.validPhoneNumber("0775555555"))
        assertFalse(Member.validPhoneNumber("077555555"))
        assertFalse(Member.validPhoneNumber("77555555"))
        assertFalse(Member.validPhoneNumber("77555555A"))
    }

    @Test
    fun formatAgeAndGender() {
        val m1 = MemberFactory.build(birthdate = tenYearsAgo, gender = Member.Gender.M)
        val m2 = MemberFactory.build(birthdate = fiveYearsAgo, gender = Member.Gender.F)
        assertEquals(m1.formatAgeAndGender(), "10 - M")
        assertEquals(m2.formatAgeAndGender(), "5 - F")
    }

    @Test
    fun diff() {
        val member = MemberFactory.build()
        val updatedMember = member.copy(phoneNumber = "775555555", cardId = "RWI123456")

        val deltas = member.diff(updatedMember)

        assertEquals(2, deltas.size)
        assert(deltas.contains(Delta(action = Delta.Action.EDIT,
                                     modelName = Delta.ModelName.MEMBER,
                                     modelId = member.id,
                                     field = "phoneNumber")))
        assert(deltas.contains(Delta(action = Delta.Action.EDIT,
                                     modelName = Delta.ModelName.MEMBER,
                                     modelId = member.id,
                                     field = "cardId")))
    }
}
