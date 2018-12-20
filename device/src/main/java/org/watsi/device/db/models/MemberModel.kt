package org.watsi.device.db.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.watsi.domain.entities.Member
import java.util.UUID

@Entity(tableName = "members")
data class MemberModel(@PrimaryKey val id: UUID,
                       val enrolledAt: Instant,
                       val createdAt: Instant,
                       val updatedAt: Instant,
                       val householdId: UUID,
                       val photoId: UUID?,
                       val thumbnailPhotoId: UUID?,
                       val photoUrl: String?,
                       val cardId: String?,
                       val name: String,
                       val gender: Member.Gender,
                       val language: String?,
                       val birthdate: LocalDate,
                       val birthdateAccuracy: Member.DateAccuracy = Member.DateAccuracy.Y,
                       val fingerprintsGuid: UUID?,
                       val phoneNumber: String?) {
    init {
        if (name.isBlank()) {
            throw ModelValidationException("Name cannot be blank")
        }
    }

    fun toMember(): Member {
        return Member(id = id,
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
                      phoneNumber = phoneNumber)
    }

    companion object {
        fun fromMember(member: Member, clock: Clock): MemberModel {
            val now = clock.instant()
            return MemberModel(id = member.id,
                               enrolledAt = member.enrolledAt,
                               createdAt = now,
                               updatedAt = now,
                               householdId = member.householdId,
                               photoId = member.photoId,
                               thumbnailPhotoId = member.thumbnailPhotoId,
                               photoUrl = member.photoUrl,
                               cardId = member.cardId,
                               name = member.name,
                               gender = member.gender,
                               language = member.language,
                               birthdate = member.birthdate,
                               birthdateAccuracy = member.birthdateAccuracy,
                               fingerprintsGuid = member.fingerprintsGuid,
                               phoneNumber = member.phoneNumber)
        }
    }
}