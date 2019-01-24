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
    val fingerprintsGuid: UUID?,
    val phoneNumber: String?,
    val membershipNumber: String?,
    val medicalRecordNumber: String?,
    val needsRenewal: Boolean?
) : Serializable {

    enum class Gender { M, F }
    enum class DateAccuracy { Y, M, D }

    fun isAbsentee(clock: Clock): Boolean {
        return (photoUrl == null && thumbnailPhotoId == null) ||
                (requiresFingerprint(clock) && fingerprintsGuid == null)
    }

    fun requiresFingerprint(clock: Clock): Boolean {
        return getAgeYears(clock) >= 6
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

    companion object {
        const val LANGUAGE_CHOICE_OTHER = "Other"
        const val LANGUAGE_RUKIGA = "Rukiga"
        const val LANGUAGE_RUTOORO = "Rutooro"
        const val LANGUAGE_KINYARWANDA = "Kinyarwanda"
        val COMMON_LANGUAGES = listOf(LANGUAGE_RUKIGA, LANGUAGE_RUTOORO, LANGUAGE_KINYARWANDA)
        val LANGUAGE_CHOICES = listOf(LANGUAGE_RUKIGA, LANGUAGE_RUTOORO, LANGUAGE_KINYARWANDA,
                LANGUAGE_CHOICE_OTHER)
        const val MAX_AGE = 200

        fun isValidName(name: String): Boolean {
            return name.split(' ').filter{ it.isNotBlank() }.count() >= 3
        }

        fun isValidMedicalRecordNumber(medicalRecordNumber: String): Boolean {
            return medicalRecordNumber.length in 6..7
        }

        fun isValidCardId(cardId: String): Boolean {
            return cardId.matches(Regex("[A-Z]{3}[0-9]{6}"))
        }

        fun formatCardId(cardId: String): String {
            return "${cardId.substring(0, 3)} ${cardId.substring(3, 6)} ${cardId.substring(6, 9)}"
        }
    }
}
