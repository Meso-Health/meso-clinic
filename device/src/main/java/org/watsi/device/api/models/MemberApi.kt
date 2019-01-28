package org.watsi.device.api.models

import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Member.ArchivedReason
import org.watsi.domain.entities.Member.RelationshipToHead
import java.util.UUID

data class MemberApi(
    @SerializedName(ID_FIELD) val id: UUID,
    @SerializedName(ENROLLED_AT_FIELD) val enrolledAt: Instant,
    @SerializedName(HOUSEHOLD_ID_FIELD) val householdId: UUID?,
    @SerializedName(CARD_ID_FIELD) val cardId: String?,
    @SerializedName(NAME_FIELD) val name: String,
    @SerializedName(GENDER_FIELD) val gender: Member.Gender,
    @SerializedName(BIRTHDATE_FIELD) val birthdate: LocalDate,
    @SerializedName(BIRTHDATE_ACCURACY_FIELD)
    val birthdateAccuracy: Member.DateAccuracy = Member.DateAccuracy.Y,
    @SerializedName(FINGERPRINTS_GUID_FIELD) val fingerprintsGuid: UUID?,
    @SerializedName(PHONE_NUMBER_FIELD) val phoneNumber: String?,
    @SerializedName(LANGUAGE_FIELD) val language: String?,
    @SerializedName(OTHER_LANGUAGE_FIELD) val otherLanguage: String?,
    @Expose(serialize = false)
    @SerializedName(PHOTO_URL_FIELD) val photoUrl: String?,
    @SerializedName(MEMBERSHIP_NUMBER_FIELD) val membershipNumber: String?,
    @SerializedName(MEDICAL_RECORD_NUMBER_FIELD) val medicalRecordNumber: String?,
    @Expose(serialize = false)
    @SerializedName(NEEDS_RENEWAL) val needsRenewal: Boolean?,
    @SerializedName(RELATIONSHIP_TO_HEAD) val relationshipToHead: RelationshipToHead?,
    @SerializedName(ARCHIVED_AT) val archivedAt: Instant?,
    @SerializedName(ARCHIVED_REASON) val archivedReason: ArchivedReason?
) {

    constructor (member: Member) :
            this(id = member.id,
                 enrolledAt = member.enrolledAt,
                 householdId = member.householdId,
                 cardId = member.cardId,
                 name = member.name,
                 gender = member.gender,
                 birthdate = member.birthdate,
                 birthdateAccuracy = member.birthdateAccuracy,
                 fingerprintsGuid = member.fingerprintsGuid,
                 phoneNumber = member.phoneNumber,
                 language = preferredLanguage(member),
                 otherLanguage = preferredLanguageOther(member),
                 photoUrl = member.photoUrl,
                 membershipNumber = member.membershipNumber,
                 medicalRecordNumber = member.medicalRecordNumber,
                 needsRenewal = member.needsRenewal,
                 relationshipToHead = member.relationshipToHead,
                 archivedAt = member.archivedAt,
                 archivedReason = member.archivedReason
            )

    fun toMember(persistedMember: Member?): Member {
        // do not overwrite the local thumbnail photo if the fetched photo is not different
        val thumbnailPhotoId = persistedMember?.let {
            if (it.photoUrl == photoUrl || it.photoUrl == null) it.thumbnailPhotoId else null
        }
        return Member(
            id = id,
            enrolledAt = enrolledAt,
            householdId = householdId,
            cardId = cardId,
            name = name,
            gender = gender,
            birthdate = birthdate,
            birthdateAccuracy = birthdateAccuracy,
            fingerprintsGuid = fingerprintsGuid,
            phoneNumber = phoneNumber,
            language = language,
            photoId = persistedMember?.photoId,
            thumbnailPhotoId = thumbnailPhotoId,
            photoUrl = photoUrl,
            membershipNumber = membershipNumber,
            medicalRecordNumber = medicalRecordNumber,
            needsRenewal = needsRenewal,
            relationshipToHead = relationshipToHead,
            archivedAt = archivedAt,
            archivedReason = archivedReason
        )
    }

    companion object {
        const val ID_FIELD = "id"
        const val ENROLLED_AT_FIELD = "enrolled_at"
        const val HOUSEHOLD_ID_FIELD = "household_id"
        const val CARD_ID_FIELD = "card_id"
        const val NAME_FIELD = "full_name"
        const val GENDER_FIELD = "gender"
        const val BIRTHDATE_FIELD = "birthdate"
        const val BIRTHDATE_ACCURACY_FIELD = "birthdate_accuracy"
        const val FINGERPRINTS_GUID_FIELD = "fingerprints_guid"
        const val PHONE_NUMBER_FIELD = "phone_number"
        const val LANGUAGE_FIELD = "preferred_language"
        const val OTHER_LANGUAGE_FIELD = "preferred_language_other"
        const val PHOTO_URL_FIELD = "photo_url"
        const val MEMBERSHIP_NUMBER_FIELD = "membership_number"
        const val MEDICAL_RECORD_NUMBER_FIELD = "medical_record_number"
        const val NEEDS_RENEWAL = "needs_renewal"
        const val RELATIONSHIP_TO_HEAD = "relationship_to_head"
        const val ARCHIVED_AT = "archived_at"
        const val ARCHIVED_REASON = "archived_reason"

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
                    "medicalRecordNumber" -> patchParams.addProperty(MEDICAL_RECORD_NUMBER_FIELD, member.medicalRecordNumber)
                    null -> Unit
                }
            }
            return patchParams
        }

        private fun preferredLanguage(member: Member): String? {
            return if (Member.COMMON_LANGUAGES.contains(member.language)) {
                member.language
            } else if (member.language != null) {
                Member.LANGUAGE_CHOICE_OTHER
            } else {
                null
            }?.toLowerCase()
        }

        private fun preferredLanguageOther(member: Member): String? {
            return if (Member.COMMON_LANGUAGES.contains(member.language)) {
                null
            } else {
                member.language?.toLowerCase()
            }
        }
    }
}
