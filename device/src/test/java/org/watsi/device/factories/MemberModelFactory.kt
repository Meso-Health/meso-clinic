package org.watsi.device.factories

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.watsi.device.db.daos.MemberDao
import org.watsi.device.db.models.MemberModel
import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Member.ArchivedReason
import org.watsi.domain.entities.Member.RelationshipToHead
import java.util.UUID

object MemberModelFactory {

    fun build(
        id: UUID = UUID.randomUUID(),
        createdAt: Instant? = null,
        updatedAt: Instant? = null,
        enrolledAt: Instant? = null,
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
        phoneNumber: String? = null,
        membershipNumber: String? = null,
        medicalRecordNumber: String? = null,
        needsRenewal: Boolean? = false,
        relationshipToHead: RelationshipToHead? = null,
        archivedAt: Instant? = null,
        archivedReason: ArchivedReason? = null,
        renewedAt: Instant? = null,
        coverageEndDate: LocalDate? = null,
        clock: Clock = Clock.systemUTC()
    ) : MemberModel {
        val now = Instant.now(clock)
        return MemberModel(
            id = id,
            createdAt = createdAt ?: now,
            updatedAt = updatedAt ?: now,
            enrolledAt = enrolledAt ?: now,
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
            phoneNumber = phoneNumber,
            membershipNumber = membershipNumber,
            medicalRecordNumber = medicalRecordNumber,
            needsRenewal = needsRenewal,
            relationshipToHead = relationshipToHead,
            archivedAt = archivedAt,
            archivedReason = archivedReason,
            renewedAt = renewedAt,
            coverageEndDate = coverageEndDate
        )
    }

    fun create(
        memberDao: MemberDao,
        createdAt: Instant? = null,
        updatedAt: Instant? = null,
        enrolledAt: Instant? = null,
        id: UUID = UUID.randomUUID(),
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
        phoneNumber: String? = null,
        membershipNumber: String? = null,
        medicalRecordNumber: String? = null,
        needsRenewal: Boolean? = false,
        relationshipToHead: RelationshipToHead? = null,
        archivedAt: Instant? = null,
        archivedReason: ArchivedReason? = null,
        renewedAt: Instant? = null,
        coverageEndDate: LocalDate? = null,
        clock: Clock = Clock.systemUTC()
    ) : MemberModel {
        val model = build(
            id = id,
            createdAt = createdAt,
            updatedAt = updatedAt,
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
            phoneNumber = phoneNumber,
            membershipNumber = membershipNumber,
            medicalRecordNumber = medicalRecordNumber,
            needsRenewal = needsRenewal,
            relationshipToHead = relationshipToHead,
            archivedAt = archivedAt,
            archivedReason = archivedReason,
            renewedAt = renewedAt,
            coverageEndDate = coverageEndDate,
            clock = clock
        )
        memberDao.insert(model)
        return model
    }
}
