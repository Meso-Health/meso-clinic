package org.watsi.device.api.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDate
import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Photo
import java.util.UUID

data class MemberApi(@SerializedName("id") val id: UUID,
                     @SerializedName("household_id") val householdId: UUID,
                     @SerializedName("card_id") val cardId: String?,
                     @SerializedName("full_name") val name: String,
                     @SerializedName("gender") val gender: Member.Gender,
                     @SerializedName("birthdate") val birthdate: LocalDate,
                     @SerializedName("birthdate_accuracy")
                     val birthdateAccuracy: Member.DateAccuracy = Member.DateAccuracy.Y,
                     @SerializedName("fingerprints_guid") val fingerprintsGuid: UUID?,
                     @SerializedName("phone_number") val phoneNumber: String?,
                     @Expose(serialize = false)
                     @SerializedName("photo_url") val photoUrl: String?) {

    fun toMember(): Member {
        val thumbnailPhotoId = if (photoUrl != null) {
            Photo(id = UUID.randomUUID(), bytes = null, url = photoUrl).id
            // TODO: need to persist model
        } else {
            null
        }

        return Member(id = id,
                      householdId = householdId,
                      cardId = cardId,
                      name = name,
                      gender = gender,
                      birthdate = birthdate,
                      birthdateAccuracy = birthdateAccuracy,
                      fingerprintsGuid = fingerprintsGuid,
                      phoneNumber = phoneNumber,
                      photoId = null,
                      thumbnailPhotoId = thumbnailPhotoId)
    }
}
