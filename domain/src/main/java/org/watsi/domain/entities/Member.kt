package org.watsi.domain.entities

import com.google.gson.Gson
import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import org.watsi.domain.utils.DateUtils
import java.io.Serializable
import java.util.UUID

data class Member(val id: UUID,
                  val householdId: UUID,
                  val photoId: UUID?,
                  val thumbnailPhotoId: UUID?,
                  val photoUrl: String?,
                  val cardId: String?,
                  val name: String,
                  val gender: Gender,
                  val birthdate: LocalDate,
                  val birthdateAccuracy: DateAccuracy = DateAccuracy.Y,
                  val fingerprintsGuid: UUID?,
                  val phoneNumber: String?) : Serializable {

    enum class Gender { M, F }
    enum class DateAccuracy { Y, M, D }

    fun isAbsentee(clock: Clock = Clock.systemDefaultZone()): Boolean {
        return (photoUrl == null && thumbnailPhotoId == null) ||
                (requiresFingerprint(clock) && fingerprintsGuid == null)
    }

    fun requiresFingerprint(clock: Clock = Clock.systemDefaultZone()): Boolean {
        return getAgeYears(clock) >= 6
    }

    fun getAgeMonths(clock: Clock = Clock.systemDefaultZone()): Int {
        return DateUtils.getMonthsAgo(birthdate, clock)
    }

    fun getAgeYears(clock: Clock = Clock.systemDefaultZone()): Int {
        return DateUtils.getYearsAgo(birthdate, clock)
    }

    fun formattedPhoneNumber(): String? {
        return when (phoneNumber?.length) {
            10 -> "(0) ${phoneNumber.substring(1, 4)} ${phoneNumber.substring(4, 7)} " +
                    "${phoneNumber.substring(7)}"
            0 -> "(0) ${phoneNumber.substring(0, 3)} ${phoneNumber.substring(3, 6)} " +
                    "${phoneNumber.substring(6)}"
            else -> null
        }
    }

    fun diff(previous: Member): List<Delta> {
        val gson = Gson()
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

    companion object {
        fun validCardId(cardId: String): Boolean {
            return cardId.matches(Regex("[A-Z]{3}[0-9]{6}"))
        }

        fun validPhoneNumber(phoneNumber: String): Boolean {
            return phoneNumber.matches("0?[1-9]\\d{8}".toRegex())
        }
    }
}
