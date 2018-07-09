package org.watsi.device.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.watsi.device.api.models.MemberApi
import org.watsi.domain.factories.MemberFactory
import java.util.UUID

class MemberApiTest {

    val member = MemberFactory.build()
    val memberApi = MemberApi(member)

    @Test
    fun toMember_copiesAttributes() {
        val parsedMember = memberApi.toMember(null)

        assertEquals(memberApi.id, parsedMember.id)
        assertEquals(memberApi.enrolledAt, parsedMember.enrolledAt)
        assertEquals(memberApi.householdId, parsedMember.householdId)
        assertEquals(memberApi.cardId, parsedMember.cardId)
        assertEquals(memberApi.name, parsedMember.name)
        assertEquals(memberApi.gender, parsedMember.gender)
        assertEquals(memberApi.birthdate, parsedMember.birthdate)
        assertEquals(memberApi.birthdateAccuracy, parsedMember.birthdateAccuracy)
        assertEquals(memberApi.phoneNumber, parsedMember.phoneNumber)
        assertEquals(memberApi.membershipNumber, parsedMember.membershipNumber)
        assertEquals(memberApi.medicalRecordNumber, parsedMember.medicalRecordNumber)
    }

    @Test
    fun toMember_localPhotoUrl_prependsLocalhost() {
        val localPhotoMemberApi = memberApi.copy(photoUrl = "/foo")
        val localPhotoMember = localPhotoMemberApi.toMember(null)

        assertEquals("http://localhost:5000/foo", localPhotoMember.photoUrl)
    }

    @Test
    fun toMember_persistedMemberHasPhotoId_copiesPhotoId() {
        val photoId = UUID.randomUUID()
        val persistedMember = member.copy(photoId = photoId)
        val parsedMember = memberApi.toMember(persistedMember)

        assertEquals(photoId, parsedMember.photoId)
    }

    @Test
    fun toMember_persistedMemberHasNoPhotoUrl_maintainsThumbnailPhoto() {
        val thumbnailPhotoId = UUID.randomUUID()
        val persistedMember = member.copy(photoUrl = null, thumbnailPhotoId = thumbnailPhotoId)
        val parsedMember = memberApi.toMember(persistedMember)

        assertEquals(thumbnailPhotoId, parsedMember.thumbnailPhotoId)
    }

    @Test
    fun toMember_persistedMemberAndApiResponseHaveSamePhotoUrl_maintainsThumbnailPhoto() {
        val thumbnailPhotoId = UUID.randomUUID()
        val persistedMember = member.copy(photoUrl = "foo", thumbnailPhotoId = thumbnailPhotoId)
        val parsedMember = memberApi.copy(photoUrl = "foo").toMember(persistedMember)

        assertEquals(thumbnailPhotoId, parsedMember.thumbnailPhotoId)
    }

    @Test
    fun toMember_persistedMemberHasPhotoUrlAndDoesNotMatchApiResponse_maintainsThumbnailPhoto() {
        val thumbnailPhotoId = UUID.randomUUID()
        val persistedMember = member.copy(photoUrl = "foo", thumbnailPhotoId = thumbnailPhotoId)
        val parsedMember = memberApi.copy(photoUrl = "bar").toMember(persistedMember)

        assertNull(parsedMember.thumbnailPhotoId)
    }
}
