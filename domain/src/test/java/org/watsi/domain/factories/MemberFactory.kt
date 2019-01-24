package org.watsi.domain.factories

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.watsi.domain.entities.Member
import java.util.UUID

object MemberFactory {

    fun build(
        id: UUID = UUID.randomUUID(),
        enrolledAt: Instant = Instant.now(),
        householdId: UUID? = UUID.randomUUID(),
        photoId: UUID? = null,
        thumbnailPhotoId: UUID? = null,
        photoUrl: String? = null,
        cardId: String? = null,
        name: String = "Akiiki Monday",
        gender: Member.Gender = Member.Gender.F,
        language: String? = null,
        birthdate: LocalDate = LocalDate.now(),
        birthdateAccuracy: Member.DateAccuracy = Member.DateAccuracy.Y,
        fingerprintsGuid: UUID? = null,
        phoneNumber: String? = null,
        membershipNumber: String? = null,
        medicalRecordNumber: String? = null,
        needsRenewal: Boolean? = false,
        relationshipToHead: Member.RelationshipToHead? = null
    ) : Member {
        return Member(
            id = id,
            enrolledAt = enrolledAt,
            householdId = householdId,
            photoId = photoId,
            thumbnailPhotoId = thumbnailPhotoId,
            photoUrl = photoUrl,
            cardId = cardId,
            name = name,
            gender = gender,
            language = language,
            birthdate = birthdate,
            birthdateAccuracy = birthdateAccuracy,
            fingerprintsGuid = fingerprintsGuid,
            phoneNumber = phoneNumber,
            membershipNumber = membershipNumber,
            medicalRecordNumber = medicalRecordNumber,
            needsRenewal = needsRenewal,
            relationshipToHead = relationshipToHead
        )
    }
}
