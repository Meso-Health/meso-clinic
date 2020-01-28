package org.watsi.domain.entities

import com.google.gson.GsonBuilder
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.watsi.domain.utils.DateUtils
import java.io.Serializable
import java.util.UUID

data class Member(
    val id: UUID,
    val enrolledAt: Instant,
    val householdId: UUID?,
    val photoId: UUID?,
    val thumbnailPhotoId: UUID?,
    val photoUrl: String?,
    val cardId: String?,
    val name: String,
    val gender: Gender,
    val language: String?,
    val birthdate: LocalDate,
    val birthdateAccuracy: DateAccuracy = DateAccuracy.Y,
    val phoneNumber: String?,
    val membershipNumber: String?,
    val medicalRecordNumber: String?,
    val needsRenewal: Boolean?,
    val relationshipToHead: RelationshipToHead?,
    val archivedAt: Instant?,
    val archivedReason: ArchivedReason?,
    val renewedAt: Instant?,
    val coverageEndDate: LocalDate?
) : Serializable {

    class MemberNotFoundException(message: String) : Exception(message)

    enum class Gender { M, F }
    enum class DateAccuracy { Y, M, D }
    enum class ArchivedReason {
        DEATH,
        DIVORCE,
        FORMED_OWN_HOUSEHOLD,
        JOINED_FORMAL_SECTOR,
        RELOCATION,
        UNPAID,
        OTHER
    }

    enum class RelationshipToHead {
        SELF,
        WIFE,
        HUSBAND,
        SON,
        DAUGHTER,
        MAID_OR_SHEPHERD,
        PARENT,
        SON_IN_LAW,
        OTHER
    }

    enum class MembershipStatus {
        ACTIVE,
        EXPIRED,
        DELETED,
        UNKNOWN
    }

    fun isAbsentee(): Boolean {
        return photoUrl == null && thumbnailPhotoId == null
    }

    fun getAgeYears(clock: Clock): Int {
        return DateUtils.getYearsAgo(birthdate, clock)
    }

    fun diff(previous: Member): List<Delta> {
        val gson = GsonBuilder().serializeNulls().create()
        val previousMap = gson.fromJson(gson.toJson(previous), Map::class.java) as Map<String, Any?>
        val currentMap = gson.fromJson(gson.toJson(this), Map::class.java) as Map<String, Any?>
        val diffFields = currentMap.keys.filter { currentMap[it] != previousMap[it] }
        return diffFields.map {
            Delta(action = Delta.Action.EDIT,
                  modelName = Delta.ModelName.MEMBER,
                  modelId = id,
                  field = it)
        }
    }

    fun getAgeMonths(clock: Clock): Int {
        return DateUtils.getMonthsAgo(birthdate, clock)
    }

    fun getAgeDays(clock: Clock): Int {
        return DateUtils.getDaysAgo(birthdate, clock)
    }

    fun photoExists(): Boolean {
        return photoUrl != null
    }

    fun memberStatus(clock: Clock): MembershipStatus {
        if (householdId == null || coverageEndDate == null) {
            return MembershipStatus.UNKNOWN
        }

        if (relationshipToHead == RelationshipToHead.SELF && archivedReason != null) {
            return MembershipStatus.DELETED
        }

        val currentDate = DateUtils.instantToLocalDate(clock.instant(), clock)
        return if (coverageEndDate < currentDate) {
            MembershipStatus.EXPIRED
        } else {
            MembershipStatus.ACTIVE
        }
    }

    fun beneficiaryStatus(clock: Clock): MembershipStatus {
        if (householdId == null || coverageEndDate == null) {
            return MembershipStatus.UNKNOWN
        }

        if (archivedReason == ArchivedReason.UNPAID) {
            return MembershipStatus.EXPIRED
        }

        if (archivedReason != null) {
            return MembershipStatus.DELETED
        }

        val currentDate = DateUtils.instantToLocalDate(clock.instant(), clock)
        return if (coverageEndDate < currentDate) {
            MembershipStatus.EXPIRED
        } else {
            MembershipStatus.ACTIVE
        }
    }

    companion object {
        const val LANGUAGE_CHOICE_OTHER = "Other"
        const val LANGUAGE_RUKIGA = "Rukiga"
        const val LANGUAGE_RUTOORO = "Rutooro"
        const val LANGUAGE_KINYARWANDA = "Kinyarwanda"
        val COMMON_LANGUAGES = listOf(LANGUAGE_RUKIGA, LANGUAGE_RUTOORO, LANGUAGE_KINYARWANDA)
        val LANGUAGE_CHOICES = listOf(LANGUAGE_RUKIGA, LANGUAGE_RUTOORO, LANGUAGE_KINYARWANDA,
                LANGUAGE_CHOICE_OTHER)
        const val MAX_AGE = 200
        // This sets the limit on the oldest a newborn can be to enroll.
        const val MAX_NEWBORN_AGE_IN_MONTHS = 12L

        fun isValidFullName(name: String, minLength: Int): Boolean {
            return name.split(' ').filter{ it.isNotBlank() }.count() >= minLength
        }

        fun isValidMedicalRecordNumber(medicalRecordNumber: String, minLength: Int, maxLength: Int): Boolean {
            return medicalRecordNumber.length in minLength..maxLength
        }

        fun isValidCardId(cardId: String): Boolean {
            return cardId.matches(Regex("[A-Z]{3}[0-9]{6}"))
        }

        fun formatCardId(cardId: String): String {
            return "${cardId.substring(0, 3)} ${cardId.substring(3, 6)} ${cardId.substring(6, 9)}"
        }
    }
}
