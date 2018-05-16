package org.watsi.device.api.models

import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDate
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Member
import java.util.UUID

/**
 * Data class that defines the structure of a sync Member API request.
 *
 * Uses Strings for Date/Time fields because GSON does not natively support serializing java.time
 * classes to a format our API accepts.
 */
data class MemberApi(@SerializedName(ID_FIELD) val id: UUID,
                     @SerializedName(HOUSEHOLD_ID_FIELD) val householdId: UUID,
                     @SerializedName(CARD_ID_FIELD) val cardId: String?,
                     @SerializedName(NAME_FIELD) val name: String,
                     @SerializedName(GENDER_FIELD) val gender: Member.Gender,
                     @SerializedName(BIRTHDATE_FIELD) val birthdate: String,
                     @SerializedName(BIRTHDATE_ACCURACY_FIELD)
                     val birthdateAccuracy: Member.DateAccuracy = Member.DateAccuracy.Y,
                     @SerializedName(FINGERPRINTS_GUID_FIELD) val fingerprintsGuid: UUID?,
                     @SerializedName(PHONE_NUMBER_FIELD) val phoneNumber: String?,
                     @Expose(serialize = false)
                     @SerializedName(PHOTO_URL_FIELD) val photoUrl: String?) {

    constructor (member: Member) :
            this(id = member.id,
                 householdId = member.householdId,
                 cardId = member.cardId,
                 name = member.name,
                 gender = member.gender,
                 birthdate = member.birthdate.toString(),
                 birthdateAccuracy = member.birthdateAccuracy,
                 fingerprintsGuid = member.fingerprintsGuid,
                 phoneNumber = member.phoneNumber,
                 photoUrl = member.photoUrl
            )

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
                      birthdate = LocalDate.parse(birthdate),
                      birthdateAccuracy = birthdateAccuracy,
                      fingerprintsGuid = fingerprintsGuid,
                      phoneNumber = phoneNumber,
                      photoId = persistedMember?.photoId,
                      thumbnailPhotoId = thumbnailPhotoId,
                      photoUrl = convertedPhotoUrl)
    }

    companion object {
        const val ID_FIELD = "id"
        const val HOUSEHOLD_ID_FIELD = "household_id"
        const val CARD_ID_FIELD = "card_id"
        const val NAME_FIELD = "full_name"
        const val GENDER_FIELD = "gender"
        const val BIRTHDATE_FIELD = "birthdate"
        const val BIRTHDATE_ACCURACY_FIELD = "birthdate_accuracy"
        const val FINGERPRINTS_GUID_FIELD = "fingerprints_guid"
        const val PHONE_NUMBER_FIELD = "phone_number"
        const val PHOTO_URL_FIELD = "photo_url"

        fun patch(member: Member, deltas: List<Delta>): JsonObject {
            val patchParams = JsonObject()
            patchParams.addProperty(ID_FIELD, member.id.toString())
            deltas.forEach { delta ->
                when (delta.field) {
                    "name" -> patchParams.addProperty(NAME_FIELD, member.name)
                    "phoneNumber" -> patchParams.addProperty(PHONE_NUMBER_FIELD, member.phoneNumber)
                    "fingerprintsGuid" -> {
                        patchParams.addProperty(FINGERPRINTS_GUID_FIELD, member.fingerprintsGuid.toString())
                    }
                    "cardId" -> patchParams.addProperty(CARD_ID_FIELD, member.cardId)
                    null -> Unit
                }
            }
            return patchParams
        }
    }
}
