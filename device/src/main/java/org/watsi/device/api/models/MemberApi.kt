package org.watsi.device.api.models

import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Member.ArchivedReason
import org.watsi.domain.entities.Member.RelationshipToHead
import java.util.UUID

data class MemberApi(
    val id: UUID,
    val enrolledAt: Instant,
    val householdId: UUID?,
    val cardId: String?,
    val fullName: String,
    val gender: Member.Gender,
    val birthdate: LocalDate,
    val birthdateAccuracy: Member.DateAccuracy = Member.DateAccuracy.Y,
    val phoneNumber: String?,
    val preferredLanguage: String?,
    val preferredLanguageOther: String?,
    @Expose(serialize = false) val photoUrl: String?,
    val membershipNumber: String?,
    val medicalRecordNumber: String?,
    @Expose(serialize = false) val needsRenewal: Boolean?,
    val relationshipToHead: RelationshipToHead?,
    val archivedAt: Instant?,
    val archivedReason: ArchivedReason?,
    val renewedAt: Instant?,
    val coverageEndDate: LocalDate?
) {

    constructor (member: Member) :
            this(id = member.id,
                 enrolledAt = member.enrolledAt,
                 householdId = member.householdId,
                 cardId = member.cardId,
                 fullName = member.name,
                 gender = member.gender,
                 birthdate = member.birthdate,
                 birthdateAccuracy = member.birthdateAccuracy,
                 phoneNumber = member.phoneNumber,
                 preferredLanguage = preferredLanguage(member),
                 preferredLanguageOther = preferredLanguageOther(member),
                 photoUrl = member.photoUrl,
                 membershipNumber = member.membershipNumber,
                 medicalRecordNumber = member.medicalRecordNumber,
                 needsRenewal = member.needsRenewal,
                 relationshipToHead = member.relationshipToHead,
                 archivedAt = member.archivedAt,
                 archivedReason = member.archivedReason,
                 renewedAt = member.renewedAt,
                 coverageEndDate = member.coverageEndDate
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
            name = fullName,
            gender = gender,
            birthdate = birthdate,
            birthdateAccuracy = birthdateAccuracy,
            phoneNumber = phoneNumber,
            language = preferredLanguage,
            photoId = persistedMember?.photoId,
            thumbnailPhotoId = thumbnailPhotoId,
            photoUrl = photoUrl,
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

    companion object {
        const val ID_FIELD = "id"
        const val CARD_ID_FIELD = "card_id"
        const val NAME_FIELD = "full_name"
        const val PHONE_NUMBER_FIELD = "phone_number"
        const val MEDICAL_RECORD_NUMBER_FIELD = "medical_record_number"

        fun patch(member: Member, deltas: List<Delta>): JsonObject {
            val patchParams = JsonObject()
            patchParams.addProperty(ID_FIELD, member.id.toString())
            deltas.forEach { delta ->
                when (delta.field) {
                    "name" -> patchParams.addProperty(NAME_FIELD, member.name)
                    "phoneNumber" -> patchParams.addProperty(PHONE_NUMBER_FIELD, member.phoneNumber)
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
