package org.watsi.device.factories

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.watsi.device.db.daos.MemberDao
import org.watsi.device.db.models.MemberModel
import org.watsi.domain.entities.Member
import java.util.UUID

object MemberModelFactory {

    fun build(id: UUID = UUID.randomUUID(),
              householdId: UUID = UUID.randomUUID(),
              photoId: UUID? = null,
              thumbnailPhotoId: UUID? = null,
              photoUrl: String? = null,
              cardId: String? = null,
              name: String = "Akiiki Monday",
              gender: Member.Gender = Member.Gender.F,
              birthdate: LocalDate = LocalDate.now(),
              birthdateAccuracy: Member.DateAccuracy = Member.DateAccuracy.Y,
              fingerprintsGuid: UUID? = null,
              phoneNumber: String? = null,
              createdAt: Instant? = null,
              updatedAt: Instant? = null,
              clock: Clock = Clock.systemUTC()) : MemberModel {
        val now = Instant.now(clock)
        return MemberModel(id = id,
                           householdId = householdId,
                           photoId = photoId,
                           thumbnailPhotoId = thumbnailPhotoId,
                           photoUrl = photoUrl,
                           cardId = cardId,
                           name = name,
                           gender = gender,
                           birthdate = birthdate,
                           birthdateAccuracy = birthdateAccuracy,
                           fingerprintsGuid = fingerprintsGuid,
                           phoneNumber = phoneNumber,
                           createdAt = createdAt ?: now,
                           updatedAt = updatedAt ?: now)
    }

    fun create(memberDao: MemberDao,
               id: UUID = UUID.randomUUID(),
               householdId: UUID = UUID.randomUUID(),
               photoId: UUID? = null,
               thumbnailPhotoId: UUID? = null,
               photoUrl: String? = null,
               cardId: String? = null,
               name: String = "Akiiki Monday",
               gender: Member.Gender = Member.Gender.F,
               birthdate: LocalDate = LocalDate.now(),
               birthdateAccuracy: Member.DateAccuracy = Member.DateAccuracy.Y,
               fingerprintsGuid: UUID? = null,
               phoneNumber: String? = null,
               createdAt: Instant? = null,
               updatedAt: Instant? = null,
               clock: Clock = Clock.systemUTC()) : MemberModel {
        val model = build(id = id,
                          householdId = householdId,
                          photoId = photoId,
                          thumbnailPhotoId = thumbnailPhotoId,
                          photoUrl = photoUrl,
                          cardId = cardId,
                          name = name,
                          gender = gender,
                          birthdate = birthdate,
                          birthdateAccuracy = birthdateAccuracy,
                          fingerprintsGuid = fingerprintsGuid,
                          phoneNumber = phoneNumber,
                          createdAt = createdAt,
                          updatedAt = updatedAt,
                          clock = clock)
        memberDao.insert(model)
        return model
    }
}
