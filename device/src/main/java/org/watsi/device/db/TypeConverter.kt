package org.watsi.device.db

import android.arch.persistence.room.TypeConverter
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.watsi.domain.entities.Delta
import java.util.UUID

class TypeConverter {
    private val formatter = DateTimeFormatter.ISO_DATE

    @TypeConverter
    fun fromUuid(uuid: UUID?): String? = uuid?.toString()

    @TypeConverter
    fun toUuid(uuidString: String?): UUID? = if (uuidString == null) null else UUID.fromString(uuidString)

    @TypeConverter
    fun fromInstant(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun toInstant(long: Long?): Instant? = if (long == null) null else Instant.ofEpochMilli(long)

    @TypeConverter
    fun fromLocalDate(localDate: LocalDate?): String? = localDate?.format(formatter)

    @TypeConverter
    fun toLocalDate(string: String?): LocalDate? = if (string == null) null else LocalDate.parse(string, formatter)

    @TypeConverter
    fun toAction(value: String?): Delta.Action? {
        return if (value != null) {
            Delta.Action.valueOf(value)
        } else {
            null
        }
    }

    @TypeConverter
    fun fromAction(action: Delta.Action?): String? {
        return if (action != null) {
            return action.toString()
        } else {
            null
        }
    }

    @TypeConverter
    fun toModelName(value: String?): Delta.ModelName? {
        return if (value != null) {
            Delta.ModelName.valueOf(value)
        } else {
            null
        }
    }

    @TypeConverter
    fun fromModelName(modelName: Delta.ModelName?): String? {
        return if (modelName != null) {
            modelName.toString()
        } else {
            null
        }
    }
}
