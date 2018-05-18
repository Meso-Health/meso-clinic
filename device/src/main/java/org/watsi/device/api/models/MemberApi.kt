package org.watsi.device.api.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDate
import org.watsi.domain.entities.Member
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

    fun toMember(persistedMember: Member?): Member {
        // necessary because when running locally, URL is returned relative to app directory
        val convertedPhotoUrl = if (photoUrl?.first() == '/') {
            "http://localhost:5000$photoUrl"
        } else {
            photoUrl
        }
        // do not overwrite the local thumbnail photo if the fetched photo is not different
        val thumbnailPhotoId = persistedMember?.let {
            if (it.photoUrl == photoUrl || it.photoUrl == null) it.thumbnailPhotoId else null
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
                      photoId = persistedMember?.photoId,
                      thumbnailPhotoId = thumbnailPhotoId,
                      photoUrl = convertedPhotoUrl)
    }
}
